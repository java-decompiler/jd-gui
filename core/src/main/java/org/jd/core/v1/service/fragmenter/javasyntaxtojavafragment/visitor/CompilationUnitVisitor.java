/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.fragmenter.javasyntaxtojavafragment.visitor;

import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.model.javafragment.*;
import org.jd.core.v1.model.javasyntax.AbstractJavaSyntaxVisitor;
import org.jd.core.v1.model.javasyntax.CompilationUnit;
import org.jd.core.v1.model.javasyntax.declaration.*;
import org.jd.core.v1.model.javasyntax.expression.BaseExpression;
import org.jd.core.v1.model.javasyntax.reference.*;
import org.jd.core.v1.model.javasyntax.statement.BaseStatement;
import org.jd.core.v1.model.javasyntax.type.*;
import org.jd.core.v1.model.token.*;
import org.jd.core.v1.service.fragmenter.javasyntaxtojavafragment.util.JavaFragmentFactory;

import java.util.Iterator;
import java.util.List;

import static org.jd.core.v1.model.javasyntax.declaration.Declaration.*;

public class CompilationUnitVisitor extends StatementVisitor {
    public static final KeywordToken ABSTRACT = new KeywordToken("abstract");
    public static final KeywordToken ANNOTATION = new KeywordToken("@interface");
    public static final KeywordToken CLASS = new KeywordToken("class");
    public static final KeywordToken DEFAULT = new KeywordToken("default");
    public static final KeywordToken ENUM = new KeywordToken("enum");
    public static final KeywordToken IMPLEMENTS = new KeywordToken("implements");
    public static final KeywordToken INTERFACE = new KeywordToken("interface");
    public static final KeywordToken NATIVE = new KeywordToken("native");
    public static final KeywordToken PACKAGE = new KeywordToken("package");
    public static final KeywordToken PRIVATE = new KeywordToken("private");
    public static final KeywordToken PROTECTED = new KeywordToken("protected");
    public static final KeywordToken PUBLIC = new KeywordToken("public");
    public static final KeywordToken STATIC = new KeywordToken("static");
    public static final KeywordToken THROWS = new KeywordToken("throws");

    public static final TextToken COMMENT_BRIDGE = new TextToken("/* bridge */");
    public static final TextToken COMMENT_SYNTHETIC = new TextToken("/* synthetic */");

    protected AnnotationVisitor annotationVisitor = new AnnotationVisitor();
    protected SingleLineStatementVisitor singleLineStatementVisitor = new SingleLineStatementVisitor();
    protected String mainInternalName;

    public CompilationUnitVisitor(Loader loader, String mainInternalTypeName, int majorVersion, ImportsFragment importsFragment) {
        super(loader, mainInternalTypeName, majorVersion, importsFragment);
        this.mainInternalName = mainInternalTypeName;
    }

    @Override
    public void visit(AnnotationDeclaration declaration) {
        if ((declaration.getFlags() & FLAG_SYNTHETIC) == 0) {
            fragments.add(StartMovableJavaBlockFragment.START_MOVABLE_TYPE_BLOCK);

            buildFragmentsForTypeDeclaration(declaration, declaration.getFlags() & ~FLAG_ABSTRACT, ANNOTATION);

            fragments.addTokensFragment(tokens);

            BaseFieldDeclarator annotationDeclaratorList = declaration.getAnnotationDeclarators();
            BodyDeclaration bodyDeclaration = declaration.getBodyDeclaration();

            if ((annotationDeclaratorList == null) && (bodyDeclaration == null)) {
                tokens.add(TextToken.SPACE);
                tokens.add(TextToken.LEFTRIGHTCURLYBRACKETS);
            } else {
                int fragmentCount1 = fragments.size();
                StartBodyFragment start = JavaFragmentFactory.addStartTypeBody(fragments);
                int fragmentCount2 = fragments.size();

                storeContext();
                currentInternalTypeName = declaration.getInternalTypeName();
                currentTypeName = declaration.getName();

                if (annotationDeclaratorList != null) {
                    annotationDeclaratorList.accept(this);

                    if (bodyDeclaration != null) {
                        JavaFragmentFactory.addSpacerBetweenMembers(fragments);
                    }
                }

                safeAccept(bodyDeclaration);

                restoreContext();

                if (fragmentCount2 == fragments.size()) {
                    fragments.subList(fragmentCount1, fragmentCount2).clear();
                    tokens.add(TextToken.SPACE);
                    tokens.add(TextToken.LEFTRIGHTCURLYBRACKETS);
                } else {
                    JavaFragmentFactory.addEndTypeBody(fragments, start);
                }
            }

            fragments.add(EndMovableJavaBlockFragment.END_MOVABLE_BLOCK);
        }
    }

    @Override
    public void visit(AnnotationElementValue reference) {
        visitAnnotationReference(reference);
    }

    @Override
    public void visit(AnnotationReference reference) {
        visitAnnotationReference(reference);
    }

    public void visitAnnotationReference(AnnotationReference reference) {
        tokens.add(TextToken.AT);

        BaseType type = reference.getType();

        type.accept(this);

        ElementValue elementValue = reference.getElementValue();

        if (elementValue == null) {
            BaseElementValuePair elementValuePairs = reference.getElementValuePairs();

            if (elementValuePairs != null) {
                tokens.add(StartBlockToken.START_PARAMETERS_BLOCK);
                elementValuePairs.accept(this);
                tokens.add(EndBlockToken.END_PARAMETERS_BLOCK);
            }
        } else {
            tokens.add(StartBlockToken.START_PARAMETERS_BLOCK);
            elementValue.accept(this);
            tokens.add(EndBlockToken.END_PARAMETERS_BLOCK);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void visit(AnnotationReferences list) {
        int size = list.size();

        if (size > 0) {
            Iterator<AnnotationReference> iterator = list.iterator();
            iterator.next().accept(this);

            for (int i = 1; i < size; i++) {
                tokens.add(TextToken.SPACE);
                iterator.next().accept(this);
            }
        }
    }

    @Override
    public void visit(ArrayVariableInitializer declaration) {
        int size = declaration.size();

        if (size > 0) {
            fragments.addTokensFragment(tokens);

            StartBlockFragment start = JavaFragmentFactory.addStartArrayInitializerBlock(fragments);

            if (size > 10) {
                JavaFragmentFactory.addNewLineBetweenArrayInitializerBlock(fragments);
            }

            tokens = new Tokens();

            declaration.get(0).accept(this);

            for (int i = 1; i < size; i++) {
                if (tokens.isEmpty()) {
                    JavaFragmentFactory.addSpacerBetweenArrayInitializerBlock(fragments);

                    if ((size > 10) && (i % 10 == 0)) {
                        JavaFragmentFactory.addNewLineBetweenArrayInitializerBlock(fragments);
                    }
                } else if ((size > 10) && (i % 10 == 0)) {
                    fragments.addTokensFragment(tokens);

                    JavaFragmentFactory.addSpacerBetweenArrayInitializerBlock(fragments);
                    JavaFragmentFactory.addNewLineBetweenArrayInitializerBlock(fragments);

                    tokens = new Tokens();
                } else {
                    tokens.add(TextToken.COMMA_SPACE);
                }

                declaration.get(i).accept(this);
            }

            fragments.addTokensFragment(tokens);

            if (inExpressionFlag) {
                JavaFragmentFactory.addEndArrayInitializerInParameter(fragments, start);
            } else {
                JavaFragmentFactory.addEndArrayInitializer(fragments, start);
            }

            tokens = new Tokens();
        } else {
            tokens.add(TextToken.LEFTRIGHTCURLYBRACKETS);
        }
    }

    @Override
    public void visit(BodyDeclaration declaration) {
        safeAccept(declaration.getMemberDeclarations());
    }

    @Override
    public void visit(ClassDeclaration declaration) {
        if ((declaration.getFlags() & FLAG_SYNTHETIC) == 0) {
            fragments.add(StartMovableJavaBlockFragment.START_MOVABLE_TYPE_BLOCK);

            buildFragmentsForClassOrInterfaceDeclaration(declaration, declaration.getFlags(), CLASS);

            tokens.add(StartBlockToken.START_DECLARATION_OR_STATEMENT_BLOCK);

            // Build fragments for super type
            BaseType superType = declaration.getSuperType();
            if ((superType != null) && !superType.equals(ObjectType.TYPE_OBJECT)) {
                fragments.addTokensFragment(tokens);

                JavaFragmentFactory.addSpacerBeforeExtends(fragments);

                tokens = new Tokens();
                tokens.add(EXTENDS);
                tokens.add(TextToken.SPACE);
                superType.accept(this);
                fragments.addTokensFragment(tokens);

                tokens = new Tokens();
            }

            // Build fragments for interfaces
            BaseType interfaces = declaration.getInterfaces();
            if (interfaces != null) {
                if (!tokens.isEmpty())
                    fragments.addTokensFragment(tokens);

                JavaFragmentFactory.addSpacerBeforeImplements(fragments);

                tokens = new Tokens();
                tokens.add(IMPLEMENTS);
                tokens.add(TextToken.SPACE);
                interfaces.accept(this);
                fragments.addTokensFragment(tokens);

                tokens = new Tokens();
            }

            tokens.add(EndBlockToken.END_DECLARATION_OR_STATEMENT_BLOCK);
            fragments.addTokensFragment(tokens);

            BodyDeclaration bodyDeclaration = declaration.getBodyDeclaration();

            if (bodyDeclaration == null) {
                tokens.add(TextToken.SPACE);
                tokens.add(TextToken.LEFTRIGHTCURLYBRACKETS);
            } else {
                int fragmentCount1 = fragments.size();
                StartBodyFragment start = JavaFragmentFactory.addStartTypeBody(fragments);
                int fragmentCount2 = fragments.size();

                storeContext();
                currentInternalTypeName = declaration.getInternalTypeName();
                currentTypeName = declaration.getName();
                bodyDeclaration.accept(this);
                restoreContext();

                if (fragmentCount2 == fragments.size()) {
                    fragments.subList(fragmentCount1, fragmentCount2).clear();
                    tokens.add(TextToken.SPACE);
                    tokens.add(TextToken.LEFTRIGHTCURLYBRACKETS);
                } else {
                    JavaFragmentFactory.addEndTypeBody(fragments, start);
                }
            }

            fragments.add(EndMovableJavaBlockFragment.END_MOVABLE_BLOCK);
        }
    }

    @Override
    public void visit(CompilationUnit compilationUnit) {
        // Init
        fragments.clear();
        contextStack.clear();
        currentInternalTypeName = null;

        // Add fragment for package
        int index = mainInternalName.lastIndexOf('/');
        if (index != -1) {
            Tokens tokens = new Tokens();

            tokens.add(PACKAGE);
            tokens.add(TextToken.SPACE);
            tokens.add(newTextToken(mainInternalName.substring(0, index).replace('/', '.')));
            tokens.add(TextToken.SEMICOLON);

            fragments.addTokensFragment(tokens);

            JavaFragmentFactory.addSpacerAfterPackage(fragments);
        }

        // Add fragment for imports
        if (!importsFragment.isEmpty()) {
            fragments.add(importsFragment);

            if (!fragments.isEmpty()) {
                JavaFragmentFactory.addSpacerAfterImports(fragments);
            }
        }

        JavaFragmentFactory.addSpacerBeforeMainDeclaration(fragments);

        // Visit all compilation unit
        super.visit(compilationUnit);
    }

    @Override
    public void visit(ConstructorDeclaration declaration) {
        if ((declaration.getFlags() & (FLAG_SYNTHETIC|FLAG_BRIDGE)) == 0) {
            BaseStatement statements = declaration.getStatements();

            if ((declaration.getFlags() & FLAG_ANONYMOUS) == 0) {
                fragments.add(StartMovableJavaBlockFragment.START_MOVABLE_METHOD_BLOCK);

                tokens = new Tokens();

                // Build fragments for annotations
                BaseAnnotationReference annotationReferences = declaration.getAnnotationReferences();

                if (annotationReferences != null) {
                    annotationReferences.accept(annotationVisitor);
                    fragments.addTokensFragment(tokens);
                    JavaFragmentFactory.addSpacerAfterMemberAnnotations(fragments);
                    tokens = new Tokens();
                }

                // Build tokens for access
                buildTokensForMethodAccessFlags(declaration.getFlags());

                // Build tokens for type parameters
                BaseTypeParameter typeParameters = declaration.getTypeParameters();

                if (typeParameters != null) {
                    tokens.add(TextToken.LEFTANGLEBRACKET);
                    typeParameters.accept(this);
                    tokens.add(TextToken.RIGHTANGLEBRACKET);
                    tokens.add(TextToken.SPACE);
                }

                // Build token for type declaration
                tokens.add(new DeclarationToken(DeclarationToken.CONSTRUCTOR, currentInternalTypeName, currentTypeName, declaration.getDescriptor()));

                storeContext();
                currentMethodParamNames.clear();

                BaseFormalParameter formalParameters = declaration.getFormalParameters();

                if (formalParameters == null) {
                    tokens.add(TextToken.LEFTRIGHTROUNDBRACKETS);
                } else {
                    tokens.add(StartBlockToken.START_PARAMETERS_BLOCK);
                    fragments.addTokensFragment(tokens);

                    formalParameters.accept(this);

                    tokens = new Tokens();
                    tokens.add(EndBlockToken.END_PARAMETERS_BLOCK);
                }

                BaseType exceptionTypes = declaration.getExceptionTypes();

                if (exceptionTypes != null) {
                    tokens.add(TextToken.SPACE);
                    tokens.add(THROWS);
                    tokens.add(TextToken.SPACE);
                    exceptionTypes.accept(this);
                }

                if (statements != null) {
                    fragments.addTokensFragment(tokens);
                    singleLineStatementVisitor.init();
                    statements.accept(singleLineStatementVisitor);

                    boolean singleLineStatement = singleLineStatementVisitor.isSingleLineStatement();
                    int fragmentCount1 = fragments.size();
                    StartBodyFragment start;

                    if (singleLineStatement) {
                        start = JavaFragmentFactory.addStartSingleStatementMethodBody(fragments);
                    } else {
                        start = JavaFragmentFactory.addStartMethodBody(fragments);
                    }

                    int fragmentCount2 = fragments.size();

                    statements.accept(this);

                    if (fragmentCount2 == fragments.size()) {
                        fragments.subList(fragmentCount1, fragmentCount2).clear();
                        tokens.add(TextToken.SPACE);
                        tokens.add(TextToken.LEFTRIGHTCURLYBRACKETS);
                    } else if (singleLineStatement) {
                        JavaFragmentFactory.addEndSingleStatementMethodBody(fragments, start);
                    } else {
                        JavaFragmentFactory.addEndMethodBody(fragments, start);
                    }
                }

                restoreContext();

                fragments.add(EndMovableJavaBlockFragment.END_MOVABLE_BLOCK);
            } else if ((statements != null) && (statements.size() > 0)) {
                int fragmentCount0 = fragments.size();
                fragments.add(StartMovableJavaBlockFragment.START_MOVABLE_METHOD_BLOCK);

                StartBlockFragment start = JavaFragmentFactory.addStartInstanceInitializerBlock(fragments);
                int fragmentCount2 = fragments.size();

                tokens = new Tokens();

                statements.accept(this);

                if (fragmentCount2 == fragments.size()) {
                    fragments.subList(fragmentCount0, fragmentCount2).clear();
                } else {
                    JavaFragmentFactory.addEndInstanceInitializerBlock(fragments, start);
                }

                fragments.add(EndMovableJavaBlockFragment.END_MOVABLE_BLOCK);
            }
        }
    }

    @Override
    public void visit(ElementValueArrayInitializerElementValue reference) {
        tokens.add(StartBlockToken.START_ARRAY_INITIALIZER_BLOCK);
        safeAccept(reference.getElementValueArrayInitializer());
        tokens.add(EndBlockToken.END_ARRAY_INITIALIZER_BLOCK);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void visit(ElementValues references) {
        Iterator<ElementValue> iterator = references.iterator();

        iterator.next().accept(this);

        while (iterator.hasNext()) {
            tokens.add(TextToken.COMMA_SPACE);
            iterator.next().accept(this);
        }
    }

    @Override
    public void visit(ExpressionElementValue reference) {
        reference.getExpression().accept(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void visit(ElementValuePairs references) {
        Iterator<ElementValuePair> iterator = references.iterator();

        iterator.next().accept(this);

        while (iterator.hasNext()) {
            tokens.add(TextToken.COMMA_SPACE);
            iterator.next().accept(this);
        }
    }

    @Override
    public void visit(ElementValuePair reference) {
        tokens.add(newTextToken(reference.getName()));
        tokens.add(TextToken.SPACE_EQUAL_SPACE);
        reference.getElementValue().accept(this);
    }

    @Override
    public void visit(EnumDeclaration declaration) {
        if ((declaration.getFlags() & FLAG_SYNTHETIC) == 0) {
            fragments.add(StartMovableJavaBlockFragment.START_MOVABLE_TYPE_BLOCK);

            buildFragmentsForTypeDeclaration(declaration, declaration.getFlags(), ENUM);

            // Build fragments for interfaces
            BaseType interfaces = declaration.getInterfaces();
            if (interfaces != null) {
                tokens.add(StartBlockToken.START_DECLARATION_OR_STATEMENT_BLOCK);

                fragments.addTokensFragment(tokens);

                JavaFragmentFactory.addSpacerBeforeImplements(fragments);

                tokens = new Tokens();
                tokens.add(IMPLEMENTS);
                tokens.add(TextToken.SPACE);
                interfaces.accept(this);
                fragments.addTokensFragment(tokens);

                tokens = new Tokens();

                tokens.add(EndBlockToken.END_DECLARATION_OR_STATEMENT_BLOCK);
            }

            fragments.addTokensFragment(tokens);

            StartBodyFragment start = JavaFragmentFactory.addStartTypeBody(fragments);

            storeContext();
            currentInternalTypeName = declaration.getInternalTypeName();
            currentTypeName = declaration.getName();

            List<EnumDeclaration.Constant> constants = declaration.getConstants();

            if ((constants != null) && (!constants.isEmpty())) {
                int preferredLineNumber = 0;

                for (EnumDeclaration.Constant constant : constants) {
                    if ((constant.getArguments() != null) || (constant.getBodyDeclaration() != null)) {
                        preferredLineNumber = 1;
                        break;
                    }
                }

                fragments.add(StartMovableJavaBlockFragment.START_MOVABLE_FIELD_BLOCK);

                constants.get(0).accept(this);

                int size = constants.size();

                for (int i = 1; i < size; i++) {
                    JavaFragmentFactory.addSpacerBetweenEnumValues(fragments, preferredLineNumber);
                    constants.get(i).accept(this);
                }

                fragments.add(TokensFragment.SEMICOLON);
                fragments.add(EndMovableJavaBlockFragment.END_MOVABLE_BLOCK);
            }

            BodyDeclaration bodyDeclaration = declaration.getBodyDeclaration();

            if (bodyDeclaration != null) {
                if ((constants != null) && (!constants.isEmpty())) {
                    Fragments f = fragments;

                    fragments = new Fragments();
                    bodyDeclaration.accept(this);

                    if (!fragments.isEmpty()) {
                        JavaFragmentFactory.addSpacerBetweenMembers(f);
                        f.addAll(fragments);
                    }

                    fragments = f;
                } else {
                    bodyDeclaration.accept(this);
                }
            }

            restoreContext();

            JavaFragmentFactory.addEndTypeBody(fragments, start);

            fragments.add(EndMovableJavaBlockFragment.END_MOVABLE_BLOCK);
        }
    }

    @Override
    public void visit(EnumDeclaration.Constant declaration) {
        tokens = new Tokens();

        // Build fragments for annotations
        BaseAnnotationReference annotationReferences = declaration.getAnnotationReferences();

        if (annotationReferences != null) {
            annotationReferences.accept(annotationVisitor);
            fragments.addTokensFragment(tokens);
            JavaFragmentFactory.addSpacerAfterMemberAnnotations(fragments);
            tokens = new Tokens();
        }

        // Build token for type declaration
        tokens.addLineNumberToken(declaration.getLineNumber());
        tokens.add(new DeclarationToken(DeclarationToken.FIELD, currentInternalTypeName, declaration.getName(), 'L' + currentInternalTypeName + ';'));

        storeContext();
        currentMethodParamNames.clear();

        BaseExpression arguments = declaration.getArguments();
        if (arguments != null) {
            tokens.add(StartBlockToken.START_PARAMETERS_BLOCK);
            arguments.accept(this);
            tokens.add(EndBlockToken.END_PARAMETERS_BLOCK);
        }

        fragments.addTokensFragment(tokens);

        BodyDeclaration bodyDeclaration = declaration.getBodyDeclaration();

        if (bodyDeclaration != null) {
            StartBodyFragment start = JavaFragmentFactory.addStartTypeBody(fragments);
            bodyDeclaration.accept(this);
            JavaFragmentFactory.addEndTypeBody(fragments, start);
        }

        restoreContext();
    }

    @Override
    public void visit(FieldDeclaration declaration) {
        if ((declaration.getFlags() & FLAG_SYNTHETIC) == 0) {
            fragments.add(StartMovableJavaBlockFragment.START_MOVABLE_FIELD_BLOCK);

            tokens = new Tokens();

            // Build fragments for annotations
            BaseAnnotationReference annotationReferences = declaration.getAnnotationReferences();

            if (annotationReferences != null) {
                annotationReferences.accept(annotationVisitor);
                fragments.addTokensFragment(tokens);
                JavaFragmentFactory.addSpacerAfterMemberAnnotations(fragments);
                tokens = new Tokens();
            }

            // Build tokens for access
            buildTokensForFieldAccessFlags(declaration.getFlags());

            BaseType type = declaration.getType();

            type.accept(this);

            tokens.add(StartBlockToken.START_DECLARATION_OR_STATEMENT_BLOCK);
            fragments.addTokensFragment(tokens);

            declaration.getFieldDeclarators().accept(this);

            fragments.add(TokensFragment.END_DECLARATION_OR_STATEMENT_BLOCK_SEMICOLON);

            fragments.add(EndMovableJavaBlockFragment.END_MOVABLE_BLOCK);
        }
    }

    @Override
    public void visit(FieldDeclarator fieldDeclarator) {
        FieldDeclaration fieldDeclaration = fieldDeclarator.getFieldDeclaration();
        VariableInitializer variableInitializer = fieldDeclarator.getVariableInitializer();
        String descriptor = fieldDeclaration.getType().getDescriptor();

        tokens = new Tokens();
        tokens.add(TextToken.SPACE);

        switch (fieldDeclarator.getDimension()) {
            case 0:
                tokens.add(new DeclarationToken(DeclarationToken.FIELD, currentInternalTypeName, fieldDeclarator.getName(), descriptor));
                break;
            case 1:
                tokens.add(new DeclarationToken(DeclarationToken.FIELD, currentInternalTypeName, fieldDeclarator.getName(), "[" + descriptor));
                tokens.add(TextToken.DIMENSION_1);
                break;
            case 2:
                tokens.add(new DeclarationToken(DeclarationToken.FIELD, currentInternalTypeName, fieldDeclarator.getName(), "[[" + descriptor));
                tokens.add(TextToken.DIMENSION_2);
                break;
            default:
                descriptor = new String(new char[fieldDeclarator.getDimension()]).replaceAll("\0", "[") + descriptor;
                tokens.add(new DeclarationToken(DeclarationToken.FIELD, currentInternalTypeName, fieldDeclarator.getName(), descriptor));
                tokens.add(newTextToken(new String(new char[fieldDeclarator.getDimension()]).replaceAll("\0", "[]")));
                break;
        }

        if (variableInitializer == null) {
            fragments.addTokensFragment(tokens);
        } else {
            tokens.add(TextToken.SPACE_EQUAL_SPACE);
            variableInitializer.accept(this);
            fragments.addTokensFragment(tokens);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void visit(FieldDeclarators declarators) {
        int size = declarators.size();

        if (size > 0) {
            Iterator<FieldDeclarator> iterator = declarators.iterator();
            iterator.next().accept(this);

            for (int i = 1; i < size; i++) {
                JavaFragmentFactory.addSpacerBetweenFieldDeclarators(fragments);
                iterator.next().accept(this);
            }
        }
    }

    @Override
    public void visit(FormalParameter declaration) {
        BaseAnnotationReference annotationReferences = declaration.getAnnotationReferences();

        if (annotationReferences != null) {
            annotationReferences.accept(this);
            tokens.add(TextToken.SPACE);
        }

        if (declaration.isVarargs()) {
            Type arrayType = declaration.getType();
            BaseType type = arrayType.createType(arrayType.getDimension() - 1);
            type.accept(this);
            tokens.add(TextToken.VARARGS);
        } else {
            if (declaration.isFinal()) {
                tokens.add(FINAL);
                tokens.add(TextToken.SPACE);
            }

            BaseType type = declaration.getType();

            type.accept(this);
            tokens.add(TextToken.SPACE);
        }

        String name = declaration.getName();

        tokens.add(newTextToken(name));
        currentMethodParamNames.add(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void visit(FormalParameters declarations) {
        int size = declarations.size();

        if (size > 0) {
            Iterator<FormalParameter> iterator = declarations.iterator();
            iterator.next().accept(this);

            for (int i = 1; i < size; i++) {
                tokens.add(TextToken.COMMA_SPACE);
                iterator.next().accept(this);
            }
        }
    }

    @Override
    public void visit(InstanceInitializerDeclaration declaration) {
        BaseStatement statements = declaration.getStatements();

        if (statements != null) {
            fragments.add(StartMovableJavaBlockFragment.START_MOVABLE_METHOD_BLOCK);

            storeContext();
            currentMethodParamNames.clear();

            StartBodyFragment start = JavaFragmentFactory.addStartMethodBody(fragments);
            statements.accept(this);
            JavaFragmentFactory.addEndMethodBody(fragments, start);

            fragments.add(EndMovableJavaBlockFragment.END_MOVABLE_BLOCK);

            restoreContext();
        }
    }

    @Override
    public void visit(InterfaceDeclaration declaration) {
        if ((declaration.getFlags() & FLAG_SYNTHETIC) == 0) {
            fragments.add(StartMovableJavaBlockFragment.START_MOVABLE_TYPE_BLOCK);

            buildFragmentsForClassOrInterfaceDeclaration(declaration, declaration.getFlags() & ~FLAG_ABSTRACT, INTERFACE);

            tokens.add(StartBlockToken.START_DECLARATION_OR_STATEMENT_BLOCK);

            // Build fragments for interfaces
            BaseType interfaces = declaration.getInterfaces();
            if (interfaces != null) {
                fragments.addTokensFragment(tokens);

                JavaFragmentFactory.addSpacerBeforeImplements(fragments);

                tokens = new Tokens();
                tokens.add(EXTENDS);
                tokens.add(TextToken.SPACE);
                interfaces.accept(this);
                fragments.addTokensFragment(tokens);

                tokens = new Tokens();
            }

            tokens.add(EndBlockToken.END_DECLARATION_OR_STATEMENT_BLOCK);
            fragments.addTokensFragment(tokens);

            BodyDeclaration bodyDeclaration = declaration.getBodyDeclaration();
            if (bodyDeclaration == null) {
                tokens.add(TextToken.SPACE);
                tokens.add(TextToken.LEFTRIGHTCURLYBRACKETS);
            } else {
                int fragmentCount1 = fragments.size();
                StartBodyFragment start = JavaFragmentFactory.addStartTypeBody(fragments);
                int fragmentCount2 = fragments.size();

                storeContext();
                currentInternalTypeName = declaration.getInternalTypeName();
                currentTypeName = declaration.getName();
                bodyDeclaration.accept(this);
                restoreContext();

                if (fragmentCount2 == fragments.size()) {
                    fragments.subList(fragmentCount1, fragmentCount2).clear();
                    tokens.add(TextToken.SPACE);
                    tokens.add(TextToken.LEFTRIGHTCURLYBRACKETS);
                } else {
                    JavaFragmentFactory.addEndTypeBody(fragments, start);
                }
            }

            fragments.add(EndMovableJavaBlockFragment.END_MOVABLE_BLOCK);
        }
    }

    @Override
    public void visit(ModuleDeclaration declaration) {
        boolean needNewLine = false;

        fragments.clear();
        fragments.add(StartMovableJavaBlockFragment.START_MOVABLE_TYPE_BLOCK);

        tokens = new Tokens();

        if ((declaration.getFlags() & FLAG_OPEN) != 0) {
            tokens.add(OPEN);
            tokens.add(TextToken.SPACE);
        }

        tokens.add(MODULE);
        tokens.add(TextToken.SPACE);
        tokens.add(new DeclarationToken(DeclarationToken.MODULE, declaration.getInternalTypeName(), declaration.getName(), null));
        fragments.addTokensFragment(tokens);

        StartBodyFragment start = JavaFragmentFactory.addStartTypeBody(fragments);

        tokens = new Tokens();

        if ((declaration.getRequires() != null) && !declaration.getRequires().isEmpty()) {
            Iterator<ModuleDeclaration.ModuleInfo> iterator = declaration.getRequires().iterator();
            visitModuleDeclaration(iterator.next());
            while (iterator.hasNext()) {
                tokens.add(NewLineToken.NEWLINE_1);
                visitModuleDeclaration(iterator.next());
            }
            needNewLine = true;
        }

        if ((declaration.getExports() != null) && !declaration.getExports().isEmpty()) {
            if (needNewLine) {
                tokens.add(NewLineToken.NEWLINE_2);
            }
            Iterator<ModuleDeclaration.PackageInfo> iterator = declaration.getExports().iterator();
            visitModuleDeclaration(iterator.next(), EXPORTS);
            while (iterator.hasNext()) {
                tokens.add(NewLineToken.NEWLINE_1);
                visitModuleDeclaration(iterator.next(), EXPORTS);
            }
            needNewLine = true;
        }

        if ((declaration.getOpens() != null) && !declaration.getOpens().isEmpty()) {
            if (needNewLine) {
                tokens.add(NewLineToken.NEWLINE_2);
            }
            Iterator<ModuleDeclaration.PackageInfo> iterator = declaration.getOpens().iterator();
            visitModuleDeclaration(iterator.next(), OPENS);
            while (iterator.hasNext()) {
                tokens.add(NewLineToken.NEWLINE_1);
                visitModuleDeclaration(iterator.next(), OPENS);
            }
            needNewLine = true;
        }

        if ((declaration.getUses() != null) && !declaration.getUses().isEmpty()) {
            if (needNewLine) {
                tokens.add(NewLineToken.NEWLINE_2);
            }
            Iterator<String> iterator = declaration.getUses().iterator();
            visitModuleDeclaration(iterator.next());
            while (iterator.hasNext()) {
                tokens.add(NewLineToken.NEWLINE_1);
                visitModuleDeclaration(iterator.next());
            }
            needNewLine = true;
        }

        if ((declaration.getProvides() != null) && !declaration.getProvides().isEmpty()) {
            if (needNewLine) {
                tokens.add(NewLineToken.NEWLINE_2);
            }
            Iterator<ModuleDeclaration.ServiceInfo> iterator = declaration.getProvides().iterator();
            visitModuleDeclaration(iterator.next());
            while (iterator.hasNext()) {
                tokens.add(NewLineToken.NEWLINE_1);
                visitModuleDeclaration(iterator.next());
            }
        }

        fragments.addTokensFragment(tokens);

        JavaFragmentFactory.addEndTypeBody(fragments, start);

        fragments.add(EndMovableJavaBlockFragment.END_MOVABLE_BLOCK);
    }

    protected void visitModuleDeclaration(ModuleDeclaration.ModuleInfo moduleInfo) {
        tokens.add(REQUIRES);

        if ((moduleInfo.getFlags() & FLAG_STATIC) != 0) {
            tokens.add(TextToken.SPACE);
            tokens.add(STATIC);
        }
        if ((moduleInfo.getFlags() & FLAG_TRANSITIVE) != 0) {
            tokens.add(TextToken.SPACE);
            tokens.add(TRANSITIVE);
        }

        tokens.add(TextToken.SPACE);
        tokens.add(new ReferenceToken(ReferenceToken.MODULE, "module-info", moduleInfo.getName(), null, null));
        tokens.add(TextToken.SEMICOLON);
    }

    protected void visitModuleDeclaration(ModuleDeclaration.PackageInfo packageInfo, KeywordToken keywordToken) {
        tokens.add(keywordToken);
        tokens.add(TextToken.SPACE);
        tokens.add(new ReferenceToken(ReferenceToken.PACKAGE, packageInfo.getInternalName(), packageInfo.getInternalName().replace('/', '.'), null, null));

        if ((packageInfo.getModuleInfoNames() != null) && !packageInfo.getModuleInfoNames().isEmpty()) {
            tokens.add(TextToken.SPACE);
            tokens.add(TO);

            if (packageInfo.getModuleInfoNames().size() == 1) {
                tokens.add(TextToken.SPACE);
                String moduleInfoName = packageInfo.getModuleInfoNames().get(0);
                tokens.add(new ReferenceToken(ReferenceToken.MODULE, "module-info", moduleInfoName, null, null));
            } else {
                tokens.add(StartBlockToken.START_DECLARATION_OR_STATEMENT_BLOCK);
                tokens.add(NewLineToken.NEWLINE_1);

                Iterator<String> iterator = packageInfo.getModuleInfoNames().iterator();

                String moduleInfoName = iterator.next();
                tokens.add(new ReferenceToken(ReferenceToken.MODULE, "module-info", moduleInfoName, null, null));

                while (iterator.hasNext()) {
                    tokens.add(TextToken.COMMA);
                    tokens.add(NewLineToken.NEWLINE_1);
                    moduleInfoName = iterator.next();
                    tokens.add(new ReferenceToken(ReferenceToken.MODULE, "module-info", moduleInfoName, null, null));
                }

                tokens.add(EndBlockToken.END_DECLARATION_OR_STATEMENT_BLOCK);
            }
        }

        tokens.add(TextToken.SEMICOLON);
    }

    protected void visitModuleDeclaration(String internalTypeName) {
        tokens.add(USES);
        tokens.add(TextToken.SPACE);
        tokens.add(new ReferenceToken(ReferenceToken.TYPE, internalTypeName, internalTypeName.replace('/', '.'), null, null));
        tokens.add(TextToken.SEMICOLON);
    }

    protected void visitModuleDeclaration(ModuleDeclaration.ServiceInfo serviceInfo) {
        tokens.add(PROVIDES);
        tokens.add(TextToken.SPACE);
        String internalTypeName = serviceInfo.getInterfaceTypeName();
        tokens.add(new ReferenceToken(ReferenceToken.TYPE, internalTypeName, internalTypeName.replace('/', '.'), null, null));
        tokens.add(TextToken.SPACE);
        tokens.add(WITH);

        if (serviceInfo.getImplementationTypeNames().size() == 1) {
            tokens.add(TextToken.SPACE);
            internalTypeName = serviceInfo.getImplementationTypeNames().get(0);
            tokens.add(new ReferenceToken(ReferenceToken.TYPE, internalTypeName, internalTypeName.replace('/', '.'), null, null));
        } else {
            tokens.add(StartBlockToken.START_DECLARATION_OR_STATEMENT_BLOCK);
            tokens.add(NewLineToken.NEWLINE_1);

            Iterator<String> iterator = serviceInfo.getImplementationTypeNames().iterator();

            internalTypeName = iterator.next();
            tokens.add(new ReferenceToken(ReferenceToken.TYPE, internalTypeName, internalTypeName.replace('/', '.'), null, null));

            while (iterator.hasNext()) {
                tokens.add(TextToken.COMMA);
                tokens.add(NewLineToken.NEWLINE_1);
                internalTypeName = iterator.next();
                tokens.add(new ReferenceToken(ReferenceToken.TYPE, internalTypeName, internalTypeName.replace('/', '.'), null, null));
            }

            tokens.add(EndBlockToken.END_DECLARATION_OR_STATEMENT_BLOCK);
        }

        tokens.add(TextToken.SEMICOLON);
    }

    @Override
    public void visit(LocalVariableDeclaration declaration) {
        if (declaration.isFinal()) {
            tokens.add(FINAL);
            tokens.add(TextToken.SPACE);
        }

        BaseType type = declaration.getType();

        type.accept(this);
        tokens.add(TextToken.SPACE);
        declaration.getLocalVariableDeclarators().accept(this);
    }

    @Override
    public void visit(LocalVariableDeclarator declarator) {
        if (declarator.getVariableInitializer() == null) {
            tokens.addLineNumberToken(declarator.getLineNumber());
            tokens.add(newTextToken(declarator.getName()));

            visitDimension(declarator.getDimension());
        } else {
            tokens.add(newTextToken(declarator.getName()));

            visitDimension(declarator.getDimension());

            tokens.add(TextToken.SPACE_EQUAL_SPACE);
            declarator.getVariableInitializer().accept(this);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void visit(LocalVariableDeclarators declarators) {
        int size = declarators.size();

        if (size > 0) {
            Iterator<LocalVariableDeclarator> iterator = declarators.iterator();
            iterator.next().accept(this);

            for (int i = 1; i < size; i++) {
                tokens.add(TextToken.COMMA_SPACE);
                iterator.next().accept(this);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void visit(MemberDeclarations list) {
        int size = list.size();

        if (size > 0) {
            int fragmentCount2 = fragments.size();
            Iterator<MemberDeclaration> iterator = list.iterator();

            iterator.next().accept(this);

            if (size > 1) {
                int fragmentCount1 = -1;

                for (int i = 1; i < size; i++) {
                    if (fragmentCount2 < fragments.size()) {
                        fragmentCount1 = fragments.size();
                        JavaFragmentFactory.addSpacerBetweenMembers(fragments);
                        fragmentCount2 = fragments.size();
                    }
                    iterator.next().accept(this);
                }

                if ((fragmentCount1 != -1) && (fragmentCount2 == fragments.size())) {
                    fragments.subList(fragmentCount1, fragments.size()).clear();
                }
            }
        }
    }

    @Override
    public void visit(MethodDeclaration declaration) {
        if ((declaration.getFlags() & (FLAG_SYNTHETIC | FLAG_BRIDGE)) == 0) {
            fragments.add(StartMovableJavaBlockFragment.START_MOVABLE_METHOD_BLOCK);

            tokens = new Tokens();

            // Build fragments for annotations
            BaseAnnotationReference annotationReferences = declaration.getAnnotationReferences();

            if (annotationReferences != null) {
                annotationReferences.accept(annotationVisitor);
                fragments.addTokensFragment(tokens);
                JavaFragmentFactory.addSpacerAfterMemberAnnotations(fragments);
                tokens = new Tokens();
            }

            // Build tokens for access
            buildTokensForMethodAccessFlags(declaration.getFlags());

            // Build tokens for type parameters
            BaseTypeParameter typeParameters = declaration.getTypeParameters();

            if (typeParameters != null) {
                tokens.add(TextToken.LEFTANGLEBRACKET);
                typeParameters.accept(this);
                tokens.add(TextToken.RIGHTANGLEBRACKET);
                tokens.add(TextToken.SPACE);
            }

            BaseType returnedType = declaration.getReturnedType();

            returnedType.accept(this);
            tokens.add(TextToken.SPACE);

            // Build token for type declaration
            tokens.add(new DeclarationToken(DeclarationToken.METHOD, currentInternalTypeName, declaration.getName(), declaration.getDescriptor()));

            storeContext();
            currentMethodParamNames.clear();

            BaseFormalParameter formalParameters = declaration.getFormalParameters();

            if (formalParameters == null) {
                tokens.add(TextToken.LEFTRIGHTROUNDBRACKETS);
            } else {
                tokens.add(StartBlockToken.START_PARAMETERS_BLOCK);
                fragments.addTokensFragment(tokens);

                formalParameters.accept(this);

                tokens = new Tokens();
                tokens.add(EndBlockToken.END_PARAMETERS_BLOCK);
            }

            BaseType exceptions = declaration.getExceptionTypes();

            if (exceptions != null) {
                tokens.add(TextToken.SPACE);
                tokens.add(THROWS);
                tokens.add(TextToken.SPACE);
                exceptions.accept(this);
            }

            BaseStatement statements = declaration.getStatements();

            if (statements == null) {
                ElementValue elementValue = declaration.getDefaultAnnotationValue();

                if (elementValue == null) {
                    tokens.add(TextToken.SEMICOLON);
                    fragments.addTokensFragment(tokens);
                } else {
                    tokens.add(TextToken.SPACE);
                    tokens.add(DEFAULT);
                    tokens.add(TextToken.SPACE);
                    fragments.addTokensFragment(tokens);

                    elementValue.accept(this);

                    tokens = new Tokens();
                    tokens.add(TextToken.SEMICOLON);
                    fragments.addTokensFragment(tokens);
                }
            } else {
                fragments.addTokensFragment(tokens);
                singleLineStatementVisitor.init();
                statements.accept(singleLineStatementVisitor);

                boolean singleLineStatement = singleLineStatementVisitor.isSingleLineStatement();
                int fragmentCount1 = fragments.size();
                StartBodyFragment start;

                if (singleLineStatement) {
                    start = JavaFragmentFactory.addStartSingleStatementMethodBody(fragments);
                } else {
                    start = JavaFragmentFactory.addStartMethodBody(fragments);
                }

                int fragmentCount2 = fragments.size();

                statements.accept(this);

                if (fragmentCount2 == fragments.size()) {
                    fragments.subList(fragmentCount1, fragmentCount2).clear();
                    tokens.add(TextToken.SPACE);
                    tokens.add(TextToken.LEFTRIGHTCURLYBRACKETS);
                } else if (singleLineStatement) {
                    JavaFragmentFactory.addEndSingleStatementMethodBody(fragments, start);
                } else {
                    JavaFragmentFactory.addEndMethodBody(fragments, start);
                }
            }

            restoreContext();

            fragments.add(EndMovableJavaBlockFragment.END_MOVABLE_BLOCK);
        }
    }

    @Override
    public void visit(ObjectReference reference) {
        visit((ObjectType) reference);
    }

    @Override
    public void visit(InnerObjectReference reference) {
        visit((InnerObjectType) reference);
    }

    @Override
    public void visit(StaticInitializerDeclaration declaration) {
        BaseStatement statements = declaration.getStatements();

        if (statements != null) {
            fragments.add(StartMovableJavaBlockFragment.START_MOVABLE_METHOD_BLOCK);

            storeContext();
            currentMethodParamNames.clear();

            tokens = new Tokens();
            tokens.add(STATIC);
            fragments.addTokensFragment(tokens);

            StartBodyFragment start = JavaFragmentFactory.addStartMethodBody(fragments);
            statements.accept(this);
            JavaFragmentFactory.addEndMethodBody(fragments, start);

            fragments.add(EndMovableJavaBlockFragment.END_MOVABLE_BLOCK);

            restoreContext();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void visit(TypeDeclarations declaration) {
        if (declaration.size() > 0) {
            Iterator<MemberDeclaration> iterator = declaration.iterator();

            iterator.next().accept(this);

            while (iterator.hasNext()) {
                JavaFragmentFactory.addSpacerBetweenMembers(fragments);
                iterator.next().accept(this);
            }
        }
    }

    protected void buildFragmentsForTypeDeclaration(TypeDeclaration declaration, int flags, KeywordToken keyword) {
        tokens = new Tokens();

        // Build fragments for annotations
        BaseAnnotationReference annotationReferences = declaration.getAnnotationReferences();
        if (annotationReferences != null) {
            annotationReferences.accept(annotationVisitor);
            fragments.addTokensFragment(tokens);
            JavaFragmentFactory.addSpacerAfterMemberAnnotations(fragments);
            tokens = new Tokens();
        }

        // Build tokens for access
        buildTokensForTypeAccessFlags(flags);
        tokens.add(keyword);
        tokens.add(TextToken.SPACE);

        // Build token for type declaration
        tokens.add(new DeclarationToken(DeclarationToken.TYPE, declaration.getInternalTypeName(), declaration.getName(), null));
    }

    protected void buildFragmentsForClassOrInterfaceDeclaration(InterfaceDeclaration declaration, int flags, KeywordToken keyword) {
        buildFragmentsForTypeDeclaration(declaration, flags, keyword);

        // Build tokens for type parameterTypes
        BaseTypeParameter typeParameters = declaration.getTypeParameters();

        if (typeParameters != null) {
            tokens.add(TextToken.LEFTANGLEBRACKET);
            typeParameters.accept(this);
            tokens.add(TextToken.RIGHTANGLEBRACKET);
        }
    }

    protected void buildTokensForTypeAccessFlags(int flags) {
        if ((flags & FLAG_PUBLIC) != 0) {
            tokens.add(PUBLIC);
            tokens.add(TextToken.SPACE);
        }
        if ((flags & FLAG_PROTECTED) != 0) {
            tokens.add(PROTECTED);
            tokens.add(TextToken.SPACE);
        }
        if ((flags & FLAG_PRIVATE) != 0) {
            tokens.add(PRIVATE);
            tokens.add(TextToken.SPACE);
        }
        if ((flags & FLAG_STATIC) != 0) {
            tokens.add(STATIC);
            tokens.add(TextToken.SPACE);
        }
        if ((flags & FLAG_FINAL) != 0) {
            tokens.add(FINAL);
            tokens.add(TextToken.SPACE);
        }
        if ((flags & FLAG_ABSTRACT) != 0) {
            tokens.add(ABSTRACT);
            tokens.add(TextToken.SPACE);
        }
        if ((flags & FLAG_SYNTHETIC) != 0) {
            tokens.add(StartMarkerToken.COMMENT);
            tokens.add(COMMENT_SYNTHETIC);
            tokens.add(EndMarkerToken.COMMENT);
            tokens.add(TextToken.SPACE);
        }
    }

    protected void buildTokensForFieldAccessFlags(int flags) {
        if ((flags & FLAG_PUBLIC) != 0) {
            tokens.add(PUBLIC);
            tokens.add(TextToken.SPACE);
        }
        if ((flags & FLAG_PROTECTED) != 0) {
            tokens.add(PROTECTED);
            tokens.add(TextToken.SPACE);
        }
        if ((flags & FLAG_PRIVATE) != 0) {
            tokens.add(PRIVATE);
            tokens.add(TextToken.SPACE);
        }
        if ((flags & FLAG_STATIC) != 0) {
            tokens.add(STATIC);
            tokens.add(TextToken.SPACE);
        }
        if ((flags & FLAG_FINAL) != 0) {
            tokens.add(FINAL);
            tokens.add(TextToken.SPACE);
        }
        if ((flags & FLAG_VOLATILE) != 0) {
            tokens.add(VOLATILE);
            tokens.add(TextToken.SPACE);
        }
        if ((flags & FLAG_TRANSIENT) != 0) {
            tokens.add(TRANSIENT);
            tokens.add(TextToken.SPACE);
        }
        if ((flags & FLAG_SYNTHETIC) != 0) {
            tokens.add(StartMarkerToken.COMMENT);
            tokens.add(COMMENT_SYNTHETIC);
            tokens.add(EndMarkerToken.COMMENT);
            tokens.add(TextToken.SPACE);
        }
    }

    protected void buildTokensForMethodAccessFlags(int flags) {
        if ((flags & FLAG_PUBLIC) != 0) {
            tokens.add(PUBLIC);
            tokens.add(TextToken.SPACE);
        }
        if ((flags & FLAG_PROTECTED) != 0) {
            tokens.add(PROTECTED);
            tokens.add(TextToken.SPACE);
        }
        if ((flags & FLAG_PRIVATE) != 0) {
            tokens.add(PRIVATE);
            tokens.add(TextToken.SPACE);
        }
        if ((flags & FLAG_STATIC) != 0) {
            tokens.add(STATIC);
            tokens.add(TextToken.SPACE);
        }
        if ((flags & FLAG_FINAL) != 0) {
            tokens.add(FINAL);
            tokens.add(TextToken.SPACE);
        }
        if ((flags & FLAG_SYNCHRONIZED) != 0) {
            tokens.add(SYNCHRONIZED);
            tokens.add(TextToken.SPACE);
        }
        if ((flags & FLAG_BRIDGE) != 0) {
            tokens.add(StartMarkerToken.COMMENT);
            tokens.add(COMMENT_BRIDGE);
            tokens.add(EndMarkerToken.COMMENT);
            tokens.add(TextToken.SPACE);
        }
        if ((flags & FLAG_NATIVE) != 0) {
            tokens.add(NATIVE);
            tokens.add(TextToken.SPACE);
        }
        if ((flags & FLAG_ABSTRACT) != 0) {
            tokens.add(ABSTRACT);
            tokens.add(TextToken.SPACE);
        }
        if ((flags & FLAG_STRICT) != 0) {
            tokens.add(STRICT);
            tokens.add(TextToken.SPACE);
        }
        if ((flags & FLAG_SYNTHETIC) != 0) {
            tokens.add(StartMarkerToken.COMMENT);
            tokens.add(COMMENT_SYNTHETIC);
            tokens.add(EndMarkerToken.COMMENT);
            tokens.add(TextToken.SPACE);
        }
        if ((flags & FLAG_DEFAULT) != 0) {
            tokens.add(DEFAULT);
            tokens.add(TextToken.SPACE);
        }
    }

    protected class AnnotationVisitor extends AbstractJavaSyntaxVisitor {
        @Override
        @SuppressWarnings("unchecked")
        public void visit(AnnotationReferences list) {
            if (list.size() > 0) {
                Iterator<AnnotationReference> iterator = list.iterator();

                iterator.next().accept(this);

                while (iterator.hasNext()) {
                    fragments.addTokensFragment(tokens);

                    JavaFragmentFactory.addSpacerBetweenMemberAnnotations(fragments);

                    tokens = new Tokens();

                    iterator.next().accept(this);
                }
            }
        }

        @Override
        public void visit(AnnotationElementValue reference) {
            visitAnnotationReference(reference);
        }

        @Override
        public void visit(AnnotationReference reference) {
            visitAnnotationReference(reference);
        }
    }
}