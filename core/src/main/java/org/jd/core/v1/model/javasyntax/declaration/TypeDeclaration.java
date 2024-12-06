/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.declaration;

import org.jd.core.v1.model.javasyntax.reference.BaseAnnotationReference;

public abstract class TypeDeclaration implements BaseTypeDeclaration, MemberDeclaration {
    protected BaseAnnotationReference annotationReferences;
    protected int flags;
    protected String internalTypeName;
    protected String name;
    protected BodyDeclaration bodyDeclaration;

    protected TypeDeclaration(BaseAnnotationReference annotationReferences, int flags, String internalTypeName, String name, BodyDeclaration bodyDeclaration) {
        this.annotationReferences = annotationReferences;
        this.flags = flags;
        this.internalTypeName = internalTypeName;
        this.name = name;
        this.bodyDeclaration = bodyDeclaration;
    }

    public BaseAnnotationReference getAnnotationReferences() {
        return annotationReferences;
    }

    public int getFlags() {
        return flags;
    }

    public String getInternalTypeName() {
        return internalTypeName;
    }

    public String getName() {
        return name;
    }

    public BodyDeclaration getBodyDeclaration() {
        return bodyDeclaration;
    }
}
