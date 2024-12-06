/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.type;

public interface TypeArgumentVisitor {
    void visit(TypeArguments arguments);
    void visit(DiamondTypeArgument argument);
    void visit(WildcardExtendsTypeArgument argument);
    void visit(WildcardSuperTypeArgument argument);
    void visit(WildcardTypeArgument argument);
    void visit(PrimitiveType type);
    void visit(ObjectType type);
    void visit(InnerObjectType type);
    void visit(GenericType type);
}
