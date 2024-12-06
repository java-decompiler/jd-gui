/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.type;

public abstract class AbstractTypeArgumentVisitor implements TypeArgumentVisitor {
    @Override
    public void visit(TypeArguments arguments) {
        for (TypeArgument typeArgument : arguments) {
            typeArgument.accept(this);
        }
    }

    @Override
    public void visit(DiamondTypeArgument argument) {}

    @Override
    public void visit(WildcardExtendsTypeArgument argument) {
        argument.getType().accept(this);
    }

    @Override public void visit(PrimitiveType type) {}

    @Override
    public void visit(ObjectType type) {
        safeAccept(type.getTypeArguments());
    }

    @Override
    public void visit(InnerObjectType type) {
        type.getOuterType().accept(this);
        safeAccept(type.getTypeArguments());
    }

    @Override
    public void visit(WildcardSuperTypeArgument argument) {
        argument.getType().accept(this);
    }

    @Override
    public void visit(GenericType type) {}

    @Override
    public void visit(WildcardTypeArgument argument) {}

    protected void safeAccept(TypeArgumentVisitable visitable) {
        if (visitable != null) {
            visitable.accept(this);
        }
    }
}
