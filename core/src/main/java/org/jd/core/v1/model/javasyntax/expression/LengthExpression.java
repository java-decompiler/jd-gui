/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.expression;

import org.jd.core.v1.model.javasyntax.type.PrimitiveType;
import org.jd.core.v1.model.javasyntax.type.Type;

public class LengthExpression extends AbstractLineNumberExpression {
    protected Expression expression;

    public LengthExpression(Expression expression) {
        this.expression = expression;
    }

    public LengthExpression(int lineNumber, Expression expression) {
        super(lineNumber);
        this.expression = expression;
    }

    @Override
    public Type getType() {
        return PrimitiveType.TYPE_INT;
    }

    @Override
    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public boolean isLengthExpression() { return true; }

    @Override
    public void accept(ExpressionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "LengthExpression{" + expression + "}";
    }
}
