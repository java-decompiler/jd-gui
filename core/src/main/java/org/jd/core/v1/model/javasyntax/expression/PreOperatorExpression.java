/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.expression;

import org.jd.core.v1.model.javasyntax.type.Type;

public class PreOperatorExpression extends AbstractLineNumberExpression {
    protected String operator;
    protected Expression expression;

    public PreOperatorExpression(String operator, Expression expression) {
        this.operator = operator;
        this.expression = expression;
    }

    public PreOperatorExpression(int lineNumber, String operator, Expression expression) {
        super(lineNumber);
        this.operator = operator;
        this.expression = expression;
    }

    @Override
    public String getOperator() {
        return operator;
    }

    @Override
    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public Type getType() {
        return expression.getType();
    }

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    public boolean isPreOperatorExpression() { return true; }

    @Override
    public void accept(ExpressionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "PreOperatorExpression{" + operator + " " + expression + "}";
    }
}
