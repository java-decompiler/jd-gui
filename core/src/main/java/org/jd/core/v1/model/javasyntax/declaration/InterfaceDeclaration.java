/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.declaration;

import org.jd.core.v1.model.javasyntax.reference.BaseAnnotationReference;
import org.jd.core.v1.model.javasyntax.type.BaseType;
import org.jd.core.v1.model.javasyntax.type.BaseTypeParameter;

public class InterfaceDeclaration extends TypeDeclaration {
    protected BaseTypeParameter typeParameters;
    protected BaseType interfaces;

    public InterfaceDeclaration(int flags, String internalName, String name, BaseType interfaces) {
        super(null, flags, internalName, name, null);
        this.interfaces = interfaces;
    }

    public InterfaceDeclaration(BaseAnnotationReference annotationReferences, int flags, String internalName, String name, BaseTypeParameter typeParameters, BaseType interfaces, BodyDeclaration bodyDeclaration) {
        super(annotationReferences, flags, internalName, name, bodyDeclaration);
        this.typeParameters = typeParameters;
        this.interfaces = interfaces;
    }

    public BaseTypeParameter getTypeParameters() {
        return typeParameters;
    }

    public BaseType getInterfaces() {
        return interfaces;
    }

    @Override
    public void accept(DeclarationVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "InterfaceDeclaration{" + internalTypeName + "}";
    }
}
