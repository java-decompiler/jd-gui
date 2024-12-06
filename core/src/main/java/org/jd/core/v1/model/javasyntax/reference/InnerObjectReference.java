/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.reference;

import org.jd.core.v1.model.javasyntax.type.BaseTypeArgument;
import org.jd.core.v1.model.javasyntax.type.InnerObjectType;
import org.jd.core.v1.model.javasyntax.type.ObjectType;

public class InnerObjectReference extends InnerObjectType implements Reference {

    public InnerObjectReference(String internalName, String qualifiedName, String name, ObjectType outerType) {
        super(internalName, qualifiedName, name, outerType);
    }

    public InnerObjectReference(String internalName, String qualifiedName, String name, int dimension, ObjectType outerType) {
        super(internalName, qualifiedName, name, dimension, outerType);
    }

    public InnerObjectReference(String internalName, String qualifiedName, String name, BaseTypeArgument typeArguments, ObjectType outerType) {
        super(internalName, qualifiedName, name, typeArguments, outerType);
    }

    public InnerObjectReference(String internalName, String qualifiedName, String name, BaseTypeArgument typeArguments, int dimension, ObjectType outerType) {
        super(internalName, qualifiedName, name, typeArguments, dimension, outerType);
    }

    public InnerObjectReference(InnerObjectType iot) {
        super(iot);
    }

    @Override
    public void accept(ReferenceVisitor visitor) {
        visitor.visit(this);
    }
}
