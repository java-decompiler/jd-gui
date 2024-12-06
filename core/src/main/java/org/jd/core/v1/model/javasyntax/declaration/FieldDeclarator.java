/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.declaration;

public class FieldDeclarator implements BaseFieldDeclarator {
    protected FieldDeclaration fieldDeclaration;
    protected String name;
    protected int dimension;
    protected VariableInitializer variableInitializer;

    public FieldDeclarator(String name) {
        this.name = name;
    }

    public FieldDeclarator(String name, VariableInitializer variableInitializer) {
        this.name = name;
        this.variableInitializer = variableInitializer;
    }

    public FieldDeclarator(String name, int dimension, VariableInitializer variableInitializer) {
        this.name = name;
        this.dimension = dimension;
        this.variableInitializer = variableInitializer;
    }

    @Override
    public void setFieldDeclaration(FieldDeclaration fieldDeclaration) {
        this.fieldDeclaration = fieldDeclaration;
    }

    public FieldDeclaration getFieldDeclaration() {
        return fieldDeclaration;
    }

    public String getName() {
        return name;
    }

    public int getDimension() {
        return dimension;
    }

    public VariableInitializer getVariableInitializer() {
        return variableInitializer;
    }

    public void setVariableInitializer(VariableInitializer variableInitializer) {
        this.variableInitializer = variableInitializer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FieldDeclarator)) return false;

        FieldDeclarator that = (FieldDeclarator) o;

        if (dimension != that.dimension) return false;
        if (!name.equals(that.name)) return false;
        if (variableInitializer != null ? !variableInitializer.equals(that.variableInitializer) : that.variableInitializer != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = 544278669 + name.hashCode();
        result = 31 * result + dimension;
        result = 31 * result + (variableInitializer != null ? variableInitializer.hashCode() : 0);
        return result;
    }

    @Override
    public void accept(DeclarationVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "FieldDeclarator{" + name + "}";
    }
}
