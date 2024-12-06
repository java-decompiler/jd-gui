/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.expression;

import org.jd.core.v1.model.javasyntax.type.PrimitiveType;

public class LongConstantExpression extends AbstractLineNumberTypeExpression {
    protected long value;

    public LongConstantExpression(long value) {
        super(PrimitiveType.TYPE_LONG);
        this.value = value;
    }

    public LongConstantExpression(int lineNumber, long value) {
        super(lineNumber, PrimitiveType.TYPE_LONG);
        this.value = value;
    }

    @Override
    public long getLongValue() {
        return value;
    }

    @Override
    public boolean isLongConstantExpression() { return true; }

    @Override
    public void accept(ExpressionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "LongConstantExpression{" + value + "}";
    }
}
