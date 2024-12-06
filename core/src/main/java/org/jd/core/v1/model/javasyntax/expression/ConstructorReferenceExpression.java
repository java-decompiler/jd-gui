/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.expression;

import org.jd.core.v1.model.javasyntax.type.ObjectType;
import org.jd.core.v1.model.javasyntax.type.Type;

public class ConstructorReferenceExpression extends AbstractLineNumberTypeExpression {
    protected ObjectType objectType;
    protected String descriptor;

    public ConstructorReferenceExpression(Type type, ObjectType objectType, String descriptor) {
        super(type);
        this.objectType = objectType;
        this.descriptor = descriptor;
    }

    public ConstructorReferenceExpression(int lineNumber, Type type, ObjectType objectType, String descriptor) {
        super(lineNumber, type);
        this.objectType = objectType;
        this.descriptor = descriptor;
    }

    @Override
    public ObjectType getObjectType() {
        return objectType;
    }

    @Override
    public String getDescriptor() {
        return descriptor;
    }

    @Override
    public void accept(ExpressionVisitor visitor) {
        visitor.visit(this);
    }
}
