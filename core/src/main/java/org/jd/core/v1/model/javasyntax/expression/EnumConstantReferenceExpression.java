/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.expression;


import org.jd.core.v1.model.javasyntax.type.ObjectType;
import org.jd.core.v1.model.javasyntax.type.Type;

public class EnumConstantReferenceExpression extends AbstractLineNumberExpression {
    protected ObjectType type;
    protected String name;

    public EnumConstantReferenceExpression(ObjectType type, String name) {
        this.type = type;
        this.name = name;
    }

    public EnumConstantReferenceExpression(int lineNumber, ObjectType type, String name) {
        super(lineNumber);
        this.type = type;
        this.name = name;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public ObjectType getObjectType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void accept(ExpressionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "EnumConstantReferenceExpression{type=" + type + ", name=" + name + "}";
    }
}
