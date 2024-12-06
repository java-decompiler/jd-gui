/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.expression;

import org.jd.core.v1.model.javasyntax.type.Type;

public class ThisExpression extends AbstractLineNumberExpression {
    protected Type type;
    protected boolean explicit;

    public ThisExpression(Type type) {
        this.type = type;
        this.explicit = true;
    }

    public ThisExpression(int lineNumber, Type type) {
        super(lineNumber);
        this.type = type;
        this.explicit = true;
    }

    @Override
    public Type getType() {
        return type;
    }

    public boolean isExplicit() {
        return explicit;
    }

    public void setExplicit(boolean explicit) {
        this.explicit = explicit;
    }

    @Override
    public boolean isThisExpression() { return true; }

    @Override
    public void accept(ExpressionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "ThisExpression{" + type + "}";
    }
}
