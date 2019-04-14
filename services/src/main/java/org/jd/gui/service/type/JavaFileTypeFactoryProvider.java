/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.type;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jd.gui.api.API;
import org.jd.gui.api.model.Container;
import org.jd.gui.api.model.Type;
import org.jd.gui.util.exception.ExceptionUtil;
import org.jd.gui.util.parser.antlr.ANTLRJavaParser;
import org.jd.gui.util.parser.antlr.AbstractJavaListener;
import org.jd.gui.util.parser.antlr.JavaParser;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

public class JavaFileTypeFactoryProvider extends AbstractTypeFactoryProvider {

    static {
        // Early class loading
        ANTLRJavaParser.parse(new ANTLRInputStream("class EarlyLoading{}"), new Listener(null));
    }

    // Create cache
    protected Cache<URI, Listener> cache = new Cache<>();

    @Override public String[] getSelectors() { return appendSelectors("*:file:*.java"); }

    @Override
    public Collection<Type> make(API api, Container.Entry entry) {
        Listener listener = getListener(entry);

        if (listener == null) {
            return Collections.emptyList();
        } else {
            return listener.getRootTypes();
        }
    }

    @Override
    public Type make(API api, Container.Entry entry, String fragment) {
        Listener listener = getListener(entry);

        if (listener == null) {
            return null;
        } else {
            if ((fragment != null) && (fragment.length() > 0)) {
                // Search type name in fragment. URI format : see jd.gui.api.feature.UriOpener
                int index = fragment.indexOf('-');

                if (index != -1) {
                    // Keep type name only
                    fragment = fragment.substring(0, index);
                }

                return listener.getType(fragment);
            } else {
                return listener.getMainType();
            }
        }
    }

    protected Listener getListener(Container.Entry entry) {
        URI key = entry.getUri();

        if (cache.containsKey(key)) {
            return cache.get(key);
        } else {
            Listener listener;

            try (InputStream inputStream = entry.getInputStream()) {
                ANTLRJavaParser.parse(new ANTLRInputStream(inputStream), listener = new Listener(entry));
            } catch (IOException e) {
                assert ExceptionUtil.printStackTrace(e);
                listener = null;
            }

            cache.put(key, listener);
            return listener;
        }
    }

    protected static class JavaType implements Type {
        protected int access;
        protected String name;
        protected String superName;
        protected String outerName;

        protected String displayTypeName;
        protected String displayInnerTypeName;
        protected String displayPackageName;

        protected List<Type> innerTypes = new ArrayList<>();
        protected List<Field> fields = new ArrayList<>();
        protected List<Method> methods = new ArrayList<>();

        protected JavaType outerType;

        public JavaType(
                int access, String name, String superName, String outerName,
                String displayTypeName, String displayInnerTypeName, String displayPackageName,
                JavaType outerType) {

            this.access = access;
            this.name = name;
            this.superName = superName;
            this.outerName = outerName;
            this.displayTypeName = displayTypeName;
            this.displayInnerTypeName = displayInnerTypeName;
            this.displayPackageName = displayPackageName;
            this.outerType = outerType;
        }

        public int getFlags() { return access; }
        public String getName() { return name; }
        public String getSuperName() { return superName; }
        public String getOuterName() { return outerName; }
        public String getDisplayTypeName() { return displayTypeName; }
        public String getDisplayInnerTypeName() { return displayInnerTypeName; }
        public String getDisplayPackageName() { return displayPackageName; }
        public Icon getIcon() { return getTypeIcon(access); }
        public JavaType getOuterType() { return outerType; }
        public Collection<Type> getInnerTypes() { return innerTypes; }
        public Collection<Field> getFields() { return fields; }
        public Collection<Method> getMethods() { return methods; }
    }

    protected static class JavaField implements Type.Field {
        protected int access;
        protected String name;
        protected String descriptor;

        public JavaField(int access, String name, String descriptor) {
            this.access = access;
            this.name = name;
            this.descriptor = descriptor;
        }

        public int getFlags() { return access; }
        public String getName() { return name; }
        public String getDescriptor() { return descriptor; }
        public Icon getIcon() { return getFieldIcon(access); }

        public String getDisplayName() {
            StringBuilder sb = new StringBuilder();
            sb.append(name).append(" : ");
            writeSignature(sb, descriptor, descriptor.length(), 0, false);
            return sb.toString();
        }
    }

    protected static class JavaMethod implements Type.Method {
        protected JavaType type;
        protected int access;
        protected String name;
        protected String descriptor;

        public JavaMethod(JavaType type, int access, String name, String descriptor) {
            this.type = type;
            this.access = access;
            this.name = name;
            this.descriptor = descriptor;
        }

        public int getFlags() { return access; }
        public String getName() { return name; }
        public String getDescriptor() { return descriptor; }
        public Icon getIcon() { return getMethodIcon(access); }

        public String getDisplayName() {
            String constructorName = type.getDisplayInnerTypeName();
            boolean isInnerClass = (constructorName != null);

            if (constructorName == null)
                constructorName = type.getDisplayTypeName();

            StringBuilder sb = new StringBuilder();
            writeMethodSignature(sb, access, access, isInnerClass, constructorName, name, descriptor);
            return sb.toString();
        }
    }

    protected static class Listener extends AbstractJavaListener {

        protected String displayPackageName = "";

        protected JavaType mainType = null;
        protected JavaType currentType = null;
        protected ArrayList<Type> rootTypes = new ArrayList<>();
        protected HashMap<String, Type> types = new HashMap<>();

        public Listener(Container.Entry entry) {
            super(entry);
        }

        public Type getMainType() {
            return mainType;
        }
        public Type getType(String typeName) {
            return types.get(typeName);
        }
        public ArrayList<Type> getRootTypes() {
            return rootTypes;
        }

        // --- ANTLR Listener --- //

        public void enterPackageDeclaration(JavaParser.PackageDeclarationContext ctx) {
            super.enterPackageDeclaration(ctx);
            displayPackageName = packageName.replace('/', '.');
        }

        public void enterClassDeclaration(JavaParser.ClassDeclarationContext ctx) { enterTypeDeclaration(ctx, 0); }
        public void exitClassDeclaration(JavaParser.ClassDeclarationContext ctx) { exitTypeDeclaration(); }

        public void enterEnumDeclaration(JavaParser.EnumDeclarationContext ctx) { enterTypeDeclaration(ctx, JavaType.FLAG_ENUM); }
        public void exitEnumDeclaration(JavaParser.EnumDeclarationContext ctx) { exitTypeDeclaration(); }

        public void enterInterfaceDeclaration(JavaParser.InterfaceDeclarationContext ctx) { enterTypeDeclaration(ctx, JavaType.FLAG_INTERFACE); }
        public void exitInterfaceDeclaration(JavaParser.InterfaceDeclarationContext ctx) { exitTypeDeclaration(); }

        public void enterAnnotationTypeDeclaration(JavaParser.AnnotationTypeDeclarationContext ctx) { enterTypeDeclaration(ctx, JavaType.FLAG_ANNOTATION); }
        public void exitAnnotationTypeDeclaration(JavaParser.AnnotationTypeDeclarationContext ctx) { exitTypeDeclaration(); }

        protected void enterTypeDeclaration(ParserRuleContext ctx, int access) {
            String name = ctx.getToken(JavaParser.Identifier, 0).getText();

            JavaParser.TypeContext superType = ctx.getRuleContext(JavaParser.TypeContext.class, 0);
            String superQualifiedTypeName;

            if (superType == null) {
                superQualifiedTypeName = ((access & JavaType.FLAG_INTERFACE) == 0) ? "java/lang/Object" : "";
            } else {
                superQualifiedTypeName = resolveInternalTypeName(superType.classOrInterfaceType().Identifier());
            }

            ParserRuleContext parent = ctx.getParent();

            if (parent instanceof JavaParser.TypeDeclarationContext)
                access += getTypeDeclarationContextAccessFlag(parent);
            else if (parent instanceof JavaParser.MemberDeclarationContext)
                access += getMemberDeclarationContextAccessFlag(parent.getParent());

            if (currentType == null) {
                String internalTypeName = packageName.isEmpty() ? name : packageName + "/" + name;
                String outerName = null;
                String displayTypeName = name;
                String displayInnerTypeName = null;

                currentType = new JavaType(access, internalTypeName, superQualifiedTypeName, outerName, displayTypeName, displayInnerTypeName, displayPackageName, null);
                types.put(internalTypeName, currentType);
                rootTypes.add(currentType);
                nameToInternalTypeName.put(name, internalTypeName);

                if (mainType == null) {
                    mainType = currentType;
                } else {
                    // Multi class definitions in the same file
                    String path = entry.getPath();
                    int index = path.lastIndexOf('/') + 1;

                    if (path.substring(index).startsWith(name + '.')) {
                        // Select the correct root type
                        mainType = currentType;
                    }
                }
            } else {
                String internalTypeName = currentType.getName() + '$' + name;
                String outerName = currentType.getName();
                String displayTypeName = currentType.getDisplayTypeName() + '.' + name;
                String displayInnerTypeName = name;
                JavaType subType = new JavaType(access, internalTypeName, superQualifiedTypeName, outerName, displayTypeName, displayInnerTypeName, displayPackageName, currentType);

                currentType.getInnerTypes().add(subType);
                currentType = subType;
                types.put(internalTypeName, currentType);
                nameToInternalTypeName.put(name, internalTypeName);
            }
        }

        protected void exitTypeDeclaration() {
            currentType = currentType.getOuterType();
        }

        public void enterClassBodyDeclaration(JavaParser.ClassBodyDeclarationContext ctx) {
            if (ctx.getChildCount() == 2) {
                ParseTree first = ctx.getChild(0);

                if (first instanceof TerminalNode) {
                    if (((TerminalNode)first).getSymbol().getType() == JavaParser.STATIC) {
                        currentType.getMethods().add(new JavaMethod(currentType, JavaType.FLAG_STATIC, "<clinit>", "()V"));
                    }
                }
            }
        }

        public void enterConstDeclaration(JavaParser.ConstDeclarationContext ctx) {
            JavaParser.TypeContext typeContext = ctx.type();
            int access = getClassBodyDeclarationAccessFlag(ctx.getParent().getParent());

            for (JavaParser.ConstantDeclaratorContext constantDeclaratorContext : ctx.constantDeclarator()) {
                TerminalNode identifier = constantDeclaratorContext.Identifier();
                String name = identifier.getText();
                int dimensionOnVariable = countDimension(constantDeclaratorContext.children);
                String descriptor = createDescriptor(typeContext, dimensionOnVariable);

                currentType.getFields().add(new JavaField(access, name, descriptor));
            }
        }

        public void enterFieldDeclaration(JavaParser.FieldDeclarationContext ctx) {
            JavaParser.TypeContext typeContext = ctx.type();
            int access = getClassBodyDeclarationAccessFlag(ctx.getParent().getParent());

            for (JavaParser.VariableDeclaratorContext declaration : ctx.variableDeclarators().variableDeclarator()) {
                JavaParser.VariableDeclaratorIdContext variableDeclaratorId = declaration.variableDeclaratorId();
                TerminalNode identifier = variableDeclaratorId.Identifier();
                String name = identifier.getText();
                int dimensionOnVariable = countDimension(variableDeclaratorId.children);
                String descriptor = createDescriptor(typeContext, dimensionOnVariable);

                currentType.getFields().add(new JavaField(access, name, descriptor));
            }
        }

        public void enterMethodDeclaration(JavaParser.MethodDeclarationContext ctx) {
            enterMethodDeclaration(ctx, ctx.Identifier(), ctx.formalParameters(), ctx.type());
        }

        public void enterInterfaceMethodDeclaration(JavaParser.InterfaceMethodDeclarationContext ctx) {
            enterMethodDeclaration(ctx, ctx.Identifier(), ctx.formalParameters(), ctx.type());
        }

        public void enterMethodDeclaration(
                ParserRuleContext ctx, TerminalNode identifier,
                JavaParser.FormalParametersContext formalParameters, JavaParser.TypeContext returnType) {

            int access = getClassBodyDeclarationAccessFlag(ctx.getParent().getParent());
            String name = identifier.getText();
            String paramDescriptors = createParamDescriptors(formalParameters.formalParameterList());
            String returnDescriptor = createDescriptor(returnType, 0);
            String descriptor = paramDescriptors + returnDescriptor;

            currentType.getMethods().add(new JavaMethod(currentType, access, name, descriptor));
        }

        public void enterConstructorDeclaration(JavaParser.ConstructorDeclarationContext ctx) {
            int access = getClassBodyDeclarationAccessFlag(ctx.getParent().getParent());
            String paramDescriptors = createParamDescriptors(ctx.formalParameters().formalParameterList());
            String descriptor = paramDescriptors + "V";

            currentType.getMethods().add(new JavaMethod(currentType, access, "<init>", descriptor));
        }

        protected String createParamDescriptors(JavaParser.FormalParameterListContext formalParameterList) {
            StringBuilder paramDescriptors = null;

            if (formalParameterList != null) {
                List<JavaParser.FormalParameterContext> formalParameters = formalParameterList.formalParameter();
                paramDescriptors = new StringBuilder("(");

                for (JavaParser.FormalParameterContext formalParameter : formalParameters) {
                    int dimensionOnParameter = countDimension(formalParameter.variableDeclaratorId().children);
                    paramDescriptors.append(createDescriptor(formalParameter.type(), dimensionOnParameter));
                }
            }

            return (paramDescriptors == null) ? "()" : paramDescriptors.append(')').toString();
        }

        protected int getTypeDeclarationContextAccessFlag(ParserRuleContext ctx) {
            int access = 0;

            for (JavaParser.ClassOrInterfaceModifierContext coiModifierContext : ctx.getRuleContexts(JavaParser.ClassOrInterfaceModifierContext.class)) {
                access += getAccessFlag(coiModifierContext);
            }

            return access;
        }

        protected int getMemberDeclarationContextAccessFlag(ParserRuleContext ctx) {
            int access = 0;

            for (JavaParser.ModifierContext modifierContext : ctx.getRuleContexts(JavaParser.ModifierContext.class)) {
                JavaParser.ClassOrInterfaceModifierContext coiModifierContext = modifierContext.classOrInterfaceModifier();
                if (coiModifierContext != null) {
                    access += getAccessFlag(coiModifierContext);
                }
            }

            return access;
        }

        protected int getClassBodyDeclarationAccessFlag(ParserRuleContext ctx) {
            if ((currentType.access & JavaType.FLAG_INTERFACE) == 0) {
                int access = 0;

                for (JavaParser.ModifierContext modifierContext : ctx.getRuleContexts(JavaParser.ModifierContext.class)) {
                    JavaParser.ClassOrInterfaceModifierContext coimc = modifierContext.classOrInterfaceModifier();

                    if (coimc != null) {
                        access += getAccessFlag(coimc);
                    }
                }

                return access;
            } else {
                return JavaType.FLAG_PUBLIC;
            }
        }

        protected int getAccessFlag(JavaParser.ClassOrInterfaceModifierContext ctx) {
            if (ctx.getChildCount() == 1) {
                ParseTree first = ctx.getChild(0);

                if (first instanceof TerminalNode) {
                    switch (((TerminalNode)first).getSymbol().getType()) {
                        case JavaParser.STATIC:    return JavaType.FLAG_STATIC;
                        case JavaParser.FINAL:     return JavaType.FLAG_FINAL;
                        case JavaParser.ABSTRACT:  return JavaType.FLAG_ABSTRACT;
                        case JavaParser.PUBLIC:    return JavaType.FLAG_PUBLIC;
                        case JavaParser.PROTECTED: return JavaType.FLAG_PROTECTED;
                        case JavaParser.PRIVATE:   return JavaType.FLAG_PRIVATE;
                    }
                }
            }

            return 0;
        }
    }
}
