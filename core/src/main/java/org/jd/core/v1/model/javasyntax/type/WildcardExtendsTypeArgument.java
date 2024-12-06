/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.type;

import java.util.Map;

public class WildcardExtendsTypeArgument implements TypeArgument {
    protected Type type;

    public WildcardExtendsTypeArgument(Type type) {
        this.type = type;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public boolean isTypeArgumentAssignableFrom(Map<String, BaseType> typeBounds, BaseTypeArgument typeArgument) {
        if (typeArgument.isWildcardExtendsTypeArgument()) {
            return type.isTypeArgumentAssignableFrom(typeBounds, typeArgument.getType());
        } else if (typeArgument instanceof Type) {
            return type.isTypeArgumentAssignableFrom(typeBounds, typeArgument);
        }

        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WildcardExtendsTypeArgument that = (WildcardExtendsTypeArgument) o;

        return type != null ? type.equals(that.type) : that.type == null;
    }

    @Override
    public int hashCode() {
        return 957014778 + (type != null ? type.hashCode() : 0);
    }

    @Override
    public boolean isWildcardExtendsTypeArgument() { return true; }

    @Override
    public void accept(TypeArgumentVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "WildcardExtendsTypeArgument{? extends " + type + "}";
    }
}
