/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.declaration;

import org.jd.core.v1.model.javasyntax.declaration.AnnotationDeclaration;
import org.jd.core.v1.model.javasyntax.reference.BaseAnnotationReference;

public class ClassFileAnnotationDeclaration extends AnnotationDeclaration implements ClassFileTypeDeclaration {
    protected int firstLineNumber;

    public ClassFileAnnotationDeclaration(BaseAnnotationReference annotationReferences, int flags, String internalName, String name, ClassFileBodyDeclaration bodyDeclaration) {
        super(annotationReferences, flags, internalName, name, null, bodyDeclaration);
        this.firstLineNumber = bodyDeclaration==null ? 0 : bodyDeclaration.getFirstLineNumber();
    }

    @Override
    public int getFirstLineNumber() {
        return firstLineNumber;
    }

    @Override
    public String toString() {
        return "ClassFileAnnotationDeclaration{" + internalTypeName + ", firstLineNumber=" + firstLineNumber + "}";
    }
}
