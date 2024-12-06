/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.declaration;

public class LocalVariableDeclarator implements BaseLocalVariableDeclarator {
    protected int lineNumber;
    protected String name;
    protected int dimension;
    protected VariableInitializer variableInitializer;

    public LocalVariableDeclarator(String name) {
        this.name = name;
    }

    public LocalVariableDeclarator(String name, VariableInitializer variableInitializer) {
        this.name = name;
        this.variableInitializer = variableInitializer;
    }

    public LocalVariableDeclarator(int lineNumber, String name, VariableInitializer variableInitializer) {
        this.lineNumber = lineNumber;
        this.name = name;
        this.variableInitializer = variableInitializer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    @Override
    public int getLineNumber() {
        return lineNumber;
    }

    public VariableInitializer getVariableInitializer() {
        return variableInitializer;
    }

    @Override
    public void accept(DeclarationVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "LocalVariableDeclarator{name=" + name + ", dimension" + dimension + ", variableInitializer=" + variableInitializer + "}";
    }
}
