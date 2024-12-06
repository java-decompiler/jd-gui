/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.expression;

import org.jd.core.v1.model.javasyntax.type.Type;

public class TernaryOperatorExpression extends AbstractLineNumberTypeExpression {
    protected Expression condition;
    protected Expression trueExpression;
    protected Expression falseExpression;

    public TernaryOperatorExpression(Type type, Expression condition, Expression trueExpression, Expression falseExpression) {
        super(type);
        this.condition = condition;
        this.trueExpression = trueExpression;
        this.falseExpression = falseExpression;
    }

    public TernaryOperatorExpression(int lineNumber, Type type, Expression condition, Expression trueExpression, Expression falseExpression) {
        super(lineNumber, type);
        this.condition = condition;
        this.trueExpression = trueExpression;
        this.falseExpression = falseExpression;
    }

    public Expression getCondition() {
        return condition;
    }

    public void setCondition(Expression condition) {
        this.condition = condition;
    }

    @Override
    public Expression getTrueExpression() {
        return trueExpression;
    }

    public void setTrueExpression(Expression trueExpression) {
        this.trueExpression = trueExpression;
    }

    @Override
    public Expression getFalseExpression() {
        return falseExpression;
    }

    public void setFalseExpression(Expression falseExpression) {
        this.falseExpression = falseExpression;
    }

    @Override
    public int getPriority() {
        return 15;
    }

    @Override
    public boolean isTernaryOperatorExpression() { return true; }

    @Override
    public void accept(ExpressionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "TernaryOperatorExpression{" + condition + " ? " + trueExpression + " : " + falseExpression + "}";
    }
}
