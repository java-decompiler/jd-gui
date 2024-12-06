/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.fragmenter.javasyntaxtojavafragment.visitor;

import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.api.printer.Printer;
import org.jd.core.v1.model.fragment.Fragment;
import org.jd.core.v1.model.javafragment.*;
import org.jd.core.v1.model.javasyntax.declaration.BaseFormalParameter;
import org.jd.core.v1.model.javasyntax.declaration.BodyDeclaration;
import org.jd.core.v1.model.javasyntax.declaration.FormalParameter;
import org.jd.core.v1.model.javasyntax.expression.*;
import org.jd.core.v1.model.javasyntax.statement.BaseStatement;
import org.jd.core.v1.model.javasyntax.type.*;
import org.jd.core.v1.model.token.*;
import org.jd.core.v1.service.fragmenter.javasyntaxtojavafragment.util.CharacterUtil;
import org.jd.core.v1.service.fragmenter.javasyntaxtojavafragment.util.JavaFragmentFactory;
import org.jd.core.v1.service.fragmenter.javasyntaxtojavafragment.util.StringUtil;
import org.jd.core.v1.util.DefaultList;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.jd.core.v1.model.javasyntax.type.PrimitiveType.FLAG_BOOLEAN;
import static org.jd.core.v1.model.javasyntax.type.PrimitiveType.FLAG_CHAR;

public class ExpressionVisitor extends TypeVisitor {
    public static final KeywordToken CLASS = new KeywordToken("class");
    public static final KeywordToken FALSE = new KeywordToken("false");
    public static final KeywordToken INSTANCEOF = new KeywordToken("instanceof");
    public static final KeywordToken LENGTH = new KeywordToken("length");
    public static final KeywordToken NEW = new KeywordToken("new");
    public static final KeywordToken NULL = new KeywordToken("null");
    public static final KeywordToken THIS = new KeywordToken("this");
    public static final KeywordToken TRUE = new KeywordToken("true");

    protected static final int UNKNOWN_LINE_NUMBER = Printer.UNKNOWN_LINE_NUMBER;

    protected LinkedList<Context> contextStack = new LinkedList<>();
    protected Fragments fragments = new Fragments();
    protected boolean diamondOperatorSupported;
    protected boolean inExpressionFlag = false;
    protected HashSet<String> currentMethodParamNames = new HashSet<>();
    protected String currentTypeName;
    protected HexaExpressionVisitor hexaExpressionVisitor = new HexaExpressionVisitor();

    public ExpressionVisitor(Loader loader, String mainInternalTypeName, int majorVersion, ImportsFragment importsFragment) {
        super(loader, mainInternalTypeName, majorVersion, importsFragment);
        this.diamondOperatorSupported = (majorVersion >= 51); // (majorVersion >= Java 7)
    }

    public DefaultList<Fragment> getFragments() {
        return fragments;
    }

    @Override
    public void visit(ArrayExpression expression) {
        visit(expression, expression.getExpression());
        tokens.add(StartBlockToken.START_ARRAY_BLOCK);
        expression.getIndex().accept(this);
        tokens.add(EndBlockToken.END_ARRAY_BLOCK);
    }

    @Override
    public void visit(BinaryOperatorExpression expression) {
        switch (expression.getOperator()) {
            case "&":
            case "|":
            case "^":
            case "&=":
            case "|=":
            case "^=":
                visitHexa(expression, expression.getLeftExpression());
                tokens.add(TextToken.SPACE);
                tokens.add(newTextToken(expression.getOperator()));
                tokens.add(TextToken.SPACE);
                visitHexa(expression, expression.getRightExpression());
                break;
            default:
                visit(expression, expression.getLeftExpression());
                tokens.add(TextToken.SPACE);
                tokens.add(newTextToken(expression.getOperator()));
                tokens.add(TextToken.SPACE);
                visit(expression, expression.getRightExpression());
                break;
        }
    }

    @Override
    public void visit(BooleanExpression expression) {
        if (expression.isTrue()) {
            tokens.add(TRUE);
        } else {
            tokens.add(FALSE);
        }
    }

    @Override
    public void visit(CastExpression expression) {
        if (expression.isExplicit()) {
            tokens.addLineNumberToken(expression.getLineNumber());
            tokens.add(TextToken.LEFTROUNDBRACKET);

            BaseType type = expression.getType();

            type.accept(this);
            tokens.add(TextToken.RIGHTROUNDBRACKET);
        }

        visit(expression, expression.getExpression());
    }

    @Override public void visit(CommentExpression expression) {
        tokens.add(StartMarkerToken.COMMENT);
        tokens.add(newTextToken(expression.getText()));
        tokens.add(EndMarkerToken.COMMENT);
    }

    @Override
    public void visit(ConstructorInvocationExpression expression) {
        tokens.addLineNumberToken(expression);
        tokens.add(THIS);
        tokens.add(StartBlockToken.START_PARAMETERS_BLOCK);

        BaseExpression parameters = expression.getParameters();

        if (parameters != null) {
            parameters.accept(this);
        }

        tokens.add(EndBlockToken.END_PARAMETERS_BLOCK);
    }

    @Override
    public void visit(ConstructorReferenceExpression expression) {
        ObjectType ot = expression.getObjectType();

        tokens.addLineNumberToken(expression);
        tokens.add(newTypeReferenceToken(ot, currentInternalTypeName));
        tokens.add(TextToken.COLON_COLON);
        //tokens.add(new ReferenceToken(ReferenceToken.CONSTRUCTOR, ot.getInternalName(), "new", expression.getDescriptor(), currentInternalTypeName));
        tokens.add(NEW);
    }

    @Override
    public void visit(DoubleConstantExpression expression) {
        tokens.addLineNumberToken(expression);
        tokens.add(new NumericConstantToken(String.valueOf(expression.getDoubleValue()) + 'D'));
    }

    @Override
    public void visit(EnumConstantReferenceExpression expression) {
        tokens.addLineNumberToken(expression);

        ObjectType type = expression.getObjectType();

        tokens.add(new ReferenceToken(ReferenceToken.FIELD, type.getInternalName(), expression.getName(), type.getDescriptor(), currentInternalTypeName));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void visit(Expressions list) {
        if (list != null) {
            int size = list.size();

            if (size > 0) {
                boolean ief = inExpressionFlag;
                Iterator<Expression> iterator = list.iterator();

                while (size-- > 1) {
                    inExpressionFlag = true;
                    iterator.next().accept(this);

                    if (!tokens.isEmpty()) {
                        tokens.add(TextToken.COMMA_SPACE);
                    }
                }

                inExpressionFlag = false;
                iterator.next().accept(this);
                inExpressionFlag = ief;
            }
        }
    }

    @Override
    public void visit(FieldReferenceExpression expression) {
        if (expression.getExpression() == null) {
            tokens.addLineNumberToken(expression);
            tokens.add(new TextToken(expression.getName()));
        } else {
            tokens.addLineNumberToken(expression.getExpression());

            int delta = tokens.size();

            visit(expression, expression.getExpression());
            delta -= tokens.size();
            tokens.addLineNumberToken(expression);

            if (delta != 0) {
                tokens.add(TextToken.DOT);
            }

            tokens.add(new ReferenceToken(ReferenceToken.FIELD, expression.getInternalTypeName(), expression.getName(), expression.getDescriptor(), currentInternalTypeName));
        }
    }

    @Override
    public void visit(FloatConstantExpression expression) {
        tokens.addLineNumberToken(expression);
        tokens.add(new NumericConstantToken(String.valueOf(expression.getFloatValue()) + 'F'));
    }

    @Override
    public void visit(IntegerConstantExpression expression) {
        tokens.addLineNumberToken(expression);

        PrimitiveType pt = (PrimitiveType)expression.getType();

        switch (pt.getJavaPrimitiveFlags()) {
            case FLAG_CHAR:
                tokens.add(new CharacterConstantToken(CharacterUtil.escapeChar((char)expression.getIntegerValue()), currentInternalTypeName));
                break;
            case FLAG_BOOLEAN:
                tokens.add(new BooleanConstantToken(expression.getIntegerValue() != 0));
                break;
            default:
                tokens.add(new NumericConstantToken(String.valueOf(expression.getIntegerValue())));
                break;
        }
    }

    @Override
    public void visit(InstanceOfExpression expression) {
        expression.getExpression().accept(this);
        tokens.add(TextToken.SPACE);
        tokens.add(INSTANCEOF);
        tokens.add(TextToken.SPACE);

        BaseType type = expression.getInstanceOfType();

        type.accept(this);
    }

    @Override
    public void visit(LambdaFormalParametersExpression expression) {
        BaseFormalParameter parameters = expression.getFormalParameters();

        if (parameters == null) {
            tokens.add(TextToken.LEFTRIGHTROUNDBRACKETS);
        } else {
            int size = parameters.size();

            switch (size) {
                case 0:
                    tokens.add(TextToken.LEFTRIGHTROUNDBRACKETS);
                    break;
                case 1:
                    parameters.getFirst().accept(this);
                    break;
                default:
                    tokens.add(TextToken.LEFTROUNDBRACKET);
                    Iterator<FormalParameter> iterator = parameters.iterator();
                    iterator.next().accept(this);

                    while (iterator.hasNext()) {
                        tokens.add(TextToken.COMMA_SPACE);
                        iterator.next().accept(this);
                    }

                    tokens.add(TextToken.RIGHTROUNDBRACKET);
                    break;
            }
        }

        visitLambdaBody(expression.getStatements());
    }

    @Override
    public void visit(LambdaIdentifiersExpression expression) {
        List<String> parameters = expression.getParameterNames();

        if (parameters == null) {
            tokens.add(TextToken.LEFTRIGHTROUNDBRACKETS);
        } else {
            int size = parameters.size();

            switch (size) {
                case 0:
                    tokens.add(TextToken.LEFTRIGHTROUNDBRACKETS);
                    break;
                case 1:
                    tokens.add(newTextToken(parameters.get(0)));
                    break;
                default:
                    tokens.add(TextToken.LEFTROUNDBRACKET);
                    tokens.add(newTextToken(parameters.get(0)));

                    for (int i = 1; i < size; i++) {
                        tokens.add(TextToken.COMMA_SPACE);
                        tokens.add(newTextToken(parameters.get(i)));
                    }
                    tokens.add(TextToken.RIGHTROUNDBRACKET);
                    break;
            }
        }

        visitLambdaBody(expression.getStatements());
    }

    protected void visitLambdaBody(BaseStatement statementList) {
        if (statementList != null) {
            tokens.add(TextToken.SPACE_ARROW_SPACE);

            if (statementList.isLambdaExpressionStatement()) {
                statementList.accept(this);
            } else {
                fragments.addTokensFragment(tokens);

                StartBlockFragment start = JavaFragmentFactory.addStartStatementsInLambdaBlock(fragments);

                tokens = new Tokens();
                statementList.accept(this);

                if (inExpressionFlag) {
                    JavaFragmentFactory.addEndStatementsInLambdaBlockInParameter(fragments, start);
                } else {
                    JavaFragmentFactory.addEndStatementsInLambdaBlock(fragments, start);
                }

                tokens = new Tokens();
            }
        }
    }

    @Override
    public void visit(LengthExpression expression) {
        visit(expression, expression.getExpression());
        tokens.add(TextToken.DOT);
        tokens.add(LENGTH);
    }

    @Override
    public void visit(LocalVariableReferenceExpression expression) {
        tokens.addLineNumberToken(expression);
        tokens.add(newTextToken(expression.getName()));
    }

    @Override
    public void visit(LongConstantExpression expression) {
        tokens.addLineNumberToken(expression);
        tokens.add(new NumericConstantToken(String.valueOf(expression.getLongValue()) + 'L'));
    }

    @Override
    public void visit(MethodInvocationExpression expression) {
        Expression exp = expression.getExpression();
        BaseTypeArgument nonWildcardTypeArguments = expression.getNonWildcardTypeArguments();
        BaseExpression parameters = expression.getParameters();
        boolean dot = false;

        if (exp.isThisExpression()) {
            // Nothing to do : do not print 'this.method(...)'
        } else if (exp.isObjectTypeReferenceExpression()) {
            ObjectType ot = exp.getObjectType();

            if (! ot.getInternalName().equals(currentInternalTypeName)) {
                visit(expression, exp);
                tokens.addLineNumberToken(expression);
                tokens.add(TextToken.DOT);
                dot = true;
            }
        } else {
            if (exp.isFieldReferenceExpression() || exp.isLocalVariableReferenceExpression()) {
                tokens.addLineNumberToken(expression);
                visit(expression, exp);
            } else {
                visit(expression, exp);
                tokens.addLineNumberToken(expression);
            }

            tokens.add(TextToken.DOT);
            dot = true;
        }

        tokens.addLineNumberToken(expression);

        if ((nonWildcardTypeArguments != null) && dot) {
            tokens.add(TextToken.LEFTANGLEBRACKET);
            nonWildcardTypeArguments.accept(this);
            tokens.add(TextToken.RIGHTANGLEBRACKET);
        }

        tokens.add(new ReferenceToken(ReferenceToken.METHOD, expression.getInternalTypeName(), expression.getName(), expression.getDescriptor(), currentInternalTypeName));
        tokens.add(StartBlockToken.START_PARAMETERS_BLOCK);

        if (parameters != null) {
            boolean ief = inExpressionFlag;
            inExpressionFlag = false;
            parameters.accept(this);
            inExpressionFlag = ief;
        }

        tokens.add(EndBlockToken.END_PARAMETERS_BLOCK);
    }

    @Override
    public void visit(MethodReferenceExpression expression) {
        expression.getExpression().accept(this);
        tokens.addLineNumberToken(expression);
        tokens.add(TextToken.COLON_COLON);
        tokens.add(new ReferenceToken(ReferenceToken.METHOD, expression.getInternalTypeName(), expression.getName(), expression.getDescriptor(), currentInternalTypeName));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void visit(NewArray expression) {
        tokens.addLineNumberToken(expression);
        tokens.add(NEW);
        tokens.add(TextToken.SPACE);

        BaseType type = expression.getType();

        type.accept(this);

        BaseExpression dimensionExpressionList = expression.getDimensionExpressionList();
        int dimension = expression.getType().getDimension();

        if (dimension > 0) {
            tokens.remove(tokens.size()-1);
        }

        if (dimensionExpressionList != null) {
            if (dimensionExpressionList.isList()) {
                Iterator<Expression> iterator = dimensionExpressionList.iterator();

                while (iterator.hasNext()) {
                    tokens.add(StartBlockToken.START_ARRAY_BLOCK);
                    iterator.next().accept(this);
                    tokens.add(EndBlockToken.END_ARRAY_BLOCK);
                    dimension--;
                }
            } else {
                tokens.add(StartBlockToken.START_ARRAY_BLOCK);
                dimensionExpressionList.accept(this);
                tokens.add(EndBlockToken.END_ARRAY_BLOCK);
                dimension--;
            }
        }

        visitDimension(dimension);
    }

    @Override
    public void visit(NewInitializedArray expression) {
        tokens.addLineNumberToken(expression);
        tokens.add(NEW);
        tokens.add(TextToken.SPACE);

        BaseType type = expression.getType();

        type.accept(this);
        tokens.add(TextToken.SPACE);
        expression.getArrayInitializer().accept(this);
    }

    @Override
    public void visit(NewExpression expression) {
        BodyDeclaration bodyDeclaration = expression.getBodyDeclaration();

        tokens.addLineNumberToken(expression);
        tokens.add(NEW);
        tokens.add(TextToken.SPACE);

        ObjectType objectType = expression.getObjectType();

        if ((objectType.getTypeArguments() != null) && (bodyDeclaration == null) && diamondOperatorSupported) {
            objectType = objectType.createType(DiamondTypeArgument.DIAMOND);
        }

        BaseType type = objectType;

        type.accept(this);
        tokens.add(StartBlockToken.START_PARAMETERS_BLOCK);

        BaseExpression parameters = expression.getParameters();
        if (parameters != null) {
            parameters.accept(this);
        }

        tokens.add(EndBlockToken.END_PARAMETERS_BLOCK);

        if (bodyDeclaration != null) {
            fragments.addTokensFragment(tokens);

            StartBodyFragment start = JavaFragmentFactory.addStartTypeBody(fragments);
            ObjectType ot = expression.getObjectType();

            storeContext();
            currentInternalTypeName = bodyDeclaration.getInternalTypeName();
            currentTypeName = ot.getName();
            bodyDeclaration.accept(this);

            if (! tokens.isEmpty()) {
                tokens = new Tokens();
            }

            restoreContext();

            if (inExpressionFlag) {
                JavaFragmentFactory.addEndSubTypeBodyInParameter(fragments, start);
            } else {
                JavaFragmentFactory.addEndSubTypeBody(fragments, start);
            }

            tokens = new Tokens();
        }
    }

    @Override
    public void visit(NullExpression expression) {
        tokens.addLineNumberToken(expression);
        tokens.add(NULL);
    }

    @Override
    public void visit(ObjectTypeReferenceExpression expression) {
        if (expression.isExplicit()) {
            tokens.addLineNumberToken(expression);

            BaseType type = expression.getType();

            type.accept(this);
        }
    }

    @Override
    public void visit(ParenthesesExpression expression) {
        tokens.add(StartBlockToken.START_PARAMETERS_BLOCK);
        expression.getExpression().accept(this);
        tokens.add(EndBlockToken.END_PARAMETERS_BLOCK);
    }

    @Override
    public void visit(PostOperatorExpression expression) {
        visit(expression, expression.getExpression());
        tokens.add(newTextToken(expression.getOperator()));
    }

    @Override
    public void visit(PreOperatorExpression expression) {
        tokens.addLineNumberToken(expression.getExpression());
        tokens.add(newTextToken(expression.getOperator()));
        visit(expression, expression.getExpression());
    }

    @Override
    public void visit(StringConstantExpression expression) {
        tokens.addLineNumberToken(expression);
        tokens.add(new StringConstantToken(StringUtil.escapeString(expression.getStringValue()), currentInternalTypeName));
    }

    @Override
    public void visit(SuperConstructorInvocationExpression expression) {
        tokens.addLineNumberToken(expression);
        tokens.add(SUPER);
        tokens.add(StartBlockToken.START_PARAMETERS_BLOCK);

        BaseExpression parameters = expression.getParameters();

        if (parameters != null) {
            parameters.accept(this);
        }

        tokens.add(EndBlockToken.END_PARAMETERS_BLOCK);
    }

    @Override
    public void visit(SuperExpression expression) {
        tokens.addLineNumberToken(expression);
        tokens.add(SUPER);
    }

    @Override
    public void visit(TernaryOperatorExpression expression) {
        tokens.addLineNumberToken(expression.getCondition());

        if (expression.getTrueExpression().isBooleanExpression() && expression.getFalseExpression().isBooleanExpression()) {
            BooleanExpression be1 = (BooleanExpression)expression.getTrueExpression();
            BooleanExpression be2 = (BooleanExpression)expression.getFalseExpression();

            if (be1.isTrue() && be2.isFalse()) {
                printTernaryOperatorExpression(expression.getCondition());
                return;
            }

            if (be1.isFalse() && be2.isTrue()) {
                tokens.add(TextToken.EXCLAMATION);
                printTernaryOperatorExpression(expression.getCondition());
                return;
            }
        }

        printTernaryOperatorExpression(expression.getCondition());
        tokens.add(TextToken.SPACE_QUESTION_SPACE);
        printTernaryOperatorExpression(expression.getTrueExpression());
        tokens.add(TextToken.SPACE_COLON_SPACE);
        printTernaryOperatorExpression(expression.getFalseExpression());
    }

    protected void printTernaryOperatorExpression(Expression expression) {
        if (expression.getPriority() > 3) {
            tokens.add(TextToken.LEFTROUNDBRACKET);
            expression.accept(this);
            tokens.add(TextToken.RIGHTROUNDBRACKET);
        } else {
            expression.accept(this);
        }
    }

    @Override
    public void visit(ThisExpression expression) {
        if (expression.isExplicit()) {
            tokens.addLineNumberToken(expression);
            tokens.add(THIS);
        }
    }

    @Override
    public void visit(TypeReferenceDotClassExpression expression) {
        tokens.addLineNumberToken(expression);

        BaseType type = expression.getTypeDotClass();

        type.accept(this);
        tokens.add(TextToken.DOT);
        tokens.add(CLASS);
    }

    protected void storeContext() {
        contextStack.add(new Context(currentInternalTypeName, currentTypeName, currentMethodParamNames));
    }

    protected void restoreContext() {
        Context currentContext = contextStack.removeLast();
        currentInternalTypeName = currentContext.currentInternalTypeName;
        currentTypeName = currentContext.currentTypeName;
        currentMethodParamNames = currentContext.currentMethodParamNames;
    }

    protected void visit(Expression parent, Expression child) {
        if ((parent.getPriority() < child.getPriority()) || ((parent.getPriority() == 14) && (child.getPriority() == 13))) {
            tokens.add(TextToken.LEFTROUNDBRACKET);
            child.accept(this);
            tokens.add(TextToken.RIGHTROUNDBRACKET);
        } else {
            child.accept(this);
        }
    }

    protected void visitHexa(Expression parent, Expression child) {
        if ((parent.getPriority() < child.getPriority()) || ((parent.getPriority() == 14) && (child.getPriority() == 13))) {
            tokens.add(TextToken.LEFTROUNDBRACKET);
            child.accept(hexaExpressionVisitor);
            tokens.add(TextToken.RIGHTROUNDBRACKET);
        } else {
            child.accept(hexaExpressionVisitor);
        }
    }

    protected static class Context {
        public final String currentInternalTypeName;
        public final String currentTypeName;
        public final HashSet<String> currentMethodParamNames;

        public Context(String currentInternalTypeName, String currentTypeName, HashSet<String> currentMethodParamNames) {
            this.currentInternalTypeName = currentInternalTypeName;
            this.currentTypeName = currentTypeName;
            this.currentMethodParamNames = new HashSet<>(currentMethodParamNames);
        }
    }

    protected static class Fragments extends DefaultList<Fragment> {
        public void addTokensFragment(Tokens tokens) {
            if (! tokens.isEmpty()) {
                if (tokens.getCurrentLineNumber() == UNKNOWN_LINE_NUMBER) {
                    super.add(new TokensFragment(tokens));
                } else {
                    super.add(new LineNumberTokensFragment(tokens));
                }
            }
        }
    }

    protected class HexaExpressionVisitor implements org.jd.core.v1.model.javasyntax.expression.ExpressionVisitor {
        @Override
        public void visit(IntegerConstantExpression expression) {
            tokens.addLineNumberToken(expression);

            PrimitiveType pt = (PrimitiveType)expression.getType();

            switch (pt.getJavaPrimitiveFlags()) {
                case FLAG_BOOLEAN:
                    tokens.add(new BooleanConstantToken(expression.getIntegerValue() == 1));
                    break;
                default:
                    tokens.add(new NumericConstantToken("0x" + Integer.toHexString(expression.getIntegerValue()).toUpperCase()));
                    break;
            }
        }

        @Override
        public void visit(LongConstantExpression expression) {
            tokens.addLineNumberToken(expression);
            tokens.add(new NumericConstantToken("0x" + Long.toHexString(expression.getLongValue()).toUpperCase() + 'L'));
        }

        @Override public void visit(ArrayExpression expression) { ExpressionVisitor.this.visit(expression); }
        @Override public void visit(BinaryOperatorExpression expression) { ExpressionVisitor.this.visit(expression); }
        @Override public void visit(BooleanExpression expression) { ExpressionVisitor.this.visit(expression); }
        @Override public void visit(CastExpression expression) { ExpressionVisitor.this.visit(expression); }
        @Override public void visit(CommentExpression expression) { ExpressionVisitor.this.visit(expression); }
        @Override public void visit(ConstructorInvocationExpression expression) { ExpressionVisitor.this.visit(expression); }
        @Override public void visit(ConstructorReferenceExpression expression) { ExpressionVisitor.this.visit(expression); }
        @Override public void visit(DoubleConstantExpression expression) { ExpressionVisitor.this.visit(expression); }
        @Override public void visit(EnumConstantReferenceExpression expression) { ExpressionVisitor.this.visit(expression); }
        @Override public void visit(Expressions expression) { ExpressionVisitor.this.visit(expression); }
        @Override public void visit(FieldReferenceExpression expression) { ExpressionVisitor.this.visit(expression); }
        @Override public void visit(FloatConstantExpression expression) { ExpressionVisitor.this.visit(expression); }
        @Override public void visit(InstanceOfExpression expression) { ExpressionVisitor.this.visit(expression); }
        @Override public void visit(LambdaFormalParametersExpression expression) { ExpressionVisitor.this.visit(expression); }
        @Override public void visit(LambdaIdentifiersExpression expression) { ExpressionVisitor.this.visit(expression); }
        @Override public void visit(LengthExpression expression) { ExpressionVisitor.this.visit(expression); }
        @Override public void visit(LocalVariableReferenceExpression expression) { ExpressionVisitor.this.visit(expression); }
        @Override public void visit(MethodInvocationExpression expression) { ExpressionVisitor.this.visit(expression); }
        @Override public void visit(MethodReferenceExpression expression) { ExpressionVisitor.this.visit(expression); }
        @Override public void visit(NewArray expression) { ExpressionVisitor.this.visit(expression); }
        @Override public void visit(NewExpression expression) { ExpressionVisitor.this.visit(expression); }
        @Override public void visit(NewInitializedArray expression) { ExpressionVisitor.this.visit(expression); }
        @Override public void visit(NoExpression expression) {}
        @Override public void visit(NullExpression expression) { ExpressionVisitor.this.visit(expression); }
        @Override public void visit(ObjectTypeReferenceExpression expression) { ExpressionVisitor.this.visit(expression); }
        @Override public void visit(ParenthesesExpression expression) { ExpressionVisitor.this.visit(expression); }
        @Override public void visit(PostOperatorExpression expression) { ExpressionVisitor.this.visit(expression); }
        @Override public void visit(PreOperatorExpression expression) { ExpressionVisitor.this.visit(expression); }
        @Override public void visit(StringConstantExpression expression) { ExpressionVisitor.this.visit(expression); }
        @Override public void visit(SuperConstructorInvocationExpression expression) { ExpressionVisitor.this.visit(expression); }
        @Override public void visit(SuperExpression expression) { ExpressionVisitor.this.visit(expression); }
        @Override public void visit(TernaryOperatorExpression expression) { ExpressionVisitor.this.visit(expression); }
        @Override public void visit(ThisExpression expression) { ExpressionVisitor.this.visit(expression); }
        @Override public void visit(TypeReferenceDotClassExpression expression) { ExpressionVisitor.this.visit(expression); }
    }
}
