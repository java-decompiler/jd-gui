/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.expression;

import org.jd.core.v1.model.javasyntax.type.ObjectType;
import org.jd.core.v1.model.javasyntax.type.Type;

public class ObjectTypeReferenceExpression implements Expression {
    protected int lineNumber;
    protected ObjectType type;
    protected boolean explicit;

    public ObjectTypeReferenceExpression(ObjectType type) {
        this.type = type;
        this.explicit = true;
    }

    public ObjectTypeReferenceExpression(int lineNumber, ObjectType type) {
        this.lineNumber = lineNumber;
        this.type = type;
        this.explicit = true;
    }

    public ObjectTypeReferenceExpression(ObjectType type, boolean explicit) {
        this.type = type;
        this.explicit = explicit;
    }

    public ObjectTypeReferenceExpression(int lineNumber, ObjectType type, boolean explicit) {
        this.lineNumber = lineNumber;
        this.type = type;
        this.explicit = explicit;
    }

    @Override
    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public ObjectType getObjectType() {
        return type;
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
    public int getPriority() {
        return 0;
    }

    @Override
    public boolean isObjectTypeReferenceExpression() { return true; }

    @Override
    public void accept(ExpressionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "ObjectTypeReferenceExpression{" + type + "}";
    }
}
