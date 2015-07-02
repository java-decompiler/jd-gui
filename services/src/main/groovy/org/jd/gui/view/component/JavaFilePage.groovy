/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.view.component

import groovy.transform.CompileStatic
import org.jd.gui.api.API
import org.jd.gui.api.model.Container
import org.jd.gui.util.parser.antlr.ANTLRJavaParser
import org.jd.gui.util.parser.antlr.AbstractJavaListener
import org.jd.gui.util.parser.antlr.JavaParser
import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode
import org.fife.ui.rsyntaxtextarea.SyntaxConstants

class JavaFilePage extends TypePage {

    JavaFilePage(API api, Container.Entry entry) {
        super(api, entry)
        // Load content file
        def text = entry.inputStream.text.replace('\r\n', '\n').replace('\r', '\n')
        // Parse
        def declarationListener = new DeclarationListener(entry)
        def referenceListener = new ReferenceListener(entry)

        ANTLRJavaParser.parse(new ANTLRInputStream(text), declarationListener)
        referenceListener.init(declarationListener)
        ANTLRJavaParser.parse(new ANTLRInputStream(text), referenceListener)
        // Display
        setText(text)
        // Show hyperlinks
        indexesChanged(api.collectionOfIndexes)
    }

    String getSyntaxStyle() { SyntaxConstants.SYNTAX_STYLE_JAVA }

    // --- ContentSavable --- //
    String getFileName() {
        def path = entry.path
        int index = path.lastIndexOf('/')
        return path.substring(index+1)
    }

    @CompileStatic
    class DeclarationListener extends AbstractJavaListener {

        protected StringBuffer sbTypeDeclaration = new StringBuffer()
        protected String currentInternalTypeName

        DeclarationListener(Container.Entry entry) { super(entry) }

        HashMap<String, String> getNameToInternalTypeName() { super.nameToInternalTypeName }

        // --- Add declarations --- //
        void enterPackageDeclaration(JavaParser.PackageDeclarationContext ctx) {
            super.enterPackageDeclaration(ctx);

            if (! packageName.isEmpty()) {
                sbTypeDeclaration.append(packageName).append('/');
            }
        }

        void enterImportDeclaration(JavaParser.ImportDeclarationContext ctx) {
            List<TerminalNode> identifiers = ctx.qualifiedName().Identifier()
            String internalTypeName = concatIdentifiers(identifiers)
            String typeName = identifiers.get(identifiers.size()-1).symbol.text

            nameToInternalTypeName.put(typeName, internalTypeName)
        }

        void enterClassDeclaration(JavaParser.ClassDeclarationContext ctx) { enterTypeDeclaration(ctx); }
        void exitClassDeclaration(JavaParser.ClassDeclarationContext ctx) { exitTypeDeclaration(); }

        void enterEnumDeclaration(JavaParser.EnumDeclarationContext ctx) { enterTypeDeclaration(ctx); }
        void exitEnumDeclaration(JavaParser.EnumDeclarationContext ctx) { exitTypeDeclaration(); }

        void enterInterfaceDeclaration(JavaParser.InterfaceDeclarationContext ctx) { enterTypeDeclaration(ctx); }
        void exitInterfaceDeclaration(JavaParser.InterfaceDeclarationContext ctx) { exitTypeDeclaration(); }

        void enterAnnotationTypeDeclaration(JavaParser.AnnotationTypeDeclarationContext ctx) { enterTypeDeclaration(ctx); }
        void exitAnnotationTypeDeclaration(JavaParser.AnnotationTypeDeclarationContext ctx) { exitTypeDeclaration(); }

        void enterTypeDeclaration(ParserRuleContext ctx) {
            // Type declaration
            def identifier = ctx.getToken(JavaParser.Identifier, 0);
            def typeName = identifier.text
            int position = identifier.symbol.startIndex
            int length = sbTypeDeclaration.length();

            if ((length == 0) || (sbTypeDeclaration.charAt(length-1) == '/')) {
                sbTypeDeclaration.append(typeName);
            } else {
                sbTypeDeclaration.append('$').append(typeName);
            }

            currentInternalTypeName = sbTypeDeclaration.toString()
            nameToInternalTypeName.put(typeName, currentInternalTypeName);

            // Super type reference
            JavaParser.TypeContext superType = ctx.getRuleContext(JavaParser.TypeContext.class, 0)
            String superInternalTypeName = superType ? resolveInternalTypeName(superType.classOrInterfaceType().Identifier()) : null

            def data = new TypeDeclarationData(position, typeName.length(), currentInternalTypeName, null, null, superInternalTypeName)

            declarations.put(currentInternalTypeName, data)
            typeDeclarations.put(position, data)
        }

        void exitTypeDeclaration() {
            int index = sbTypeDeclaration.lastIndexOf('$');

            if (index == -1) {
                index = sbTypeDeclaration.lastIndexOf('/') + 1;
            }

            if (index == -1) {
                sbTypeDeclaration.setLength(0);
            } else {
                sbTypeDeclaration.setLength(index);
            }

            currentInternalTypeName = sbTypeDeclaration.toString()
        }

        public void enterClassBodyDeclaration(JavaParser.ClassBodyDeclarationContext ctx) {
            if (ctx.getChildCount() == 2) {
                def first = ctx.getChild(0);

                if (first instanceof TerminalNode) {
                    if (first.getSymbol().type == JavaParser.STATIC) {
                        String name = first.text
                        int position = first.getSymbol().startIndex
                        declarations.put(currentInternalTypeName + '-<clinit>-()V', new TypePage.DeclarationData(position, 6, currentInternalTypeName, name, '()V'))
                    }
                }
            }
        }

        void enterConstDeclaration(JavaParser.ConstDeclarationContext ctx) {
            def typeContext = ctx.type();

            for (def constantDeclaratorContext : ctx.constantDeclarator()) {
                def identifier = constantDeclaratorContext.Identifier()
                def name = identifier.text
                int dimensionOnVariable = countDimension(constantDeclaratorContext.children)
                def descriptor = createDescriptor(typeContext, dimensionOnVariable)
                int position = identifier.symbol.startIndex

                declarations.put(currentInternalTypeName + '-' + name + '-' + descriptor, new TypePage.DeclarationData(position, name.length(), currentInternalTypeName, name, descriptor))
            }
        }

        void enterFieldDeclaration(JavaParser.FieldDeclarationContext ctx) {
            def typeContext = ctx.type();

            for (JavaParser.VariableDeclaratorContext declaration : ctx.variableDeclarators().variableDeclarator()) {
                def variableDeclaratorId = declaration.variableDeclaratorId()
                def identifier = variableDeclaratorId.Identifier()
                def name = identifier.text
                int dimensionOnVariable = countDimension(variableDeclaratorId.children)
                def descriptor = createDescriptor(typeContext, dimensionOnVariable)
                int position = identifier.symbol.startIndex
                def data = new TypePage.DeclarationData(position, name.length(), currentInternalTypeName, name, descriptor)

                declarations.put(currentInternalTypeName + '-' + name + '-' + descriptor, data)
            }
        }

        void enterMethodDeclaration(JavaParser.MethodDeclarationContext ctx) {
            enterMethodDeclaration(ctx, ctx.Identifier(), ctx.formalParameters(), ctx.type());
        }

        void enterInterfaceMethodDeclaration(JavaParser.InterfaceMethodDeclarationContext ctx) {
            enterMethodDeclaration(ctx, ctx.Identifier(), ctx.formalParameters(), ctx.type());
        }

        void enterMethodDeclaration(
                ParserRuleContext ctx, TerminalNode identifier,
                JavaParser.FormalParametersContext formalParameters, JavaParser.TypeContext returnType) {

            def name = identifier.text
            def paramDescriptors = createParamDescriptors(formalParameters.formalParameterList())
            def returnDescriptor = createDescriptor(returnType, 0)
            def descriptor = paramDescriptors + returnDescriptor
            int position = identifier.symbol.startIndex

            declarations.put(currentInternalTypeName + '-' + name + '-' + descriptor, new TypePage.DeclarationData(position, name.length(), currentInternalTypeName, name, descriptor))
        }

        void enterConstructorDeclaration(JavaParser.ConstructorDeclarationContext ctx) {
            def identifier = ctx.Identifier()
            def name = identifier.text
            def paramDescriptors = createParamDescriptors(ctx.formalParameters().formalParameterList())
            def descriptor = paramDescriptors + "V"
            int position = identifier.symbol.startIndex

            declarations.put(currentInternalTypeName + '-<init>-' + descriptor, new TypePage.DeclarationData(position, name.length(), currentInternalTypeName, name, descriptor))
        }

        String createParamDescriptors(JavaParser.FormalParameterListContext formalParameterList) {
            StringBuffer paramDescriptors = null

            if (formalParameterList != null) {
                def formalParameters = formalParameterList.formalParameter()
                paramDescriptors = new StringBuffer("(")

                for (def formalParameter : formalParameters) {
                    int dimensionOnParameter = countDimension(formalParameter.variableDeclaratorId().children)
                    def descriptor = createDescriptor(formalParameter.type(), dimensionOnParameter)

                    paramDescriptors.append(descriptor)
                }
            }

            return (paramDescriptors == null) ? "()" : paramDescriptors.append(')').toString();
        }
    }

    @CompileStatic
    class ReferenceListener extends AbstractJavaListener {

        protected StringBuffer sbTypeDeclaration = new StringBuffer()
        protected HashMap<String, TypePage.ReferenceData> referencesCache = new HashMap<>()
        protected String currentInternalTypeName
        protected Context currentContext = null

        ReferenceListener(Container.Entry entry) { super(entry) }

        void init(DeclarationListener declarationListener) {
            this.nameToInternalTypeName.putAll(declarationListener.nameToInternalTypeName)
        }

        // --- Add declarations --- //
        void enterPackageDeclaration(JavaParser.PackageDeclarationContext ctx) {
            super.enterPackageDeclaration(ctx);

            if (! packageName.isEmpty()) {
                sbTypeDeclaration.append(packageName).append('/');
            }
        }

        void enterImportDeclaration(JavaParser.ImportDeclarationContext ctx) {
            List<TerminalNode> identifiers = ctx.qualifiedName().Identifier()
            int position = identifiers.get(0).symbol.startIndex
            String internalTypeName = concatIdentifiers(identifiers)

            addHyperlink(new TypePage.HyperlinkReferenceData(position, internalTypeName.length(), newReferenceData(internalTypeName, null, null, null)))
        }

        void enterClassDeclaration(JavaParser.ClassDeclarationContext ctx) { enterTypeDeclaration(ctx); }
        void exitClassDeclaration(JavaParser.ClassDeclarationContext ctx) { exitTypeDeclaration(); }

        void enterEnumDeclaration(JavaParser.EnumDeclarationContext ctx) { enterTypeDeclaration(ctx); }
        void exitEnumDeclaration(JavaParser.EnumDeclarationContext ctx) { exitTypeDeclaration(); }

        void enterInterfaceDeclaration(JavaParser.InterfaceDeclarationContext ctx) { enterTypeDeclaration(ctx); }
        void exitInterfaceDeclaration(JavaParser.InterfaceDeclarationContext ctx) { exitTypeDeclaration(); }

        void enterAnnotationTypeDeclaration(JavaParser.AnnotationTypeDeclarationContext ctx) { enterTypeDeclaration(ctx); }
        void exitAnnotationTypeDeclaration(JavaParser.AnnotationTypeDeclarationContext ctx) { exitTypeDeclaration(); }

        void enterTypeDeclaration(ParserRuleContext ctx) {
            // Type declaration
            def identifier = ctx.getToken(JavaParser.Identifier, 0);
            def typeName = identifier.text
            int length = sbTypeDeclaration.length();

            if ((length == 0) || (sbTypeDeclaration.charAt(length-1) == '/')) {
                sbTypeDeclaration.append(typeName);
            } else {
                sbTypeDeclaration.append('$').append(typeName);
            }

            currentInternalTypeName = sbTypeDeclaration.toString()
            currentContext = new Context(currentContext)
        }

        void exitTypeDeclaration() {
            int index = sbTypeDeclaration.lastIndexOf('$');

            if (index == -1) {
                index = sbTypeDeclaration.lastIndexOf('/') + 1;
            }

            if (index == -1) {
                sbTypeDeclaration.setLength(0);
            } else {
                sbTypeDeclaration.setLength(index);
            }

            currentInternalTypeName = sbTypeDeclaration.toString()
        }

        void enterFormalParameters(JavaParser.FormalParametersContext ctx) {
            def formalParameterList = ctx.formalParameterList()

            if (formalParameterList != null) {
                def formalParameters = formalParameterList.formalParameter()

                for (def formalParameter : formalParameters) {
                    int dimensionOnParameter = countDimension(formalParameter.variableDeclaratorId().children)
                    def descriptor = createDescriptor(formalParameter.type(), dimensionOnParameter)
                    def name = formalParameter.variableDeclaratorId().Identifier().symbol.text

                    currentContext.nameToDescriptor.put(name, descriptor)
                }
            }
        }

        // --- Add references --- //
        void enterType(JavaParser.TypeContext ctx) {
            // Add type reference
            def classOrInterfaceType = ctx.classOrInterfaceType()

            if (classOrInterfaceType != null) {
                def identifiers = classOrInterfaceType.Identifier()
                def name = concatIdentifiers(identifiers)
                def internalTypeName = resolveInternalTypeName(identifiers)
                int position = identifiers.get(0).symbol.startIndex

                addHyperlink(new TypePage.HyperlinkReferenceData(position, name.length(), newReferenceData(internalTypeName, null, null, currentInternalTypeName)))
            }
        }

        void enterLocalVariableDeclaration(JavaParser.LocalVariableDeclarationContext ctx) {
            def typeContext = ctx.type()

            for (def variableDeclarator : ctx.variableDeclarators().variableDeclarator()) {
                def variableDeclaratorId = variableDeclarator.variableDeclaratorId()
                int dimensionOnVariable = countDimension(variableDeclaratorId.children)
                def descriptor = createDescriptor(typeContext, dimensionOnVariable)
                def name = variableDeclarator.variableDeclaratorId().Identifier().getSymbol().getText()

                currentContext.nameToDescriptor.put(name, descriptor)
            }
        }

        void enterCreator(JavaParser.CreatorContext ctx) {
            enterNewExpression(ctx.createdName().Identifier(), ctx.classCreatorRest())
        }

        void enterInnerCreator(JavaParser.InnerCreatorContext ctx) {
            enterNewExpression(Collections.singletonList(ctx.Identifier()), ctx.classCreatorRest())
        }

        void enterNewExpression(List<TerminalNode> identifiers, JavaParser.ClassCreatorRestContext classCreatorRest) {
            if (identifiers.size() > 0) {
                def name = concatIdentifiers(identifiers)
                def internalTypeName = resolveInternalTypeName(identifiers)
                int position = identifiers.get(0).symbol.startIndex

                if (classCreatorRest) {
                    // Constructor call -> Add a link to the constructor declaration
                    def expressionList = classCreatorRest.arguments().expressionList()
                    def descriptor = expressionList ? getParametersDescriptor(expressionList).append('V').toString() : '()V'

                    addHyperlink(new TypePage.HyperlinkReferenceData(position, name.length(), newReferenceData(internalTypeName, '<init>', descriptor, currentInternalTypeName)))
                } else {
                    // New type array -> Add a link to the type declaration
                    addHyperlink(new TypePage.HyperlinkReferenceData(position, name.length(), newReferenceData(internalTypeName, null, null, currentInternalTypeName)))
                }
            }
        }

        void enterExpression(JavaParser.ExpressionContext ctx) {
            switch (ctx.getChildCount()) {
                case 1:
                    TerminalNode identifier0 = getToken(ctx.children, JavaParser.Identifier, 0);

                    if (identifier0 != null) {
                        if (isAField(ctx)) {
                            def primaryContext = ctx.primary()

                            if (primaryContext) {
                                String fieldName = primaryContext.literal().StringLiteral();

                                if (!currentContext.getDescriptor(fieldName) != null) {
                                    // Not a local variable or a method parameter
                                    def fieldTypeName = searchInternalTypeNameForThisFieldName(currentInternalTypeName, fieldName)
                                    int position = ctx.Identifier().getSymbol().startIndex

                                    addHyperlink(new TypePage.HyperlinkReferenceData(position, fieldName.length(), newReferenceData(fieldTypeName, fieldName, '?', currentInternalTypeName)))
                                }
                            }
                        }
                    } else {
                        def identifier = ctx.primary().Identifier()

                        if (identifier) {
                            def symbol = identifier.getSymbol()
                            def name = symbol.text
                            def internalTypeName = nameToInternalTypeName.get(name)

                            if (internalTypeName) {
                                int position = symbol.startIndex

                                addHyperlink(new TypePage.HyperlinkReferenceData(position, name.length(), newReferenceData(internalTypeName, null, null, currentInternalTypeName)))
                            }
                        }
                    }
                    break;
                case 3:
                    if (getToken(ctx.children, JavaParser.DOT, 1) != null) {
                        // Search "expression '.' Identifier" : field reference
                        def identifier3 = getToken(ctx.children, JavaParser.Identifier, 2);

                        if ((identifier3 != null) && isAField(ctx)) {
                            def fieldTypeName = getInternalTypeName(ctx.getChild(0))

                            if (fieldTypeName) {
                                int position = identifier3.symbol.startIndex
                                def fieldName = identifier3.getText()

                                addHyperlink(new TypePage.HyperlinkReferenceData(position, fieldName.length(), newReferenceData(fieldTypeName, fieldName, '?', currentInternalTypeName)))
                            }
                        }
                    } else if (getToken(ctx.children, JavaParser.LPAREN, 1) != null) {
                        // Search "expression '(' ')'" : method reference
                        if (getToken(ctx.children, JavaParser.RPAREN, 2) != null) {
                            enterCallMethodExpression(ctx, null)
                        }
                    }
                    break;
                case 4:
                    if (getToken(ctx.children, JavaParser.LPAREN, 1) != null) {
                        // Search "expression '(' expressionList ')'" : method reference
                        if (getToken(ctx.children, JavaParser.RPAREN, 3) != null) {
                            def expressionListContext = ctx.expressionList();

                            if ((expressionListContext != null) && (expressionListContext == ctx.children.get(2))) {
                                enterCallMethodExpression(ctx, expressionListContext)
                            }
                        }
                    }
                    break;
            }
        }

        void enterCallMethodExpression(JavaParser.ExpressionContext ctx, JavaParser.ExpressionListContext expressionListContext) {
            ParseTree first = ctx.children.get(0)

            if (first instanceof JavaParser.ExpressionContext) {
                switch (first.getChildCount()) {
                    case 1:
                        def primary = first.primary()
                        def identifier = primary.Identifier()

                        if (identifier) {
                            def symbol = identifier.getSymbol()

                            if (symbol) {
                                String methodName = symbol.text
                                String methodTypeName = searchInternalTypeNameForThisMethodName(currentInternalTypeName, methodName)

                                if (methodTypeName) {
                                    int position = symbol.startIndex
                                    def methodDescriptor = expressionListContext ? getParametersDescriptor(expressionListContext).append('?').toString() : '()?'

                                    addHyperlink(new TypePage.HyperlinkReferenceData(position, methodName.length(), newReferenceData(methodTypeName, methodName, methodDescriptor, currentInternalTypeName)))
                                }
                            }
                        } else {
                            def symbol = primary.getChild(TerminalNode.class, 0).getSymbol()

                            if (symbol) {
                                switch (symbol.type) {
                                    case JavaParser.THIS:
                                        int position = symbol.startIndex
                                        def methodDescriptor = expressionListContext ? getParametersDescriptor(expressionListContext).append('?').toString() : '()?'

                                        addHyperlink(new TypePage.HyperlinkReferenceData(position, 4, newReferenceData(currentInternalTypeName, '<init>', methodDescriptor, currentInternalTypeName)))
                                        break
                                    case JavaParser.SUPER:
                                        def data = declarations.get(currentInternalTypeName)

                                        if (data instanceof TypeDeclarationData) {
                                            int position = symbol.startIndex
                                            def methodTypeName = ((TypeDeclarationData) data).superTypeName
                                            def methodDescriptor = expressionListContext ? getParametersDescriptor(expressionListContext).append('?').toString() : '()?'

                                            addHyperlink(new TypePage.HyperlinkReferenceData(position, 5, newReferenceData(methodTypeName, '<init>', methodDescriptor, currentInternalTypeName)))
                                        }
                                        break
                                }
                            }
                        }
                        break
                    case 3:
                        // Search "expression '.' Identifier"
                        ParseTree dot = first.getChild(1)

                        if ((dot instanceof TerminalNode) && (dot.getSymbol().getType() == JavaParser.DOT)) {
                            ParseTree identifier3 = first.getChild(2)

                            if ((identifier3 instanceof TerminalNode) && (identifier3.getSymbol().type == JavaParser.Identifier)) {
                                String methodTypeName = getInternalTypeName(first.getChild(0))

                                if (methodTypeName) {
                                    int position = identifier3.getSymbol().startIndex
                                    def methodName = identifier3.getText()
                                    def methodDescriptor = expressionListContext ? getParametersDescriptor(expressionListContext).append('?').toString() : '()?'

                                    addHyperlink(new TypePage.HyperlinkReferenceData(position, methodName.length(), newReferenceData(methodTypeName, methodName, methodDescriptor, currentInternalTypeName)))
                                }
                            }
                        }
                        break
                }
            }
        }

        StringBuffer getParametersDescriptor(JavaParser.ExpressionListContext expressionListContext) {
            def sb = new StringBuffer('(')
            for (def exp : expressionListContext.expression()) sb.append('?')
            sb.append(')')
            return sb
        }

        boolean isAField(JavaParser.ExpressionContext ctx) {
            def parent = ctx.parent

            if (parent instanceof JavaParser.ExpressionContext) {
                int size = parent.getChildCount();

                if (parent.getChild(size - 1) != ctx) {
                    for (int i=0; i<size; i++) {
                        if (parent.getChild(i) == ctx) {
                            def next = parent.getChild(i+1)

                            if (next instanceof TerminalNode) {
                                switch (next.getSymbol().getType()) {
                                    case JavaParser.DOT:
                                    case JavaParser.LPAREN:
                                        return false
                                }
                            }
                        }
                    }
                }
            }

            return true
        }

        String getInternalTypeName(ParseTree pt) {
            if (pt instanceof JavaParser.ExpressionContext) {

                if (pt.getChildCount() == 1) {
                    def primary = pt.primary()
                    def identifier = primary.Identifier()

                    if (identifier) {
                        String name = identifier.getSymbol().text
                        String descriptor = currentContext?.getDescriptor(name);

                        if (descriptor) {
                            // Is a local variable or a method parameter
                            if (descriptor.charAt(0) == 'L') {
                                return descriptor.substring(1, descriptor.length() - 1)
                            }
                        } else if (currentInternalTypeName) {
                            String internalTypeName = searchInternalTypeNameForThisFieldName(currentInternalTypeName, name)

                            if (internalTypeName) {
                                // Is a field
                                return internalTypeName
                            } else {
                                internalTypeName = resolveInternalTypeName(Collections.singletonList(identifier))

                                if (internalTypeName) {
                                    // Is a type
                                    return internalTypeName
                                } else {
                                    // Not found
                                    return null
                                }
                            }
                        }
                    } else {
                        def symbol = primary.getChild(TerminalNode.class, 0)?.getSymbol()

                        if (symbol) {
                            switch (symbol.type) {
                                case JavaParser.THIS:
                                    return currentInternalTypeName
                                case JavaParser.SUPER:
                                    def data = declarations.get(currentInternalTypeName)

                                    if (data instanceof TypeDeclarationData) {
                                        return ((TypeDeclarationData)data).superTypeName
                                    } else {
                                        return null
                                    }
                            }
                        }
                    }
                }
            }

            return null
        }

        String searchInternalTypeNameForThisFieldName(String internalTypeName, String name) {
            String prefix = internalTypeName + '-' + name + '-'
            int length = prefix.length()

            for (def entry : declarations.entrySet()) {
                if (entry.key.startsWith(prefix) && (entry.key.charAt(length) != '(')) {
                    return entry.value.typeName
                }
            }

            // Not found
            int index = internalTypeName.lastIndexOf('$')

            if (index != -1) {
                // Search in the outer type
                internalTypeName = internalTypeName.substring(0, index)

                return searchInternalTypeNameForThisFieldName(internalTypeName, name)
            }

            // Not found
            return null
        }

        String searchInternalTypeNameForThisMethodName(String internalTypeName, String name) {
            String prefix = internalTypeName + '-' + name + '-('

            for (def entry : declarations.entrySet()) {
                if (entry.key.startsWith(prefix)) {
                    return entry.value.typeName
                }
            }

            // Not found
            int index = internalTypeName.lastIndexOf('$')

            if (index != -1) {
                // Search in the outer type
                internalTypeName = internalTypeName.substring(0, index)

                return searchInternalTypeNameForThisMethodName(internalTypeName, name)
            }

            // Not found
            return null
        }

        TerminalNode getToken(List<ParseTree> children, int type, int i) {
            ParseTree pt = children.get(i);

            if (pt instanceof TerminalNode) {
                if (((TerminalNode)pt).getSymbol().getType() == type) {
                    return (TerminalNode)pt;
                }
            }

            return null;
        }

        void enterBlock(JavaParser.BlockContext ctx) {
            currentContext = new Context(currentContext)
        }

        void exitBlock(JavaParser.BlockContext ctx) {
            currentContext = currentContext.outerContext
        }

        TypePage.ReferenceData newReferenceData(String internalName, String name, String descriptor, String scopeInternalName) {
            def key = internalName + '-' + name + '-'+ descriptor + '-' + scopeInternalName
            def reference = referencesCache.get(key)

            if (reference == null) {
                reference = new TypePage.ReferenceData(internalName, name, descriptor, scopeInternalName)
                referencesCache.put(key, reference)
                references.add(reference)
            }

            return reference
        }

        // --- Add strings --- //
        void enterLiteral(JavaParser.LiteralContext ctx) {
            def stringLiteral = ctx.StringLiteral()

            if (stringLiteral != null) {
                String str = stringLiteral.getSymbol().getText()
                int position = stringLiteral.getSymbol().getStartIndex()

                strings.add(new TypePage.StringData(position, str.length(), str, currentInternalTypeName))
            }
        }
    }

    @CompileStatic
    static class Context {
        Context outerContext

        HashMap<String, String> nameToDescriptor = new HashMap<>()

        Context(Context outerContext) {
            this.outerContext = outerContext
        }

        /**
         * @param name Parameter or variable name
         * @return Qualified type name
         */
        String getDescriptor(String name) {
            String descriptor = nameToDescriptor.get(name)

            if ((descriptor == null) && (outerContext != null)) {
                descriptor = outerContext.getDescriptor(name)
            }

            return descriptor
        }
    }

    @CompileStatic
    static class TypeDeclarationData extends TypePage.DeclarationData {
        String superTypeName

        TypeDeclarationData(int startPosition, int length, String type, String name, String descriptor, String superTypeName) {
            super(startPosition, length, type, name, descriptor)

            this.superTypeName = superTypeName
        }
    }
}
