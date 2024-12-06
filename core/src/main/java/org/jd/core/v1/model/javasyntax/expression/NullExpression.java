/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.expression;

import org.jd.core.v1.model.javasyntax.type.Type;

public class NullExpression extends AbstractLineNumberTypeExpression {
    public NullExpression(Type type) {
        super(type);
    }

    public NullExpression(int lineNumber, Type type) {
        super(lineNumber, type);
    }

    @Override
    public boolean isNullExpression() { return true; }

    @Override
    public void accept(ExpressionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "NullExpression{type=" + type + "}";
    }
}
