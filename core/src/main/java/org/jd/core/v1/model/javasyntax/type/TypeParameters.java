/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.type;

import org.jd.core.v1.util.DefaultList;

import java.util.Collection;

public class TypeParameters extends DefaultList<TypeParameter> implements BaseTypeParameter {
    public TypeParameters() {}

    public TypeParameters(int capacity) {
        super(capacity);
    }

    public TypeParameters(Collection<TypeParameter> collection) {
        super(collection);
        assert (collection != null) && (collection.size() > 1) : "Uses 'TypeParameter' instead";
    }

    @SuppressWarnings("unchecked")
    public TypeParameters(TypeParameter type, TypeParameter... types) {
        super(types.length + 1);
        assert (types != null) && (types.length > 0) : "Uses 'TypeParameter' instead";

        add(type);

        for (TypeParameter t : types) {
            add(t);
        }
    }

    @Override
    public void accept(TypeParameterVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(get(0).toString());

        for (int i=1; i<size(); i++) {
            sb.append(" & ");
            sb.append(get(i).toString());
        }

        return sb.toString();
    }
}
