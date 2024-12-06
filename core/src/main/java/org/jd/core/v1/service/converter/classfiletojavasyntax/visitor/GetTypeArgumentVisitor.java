/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.visitor;

import org.jd.core.v1.model.javasyntax.type.*;

public class GetTypeArgumentVisitor implements TypeVisitor {
    protected BaseTypeArgument typeArguments;

    public void init() {
        this.typeArguments = null;
    }

    public BaseTypeArgument getTypeArguments() {
        return typeArguments;
    }

    @Override public void visit(ObjectType type) { typeArguments = type.getTypeArguments(); }
    @Override public void visit(InnerObjectType type) { typeArguments = type.getTypeArguments(); }

    @Override public void visit(PrimitiveType type) { typeArguments = null; }
    @Override public void visit(GenericType type) { typeArguments = null; }
    @Override public void visit(Types types) { typeArguments = null; }
}
