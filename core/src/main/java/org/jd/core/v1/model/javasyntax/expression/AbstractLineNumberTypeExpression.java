/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.expression;

import org.jd.core.v1.model.javasyntax.type.Type;

public abstract class AbstractLineNumberTypeExpression extends AbstractLineNumberExpression {
    protected Type type;

    protected AbstractLineNumberTypeExpression(Type type) {
        this.type = type;
    }

    protected AbstractLineNumberTypeExpression(int lineNumber, Type type) {
        super(lineNumber);
        this.type = type;
    }

    @Override
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
