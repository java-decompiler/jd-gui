/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.reference;

import org.jd.core.v1.model.javasyntax.type.BaseTypeArgument;
import org.jd.core.v1.model.javasyntax.type.ObjectType;

public class ObjectReference extends ObjectType implements Reference {
    public ObjectReference(String internalName, String qualifiedName, String name) {
        super(internalName, qualifiedName, name);
    }

    public ObjectReference(String internalName, String qualifiedName, String name, int dimension) {
        super(internalName, qualifiedName, name, dimension);
    }

    public ObjectReference(String internalName, String qualifiedName, String name, BaseTypeArgument typeArguments) {
        super(internalName, qualifiedName, name, typeArguments);
    }

    public ObjectReference(String internalName, String qualifiedName, String name, BaseTypeArgument typeArguments, int dimension) {
        super(internalName, qualifiedName, name, typeArguments, dimension);
    }

    @Override
    public void accept(ReferenceVisitor visitor) {
        visitor.visit(this);
    }
}
