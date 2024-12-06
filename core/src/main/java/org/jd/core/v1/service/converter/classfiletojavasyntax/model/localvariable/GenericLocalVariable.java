/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.model.localvariable;

import org.jd.core.v1.model.javasyntax.type.BaseType;
import org.jd.core.v1.model.javasyntax.type.GenericType;
import org.jd.core.v1.model.javasyntax.type.Type;

import java.util.Map;

public class GenericLocalVariable extends AbstractLocalVariable {
    protected GenericType type;

    public GenericLocalVariable(int index, int offset, GenericType type) {
        super(index, offset, null);
        this.type = type;
    }

    public GenericLocalVariable(int index, int offset, GenericType type, String name) {
        super(index, offset, name);
        this.type = type;
    }

    @Override
    public GenericType getType() {
        return type;
    }

    public void setType(GenericType type) {
        this.type = type;
    }

    @Override
    public int getDimension() {
        return type.getDimension();
    }

    @Override
    public void accept(LocalVariableVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("GenericLocalVariable{");
        sb.append(type.getName());

        if (type.getDimension() > 0) {
            sb.append(new String(new char[type.getDimension()]).replaceAll("\0", "[]"));
        }

        sb.append(' ').append(name).append(", index=").append(index);

        if (next != null) {
            sb.append(", next=").append(next);
        }

        return sb.append("}").toString();
    }

    @Override public boolean isAssignableFrom(Map<String, BaseType> typeBounds, Type otherType) {
        return type.equals(otherType);
    }
    @Override public void typeOnRight(Map<String, BaseType> typeBounds, Type type) {}
    @Override public void typeOnLeft(Map<String, BaseType> typeBounds, Type type) {}

    @Override public boolean isAssignableFrom(Map<String, BaseType> typeBounds, AbstractLocalVariable variable) { return isAssignableFrom(typeBounds, variable.getType()); }
    @Override public void variableOnRight(Map<String, BaseType> typeBounds, AbstractLocalVariable variable) {}
    @Override public void variableOnLeft(Map<String, BaseType> typeBounds, AbstractLocalVariable variable) {}
}
