/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.expression;

import org.jd.core.v1.model.javasyntax.type.ObjectType;
import org.jd.core.v1.util.Base;

import static org.jd.core.v1.model.javasyntax.expression.NoExpression.NO_EXPRESSION;

public interface BaseExpression extends Base<Expression> {
    void accept(ExpressionVisitor visitor);

    default boolean isArrayExpression() { return false; }
    default boolean isBinaryOperatorExpression() { return false; }
    default boolean isBooleanExpression() { return false; }
    default boolean isCastExpression() { return false; }
    default boolean isConstructorInvocationExpression() { return false; }
    default boolean isDoubleConstantExpression() { return false; }
    default boolean isFieldReferenceExpression() { return false; }
    default boolean isFloatConstantExpression() { return false; }
    default boolean isIntegerConstantExpression() { return false; }
    default boolean isLengthExpression() { return false; }
    default boolean isLocalVariableReferenceExpression() { return false; }
    default boolean isLongConstantExpression() { return false; }
    default boolean isMethodInvocationExpression() { return false; }
    default boolean isNewArray() { return false; }
    default boolean isNewExpression() { return false; }
    default boolean isNewInitializedArray() { return false; }
    default boolean isNullExpression() { return false; }
    default boolean isObjectTypeReferenceExpression() { return false; }
    default boolean isPostOperatorExpression() { return false; }
    default boolean isPreOperatorExpression() { return false; }
    default boolean isStringConstantExpression() { return false; }
    default boolean isSuperConstructorInvocationExpression() { return false; }
    default boolean isSuperExpression() { return false; }
    default boolean isTernaryOperatorExpression() { return false; }
    default boolean isThisExpression() { return false; }

    default BaseExpression getDimensionExpressionList() { return NO_EXPRESSION; }
    default BaseExpression getParameters() { return NO_EXPRESSION; }

    default Expression getCondition() { return NO_EXPRESSION; }
    default Expression getExpression() { return NO_EXPRESSION; }
    default Expression getTrueExpression() { return NO_EXPRESSION; }
    default Expression getFalseExpression() { return NO_EXPRESSION; }
    default Expression getIndex() { return NO_EXPRESSION; }
    default Expression getLeftExpression() { return NO_EXPRESSION; }
    default Expression getRightExpression() { return NO_EXPRESSION; }

    default String getDescriptor() { return ""; }
    default double getDoubleValue() { return 0D; }
    default float getFloatValue() { return 0F; }
    default int getIntegerValue() { return 0; }
    default String getInternalTypeName() { return ""; }
    default long getLongValue() { return 0L; }
    default String getName() { return ""; }
    default ObjectType getObjectType() { return ObjectType.TYPE_UNDEFINED_OBJECT; }
    default String getOperator() { return ""; }
    default String getStringValue() { return ""; }
}
