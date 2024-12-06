/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.expression;

import org.jd.core.v1.model.javasyntax.type.PrimitiveType;

public class DoubleConstantExpression extends AbstractLineNumberTypeExpression {
    protected double value;

    public DoubleConstantExpression(double value) {
        super(PrimitiveType.TYPE_DOUBLE);
        this.value = value;
    }

    public DoubleConstantExpression(int lineNumber, double value) {
        super(lineNumber, PrimitiveType.TYPE_DOUBLE);
        this.value = value;
    }

    @Override
    public double getDoubleValue() {
        return value;
    }

    @Override
    public boolean isDoubleConstantExpression() { return true; }

    @Override
    public void accept(ExpressionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "DoubleConstantExpression{" + value + "}";
    }
}
