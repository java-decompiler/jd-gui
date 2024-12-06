/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.util;

import org.jd.core.v1.model.classfile.ClassFile;
import org.jd.core.v1.model.classfile.Field;
import org.jd.core.v1.model.classfile.Method;
import org.jd.core.v1.model.classfile.attribute.*;
import org.jd.core.v1.model.javasyntax.declaration.BaseFormalParameter;
import org.jd.core.v1.model.javasyntax.declaration.FormalParameters;
import org.jd.core.v1.model.javasyntax.reference.BaseAnnotationReference;
import org.jd.core.v1.model.javasyntax.statement.Statements;
import org.jd.core.v1.model.javasyntax.type.*;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.declaration.ClassFileConstructorOrMethodDeclaration;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.declaration.ClassFileFormalParameter;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.localvariable.*;
import org.jd.core.v1.service.converter.classfiletojavasyntax.visitor.*;
import org.jd.core.v1.util.DefaultList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.jd.core.v1.model.classfile.Constants.ACC_ENUM;
import static org.jd.core.v1.model.classfile.Constants.ACC_STATIC;
import static org.jd.core.v1.model.javasyntax.declaration.MethodDeclaration.*;

public class LocalVariableMaker {
    protected LocalVariableSet localVariableSet = new LocalVariableSet();
    protected HashSet<String> names = new HashSet<>();
    protected HashSet<String> blackListNames = new HashSet<>();
    protected Frame currentFrame = new RootFrame();
    protected AbstractLocalVariable[] localVariableCache;

    protected TypeMaker typeMaker;
    protected Map<String, BaseType> typeBounds;
    protected FormalParameters formalParameters;

    protected PopulateBlackListNamesVisitor populateBlackListNamesVisitor = new PopulateBlackListNamesVisitor(blackListNames);
    protected SearchInTypeArgumentVisitor searchInTypeArgumentVisitor = new SearchInTypeArgumentVisitor();
    protected CreateParameterVisitor createParameterVisitor;
    protected CreateLocalVariableVisitor createLocalVariableVisitor;

    @SuppressWarnings("unchecked")
    public LocalVariableMaker(TypeMaker typeMaker, ClassFileConstructorOrMethodDeclaration comd, boolean constructor) {
        ClassFile classFile = comd.getClassFile();
        Method method = comd.getMethod();
        BaseType parameterTypes = comd.getParameterTypes();

        this.typeMaker = typeMaker;
        this.typeBounds = comd.getTypeBounds();
        this.createParameterVisitor = new CreateParameterVisitor(typeMaker);
        this.createLocalVariableVisitor = new CreateLocalVariableVisitor(typeMaker);

        // Initialize local black list variable names
        if (classFile.getFields() != null) {
            for (Field field : classFile.getFields()) {
                String descriptor = field.getDescriptor();

                if (descriptor.charAt(descriptor.length() - 1) == ';') {
                    typeMaker.makeFromDescriptor(descriptor).accept(populateBlackListNamesVisitor);
                }
            }
        }

        typeMaker.makeFromInternalTypeName(classFile.getInternalTypeName()).accept(populateBlackListNamesVisitor);

        if (classFile.getSuperTypeName() != null) {
            typeMaker.makeFromInternalTypeName(classFile.getSuperTypeName()).accept(populateBlackListNamesVisitor);
        }

        if (classFile.getInterfaceTypeNames() != null) {
            for (String interfaceTypeName : classFile.getInterfaceTypeNames()) {
                typeMaker.makeFromInternalTypeName(interfaceTypeName).accept(populateBlackListNamesVisitor);
            }
        }

        if (parameterTypes != null) {
            if (parameterTypes.isList()) {
                for (Type type : parameterTypes) {
                    type.accept(populateBlackListNamesVisitor);
                }
            } else {
                parameterTypes.getFirst().accept(populateBlackListNamesVisitor);
            }
        }

        // Initialize local variables from 'LocalVariableTable' and 'LocalVariableTypeTable' attributes
        initLocalVariablesFromAttributes(method);

        // Initialize local variables from access flags & signature
        int firstVariableIndex = 0;

        if ((method.getAccessFlags() & FLAG_STATIC) == 0) {
            if (localVariableSet.root(0) == null) {
                // Local variable missing
                localVariableSet.add(0, new ObjectLocalVariable(typeMaker, 0, 0, typeMaker.makeFromInternalTypeName(classFile.getInternalTypeName()), "this"));
            }
            firstVariableIndex = 1;
        }

        if (constructor) {
            if (classFile.isEnum()) {
                if (localVariableSet.root(1) == null) {
                    // Local variable missing
                    localVariableSet.add(1, new ObjectLocalVariable(typeMaker, 1, 0, ObjectType.TYPE_STRING, "this$enum$name"));
                }
                if (localVariableSet.root(2) == null) {
                    // Local variable missing
                    localVariableSet.add(2, new PrimitiveLocalVariable(2, 0, PrimitiveType.TYPE_INT, "this$enum$index"));
                }
            } else if ((classFile.getOuterClassFile() != null) && !classFile.isStatic()) {
                if (localVariableSet.root(1) == null) {
                    // Local variable missing
                    localVariableSet.add(1, new ObjectLocalVariable(typeMaker, 1, 0, typeMaker.makeFromInternalTypeName(classFile.getOuterClassFile().getInternalTypeName()), "this$0"));
                }
            }
        }

        if (parameterTypes != null) {
            int lastParameterIndex = parameterTypes.size() - 1;
            boolean varargs = ((method.getAccessFlags() & FLAG_VARARGS) != 0);

            initLocalVariablesFromParameterTypes(classFile, parameterTypes, varargs, firstVariableIndex, lastParameterIndex);

            // Create list of parameterTypes
            formalParameters = new FormalParameters();

            AttributeParameterAnnotations rvpa = method.getAttribute("RuntimeVisibleParameterAnnotations");
            AttributeParameterAnnotations ripa = method.getAttribute("RuntimeInvisibleParameterAnnotations");

            if ((rvpa == null) && (ripa == null)) {
                for (int parameterIndex=0, variableIndex=firstVariableIndex; parameterIndex<=lastParameterIndex; parameterIndex++, variableIndex++) {
                    AbstractLocalVariable lv = localVariableSet.root(variableIndex);

                    formalParameters.add(new ClassFileFormalParameter(lv, varargs && (parameterIndex==lastParameterIndex)));

                    if (PrimitiveType.TYPE_LONG.equals(lv.getType()) || PrimitiveType.TYPE_DOUBLE.equals(lv.getType())) {
                        variableIndex++;
                    }
                }
            } else {
                Annotations[] visiblesArray = (rvpa == null) ? null : rvpa.getParameterAnnotations();
                Annotations[] invisiblesArray = (ripa == null) ? null : ripa.getParameterAnnotations();
                AnnotationConverter annotationConverter = new AnnotationConverter(typeMaker);

                for (int parameterIndex=0, variableIndex=firstVariableIndex; parameterIndex<=lastParameterIndex; parameterIndex++, variableIndex++) {
                    AbstractLocalVariable lv = localVariableSet.root(variableIndex);

                    Annotations visibles = ((visiblesArray == null) || (visiblesArray.length <= parameterIndex)) ? null : visiblesArray[parameterIndex];
                    Annotations invisibles = ((invisiblesArray == null) || (invisiblesArray.length <= parameterIndex)) ? null : invisiblesArray[parameterIndex];
                    BaseAnnotationReference annotationReferences = annotationConverter.convert(visibles, invisibles);

                    formalParameters.add(new ClassFileFormalParameter(annotationReferences, lv, varargs && (parameterIndex==lastParameterIndex)));

                    if (PrimitiveType.TYPE_LONG.equals(lv.getType()) || PrimitiveType.TYPE_DOUBLE.equals(lv.getType())) {
                        variableIndex++;
                    }
                }
            }
        }

        // Initialize root frame and cache
        localVariableCache = localVariableSet.initialize(currentFrame);
    }

    protected void initLocalVariablesFromAttributes(Method method) {
        AttributeCode code = method.getAttribute("Code");

        // Init local variables from attributes
        if (code != null) {
            AttributeLocalVariableTable localVariableTable = code.getAttribute("LocalVariableTable");

            if (localVariableTable != null) {
                boolean staticFlag = (method.getAccessFlags() & FLAG_STATIC) != 0;

                for (org.jd.core.v1.model.classfile.attribute.LocalVariable localVariable : localVariableTable.getLocalVariableTable()) {
                    int index = localVariable.getIndex();
                    int startPc = (!staticFlag && index==0) ? 0 : localVariable.getStartPc();
                    String descriptor = localVariable.getDescriptor();
                    String name = localVariable.getName();
                    AbstractLocalVariable lv;

                    if (descriptor.charAt(descriptor.length() - 1) == ';') {
                        lv = new ObjectLocalVariable(typeMaker, index, startPc, typeMaker.makeFromDescriptor(descriptor), name);
                    } else {
                        int dimension = TypeMaker.countDimension(descriptor);

                        if (dimension == 0) {
                            lv = new PrimitiveLocalVariable(index, startPc, PrimitiveType.getPrimitiveType(descriptor.charAt(0)), name);
                        } else {
                            lv = new ObjectLocalVariable(typeMaker, index, startPc, typeMaker.makeFromSignature(descriptor.substring(dimension)).createType(dimension), name);
                        }
                    }

                    localVariableSet.add(index, lv);
                    names.add(name);
                }
            }

            AttributeLocalVariableTypeTable localVariableTypeTable = code.getAttribute("LocalVariableTypeTable");

            if (localVariableTypeTable != null) {
                UpdateTypeVisitor updateTypeVisitor = new UpdateTypeVisitor(localVariableSet);

                for (LocalVariableType lv : localVariableTypeTable.getLocalVariableTypeTable()) {
                    updateTypeVisitor.setLocalVariableType(lv);
                    typeMaker.makeFromSignature(lv.getSignature()).accept(updateTypeVisitor);
                }
            }
        }
    }

    protected void initLocalVariablesFromParameterTypes(ClassFile classFile, BaseType parameterTypes, boolean varargs, int firstVariableIndex, int lastParameterIndex) {
        HashMap<Type, Boolean> typeMap = new HashMap<>();
        DefaultList<Type> t = parameterTypes.getList();

        for (int parameterIndex=0; parameterIndex<=lastParameterIndex; parameterIndex++) {
            Type type = t.get(parameterIndex);
            typeMap.put(type, Boolean.valueOf(typeMap.containsKey(type)));
        }

        String parameterNamePrefix = "param";

        if (classFile.getOuterClassFile() != null) {
            int innerTypeDepth = 1;
            ObjectType type = typeMaker.makeFromInternalTypeName(classFile.getOuterClassFile().getInternalTypeName());

            while ((type != null) && type.isInnerObjectType()) {
                innerTypeDepth++;
                type = type.getOuterType();
            }

            parameterNamePrefix += innerTypeDepth;
        }

        StringBuilder sb = new StringBuilder();
        GenerateParameterSuffixNameVisitor generateParameterSuffixNameVisitor = new GenerateParameterSuffixNameVisitor();

        for (int parameterIndex=0, variableIndex=firstVariableIndex; parameterIndex<=lastParameterIndex; parameterIndex++, variableIndex++) {
            Type type = t.get(parameterIndex);
            AbstractLocalVariable lv = localVariableSet.root(variableIndex);

            if (lv == null) {
                sb.setLength(0);
                sb.append(parameterNamePrefix);

                if ((parameterIndex == lastParameterIndex) && varargs) {
                    sb.append("VarArgs");
//                } else if (type.getDimension() > 1) {
//                    sb.append("ArrayOfArray");
                } else {
                    if (type.getDimension() > 0) {
                        sb.append("ArrayOf");
                    }
                    type.accept(generateParameterSuffixNameVisitor);
                    sb.append(generateParameterSuffixNameVisitor.getSuffix());
                }

                int length = sb.length();
                int counter = 1;

                if (typeMap.get(type)) {
                    sb.append(counter++);
                }

                String name = sb.toString();

                while (names.contains(name)) {
                    sb.setLength(length);
                    sb.append(counter++);
                    name = sb.toString();
                }

                names.add(name);
                createParameterVisitor.init(variableIndex, name);
                type.accept(createParameterVisitor);

                AbstractLocalVariable alv = createParameterVisitor.getLocalVariable();

                alv.setDeclared(true);
                localVariableSet.add(variableIndex, alv);
            }

            if (PrimitiveType.TYPE_LONG.equals(type) || PrimitiveType.TYPE_DOUBLE.equals(type)) {
                variableIndex++;
            }
        }
    }

    public AbstractLocalVariable getLocalVariable(int index, int offset) {
        AbstractLocalVariable lv = localVariableCache[index];

        if (lv == null) {
            lv = currentFrame.getLocalVariable(index);
//            assert lv != null : "getLocalVariable : local variable not found";
            if (lv == null) {
                lv = new ObjectLocalVariable(typeMaker, index, offset, ObjectType.TYPE_OBJECT, "SYNTHETIC_LOCAL_VARIABLE_"+index, true);
            }
        } else if (lv.getFrame() != currentFrame) {
            Frame frame = searchCommonParentFrame(lv.getFrame(), currentFrame);
            frame.mergeLocalVariable(typeBounds, this, lv);

            if (lv.getFrame() != frame) {
                lv.getFrame().removeLocalVariable(lv);
                frame.addLocalVariable(lv);
            }
        }

        lv.setToOffset(offset);

        return lv;
    }

    protected AbstractLocalVariable searchLocalVariable(int index, int offset) {
        AbstractLocalVariable lv = localVariableSet.get(index, offset);

        if (lv == null) {
            lv = currentFrame.getLocalVariable(index);
        } else {
            AbstractLocalVariable lv2 = currentFrame.getLocalVariable(index);

            if ((lv2 != null) && ((lv.getName() == null) ? (lv2.getName() == null) : lv.getName().equals(lv2.getName())) && lv.getType().equals(lv2.getType())) {
                lv = lv2;
            }

            localVariableSet.remove(index, offset);
        }

        return lv;
    }

    public boolean isCompatible(AbstractLocalVariable lv, Type valueType) {
        if (valueType == ObjectType.TYPE_UNDEFINED_OBJECT) {
            return true;
        } else if (valueType.isObjectType() && (lv.getType().getDimension() == valueType.getDimension())) {
            ObjectType valueObjectType = (ObjectType) valueType;

            if (lv.getType().isObjectType()) {
                ObjectType lvObjectType = (ObjectType) lv.getType();

                BaseTypeArgument lvTypeArguments = lvObjectType.getTypeArguments();
                BaseTypeArgument valueTypeArguments = valueObjectType.getTypeArguments();

                if ((lvTypeArguments == null) || (valueTypeArguments == null) || (valueTypeArguments == WildcardTypeArgument.WILDCARD_TYPE_ARGUMENT)) {
                    return typeMaker.isRawTypeAssignable(lvObjectType, valueObjectType);
                }

                searchInTypeArgumentVisitor.init();
                lvTypeArguments.accept(searchInTypeArgumentVisitor);

                if (!searchInTypeArgumentVisitor.containsGeneric()) {
                    searchInTypeArgumentVisitor.init();
                    valueTypeArguments.accept(searchInTypeArgumentVisitor);

                    if (searchInTypeArgumentVisitor.containsGeneric()) {
                        return typeMaker.isRawTypeAssignable(lvObjectType, valueObjectType);
                    }
                }
            } else if (lv.getType().isGenericType() && valueObjectType.getInternalName().equals(ObjectType.TYPE_OBJECT.getInternalName())) {
                return true;
            }
        }

        return false;
    }

    public AbstractLocalVariable getLocalVariableInAssignment(Map<String, BaseType> typeBounds, int index, int offset, Type valueType) {
        AbstractLocalVariable lv = searchLocalVariable(index, offset);

        if (lv == null) {
            // Create a new local variable
            createLocalVariableVisitor.init(index, offset);
            valueType.accept(createLocalVariableVisitor);
            lv = createLocalVariableVisitor.getLocalVariable();
        } else if (lv.isAssignableFrom(typeBounds, valueType) || isCompatible(lv, valueType)) {
            // Assignable, reduce type
            lv.typeOnRight(typeBounds, valueType);
        } else if (!lv.getType().isGenericType() || (ObjectType.TYPE_OBJECT != valueType)) {
            // Not assignable -> Create a new local variable
            createLocalVariableVisitor.init(index, offset);
            valueType.accept(createLocalVariableVisitor);
            lv = createLocalVariableVisitor.getLocalVariable();
        }

        lv.setToOffset(offset);
        store(lv);

        return lv;
    }

    public AbstractLocalVariable getLocalVariableInNullAssignment(int index, int offset, Type valueType) {
        AbstractLocalVariable lv = searchLocalVariable(index, offset);

        if (lv == null) {
            // Create a new local variable
            createLocalVariableVisitor.init(index, offset);
            valueType.accept(createLocalVariableVisitor);
            lv = createLocalVariableVisitor.getLocalVariable();
        } else {
            Type type = lv.getType();

            if ((type.getDimension() == 0) && type.isPrimitiveType()) {
                // Not assignable -> Create a new local variable
                createLocalVariableVisitor.init(index, offset);
                valueType.accept(createLocalVariableVisitor);
                lv = createLocalVariableVisitor.getLocalVariable();
            }
        }

        lv.setToOffset(offset);
        store(lv);

        return lv;
    }

    public AbstractLocalVariable getLocalVariableInAssignment(Map<String, BaseType> typeBounds, int index, int offset, AbstractLocalVariable valueLocalVariable) {
        AbstractLocalVariable lv = searchLocalVariable(index, offset);

        if (lv == null) {
            // Create a new local variable
            createLocalVariableVisitor.init(index, offset);
            valueLocalVariable.accept(createLocalVariableVisitor);
            lv = createLocalVariableVisitor.getLocalVariable();
        } else if (lv.isAssignableFrom(typeBounds, valueLocalVariable) || isCompatible(lv, valueLocalVariable.getType())) {
            // Assignable
        } else if (!lv.getType().isGenericType() || (ObjectType.TYPE_OBJECT != valueLocalVariable.getType())) {
            // Not assignable -> Create a new local variable
            createLocalVariableVisitor.init(index, offset);
            valueLocalVariable.accept(createLocalVariableVisitor);
            lv = createLocalVariableVisitor.getLocalVariable();
        }

        lv.variableOnRight(typeBounds, valueLocalVariable);
        lv.setToOffset(offset);
        store(lv);

        return lv;
    }

    public AbstractLocalVariable getExceptionLocalVariable(int index, int offset, ObjectType type) {
        AbstractLocalVariable lv;

        if (index == -1) {
            currentFrame.setExceptionLocalVariable(lv = new ObjectLocalVariable(typeMaker, index, offset, type, null, true));
        } else {
            lv = localVariableSet.remove(index, offset);

            if (lv == null) {
                lv = new ObjectLocalVariable(typeMaker, index, offset, type, null, true);
            } else {
                lv.setDeclared(true);
            }

            currentFrame.addLocalVariable(lv);
        }

        return lv;
    }

    public void removeLocalVariable(AbstractLocalVariable lv) {
        int index = lv.getIndex();

        if (index < localVariableCache.length) {
            // Remove from cache
            localVariableCache[index] = null;
            // Remove from current frame
            currentFrame.removeLocalVariable(lv);
        }
    }

    protected void store(AbstractLocalVariable lv) {
        // Store to cache
        int index = lv.getIndex();

        if (index >= localVariableCache.length) {
            AbstractLocalVariable[] tmp = localVariableCache;
            localVariableCache = new AbstractLocalVariable[index * 2];
            System.arraycopy(tmp, 0, localVariableCache, 0, tmp.length);
        }

        localVariableCache[index] = lv;

        // Store to current frame
        if (lv.getFrame() == null) {
            currentFrame.addLocalVariable(lv);
        }
    }

    public boolean containsName(String name) {
        return names.contains(name);
    }

    public void make(boolean containsLineNumber, TypeMaker typeMaker) {
        currentFrame.updateLocalVariableInForStatements(typeMaker);
        currentFrame.createNames(blackListNames);
        currentFrame.createDeclarations(containsLineNumber);
    }

    public BaseFormalParameter getFormalParameters() {
        return formalParameters;
    }

    public void pushFrame(Statements statements) {
        Frame parent = currentFrame;
        currentFrame = new Frame(currentFrame, statements);
        parent.addChild(currentFrame);
    }

    public void popFrame() {
        currentFrame.close();
        currentFrame = currentFrame.getParent();
    }

    protected static Frame searchCommonParentFrame(Frame frame1, Frame frame2) {
        if (frame1 == frame2) {
            return frame1;
        }

        if (frame2.getParent() == frame1) {
            return frame1;
        }

        if (frame1.getParent() == frame2) {
            return frame2;
        }

        HashSet<Frame> set = new HashSet<>();

        while (frame1 != null) {
            set.add(frame1);
            frame1 = frame1.getParent();
        }

        while (frame2 != null) {
            if (set.contains(frame2)) {
                return frame2;
            }
            frame2 = frame2.getParent();
        }

        return null;
    }

    public void changeFrame(AbstractLocalVariable localVariable) {
        Frame frame = LocalVariableMaker.searchCommonParentFrame(localVariable.getFrame(), currentFrame);

        if (localVariable.getFrame() != frame) {
            localVariable.getFrame().removeLocalVariable(localVariable);
            frame.addLocalVariable(localVariable);
        }
    }
}
