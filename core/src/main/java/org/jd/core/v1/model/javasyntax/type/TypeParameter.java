/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.type;

public class TypeParameter implements BaseTypeParameter {
    protected String identifier;

    public TypeParameter(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public void accept(TypeParameterVisitor visitor) {
        visitor.visit(this);
    }

    public String toString() {
        return "TypeParameter{identifier=" + identifier + "}";
    }
}
