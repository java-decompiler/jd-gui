/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.visitor;

import org.jd.core.v1.model.javasyntax.type.*;

import java.util.Map;

public class PopulateBindingsWithTypeParameterVisitor implements TypeParameterVisitor {
    protected Map<String, TypeArgument> bindings = null;
    protected Map<String, BaseType> typeBounds = null;

    public void init(Map<String, TypeArgument> bindings, Map<String, BaseType> typeBounds) {
        this.bindings = bindings;
        this.typeBounds = typeBounds;
    }

    @Override
    public void visit(TypeParameter parameter) {
        bindings.put(parameter.getIdentifier(), null);
    }

    @Override
    public void visit(TypeParameterWithTypeBounds parameter) {
        bindings.put(parameter.getIdentifier(), null);
        typeBounds.put(parameter.getIdentifier(), parameter.getTypeBounds());
    }

    @Override
    public void visit(TypeParameters parameters) {
        for (TypeParameter parameter : parameters) {
            parameter.accept(this);
        }
    }
}
