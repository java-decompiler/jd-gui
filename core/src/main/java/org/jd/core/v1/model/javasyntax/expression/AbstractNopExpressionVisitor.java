/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.expression;

public abstract class AbstractNopExpressionVisitor implements ExpressionVisitor {
    @Override public void visit(ArrayExpression expression) {}
    @Override public void visit(BinaryOperatorExpression expression) {}
    @Override public void visit(BooleanExpression expression) {}
    @Override public void visit(CastExpression expression) {}
    @Override public void visit(CommentExpression expression) {}
    @Override public void visit(ConstructorInvocationExpression expression) {}
    @Override public void visit(ConstructorReferenceExpression expression) {}
    @Override public void visit(DoubleConstantExpression expression) {}
    @Override public void visit(EnumConstantReferenceExpression expression) {}
    @Override public void visit(Expressions expression) {}
    @Override public void visit(FieldReferenceExpression expression) {}
    @Override public void visit(FloatConstantExpression expression) {}
    @Override public void visit(IntegerConstantExpression expression) {}
    @Override public void visit(InstanceOfExpression expression) {}
    @Override public void visit(LambdaFormalParametersExpression expression) {}
    @Override public void visit(LambdaIdentifiersExpression expression) {}
    @Override public void visit(LengthExpression expression) {}
    @Override public void visit(LocalVariableReferenceExpression expression) {}
    @Override public void visit(LongConstantExpression expression) {}
    @Override public void visit(MethodInvocationExpression expression) {}
    @Override public void visit(MethodReferenceExpression expression) {}
    @Override public void visit(NewArray expression) {}
    @Override public void visit(NewExpression expression) {}
    @Override public void visit(NewInitializedArray expression) {}
    @Override public void visit(NoExpression expression) {}
    @Override public void visit(NullExpression expression) {}
    @Override public void visit(ObjectTypeReferenceExpression expression) {}
    @Override public void visit(ParenthesesExpression expression) {}
    @Override public void visit(PostOperatorExpression expression) {}
    @Override public void visit(PreOperatorExpression expression) {}
    @Override public void visit(StringConstantExpression expression) {}
    @Override public void visit(SuperConstructorInvocationExpression expression) {}
    @Override public void visit(SuperExpression expression) {}
    @Override public void visit(TernaryOperatorExpression expression) {}
    @Override public void visit(ThisExpression expression) {}
    @Override public void visit(TypeReferenceDotClassExpression expression) {}
}
