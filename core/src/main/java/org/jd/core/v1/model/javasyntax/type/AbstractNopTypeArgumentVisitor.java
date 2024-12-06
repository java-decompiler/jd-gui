/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.type;

public abstract class AbstractNopTypeArgumentVisitor implements TypeArgumentVisitor {
    @Override public void visit(TypeArguments arguments) {}
    @Override public void visit(DiamondTypeArgument argument) {}
    @Override public void visit(WildcardExtendsTypeArgument argument) {}
    @Override public void visit(PrimitiveType type) {}
    @Override public void visit(ObjectType type) {}
    @Override public void visit(InnerObjectType type) {}
    @Override public void visit(WildcardSuperTypeArgument argument) {}
    @Override public void visit(GenericType type) {}
    @Override public void visit(WildcardTypeArgument argument) {}
}
