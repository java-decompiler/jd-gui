/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.statement;

import org.jd.core.v1.model.javasyntax.expression.Expression;

public class ReturnExpressionStatement implements Statement {
    protected int lineNumber;
    protected Expression expression;

    public ReturnExpressionStatement(Expression expression) {
        this(expression.getLineNumber(), expression);
    }

    public ReturnExpressionStatement(int lineNumber, Expression expression) {
        this.lineNumber = lineNumber;
        this.expression = expression;
        assert expression != null;
    }

    @Override
    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    @Override
    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        assert expression != null;
        this.expression = expression;
    }

    @SuppressWarnings("unchecked")
    public <T extends Expression> T getGenericExpression() {
        return (T)expression;
    }

    @Override
    public boolean isReturnExpressionStatement() { return true; }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "ReturnExpressionStatement{return " + expression + "}";
    }
}
