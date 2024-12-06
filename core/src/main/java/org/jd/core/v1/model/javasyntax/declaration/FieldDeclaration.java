/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.declaration;

import org.jd.core.v1.model.javasyntax.reference.BaseAnnotationReference;
import org.jd.core.v1.model.javasyntax.type.Type;

public class FieldDeclaration implements MemberDeclaration {
    protected BaseAnnotationReference annotationReferences;
    protected int flags;
    protected Type type;
    protected BaseFieldDeclarator fieldDeclarators;

    public FieldDeclaration(int flags, Type type, BaseFieldDeclarator fieldDeclarators) {
        this.flags = flags;
        this.type = type;
        this.fieldDeclarators = fieldDeclarators;
        fieldDeclarators.setFieldDeclaration(this);
    }

    public FieldDeclaration(BaseAnnotationReference annotationReferences, int flags, Type type, BaseFieldDeclarator fieldDeclarators) {
        this.flags = flags;
        this.annotationReferences = annotationReferences;
        this.type = type;
        this.fieldDeclarators = fieldDeclarators;
        fieldDeclarators.setFieldDeclaration(this);
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public BaseAnnotationReference getAnnotationReferences() {
        return annotationReferences;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public BaseFieldDeclarator getFieldDeclarators() {
        return fieldDeclarators;
    }

    public void setFieldDeclarators(BaseFieldDeclarator fieldDeclarators) {
        this.fieldDeclarators = fieldDeclarators;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FieldDeclaration)) return false;

        FieldDeclaration that = (FieldDeclaration) o;

        if (flags != that.flags) return false;
        if (annotationReferences != null ? !annotationReferences.equals(that.annotationReferences) : that.annotationReferences != null)
            return false;
        if (!fieldDeclarators.equals(that.fieldDeclarators)) return false;
        if (!type.equals(that.type)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = 327494460 + flags;
        result = 31 * result + (annotationReferences != null ? annotationReferences.hashCode() : 0);
        result = 31 * result + type.hashCode();
        result = 31 * result + fieldDeclarators.hashCode();
        return result;
    }

    @Override
    public void accept(DeclarationVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "FieldDeclaration{" + type + " " + fieldDeclarators + "}";
    }
}
