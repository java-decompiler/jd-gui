/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.expression;

import org.jd.core.v1.model.javasyntax.type.PrimitiveType;
import org.jd.core.v1.model.javasyntax.type.Type;

public class BooleanExpression extends AbstractLineNumberExpression {
    public static final BooleanExpression TRUE = new BooleanExpression(true);
    public static final BooleanExpression FALSE = new BooleanExpression(false);

    protected boolean value;

    protected BooleanExpression(boolean value) {
        this.value = value;
    }

    public BooleanExpression(int lineNumber, boolean value) {
        super(lineNumber);
        this.value = value;
    }

    @Override
    public Type getType() {
        return PrimitiveType.TYPE_BOOLEAN;
    }

    public boolean isTrue() {
        return value;
    }

    public boolean isFalse() {
        return !value;
    }

    @Override
    public boolean isBooleanExpression() { return true; }

    @Override
    public void accept(ExpressionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "BooleanExpression{" + value + "}";
    }
}
