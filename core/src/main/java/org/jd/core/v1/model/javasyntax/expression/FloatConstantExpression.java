/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.expression;

import org.jd.core.v1.model.javasyntax.type.PrimitiveType;

public class FloatConstantExpression extends AbstractLineNumberTypeExpression {
    protected float value;

    public FloatConstantExpression(float value) {
        super(PrimitiveType.TYPE_FLOAT);
        this.value = value;
    }

    public FloatConstantExpression(int lineNumber, float value) {
        super(lineNumber, PrimitiveType.TYPE_FLOAT);
        this.value = value;
    }

    public float getFloatValue() {
        return value;
    }

    @Override
    public boolean isFloatConstantExpression() { return true; }

    @Override
    public void accept(ExpressionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "FloatConstantExpression{" + value + "}";
    }
}
