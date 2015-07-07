/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.service.indexer;

import org.jd.gui.api.API;
import org.jd.gui.api.model.Container;
import org.jd.gui.api.model.Indexes;
import org.jd.gui.util.parser.antlr.ANTLRJavaParser;
import org.jd.gui.util.parser.antlr.AbstractJavaListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jd.gui.util.parser.antlr.JavaParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Unsafe thread implementation of java file indexer.
 */
public class JavaFileIndexerProvider extends AbstractIndexerProvider {

    static {
        // Early class loading
        ANTLRJavaParser.parse(new ANTLRInputStream("class EarlyLoading{}"), new Listener(null));
    }

    /**
     * @return local + optional external selectors
     */
    public String[] getSelectors() {
        List<String> externalSelectors = getExternalSelectors();

        if (externalSelectors == null) {
            return new String[] { "*:file:*.java" };
        } else {
            int size = externalSelectors.size();
            String[] selectors = new String[size+1];
            externalSelectors.toArray(selectors);
            selectors[size] = "*:file:*.java";
            return selectors;
        }
    }

    /**
     * Index format : @see jd.gui.spi.Indexer
     */
    @SuppressWarnings("unchecked")
    public void index(API api, Container.Entry entry, Indexes indexes) {
        try (InputStream inputStream = entry.getInputStream()) {
            Listener listener = new Listener(entry);
            ANTLRJavaParser.parse(new ANTLRInputStream(inputStream), listener);

            // Append sets to indexes
            addToIndex(indexes, "typeDeclarations", listener.getTypeDeclarationSet(), entry);
            addToIndex(indexes, "constructorDeclarations", listener.getConstructorDeclarationSet(), entry);
            addToIndex(indexes, "methodDeclarations", listener.getMethodDeclarationSet(), entry);
            addToIndex(indexes, "fieldDeclarations", listener.getFieldDeclarationSet(), entry);
            addToIndex(indexes, "typeReferences", listener.getTypeReferenceSet(), entry);
            addToIndex(indexes, "constructorReferences", listener.getConstructorReferenceSet(), entry);
            addToIndex(indexes, "methodReferences", listener.getMethodReferenceSet(), entry);
            addToIndex(indexes, "fieldReferences", listener.getFieldReferenceSet(), entry);
            addToIndex(indexes, "strings", listener.getStringSet(), entry);

            // Populate map [super type name : [sub type name]]
            Map<String, Collection> index = indexes.getIndex("subTypeNames");

            for (Map.Entry<String, HashSet<String>> e : listener.getSuperTypeNamesMap().entrySet()) {
                String typeName = e.getKey();

                for (String superTypeName : e.getValue()) {
                    index.get(superTypeName).add(typeName);
                }
            }
        } catch (IOException ignore) {
        }
    }

    protected static class Listener extends AbstractJavaListener {

        protected HashSet<String> typeDeclarationSet = new HashSet<>();
        protected HashSet<String> constructorDeclarationSet = new HashSet<>();
        protected HashSet<String> methodDeclarationSet = new HashSet<>();
        protected HashSet<String> fieldDeclarationSet = new HashSet<>();
        protected HashSet<String> typeReferenceSet = new HashSet<>();
        protected HashSet<String> constructorReferenceSet = new HashSet<>();
        protected HashSet<String> methodReferenceSet = new HashSet<>();
        protected HashSet<String> fieldReferenceSet = new HashSet<>();
        protected HashSet<String> stringSet = new HashSet<>();
        protected HashMap<String, HashSet<String>> superTypeNamesMap = new HashMap<>();

        protected StringBuffer sbTypeDeclaration = new StringBuffer();

        public Listener(Container.Entry entry) {
            super(entry);
        }

        public HashSet<String> getTypeDeclarationSet() { return typeDeclarationSet; }
        public HashSet<String> getConstructorDeclarationSet() { return constructorDeclarationSet; }
        public HashSet<String> getMethodDeclarationSet() { return methodDeclarationSet; }
        public HashSet<String> getFieldDeclarationSet() { return fieldDeclarationSet; }
        public HashSet<String> getTypeReferenceSet() { return typeReferenceSet; }
        public HashSet<String> getConstructorReferenceSet() { return constructorReferenceSet; }
        public HashSet<String> getMethodReferenceSet() { return methodReferenceSet; }
        public HashSet<String> getFieldReferenceSet() { return fieldReferenceSet; }
        public HashSet<String> getStringSet() { return stringSet; }
        public HashMap<String, HashSet<String>> getSuperTypeNamesMap() { return superTypeNamesMap; }

        // --- ANTLR Listener --- //

        public void enterPackageDeclaration(JavaParser.PackageDeclarationContext ctx) {
            super.enterPackageDeclaration(ctx);

            if (! packageName.isEmpty()) {
                sbTypeDeclaration.append(packageName).append('/');
            }
        }

        public void enterClassDeclaration(JavaParser.ClassDeclarationContext ctx) { enterTypeDeclaration(ctx); }
        public void exitClassDeclaration(JavaParser.ClassDeclarationContext ctx) { exitTypeDeclaration(); }

        public void enterEnumDeclaration(JavaParser.EnumDeclarationContext ctx) { enterTypeDeclaration(ctx); }
        public void exitEnumDeclaration(JavaParser.EnumDeclarationContext ctx) { exitTypeDeclaration(); }

        public void enterInterfaceDeclaration(JavaParser.InterfaceDeclarationContext ctx) { enterTypeDeclaration(ctx); }
        public void exitInterfaceDeclaration(JavaParser.InterfaceDeclarationContext ctx) { exitTypeDeclaration(); }

        public void enterAnnotationTypeDeclaration(JavaParser.AnnotationTypeDeclarationContext ctx) { enterTypeDeclaration(ctx); }
        public void exitAnnotationTypeDeclaration(JavaParser.AnnotationTypeDeclarationContext ctx) { exitTypeDeclaration(); }

        protected void enterTypeDeclaration(ParserRuleContext ctx) {
            // Add type declaration
            String typeName = ctx.getToken(JavaParser.Identifier, 0).getText();
            int length = sbTypeDeclaration.length();

            if ((length == 0) || (sbTypeDeclaration.charAt(length-1) == '/')) {
                sbTypeDeclaration.append(typeName);
            } else {
                sbTypeDeclaration.append('$').append(typeName);
            }

            String internalTypeName = sbTypeDeclaration.toString();
            typeDeclarationSet.add(internalTypeName);
            nameToInternalTypeName.put(typeName, internalTypeName);

            HashSet<String> superInternalTypeNameSet = new HashSet<>();

            // Add super type reference
            JavaParser.TypeContext superType = ctx.getRuleContext(JavaParser.TypeContext.class, 0);
            if (superType != null) {
                String superQualifiedTypeName = resolveInternalTypeName(superType.classOrInterfaceType().Identifier());

                if (superQualifiedTypeName.charAt(0) != '*')
                    superInternalTypeNameSet.add(superQualifiedTypeName);
            }

            // Add implementation references
            JavaParser.TypeListContext superInterfaces = ctx.getRuleContext(JavaParser.TypeListContext.class, 0);
            if (superInterfaces != null) {
                for (JavaParser.TypeContext superInterface : superInterfaces.type()) {
                    String superQualifiedInterfaceName = resolveInternalTypeName(superInterface.classOrInterfaceType().Identifier());

                    if (superQualifiedInterfaceName.charAt(0) != '*')
                        superInternalTypeNameSet.add(superQualifiedInterfaceName);
                }
            }

            if (! superInternalTypeNameSet.isEmpty()) {
                superTypeNamesMap.put(internalTypeName, superInternalTypeNameSet);
            }
        }

        protected void exitTypeDeclaration() {
            int index = sbTypeDeclaration.lastIndexOf("$");

            if (index == -1) {
                index = sbTypeDeclaration.lastIndexOf("/") + 1;
            }

            if (index == -1) {
                sbTypeDeclaration.setLength(0);
            } else {
                sbTypeDeclaration.setLength(index);
            }
        }

        public void enterType(JavaParser.TypeContext ctx) {
            // Add type reference
            JavaParser.ClassOrInterfaceTypeContext classOrInterfaceType = ctx.classOrInterfaceType();

            if (classOrInterfaceType != null) {
                String internalTypeName = resolveInternalTypeName(classOrInterfaceType.Identifier());

                if (internalTypeName.charAt(0) != '*')
                    typeReferenceSet.add(internalTypeName);
            }
        }

        public void enterConstDeclaration(JavaParser.ConstDeclarationContext ctx) {
            for (JavaParser.ConstantDeclaratorContext constantDeclaratorContext : ctx.constantDeclarator()) {
                String name = constantDeclaratorContext.Identifier().getText();
                fieldDeclarationSet.add(name);
            }
        }

        public void enterFieldDeclaration(JavaParser.FieldDeclarationContext ctx) {
            for (JavaParser.VariableDeclaratorContext declaration : ctx.variableDeclarators().variableDeclarator()) {
                String name = declaration.variableDeclaratorId().Identifier().getText();
                fieldDeclarationSet.add(name);
            }
        }

        public void enterMethodDeclaration(JavaParser.MethodDeclarationContext ctx) {
            String name = ctx.Identifier().getText();
            methodDeclarationSet.add(name);
        }

        public void enterInterfaceMethodDeclaration(JavaParser.InterfaceMethodDeclarationContext ctx) {
            String name = ctx.Identifier().getText();
            methodDeclarationSet.add(name);
        }

        public void enterConstructorDeclaration(JavaParser.ConstructorDeclarationContext ctx) {
            String name = ctx.Identifier().getText();
            constructorDeclarationSet.add(name);
        }

        public void enterCreatedName(JavaParser.CreatedNameContext ctx) {
            String internalTypeName = resolveInternalTypeName(ctx.Identifier());

            if ((internalTypeName != null) && (internalTypeName.charAt(0) != '*'))
                constructorReferenceSet.add(internalTypeName);
        }

        public void enterExpression(JavaParser.ExpressionContext ctx) {
            switch (ctx.getChildCount()) {
                case 3:
                    if (getToken(ctx.children, 1, JavaParser.DOT) != null) {
                        // Search "expression '.' Identifier" : field
                        TerminalNode identifier3 = getToken(ctx.children, 2, JavaParser.Identifier);

                        if (identifier3 != null) {
                            String fieldName = identifier3.getText();
                            fieldReferenceSet.add(fieldName);
                        }
                    } else if (getToken(ctx.children, 1, JavaParser.LPAREN) != null) {
                        // Search "expression '(' ')'" : method
                        if (getToken(ctx.children, 2, JavaParser.RPAREN) != null) {
                            TerminalNode identifier0 = getRightTerminalNode(ctx.children.get(0));

                            if (identifier0 != null) {
                                String methodName = identifier0.getText();
                                methodReferenceSet.add(methodName);
                            }
                        }
                    }
                    break;
                case 4:
                    if (getToken(ctx.children, 1, JavaParser.LPAREN) != null) {
                        // Search "expression '(' expressionList ')'" : method
                        if (getToken(ctx.children, 3, JavaParser.RPAREN) != null) {
                            JavaParser.ExpressionListContext expressionListContext = ctx.expressionList();

                            if ((expressionListContext != null) && (expressionListContext == ctx.children.get(2))) {
                                TerminalNode identifier0 = getRightTerminalNode(ctx.children.get(0));

                                if (identifier0 != null) {
                                    String methodName = identifier0.getText();
                                    methodReferenceSet.add(methodName);
                                }
                            }
                        }
                    }
                    break;
            }
        }

        protected TerminalNode getToken(List<ParseTree> children, int i, int type) {
            ParseTree pt = children.get(i);

            if (pt instanceof TerminalNode) {
                if (((TerminalNode)pt).getSymbol().getType() == type) {
                    return (TerminalNode)pt;
                }
            }

            return null;
        }

        protected TerminalNode getRightTerminalNode(ParseTree pt) {
            if (pt instanceof ParserRuleContext) {
                List<ParseTree> children = ((ParserRuleContext)pt).children;
                int size = children.size();

                if (size > 0) {
                    ParseTree last = children.get(size - 1);

                    if (last instanceof TerminalNode) {
                        return (TerminalNode) last;
                    } else {
                        return getRightTerminalNode(last);
                    }
                }
            }

            return null;
        }

        public void enterLiteral(JavaParser.LiteralContext ctx) {
            TerminalNode stringLiteral = ctx.StringLiteral();
            if (stringLiteral != null) {
                stringSet.add(stringLiteral.getSymbol().getText());
            }
        }
    }
}
