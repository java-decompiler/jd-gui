/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.visitor;

import org.jd.core.v1.model.classfile.ClassFile;
import org.jd.core.v1.model.javasyntax.AbstractJavaSyntaxVisitor;
import org.jd.core.v1.model.javasyntax.declaration.*;
import org.jd.core.v1.model.javasyntax.expression.*;
import org.jd.core.v1.model.javasyntax.statement.*;
import org.jd.core.v1.model.javasyntax.type.BaseType;
import org.jd.core.v1.model.javasyntax.type.ObjectType;
import org.jd.core.v1.model.javasyntax.type.Type;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.declaration.*;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.expression.ClassFileConstructorInvocationExpression;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.expression.ClassFileLocalVariableReferenceExpression;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.expression.ClassFileNewExpression;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.expression.ClassFileSuperConstructorInvocationExpression;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.localvariable.AbstractLocalVariable;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.TypeMaker;
import org.jd.core.v1.util.DefaultList;

import java.util.*;

import static org.jd.core.v1.model.classfile.Constants.ACC_STATIC;
import static org.jd.core.v1.model.javasyntax.declaration.Declaration.FLAG_SYNTHETIC;

public class InitInnerClassVisitor extends AbstractJavaSyntaxVisitor {
    protected UpdateFieldDeclarationsAndReferencesVisitor updateFieldDeclarationsAndReferencesVisitor = new UpdateFieldDeclarationsAndReferencesVisitor();
    protected DefaultList<String> syntheticInnerFieldNames = new DefaultList<>();
    protected String outerTypeFieldName;

    @Override
    public void visit(AnnotationDeclaration declaration) {
        safeAccept(declaration.getBodyDeclaration());
    }

    @Override
    public void visit(ClassDeclaration declaration) {
        safeAccept(declaration.getBodyDeclaration());
    }

    @Override
    public void visit(EnumDeclaration declaration) {
        safeAccept(declaration.getBodyDeclaration());
    }

    @Override
    public void visit(InterfaceDeclaration declaration) {
        safeAccept(declaration.getBodyDeclaration());
    }

    @Override
    public void visit(BodyDeclaration declaration) {
        ClassFileBodyDeclaration bodyDeclaration = (ClassFileBodyDeclaration)declaration;

        // Init attributes
        outerTypeFieldName = null;
        syntheticInnerFieldNames.clear();
        // Visit methods
        safeAcceptListDeclaration(bodyDeclaration.getMethodDeclarations());
        // Init values
        bodyDeclaration.setOuterTypeFieldName(outerTypeFieldName);

        if (!syntheticInnerFieldNames.isEmpty()) {
            bodyDeclaration.setSyntheticInnerFieldNames(new DefaultList<>(syntheticInnerFieldNames));
        }

        if ((outerTypeFieldName != null) || !syntheticInnerFieldNames.isEmpty()) {
            updateFieldDeclarationsAndReferencesVisitor.visit(bodyDeclaration);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void visit(ConstructorDeclaration declaration) {
        ClassFileConstructorDeclaration cfcd = (ClassFileConstructorDeclaration)declaration;
        ClassFile classFile = cfcd.getClassFile();
        ClassFile outerClassFile = classFile.getOuterClassFile();
        boolean removeFirstParameter = false;

        syntheticInnerFieldNames.clear();

        // Search synthetic field initialization
        if (cfcd.getStatements().isList()) {
            Iterator<Statement> iterator = cfcd.getStatements().iterator();

            while (iterator.hasNext()) {
                Statement statement = iterator.next();

                if (statement.isExpressionStatement()) {
                    Expression expression = statement.getExpression();

                    if (expression.isSuperConstructorInvocationExpression()) {
                        // 'super(...)'
                        break;
                    }

                    if (expression.isConstructorInvocationExpression()) {
                        // 'this(...)'
                        if ((outerClassFile != null) && !classFile.isStatic()) {
                            // Inner non-static class --> First parameter is the synthetic outer reference
                            removeFirstParameter = true;
                        }
                        break;
                    }

                    if (expression.isBinaryOperatorExpression()) {
                        Expression e = expression.getLeftExpression();

                        if (e.isFieldReferenceExpression()) {
                            String name = e.getName();

                            if (name.startsWith("this$")) {
                                outerTypeFieldName = name;
                                removeFirstParameter = true;
                            } else if (name.startsWith("val$")) {
                                syntheticInnerFieldNames.add(name);
                            }
                        }
                    }
                }

                iterator.remove();
            }
        }

        // Remove synthetic parameters
        BaseFormalParameter parameters = cfcd.getFormalParameters();

        if (parameters != null) {
            if (parameters.isList()) {
                List<FormalParameter> list = parameters.getList();

                if (removeFirstParameter) {
                    // Remove outer this
                    list.remove(0);
                }

                int count = syntheticInnerFieldNames.size();

                if (count > 0) {
                    // Remove outer local variable reference
                    int size = list.size();
                    list.subList(size - count, size).clear();
                }
            } else if (removeFirstParameter || !syntheticInnerFieldNames.isEmpty()) {
                // Remove outer this and outer local variable reference
                cfcd.setFormalParameters(null);
            }
        }

        // Anonymous class constructor ?
        if (outerClassFile != null) {
            String outerTypeName = outerClassFile.getInternalTypeName();
            String internalTypeName = cfcd.getClassFile().getInternalTypeName();
            int min;

            if (internalTypeName.startsWith(outerTypeName + '$')) {
                min = outerTypeName.length() + 1;
            } else {
                min = internalTypeName.lastIndexOf('$') + 1;
            }

            if (Character.isDigit(internalTypeName.charAt(min))) {
                int i = internalTypeName.length();
                boolean anonymousFlag = true;

                while (--i > min) {
                    if (!Character.isDigit(internalTypeName.charAt(i))) {
                        anonymousFlag = false;
                        break;
                    }
                }

                if (anonymousFlag) {
                    // Mark anonymous class constructor
                    cfcd.setFlags(cfcd.getFlags() | Declaration.FLAG_ANONYMOUS);
                }
            }
        }
    }

    @Override public void visit(MethodDeclaration declaration) {}
    @Override public void visit(StaticInitializerDeclaration declaration) {}

    protected class UpdateFieldDeclarationsAndReferencesVisitor extends AbstractUpdateExpressionVisitor {
        protected ClassFileBodyDeclaration bodyDeclaration;
        protected boolean syntheticField;

        @Override
        public void visit(BodyDeclaration declaration) {
            bodyDeclaration = (ClassFileBodyDeclaration)declaration;
            safeAcceptListDeclaration(bodyDeclaration.getFieldDeclarations());
            safeAcceptListDeclaration(bodyDeclaration.getMethodDeclarations());
        }

        @Override
        public void visit(FieldDeclaration declaration) {
            syntheticField = false;
            declaration.getFieldDeclarators().accept(this);

            if (syntheticField) {
                declaration.setFlags(declaration.getFlags()|FLAG_SYNTHETIC);
            }
        }

        @Override
        public void visit(FieldDeclarator declarator) {
            String name = declarator.getName();

            if (name.equals(outerTypeFieldName) || syntheticInnerFieldNames.contains(name)) {
                syntheticField = true;
            }
        }

        @Override public void visit(StaticInitializerDeclaration declaration) {}

        @Override
        public void visit(MethodDeclaration declaration) {
            safeAccept(declaration.getStatements());
        }

        @Override
        public void visit(NewExpression expression) {
            if (expression.getParameters() != null) {
                expression.setParameters(updateBaseExpression(expression.getParameters()));
                expression.getParameters().accept(this);
            }
            safeAccept(expression.getBodyDeclaration());
        }

        @Override
        public void visit(FieldReferenceExpression expression) {
            if (expression.getName().startsWith("this$")) {
                if (expression.getInternalTypeName().equals(bodyDeclaration.getInternalTypeName())) {
                    if (expression.getName().equals(outerTypeFieldName)) {
                        ObjectType objectType = (ObjectType)expression.getType();
                        Expression exp = (expression.getExpression() == null) ? expression : expression.getExpression();
                        expression.setExpression(new ObjectTypeReferenceExpression(exp.getLineNumber(), objectType.createType(null)));
                        expression.setName("this");
                    }
                } else {
                    ClassFileTypeDeclaration typeDeclaration = bodyDeclaration.getInnerTypeDeclaration(expression.getInternalTypeName());

                    if ((typeDeclaration != null) && typeDeclaration.isClassDeclaration()) {
                        ClassFileBodyDeclaration cfbd = (ClassFileBodyDeclaration) typeDeclaration.getBodyDeclaration();
                        String outerInternalTypeName = cfbd.getOuterBodyDeclaration().getInternalTypeName();
                        ObjectType objectType = (ObjectType)expression.getType();

                        if (outerInternalTypeName.equals(objectType.getInternalName())) {
                            Expression exp = (expression.getExpression() == null) ? expression : expression.getExpression();
                            expression.setExpression(new ObjectTypeReferenceExpression(exp.getLineNumber(), objectType.createType(null)));
                            expression.setName("this");
                        }
                    }
                }
            } else if (expression.getName().startsWith("val$")) {
                expression.setName(expression.getName().substring(4));
                expression.setExpression(null);
            } else {
                super.visit(expression);
            }
        }

        @Override
        protected Expression updateExpression(Expression expression) {
            if (expression.isLocalVariableReferenceExpression()) {
                if ((expression.getName() != null) && expression.getName().equals(outerTypeFieldName) && expression.getType().isObjectType()) {
                    ObjectType objectType = (ObjectType)expression.getType();
                    if (bodyDeclaration.getOuterBodyDeclaration().getInternalTypeName().equals(objectType.getInternalName())) {
                        return new FieldReferenceExpression(objectType, new ObjectTypeReferenceExpression(expression.getLineNumber(), objectType.createType(null)), objectType.getInternalName(), "this", objectType.getDescriptor());
                    }
                }
            }

            return expression;
        }
    }

    public static class UpdateNewExpressionVisitor extends AbstractJavaSyntaxVisitor {
        protected TypeMaker typeMaker;
        protected ClassFileBodyDeclaration bodyDeclaration;
        protected ClassFile classFile;
        protected HashMap<String, String> finalLocalVariableNameMap = new HashMap<>();
        protected DefaultList<ClassFileClassDeclaration> localClassDeclarations = new DefaultList<>();
        protected HashSet<NewExpression> newExpressions = new HashSet<>();
        protected int lineNumber;

        public UpdateNewExpressionVisitor(TypeMaker typeMaker) {
            this.typeMaker = typeMaker;
        }

        @Override
        public void visit(BodyDeclaration declaration) {
            bodyDeclaration = (ClassFileBodyDeclaration)declaration;
            safeAcceptListDeclaration(bodyDeclaration.getMethodDeclarations());
        }

        @Override
        public void visit(ConstructorDeclaration declaration) {
            classFile = ((ClassFileConstructorDeclaration)declaration).getClassFile();
            finalLocalVariableNameMap.clear();
            localClassDeclarations.clear();

            safeAccept(declaration.getStatements());

            if (! finalLocalVariableNameMap.isEmpty()) {
                UpdateParametersAndLocalVariablesVisitor visitor = new UpdateParametersAndLocalVariablesVisitor();

                declaration.getStatements().accept(visitor);

                if (declaration.getFormalParameters() != null) {
                    declaration.getFormalParameters().accept(visitor);
                }
            }

            if (! localClassDeclarations.isEmpty()) {
                localClassDeclarations.sort(new MemberDeclarationComparator());
                declaration.accept(new AddLocalClassDeclarationVisitor());
            }
        }

        @Override
        public void visit(MethodDeclaration declaration) {
            finalLocalVariableNameMap.clear();
            localClassDeclarations.clear();
            safeAccept(declaration.getStatements());

            if (! finalLocalVariableNameMap.isEmpty()) {
                UpdateParametersAndLocalVariablesVisitor visitor = new UpdateParametersAndLocalVariablesVisitor();

                declaration.getStatements().accept(visitor);

                if (declaration.getFormalParameters() != null) {
                    declaration.getFormalParameters().accept(visitor);
                }
            }

            if (! localClassDeclarations.isEmpty()) {
                localClassDeclarations.sort(new MemberDeclarationComparator());
                declaration.accept(new AddLocalClassDeclarationVisitor());
            }
        }

        @Override
        public void visit(StaticInitializerDeclaration declaration) {
            finalLocalVariableNameMap.clear();
            localClassDeclarations.clear();
            safeAccept(declaration.getStatements());

            if (! finalLocalVariableNameMap.isEmpty()) {
                declaration.getStatements().accept(new UpdateParametersAndLocalVariablesVisitor());
            }

            if (! localClassDeclarations.isEmpty()) {
                localClassDeclarations.sort(new MemberDeclarationComparator());
                declaration.accept(new AddLocalClassDeclarationVisitor());
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void visit(Statements list) {
            if (!list.isEmpty()) {
                ListIterator<Statement> iterator = list.listIterator();

                while (iterator.hasNext()) {
                    //iterator.next().accept(this);
                    Statement s = iterator.next();
                    s.accept(this);

                    if ((lineNumber == Expression.UNKNOWN_LINE_NUMBER) && !localClassDeclarations.isEmpty()) {
                        iterator.previous();

                        for (TypeDeclaration typeDeclaration : localClassDeclarations) {
                            iterator.add(new TypeDeclarationStatement(typeDeclaration));
                        }

                        localClassDeclarations.clear();
                        iterator.next();
                    }
                }
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void visit(NewExpression expression) {
            if (!newExpressions.contains(expression)) {
                newExpressions.add(expression);

                ClassFileNewExpression ne = (ClassFileNewExpression)expression;
                ClassFileBodyDeclaration cfbd = null;

                if (ne.getBodyDeclaration() == null) {
                    ObjectType type = ne.getObjectType();
                    String internalName = type.getInternalName();
                    ClassFileTypeDeclaration typeDeclaration = bodyDeclaration.getInnerTypeDeclaration(internalName);

                    if (typeDeclaration == null) {
                        for (ClassFileBodyDeclaration bd = bodyDeclaration; bd != null; bd = bd.getOuterBodyDeclaration()) {
                            if (bd.getInternalTypeName().equals(internalName)) {
                                cfbd = bd;
                                break;
                            }
                        }
                    } else if (typeDeclaration.isClassDeclaration()) {
                        ClassFileClassDeclaration cfcd = (ClassFileClassDeclaration) typeDeclaration;
                        cfbd = (ClassFileBodyDeclaration) cfcd.getBodyDeclaration();

                        if ((type.getQualifiedName() == null) && (type.getName() != null)) {
                            // Local class
                            cfcd.setFlags(cfcd.getFlags() & (~FLAG_SYNTHETIC));
                            localClassDeclarations.add(cfcd);
                            bodyDeclaration.removeInnerType(internalName);
                            lineNumber = ne.getLineNumber();
                        }
                    }
                } else {
                    // Anonymous class
                    cfbd = (ClassFileBodyDeclaration) ne.getBodyDeclaration();
                }

                if (cfbd != null) {
                    BaseExpression parameters = ne.getParameters();
                    BaseType parameterTypes = ne.getParameterTypes();

                    if (parameters != null) {
                        // Remove synthetic parameters
                        DefaultList<String> syntheticInnerFieldNames = cfbd.getSyntheticInnerFieldNames();

                        if (parameters.isList()) {
                            DefaultList<Expression> list = parameters.getList();
                            DefaultList<Type> types = parameterTypes.getList();

                            if (cfbd.getOuterTypeFieldName() != null) {
                                // Remove outer this
                                list.removeFirst();
                                types.removeFirst();
                            }

                            if (syntheticInnerFieldNames != null) {
                                // Remove outer local variable reference
                                int size = list.size();
                                int count = syntheticInnerFieldNames.size();
                                List<Expression> lastParameters = list.subList(size - count, size);
                                Iterator<Expression> parameterIterator = lastParameters.iterator();
                                Iterator<String> syntheticInnerFieldNameIterator = syntheticInnerFieldNames.iterator();

                                while (parameterIterator.hasNext()) {
                                    Expression param = parameterIterator.next();
                                    String syntheticInnerFieldName = syntheticInnerFieldNameIterator.next();

                                    if (param.isCastExpression()) {
                                        param = param.getExpression();
                                    }

                                    if (param.isLocalVariableReferenceExpression()) {
                                        AbstractLocalVariable lv = ((ClassFileLocalVariableReferenceExpression) param).getLocalVariable();
                                        String localVariableName = syntheticInnerFieldName.substring(4);
                                        finalLocalVariableNameMap.put(lv.getName(), localVariableName);
                                    }
                                }

                                lastParameters.clear();
                                types.subList(size - count, size).clear();
                            }
                        } else if (cfbd.getOuterTypeFieldName() != null) {
                            // Remove outer this
                            ne.setParameters(null);
                            ne.setParameterTypes(null);
                        } else if (syntheticInnerFieldNames != null) {
                            // Remove outer local variable reference
                            Expression param = parameters.getFirst();

                            if (param.isCastExpression()) {
                                param = param.getExpression();
                            }

                            if (param.isLocalVariableReferenceExpression()) {
                                AbstractLocalVariable lv = ((ClassFileLocalVariableReferenceExpression) param).getLocalVariable();
                                String localVariableName = syntheticInnerFieldNames.getFirst().substring(4);
                                finalLocalVariableNameMap.put(lv.getName(), localVariableName);
                                ne.setParameters(null);
                                ne.setParameterTypes(null);
                            }
                        }

                        // Is the last parameter synthetic ?
                        parameters = ne.getParameters();

                        if ((parameters != null) && (parameters.size() > 0) && parameters.getLast().isNullExpression()) {
                            parameterTypes = ne.getParameterTypes();

                            if (parameterTypes.getLast().getName() == null) {
                                // Yes. Remove it.
                                if (parameters.isList()) {
                                    parameters.getList().removeLast();
                                    parameterTypes.getList().removeLast();
                                } else {
                                    ne.setParameters(null);
                                    ne.setParameterTypes(null);
                                }
                            }
                        }
                    }
                }
            }

            safeAccept(expression.getParameters());
        }

        @Override
        public void visit(SuperConstructorInvocationExpression expression) {
            ClassFileSuperConstructorInvocationExpression scie = (ClassFileSuperConstructorInvocationExpression)expression;
            BaseExpression parameters = scie.getParameters();

            if ((parameters != null) && (parameters.size() > 0)) {
                // Remove outer 'this' reference parameter
                Type firstParameterType = parameters.getFirst().getType();

                if (firstParameterType.isObjectType() && !classFile.isStatic() && (bodyDeclaration.getOuterTypeFieldName() != null)) {
                    TypeMaker.TypeTypes superTypeTypes = typeMaker.makeTypeTypes(classFile.getSuperTypeName());

                    if ((superTypeTypes != null) && superTypeTypes.thisType.isInnerObjectType()) {
                        if (typeMaker.isRawTypeAssignable(superTypeTypes.thisType.getOuterType(), (ObjectType)firstParameterType)) {
                            scie.setParameters(removeFirstItem(parameters));
                            scie.setParameterTypes(removeFirstItem(scie.getParameterTypes()));
                        }
                    }
                }

                // Remove last synthetic parameter
                expression.setParameters(removeLastSyntheticParameter(scie.getParameters(), scie.getParameterTypes()));
            }
        }

        @Override
        public void visit(ConstructorInvocationExpression expression) {
            ClassFileConstructorInvocationExpression cie = (ClassFileConstructorInvocationExpression)expression;
            BaseExpression parameters = cie.getParameters();

            if ((parameters != null) && (parameters.size() > 0)) {
                // Remove outer this reference parameter
                if (bodyDeclaration.getOuterTypeFieldName() != null) {
                    cie.setParameters(removeFirstItem(parameters));
                    cie.setParameterTypes(removeFirstItem(cie.getParameterTypes()));
                }

                // Remove last synthetic parameter
                cie.setParameters(removeLastSyntheticParameter(cie.getParameters(), cie.getParameterTypes()));
            }
        }

        protected BaseExpression removeFirstItem(BaseExpression parameters) {
            if (parameters.isList()) {
                parameters.getList().removeFirst();
            } else {
                parameters = null;
            }

            return parameters;
        }

        protected BaseType removeFirstItem(BaseType types) {
            if (types.isList()) {
                types.getList().removeFirst();
            } else {
                types = null;
            }

            return types;
        }

        protected BaseExpression removeLastSyntheticParameter(BaseExpression parameters, BaseType parameterTypes) {
            // Is the last parameter synthetic ?
            if ((parameters != null) && (parameters.size() > 0) && parameters.getLast().isNullExpression()) {
                if (parameterTypes.getLast().getName() == null) {
                    // Yes. Remove it.
                    if (parameters.isList()) {
                        parameters.getList().removeLast();
                    } else {
                        parameters = null;
                    }
                }
            }

            return parameters;
        }

        protected class UpdateParametersAndLocalVariablesVisitor extends AbstractJavaSyntaxVisitor {
            protected boolean fina1;

            @Override
            public void visit(FormalParameter declaration) {
                if (finalLocalVariableNameMap.containsKey(declaration.getName())) {
                    declaration.setFinal(true);
                    declaration.setName(finalLocalVariableNameMap.get(declaration.getName()));
                }
            }

            @Override
            public void visit(LocalVariableDeclarationStatement statement) {
                fina1 = false;
                statement.getLocalVariableDeclarators().accept(this);
                statement.setFinal(fina1);
            }

            @Override
            public void visit(LocalVariableDeclaration declaration) {
                fina1 = false;
                declaration.getLocalVariableDeclarators().accept(this);
                declaration.setFinal(fina1);
            }

            @Override
            public void visit(LocalVariableDeclarator declarator) {
                if (finalLocalVariableNameMap.containsKey(declarator.getName())) {
                    fina1 = true;
                    declarator.setName(finalLocalVariableNameMap.get(declarator.getName()));
                }
            }
        }

        protected class AddLocalClassDeclarationVisitor extends AbstractJavaSyntaxVisitor {
            protected SearchFirstLineNumberVisitor searchFirstLineNumberVisitor = new SearchFirstLineNumberVisitor();
            protected int lineNumber = Expression.UNKNOWN_LINE_NUMBER;

            @Override
            public void visit(ConstructorDeclaration declaration) {
                ClassFileConstructorDeclaration cfcd = (ClassFileConstructorDeclaration)declaration;
                cfcd.setStatements(addLocalClassDeclarations(cfcd.getStatements()));
            }

            @Override
            public void visit(MethodDeclaration declaration) {
                ClassFileMethodDeclaration cfmd = (ClassFileMethodDeclaration)declaration;
                cfmd.setStatements(addLocalClassDeclarations(cfmd.getStatements()));
            }

            @Override
            public void visit(StaticInitializerDeclaration declaration) {
                ClassFileStaticInitializerDeclaration cfsid = (ClassFileStaticInitializerDeclaration)declaration;
                cfsid.setStatements(addLocalClassDeclarations(cfsid.getStatements()));
            }

            @SuppressWarnings("unchecked")
            protected BaseStatement addLocalClassDeclarations(BaseStatement statements) {
                if (!localClassDeclarations.isEmpty()) {
                    if (statements.isStatements()) {
                        statements.accept(this);
                    } else {
                        ClassFileClassDeclaration declaration = localClassDeclarations.get(0);

                        searchFirstLineNumberVisitor.init();
                        statements.accept(searchFirstLineNumberVisitor);

                        if (searchFirstLineNumberVisitor.getLineNumber() != -1) {
                            lineNumber = searchFirstLineNumberVisitor.getLineNumber();
                        }

                        if (declaration.getFirstLineNumber() <= lineNumber) {
                            Statements list = new Statements();
                            Iterator<ClassFileClassDeclaration> declarationIterator = localClassDeclarations.iterator();

                            list.add(new TypeDeclarationStatement(declaration));
                            declarationIterator.next();
                            declarationIterator.remove();

                            while (declarationIterator.hasNext() && ((declaration = declarationIterator.next()).getFirstLineNumber() <= lineNumber)) {
                                list.add(new TypeDeclarationStatement(declaration));
                                declarationIterator.remove();
                            }

                            if (statements.isList()) {
                                list.addAll(statements.getList());
                            } else {
                                list.add(statements.getFirst());
                            }
                            statements = list;
                        } else {
                            statements.accept(this);
                        }
                    }
                }

                return statements;
            }

            @Override
            @SuppressWarnings("unchecked")
            public void visit(Statements list) {
                if (!localClassDeclarations.isEmpty() && !list.isEmpty()) {
                    ListIterator<Statement> statementIterator = list.listIterator();
                    Iterator<ClassFileClassDeclaration> declarationIterator = localClassDeclarations.iterator();
                    ClassFileClassDeclaration declaration = declarationIterator.next();

                    while (statementIterator.hasNext()) {
                        Statement statement = statementIterator.next();

                        searchFirstLineNumberVisitor.init();
                        statement.accept(searchFirstLineNumberVisitor);

                        if (searchFirstLineNumberVisitor.getLineNumber() != -1) {
                            lineNumber = searchFirstLineNumberVisitor.getLineNumber();
                        }

                        while (declaration.getFirstLineNumber() <= lineNumber) {
                            statementIterator.previous();
                            statementIterator.add(new TypeDeclarationStatement(declaration));
                            statementIterator.next();
                            declarationIterator.remove();

                            if (!declarationIterator.hasNext()) {
                                return;
                            }

                            declaration = declarationIterator.next();
                        }
                    }
                }
            }
        }

        protected class MemberDeclarationComparator implements Comparator<ClassFileMemberDeclaration> {
            public int compare(ClassFileMemberDeclaration md1, ClassFileMemberDeclaration md2) {
                return md1.getFirstLineNumber() - md2.getFirstLineNumber();
            }
        }
    }
}
