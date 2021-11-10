/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view.component;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.jd.gui.api.API;
import org.jd.gui.api.model.Container;
import org.jd.gui.util.io.TextReader;
import org.jd.gui.util.parser.antlr.ANTLRJavaParser;
import org.jd.gui.util.parser.antlr.AbstractJavaListener;
import org.jd.gui.util.parser.antlr.JavaParser;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavaFilePage extends TypePage {

    public JavaFilePage(API api, Container.Entry entry) {
        super(api, entry);
        // Load content file
        String text = TextReader.getText(entry.getInputStream()).replace("\r\n", "\n").replace('\r', '\n');
        // Parse
        DeclarationListener declarationListener = new DeclarationListener(entry);
        ReferenceListener referenceListener = new ReferenceListener(entry);

        ANTLRJavaParser.parse(new ANTLRInputStream(text), declarationListener);
        referenceListener.init(declarationListener);
        ANTLRJavaParser.parse(new ANTLRInputStream(text), referenceListener);
        // Display
        setText(text);
        initLineNumbers();
    }

    public String getSyntaxStyle() { return SyntaxConstants.SYNTAX_STYLE_JAVA; }

    // --- ContentSavable --- //
    public String getFileName() {
        String path = entry.getPath();
        int index = path.lastIndexOf('/');
        return path.substring(index+1);
    }

    public class DeclarationListener extends AbstractJavaListener {
        protected StringBuilder sbTypeDeclaration = new StringBuilder();
        protected String currentInternalTypeName;

        public DeclarationListener(Container.Entry entry) { super(entry); }

        public HashMap<String, String> getNameToInternalTypeName() { return super.nameToInternalTypeName; }

        // --- Add declarations --- //
        public void enterPackageDeclaration(JavaParser.PackageDeclarationContext ctx) {
            super.enterPackageDeclaration(ctx);

            if (! packageName.isEmpty()) {
                sbTypeDeclaration.append(packageName).append('/');
            }
        }

        public void enterImportDeclaration(JavaParser.ImportDeclarationContext ctx) {
            List<TerminalNode> identifiers = ctx.qualifiedName().Identifier();
            String internalTypeName = concatIdentifiers(identifiers);
            String typeName = identifiers.get(identifiers.size()-1).getSymbol().getText();

            nameToInternalTypeName.put(typeName, internalTypeName);
        }

        public void enterClassDeclaration(JavaParser.ClassDeclarationContext ctx) { enterTypeDeclaration(ctx); }
        public void exitClassDeclaration(JavaParser.ClassDeclarationContext ctx) { exitTypeDeclaration(); }

        public void enterEnumDeclaration(JavaParser.EnumDeclarationContext ctx) { enterTypeDeclaration(ctx); }
        public void exitEnumDeclaration(JavaParser.EnumDeclarationContext ctx) { exitTypeDeclaration(); }

        public void enterInterfaceDeclaration(JavaParser.InterfaceDeclarationContext ctx) { enterTypeDeclaration(ctx); }
        public void exitInterfaceDeclaration(JavaParser.InterfaceDeclarationContext ctx) { exitTypeDeclaration(); }

        public void enterAnnotationTypeDeclaration(JavaParser.AnnotationTypeDeclarationContext ctx) { enterTypeDeclaration(ctx); }
        public void exitAnnotationTypeDeclaration(JavaParser.AnnotationTypeDeclarationContext ctx) { exitTypeDeclaration(); }

        public void enterTypeDeclaration(ParserRuleContext ctx) {
            // Type declaration
            TerminalNode identifier = ctx.getToken(JavaParser.Identifier, 0);
            String typeName = identifier.getText();
            int position = identifier.getSymbol().getStartIndex();
            int length = sbTypeDeclaration.length();

            if ((length == 0) || (sbTypeDeclaration.charAt(length-1) == '/')) {
                sbTypeDeclaration.append(typeName);
            } else {
                sbTypeDeclaration.append('$').append(typeName);
            }

            currentInternalTypeName = sbTypeDeclaration.toString();
            nameToInternalTypeName.put(typeName, currentInternalTypeName);

            // Super type reference
            JavaParser.TypeContext superType = ctx.getRuleContext(JavaParser.TypeContext.class, 0);
            String superInternalTypeName = (superType != null) ? resolveInternalTypeName(superType.classOrInterfaceType().Identifier()) : null;
            TypeDeclarationData data = new TypeDeclarationData(position, typeName.length(), currentInternalTypeName, null, null, superInternalTypeName);

            declarations.put(currentInternalTypeName, data);
            typeDeclarations.put(position, data);
        }

        public void exitTypeDeclaration() {
            int index = sbTypeDeclaration.lastIndexOf("$");

            if (index == -1) {
                index = sbTypeDeclaration.lastIndexOf("/") + 1;
            }

            if (index == -1) {
                sbTypeDeclaration.setLength(0);
            } else {
                sbTypeDeclaration.setLength(index);
            }

            currentInternalTypeName = sbTypeDeclaration.toString();
        }

        public void enterClassBodyDeclaration(JavaParser.ClassBodyDeclarationContext ctx) {
            if (ctx.getChildCount() == 2) {
                ParseTree first = ctx.getChild(0);

                if (first instanceof TerminalNode) {
                    TerminalNode f = (TerminalNode)first;

                    if (f.getSymbol().getType() == JavaParser.STATIC) {
                        String name = f.getText();
                        int position = f.getSymbol().getStartIndex();
                        declarations.put(currentInternalTypeName + "-<clinit>-()V", new TypePage.DeclarationData(position, 6, currentInternalTypeName, name, "()V"));
                    }
                }
            }
        }

        public void enterConstDeclaration(JavaParser.ConstDeclarationContext ctx) {
            JavaParser.TypeContext typeContext = ctx.type();

            for (JavaParser.ConstantDeclaratorContext constantDeclaratorContext : ctx.constantDeclarator()) {
                TerminalNode identifier = constantDeclaratorContext.Identifier();
                String name = identifier.getText();
                int dimensionOnVariable = countDimension(constantDeclaratorContext.children);
                String descriptor = createDescriptor(typeContext, dimensionOnVariable);
                int position = identifier.getSymbol().getStartIndex();

                declarations.put(currentInternalTypeName + '-' + name + '-' + descriptor, new TypePage.DeclarationData(position, name.length(), currentInternalTypeName, name, descriptor));
            }
        }

        public void enterFieldDeclaration(JavaParser.FieldDeclarationContext ctx) {
            JavaParser.TypeContext typeContext = ctx.type();

            for (JavaParser.VariableDeclaratorContext declaration : ctx.variableDeclarators().variableDeclarator()) {
                JavaParser.VariableDeclaratorIdContext variableDeclaratorId = declaration.variableDeclaratorId();
                TerminalNode identifier = variableDeclaratorId.Identifier();
                String name = identifier.getText();
                int dimensionOnVariable = countDimension(variableDeclaratorId.children);
                String descriptor = createDescriptor(typeContext, dimensionOnVariable);
                int position = identifier.getSymbol().getStartIndex();
                TypePage.DeclarationData data = new TypePage.DeclarationData(position, name.length(), currentInternalTypeName, name, descriptor);

                declarations.put(currentInternalTypeName + '-' + name + '-' + descriptor, data);
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

            String name = identifier.getText();
            String paramDescriptors = createParamDescriptors(formalParameters.formalParameterList());
            String returnDescriptor = createDescriptor(returnType, 0);
            String descriptor = paramDescriptors + returnDescriptor;
            int position = identifier.getSymbol().getStartIndex();

            declarations.put(currentInternalTypeName + '-' + name + '-' + descriptor, new TypePage.DeclarationData(position, name.length(), currentInternalTypeName, name, descriptor));
        }

        public void enterConstructorDeclaration(JavaParser.ConstructorDeclarationContext ctx) {
            TerminalNode identifier = ctx.Identifier();
            String name = identifier.getText();
            String paramDescriptors = createParamDescriptors(ctx.formalParameters().formalParameterList());
            String descriptor = paramDescriptors + "V";
            int position = identifier.getSymbol().getStartIndex();

            declarations.put(currentInternalTypeName + "-<init>-" + descriptor, new TypePage.DeclarationData(position, name.length(), currentInternalTypeName, name, descriptor));
        }

        public String createParamDescriptors(JavaParser.FormalParameterListContext formalParameterList) {
            StringBuilder paramDescriptors = null;

            if (formalParameterList != null) {
                List<JavaParser.FormalParameterContext> formalParameters = formalParameterList.formalParameter();
                paramDescriptors = new StringBuilder("(");

                for (JavaParser.FormalParameterContext formalParameter : formalParameters) {
                    int dimensionOnParameter = countDimension(formalParameter.variableDeclaratorId().children);
                    String descriptor = createDescriptor(formalParameter.type(), dimensionOnParameter);

                    paramDescriptors.append(descriptor);
                }
            }

            return (paramDescriptors == null) ? "()" : paramDescriptors.append(')').toString();
        }
    }

    public class ReferenceListener extends AbstractJavaListener {
        protected StringBuilder sbTypeDeclaration = new StringBuilder();
        protected HashMap<String, TypePage.ReferenceData> referencesCache = new HashMap<>();
        protected String currentInternalTypeName;
        protected Context currentContext = null;

        public ReferenceListener(Container.Entry entry) { super(entry); }

        public void init(DeclarationListener declarationListener) {
            this.nameToInternalTypeName.putAll(declarationListener.getNameToInternalTypeName());
        }

        // --- Add declarations --- //
        public void enterPackageDeclaration(JavaParser.PackageDeclarationContext ctx) {
            super.enterPackageDeclaration(ctx);

            if (! packageName.isEmpty()) {
                sbTypeDeclaration.append(packageName).append('/');
            }
        }

        public void enterImportDeclaration(JavaParser.ImportDeclarationContext ctx) {
            List<TerminalNode> identifiers = ctx.qualifiedName().Identifier();
            int position = identifiers.get(0).getSymbol().getStartIndex();
            String internalTypeName = concatIdentifiers(identifiers);

            addHyperlink(new TypePage.HyperlinkReferenceData(position, internalTypeName.length(), newReferenceData(internalTypeName, null, null, null)));
        }

        public void enterClassDeclaration(JavaParser.ClassDeclarationContext ctx) { enterTypeDeclaration(ctx); }
        public void exitClassDeclaration(JavaParser.ClassDeclarationContext ctx) { exitTypeDeclaration(); }

        public void enterEnumDeclaration(JavaParser.EnumDeclarationContext ctx) { enterTypeDeclaration(ctx); }
        public void exitEnumDeclaration(JavaParser.EnumDeclarationContext ctx) { exitTypeDeclaration(); }

        public void enterInterfaceDeclaration(JavaParser.InterfaceDeclarationContext ctx) { enterTypeDeclaration(ctx); }
        public void exitInterfaceDeclaration(JavaParser.InterfaceDeclarationContext ctx) { exitTypeDeclaration(); }

        public void enterAnnotationTypeDeclaration(JavaParser.AnnotationTypeDeclarationContext ctx) { enterTypeDeclaration(ctx); }
        public void exitAnnotationTypeDeclaration(JavaParser.AnnotationTypeDeclarationContext ctx) { exitTypeDeclaration(); }

        public void enterTypeDeclaration(ParserRuleContext ctx) {
            // Type declaration
            TerminalNode identifier = ctx.getToken(JavaParser.Identifier, 0);
            String typeName = identifier.getText();
            int length = sbTypeDeclaration.length();

            if ((length == 0) || (sbTypeDeclaration.charAt(length-1) == '/')) {
                sbTypeDeclaration.append(typeName);
            } else {
                sbTypeDeclaration.append('$').append(typeName);
            }

            currentInternalTypeName = sbTypeDeclaration.toString();
            currentContext = new Context(currentContext);
        }

        public void exitTypeDeclaration() {
            int index = sbTypeDeclaration.lastIndexOf("$");

            if (index == -1) {
                index = sbTypeDeclaration.lastIndexOf("/") + 1;
            }

            if (index == -1) {
                sbTypeDeclaration.setLength(0);
            } else {
                sbTypeDeclaration.setLength(index);
            }

            currentInternalTypeName = sbTypeDeclaration.toString();
        }

        public void enterFormalParameters(JavaParser.FormalParametersContext ctx) {
            JavaParser.FormalParameterListContext formalParameterList = ctx.formalParameterList();

            if (formalParameterList != null) {
                List<JavaParser.FormalParameterContext> formalParameters = formalParameterList.formalParameter();

                for (JavaParser.FormalParameterContext formalParameter : formalParameters) {
                    int dimensionOnParameter = countDimension(formalParameter.variableDeclaratorId().children);
                    String descriptor = createDescriptor(formalParameter.type(), dimensionOnParameter);
                    String name = formalParameter.variableDeclaratorId().Identifier().getSymbol().getText();

                    currentContext.nameToDescriptor.put(name, descriptor);
                }
            }
        }

        // --- Add references --- //
        public void enterType(JavaParser.TypeContext ctx) {
            // Add type reference
            JavaParser.ClassOrInterfaceTypeContext classOrInterfaceType = ctx.classOrInterfaceType();

            if (classOrInterfaceType != null) {
                List<TerminalNode> identifiers = classOrInterfaceType.Identifier();
                String name = concatIdentifiers(identifiers);
                String internalTypeName = resolveInternalTypeName(identifiers);
                int position = identifiers.get(0).getSymbol().getStartIndex();

                addHyperlink(new TypePage.HyperlinkReferenceData(position, name.length(), newReferenceData(internalTypeName, null, null, currentInternalTypeName)));
            }
        }

        public void enterLocalVariableDeclaration(JavaParser.LocalVariableDeclarationContext ctx) {
            JavaParser.TypeContext typeContext = ctx.type();

            for (JavaParser.VariableDeclaratorContext variableDeclarator : ctx.variableDeclarators().variableDeclarator()) {
                JavaParser.VariableDeclaratorIdContext variableDeclaratorId = variableDeclarator.variableDeclaratorId();
                int dimensionOnVariable = countDimension(variableDeclaratorId.children);
                String descriptor = createDescriptor(typeContext, dimensionOnVariable);
                String name = variableDeclarator.variableDeclaratorId().Identifier().getSymbol().getText();

                currentContext.nameToDescriptor.put(name, descriptor);
            }
        }

        public void enterCreator(JavaParser.CreatorContext ctx) {
            enterNewExpression(ctx.createdName().Identifier(), ctx.classCreatorRest());
        }

        public void enterInnerCreator(JavaParser.InnerCreatorContext ctx) {
            enterNewExpression(Collections.singletonList(ctx.Identifier()), ctx.classCreatorRest());
        }

        public void enterNewExpression(List<TerminalNode> identifiers, JavaParser.ClassCreatorRestContext classCreatorRest) {
            if (identifiers.size() > 0) {
                String name = concatIdentifiers(identifiers);
                String internalTypeName = resolveInternalTypeName(identifiers);
                int position = identifiers.get(0).getSymbol().getStartIndex();

                if (classCreatorRest != null) {
                    // Constructor call -> Add a link to the constructor declaration
                    JavaParser.ExpressionListContext expressionList = classCreatorRest.arguments().expressionList();
                    String descriptor = (expressionList != null) ? getParametersDescriptor(expressionList).append('V').toString() : "()V";

                    addHyperlink(new TypePage.HyperlinkReferenceData(position, name.length(), newReferenceData(internalTypeName, "<init>", descriptor, currentInternalTypeName)));
                } else {
                    // New type array -> Add a link to the type declaration
                    addHyperlink(new TypePage.HyperlinkReferenceData(position, name.length(), newReferenceData(internalTypeName, null, null, currentInternalTypeName)));
                }
            }
        }

        public void enterExpression(JavaParser.ExpressionContext ctx) {
            switch (ctx.getChildCount()) {
                case 1:
                    TerminalNode identifier0 = getToken(ctx.children, JavaParser.Identifier, 0);

                    if (identifier0 != null) {
                        if (isAField(ctx)) {
                            JavaParser.PrimaryContext primaryContext = ctx.primary();

                            if (primaryContext != null) {
                                String fieldName = primaryContext.literal().StringLiteral().getText();

                                if (currentContext.getDescriptor(fieldName) == null) {
                                    // Not a local variable or a method parameter
                                    String fieldTypeName = searchInternalTypeNameForThisFieldName(currentInternalTypeName, fieldName);
                                    int position = ctx.Identifier().getSymbol().getStartIndex();

                                    addHyperlink(new TypePage.HyperlinkReferenceData(position, fieldName.length(), newReferenceData(fieldTypeName, fieldName, "?", currentInternalTypeName)));
                                }
                            }
                        }
                    } else if (ctx.primary() != null) {
                        TerminalNode identifier = ctx.primary().Identifier();

                        if (identifier != null) {
                            Token symbol = identifier.getSymbol();
                            String name = symbol.getText();
                            String internalTypeName = nameToInternalTypeName.get(name);

                            if (internalTypeName != null) {
                                int position = symbol.getStartIndex();

                                addHyperlink(new TypePage.HyperlinkReferenceData(position, name.length(), newReferenceData(internalTypeName, null, null, currentInternalTypeName)));
                            }
                        }
                    }
                    break;
                case 3:
                    if (getToken(ctx.children, JavaParser.DOT, 1) != null) {
                        // Search "expression '.' Identifier" : field reference
                        TerminalNode identifier3 = getToken(ctx.children, JavaParser.Identifier, 2);

                        if ((identifier3 != null) && isAField(ctx)) {
                            String fieldTypeName = getInternalTypeName(ctx.getChild(0));

                            if (fieldTypeName != null) {
                                int position = identifier3.getSymbol().getStartIndex();
                                String fieldName = identifier3.getText();

                                addHyperlink(new TypePage.HyperlinkReferenceData(position, fieldName.length(), newReferenceData(fieldTypeName, fieldName, "?", currentInternalTypeName)));
                            }
                        }
                    } else if (getToken(ctx.children, JavaParser.LPAREN, 1) != null) {
                        // Search "expression '(' ')'" : method reference
                        if (getToken(ctx.children, JavaParser.RPAREN, 2) != null) {
                            enterCallMethodExpression(ctx, null);
                        }
                    }
                    break;
                case 4:
                    if (getToken(ctx.children, JavaParser.LPAREN, 1) != null) {
                        // Search "expression '(' expressionList ')'" : method reference
                        if (getToken(ctx.children, JavaParser.RPAREN, 3) != null) {
                            JavaParser.ExpressionListContext expressionListContext = ctx.expressionList();

                            if ((expressionListContext != null) && (expressionListContext == ctx.children.get(2))) {
                                enterCallMethodExpression(ctx, expressionListContext);
                            }
                        }
                    }
                    break;
            }
        }

        public void enterCallMethodExpression(JavaParser.ExpressionContext ctx, JavaParser.ExpressionListContext expressionListContext) {
            ParseTree first = ctx.children.get(0);

            if (first instanceof JavaParser.ExpressionContext) {
                JavaParser.ExpressionContext f = (JavaParser.ExpressionContext)first;

                switch (f.getChildCount()) {
                    case 1:
                        JavaParser.PrimaryContext primary = f.primary();
                        TerminalNode identifier = primary.Identifier();

                        if (identifier != null) {
                            Token symbol = identifier.getSymbol();

                            if (symbol != null) {
                                String methodName = symbol.getText();
                                String methodTypeName = searchInternalTypeNameForThisMethodName(currentInternalTypeName, methodName);

                                if (methodTypeName != null) {
                                    int position = symbol.getStartIndex();
                                    String methodDescriptor = (expressionListContext != null) ? getParametersDescriptor(expressionListContext).append('?').toString() : "()?";

                                    addHyperlink(new TypePage.HyperlinkReferenceData(position, methodName.length(), newReferenceData(methodTypeName, methodName, methodDescriptor, currentInternalTypeName)));
                                }
                            }
                        } else {
                            Token symbol = primary.getChild(TerminalNode.class, 0).getSymbol();

                            if (symbol != null) {
                                switch (symbol.getType()) {
                                    case JavaParser.THIS:
                                        int position = symbol.getStartIndex();
                                        String methodDescriptor = (expressionListContext != null) ? getParametersDescriptor(expressionListContext).append('?').toString() : "()?";

                                        addHyperlink(new TypePage.HyperlinkReferenceData(position, 4, newReferenceData(currentInternalTypeName, "<init>", methodDescriptor, currentInternalTypeName)));
                                        break;
                                    case JavaParser.SUPER:
                                        DeclarationData data = declarations.get(currentInternalTypeName);

                                        if (data instanceof TypeDeclarationData) {
                                            position = symbol.getStartIndex();
                                            String methodTypeName = ((TypeDeclarationData) data).superTypeName;
                                            methodDescriptor = (expressionListContext != null) ? getParametersDescriptor(expressionListContext).append('?').toString() : "()?";

                                            addHyperlink(new TypePage.HyperlinkReferenceData(position, 5, newReferenceData(methodTypeName, "<init>", methodDescriptor, currentInternalTypeName)));
                                        }
                                        break;
                                }
                            }
                        }
                        break;
                    case 3:
                        // Search "expression '.' Identifier"
                        ParseTree dot = first.getChild(1);

                        if ((dot instanceof TerminalNode) && (((TerminalNode)dot).getSymbol().getType() == JavaParser.DOT)) {
                            ParseTree identifier3 = first.getChild(2);

                            if (identifier3 instanceof TerminalNode) {
                                TerminalNode i3 = (TerminalNode)identifier3;

                                if (i3.getSymbol().getType() == JavaParser.Identifier) {
                                    String methodTypeName = getInternalTypeName(first.getChild(0));

                                    if (methodTypeName != null) {
                                        int position = i3.getSymbol().getStartIndex();
                                        String methodName = i3.getText();
                                        String methodDescriptor = (expressionListContext != null) ? getParametersDescriptor(expressionListContext).append('?').toString() : "()?";

                                        addHyperlink(new TypePage.HyperlinkReferenceData(position, methodName.length(), newReferenceData(methodTypeName, methodName, methodDescriptor, currentInternalTypeName)));
                                    }
                                }
                            }
                        }
                        break;
                }
            }
        }

        public StringBuilder getParametersDescriptor(JavaParser.ExpressionListContext expressionListContext) {
            StringBuilder sb = new StringBuilder('(');
            for (JavaParser.ExpressionContext exp : expressionListContext.expression()) sb.append('?');
            sb.append(')');
            return sb;
        }

        public boolean isAField(JavaParser.ExpressionContext ctx) {
            RuleContext parent = ctx.parent;

            if (parent instanceof JavaParser.ExpressionContext) {
                int size = parent.getChildCount();

                if (parent.getChild(size - 1) != ctx) {
                    for (int i=0; i<size; i++) {
                        if (parent.getChild(i) == ctx) {
                            ParseTree next = parent.getChild(i+1);

                            if (next instanceof TerminalNode) {
                                switch (((TerminalNode)next).getSymbol().getType()) {
                                    case JavaParser.DOT:
                                    case JavaParser.LPAREN:
                                        return false;
                                }
                            }
                        }
                    }
                }
            }

            return true;
        }

        public String getInternalTypeName(ParseTree pt) {
            if (pt instanceof JavaParser.ExpressionContext) {

                if (pt.getChildCount() == 1) {
                    JavaParser.PrimaryContext primary = ((JavaParser.ExpressionContext)pt).primary();
                    TerminalNode identifier = primary.Identifier();

                    if (identifier != null) {
                        String name = identifier.getSymbol().getText();
                        String descriptor = (currentContext == null) ? null : currentContext.getDescriptor(name);

                        if (descriptor != null) {
                            // Is a local variable or a method parameter
                            if (descriptor.charAt(0) == 'L') {
                                return descriptor.substring(1, descriptor.length() - 1);
                            }
                        } else if (currentInternalTypeName != null) {
                            String internalTypeName = searchInternalTypeNameForThisFieldName(currentInternalTypeName, name);

                            if (internalTypeName != null) {
                                // Is a field
                                return internalTypeName;
                            } else {
                                internalTypeName = resolveInternalTypeName(Collections.singletonList(identifier));

                                if (internalTypeName != null) {
                                    // Is a type
                                    return internalTypeName;
                                } else {
                                    // Not found
                                    return null;
                                }
                            }
                        }
                    } else {
                        TerminalNode tn = primary.getChild(TerminalNode.class, 0);
                        Token symbol = (tn == null) ? null : tn.getSymbol();

                        if (symbol != null) {
                            switch (symbol.getType()) {
                                case JavaParser.THIS:
                                    return currentInternalTypeName;
                                case JavaParser.SUPER:
                                    DeclarationData data = declarations.get(currentInternalTypeName);

                                    if (data instanceof TypeDeclarationData) {
                                        return ((TypeDeclarationData)data).superTypeName;
                                    } else {
                                        return null;
                                    }
                            }
                        }
                    }
                }
            }

            return null;
        }

        public String searchInternalTypeNameForThisFieldName(String internalTypeName, String name) {
            String prefix = internalTypeName + '-' + name + '-';
            int length = prefix.length();

            for (Map.Entry<String, DeclarationData> entry : declarations.entrySet()) {
                if (entry.getKey().startsWith(prefix) && (entry.getKey().charAt(length) != '(')) {
                    return entry.getValue().typeName;
                }
            }

            // Not found
            int index = internalTypeName.lastIndexOf('$');

            if (index != -1) {
                // Search in the outer type
                internalTypeName = internalTypeName.substring(0, index);

                return searchInternalTypeNameForThisFieldName(internalTypeName, name);
            }

            // Not found
            return null;
        }

        public String searchInternalTypeNameForThisMethodName(String internalTypeName, String name) {
            String prefix = internalTypeName + '-' + name + "-(";

            for (Map.Entry<String, DeclarationData> entry : declarations.entrySet()) {
                if (entry.getKey().startsWith(prefix)) {
                    return entry.getValue().typeName;
                }
            }

            // Not found
            int index = internalTypeName.lastIndexOf('$');

            if (index != -1) {
                // Search in the outer type
                internalTypeName = internalTypeName.substring(0, index);

                return searchInternalTypeNameForThisMethodName(internalTypeName, name);
            }

            // Not found
            return null;
        }

        public TerminalNode getToken(List<ParseTree> children, int type, int i) {
            ParseTree pt = children.get(i);

            if (pt instanceof TerminalNode) {
                if (((TerminalNode)pt).getSymbol().getType() == type) {
                    return (TerminalNode)pt;
                }
            }

            return null;
        }

        public void enterBlock(JavaParser.BlockContext ctx) {
            currentContext = new Context(currentContext);
        }

        public void exitBlock(JavaParser.BlockContext ctx) {
            currentContext = currentContext.outerContext;
        }

        public TypePage.ReferenceData newReferenceData(String internalName, String name, String descriptor, String scopeInternalName) {
            String key = internalName + '-' + name + '-'+ descriptor + '-' + scopeInternalName;
            TypePage.ReferenceData reference = referencesCache.get(key);

            if (reference == null) {
                reference = new TypePage.ReferenceData(internalName, name, descriptor, scopeInternalName);
                referencesCache.put(key, reference);
                references.add(reference);
            }

            return reference;
        }

        // --- Add strings --- //
        public void enterLiteral(JavaParser.LiteralContext ctx) {
            TerminalNode stringLiteral = ctx.StringLiteral();

            if (stringLiteral != null) {
                String str = stringLiteral.getSymbol().getText();
                int position = stringLiteral.getSymbol().getStartIndex();

                strings.add(new TypePage.StringData(position, str.length(), str, currentInternalTypeName));
            }
        }
    }

    public static class Context {
        protected Context outerContext;

        protected HashMap<String, String> nameToDescriptor = new HashMap<>();

        public Context(Context outerContext) {
            this.outerContext = outerContext;
        }

        /**
         * @param name Parameter or variable name
         * @return Qualified type name
         */
        public String getDescriptor(String name) {
            String descriptor = nameToDescriptor.get(name);

            if ((descriptor == null) && (outerContext != null)) {
                descriptor = outerContext.getDescriptor(name);
            }

            return descriptor;
        }
    }

    public static class TypeDeclarationData extends TypePage.DeclarationData {
        protected String superTypeName;

        public TypeDeclarationData(int startPosition, int length, String type, String name, String descriptor, String superTypeName) {
            super(startPosition, length, type, name, descriptor);

            this.superTypeName = superTypeName;
        }
    }
}
