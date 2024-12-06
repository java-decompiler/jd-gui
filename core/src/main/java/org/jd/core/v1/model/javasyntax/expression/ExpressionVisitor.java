/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.expression;

public interface ExpressionVisitor {
    void visit(ArrayExpression expression);
    void visit(BinaryOperatorExpression expression);
    void visit(BooleanExpression expression);
    void visit(CastExpression expression);
    void visit(CommentExpression expression);
    void visit(ConstructorInvocationExpression expression);
    void visit(ConstructorReferenceExpression expression);
    void visit(DoubleConstantExpression expression);
    void visit(EnumConstantReferenceExpression expression);
    void visit(Expressions expression);
    void visit(FieldReferenceExpression expression);
    void visit(FloatConstantExpression expression);
    void visit(IntegerConstantExpression expression);
    void visit(InstanceOfExpression expression);
    void visit(LambdaFormalParametersExpression expression);
    void visit(LambdaIdentifiersExpression expression);
    void visit(LengthExpression expression);
    void visit(LocalVariableReferenceExpression expression);
    void visit(LongConstantExpression expression);
    void visit(MethodInvocationExpression expression);
    void visit(MethodReferenceExpression expression);
    void visit(NewArray expression);
    void visit(NewExpression expression);
    void visit(NewInitializedArray expression);
    void visit(NoExpression expression);
    void visit(NullExpression expression);
    void visit(ObjectTypeReferenceExpression expression);
    void visit(ParenthesesExpression expression);
    void visit(PostOperatorExpression expression);
    void visit(PreOperatorExpression expression);
    void visit(StringConstantExpression expression);
    void visit(SuperConstructorInvocationExpression expression);
    void visit(SuperExpression expression);
    void visit(TernaryOperatorExpression expression);
    void visit(ThisExpression expression);
    void visit(TypeReferenceDotClassExpression expression);
}
