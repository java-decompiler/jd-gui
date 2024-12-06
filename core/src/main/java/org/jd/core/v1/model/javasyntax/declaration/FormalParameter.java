/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.declaration;

import org.jd.core.v1.model.javasyntax.reference.BaseAnnotationReference;
import org.jd.core.v1.model.javasyntax.type.Type;

public class FormalParameter implements BaseFormalParameter {
    protected BaseAnnotationReference annotationReferences;
    protected boolean fina1;
    protected Type type;
    protected boolean varargs;
    protected String name;

    public FormalParameter(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    public FormalParameter(BaseAnnotationReference annotationReferences, Type type, String name) {
        this.annotationReferences = annotationReferences;
        this.type = type;
        this.name = name;
    }

    public FormalParameter(Type type, boolean varargs, String name) {
        this.type = type;
        this.varargs = varargs;
        this.name = name;
    }

    public FormalParameter(BaseAnnotationReference annotationReferences, Type type, boolean varargs, String name) {
        this.annotationReferences = annotationReferences;
        this.type = type;
        this.varargs = varargs;
        this.name = name;
    }

    public BaseAnnotationReference getAnnotationReferences() {
        return annotationReferences;
    }

    public boolean isFinal() {
        return fina1;
    }

    public void setFinal(boolean fina1) {
        this.fina1 = fina1;
    }

    public Type getType() {
        return type;
    }

    public boolean isVarargs() {
        return varargs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void accept(DeclarationVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        String s = "FormalParameter{";

        if (annotationReferences != null)
            s += annotationReferences + " ";

        if (varargs)
            s += type.createType(type.getDimension()-1) + "... ";
        else
            s += type + " ";

        return s + name + "}";
    }
}
