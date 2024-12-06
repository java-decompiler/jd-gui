/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.expression;

import org.jd.core.v1.model.javasyntax.type.Type;

import static org.jd.core.v1.model.javasyntax.type.ObjectType.TYPE_UNDEFINED_OBJECT;

public class ArrayExpression extends AbstractLineNumberTypeExpression {
    protected Expression expression;
    protected Expression index;

    public ArrayExpression(Expression expression, Expression index) {
        super(createItemType(expression));
        this.expression = expression;
        this.index = index;
    }

    public ArrayExpression(int lineNumber, Expression expression, Expression index) {
        super(lineNumber, createItemType(expression));
        this.expression = expression;
        this.index = index;
    }

    @Override
    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public Expression getIndex() {
        return index;
    }

    public void setIndex(Expression index) {
        this.index = index;
    }

    @Override
    public int getPriority() {
        return 1;
    }

    protected static Type createItemType(Expression expression) {
        Type type = expression.getType();
        int dimension = type.getDimension();

        return type.createType((dimension > 0) ? dimension-1 : 0);
    }

    @Override
    public boolean isArrayExpression() { return true; }

    @Override
    public void accept(ExpressionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "ArrayExpression{" + expression + "[" + index + "]}";
    }
}
