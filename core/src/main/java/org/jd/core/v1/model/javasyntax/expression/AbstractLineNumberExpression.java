/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.expression;

public abstract class AbstractLineNumberExpression implements Expression {
    protected int lineNumber;

    protected AbstractLineNumberExpression() {
        this.lineNumber = UNKNOWN_LINE_NUMBER;
    }

    protected AbstractLineNumberExpression(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    @Override
    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
