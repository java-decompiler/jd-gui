/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.type;

public class InnerObjectType extends ObjectType {
    protected ObjectType outerType;

    public InnerObjectType(String internalName, String qualifiedName, String name, ObjectType outerType) {
        super(internalName, qualifiedName, name);
        this.outerType = outerType;
        assert (name == null) || !Character.isDigit(name.charAt(0)) || (qualifiedName == null);
    }

    public InnerObjectType(String internalName, String qualifiedName, String name, int dimension, ObjectType outerType) {
        super(internalName, qualifiedName, name, dimension);
        this.outerType = outerType;
        assert (name == null) || !Character.isDigit(name.charAt(0)) || (qualifiedName == null);
    }

    public InnerObjectType(String internalName, String qualifiedName, String name, BaseTypeArgument typeArguments, ObjectType outerType) {
        super(internalName, qualifiedName, name, typeArguments);
        this.outerType = outerType;
        assert (name == null) || !Character.isDigit(name.charAt(0)) || (qualifiedName == null);
    }

    public InnerObjectType(String internalName, String qualifiedName, String name, BaseTypeArgument typeArguments, int dimension, ObjectType outerType) {
        super(internalName, qualifiedName, name, typeArguments, dimension);
        this.outerType = outerType;
        assert (name == null) || !Character.isDigit(name.charAt(0)) || (qualifiedName == null);
    }

    public InnerObjectType(InnerObjectType iot) {
        super(iot);
        this.outerType = iot.outerType;
    }

    @Override
    public ObjectType getOuterType() {
        return outerType;
    }

    @Override
    public Type createType(int dimension) {
        assert dimension >= 0;
        return new InnerObjectType(internalName, qualifiedName, name, typeArguments, dimension, outerType);
    }

    public ObjectType createType(BaseTypeArgument typeArguments) {
        return new InnerObjectType(internalName, qualifiedName, name, typeArguments, dimension, outerType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InnerObjectType)) return false;
        if (!super.equals(o)) return false;

        InnerObjectType that = (InnerObjectType) o;

        if (!outerType.equals(that.outerType)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = 111476860 + super.hashCode();
        result = 31 * result + outerType.hashCode();
        return result;
    }

    @Override
    public boolean isInnerObjectType() {
        return true;
    }

    @Override
    public boolean isInnerObjectTypeArgument() {
        return true;
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void accept(TypeArgumentVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        if (typeArguments == null) {
            return "InnerObjectType{" + outerType.toString() + "." + descriptor + "}";
        } else {
            return "InnerObjectType{" + outerType.toString() + "." + descriptor + "<" + typeArguments + ">}";
        }
    }
}
