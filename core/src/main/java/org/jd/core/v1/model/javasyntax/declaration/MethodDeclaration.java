/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.declaration;

import org.jd.core.v1.model.javasyntax.reference.BaseAnnotationReference;
import org.jd.core.v1.model.javasyntax.reference.ElementValue;
import org.jd.core.v1.model.javasyntax.statement.BaseStatement;
import org.jd.core.v1.model.javasyntax.type.BaseType;
import org.jd.core.v1.model.javasyntax.type.BaseTypeParameter;
import org.jd.core.v1.model.javasyntax.type.Type;

import static org.jd.core.v1.model.classfile.Constants.ACC_STATIC;

public class MethodDeclaration implements MemberDeclaration {
    protected BaseAnnotationReference annotationReferences;
    protected int flags;
    protected String name;
    protected BaseTypeParameter typeParameters;
    protected Type returnedType;
    protected BaseFormalParameter formalParameters;
    protected BaseType exceptionTypes;
    protected String descriptor;
    protected BaseStatement statements;
    protected ElementValue defaultAnnotationValue;

    public MethodDeclaration(int flags, String name, Type returnedType, String descriptor) {
        this.flags = flags;
        this.name = name;
        this.returnedType = returnedType;
        this.descriptor = descriptor;
    }

    public MethodDeclaration(int flags, String name, Type returnedType, String descriptor, BaseStatement statements) {
        this.flags = flags;
        this.name = name;
        this.returnedType = returnedType;
        this.descriptor = descriptor;
        this.statements = statements;
    }

    public MethodDeclaration(int flags, String name, Type returnedType, String descriptor, ElementValue defaultAnnotationValue) {
        this.flags = flags;
        this.name = name;
        this.returnedType = returnedType;
        this.descriptor = descriptor;
        this.defaultAnnotationValue = defaultAnnotationValue;
    }

    public MethodDeclaration(int flags, String name, Type returnedType, BaseFormalParameter formalParameters, String descriptor, BaseStatement statements) {
        this.flags = flags;
        this.name = name;
        this.returnedType = returnedType;
        this.formalParameters = formalParameters;
        this.descriptor = descriptor;
        this.statements = statements;
    }

    public MethodDeclaration(int flags, String name, Type returnedType, BaseFormalParameter formalParameters, String descriptor, ElementValue defaultAnnotationValue) {
        this.flags = flags;
        this.name = name;
        this.returnedType = returnedType;
        this.formalParameters = formalParameters;
        this.descriptor = descriptor;
        this.defaultAnnotationValue = defaultAnnotationValue;
    }

    public MethodDeclaration(BaseAnnotationReference annotationReferences, int flags, String name, BaseTypeParameter typeParameters, Type returnedType, BaseFormalParameter formalParameters, BaseType exceptionTypes, String descriptor, BaseStatement statements, ElementValue defaultAnnotationValue) {
        this.annotationReferences = annotationReferences;
        this.flags = flags;
        this.name = name;
        this.typeParameters = typeParameters;
        this.returnedType = returnedType;
        this.formalParameters = formalParameters;
        this.exceptionTypes = exceptionTypes;
        this.descriptor = descriptor;
        this.statements = statements;
        this.defaultAnnotationValue = defaultAnnotationValue;
    }

    public BaseAnnotationReference getAnnotationReferences() {
        return annotationReferences;
    }

    public int getFlags() {
        return flags;
    }

    public boolean isStatic() { return (flags & ACC_STATIC) != 0; }

    public String getName() {
        return name;
    }

    public BaseTypeParameter getTypeParameters() {
        return typeParameters;
    }

    public Type getReturnedType() {
        return returnedType;
    }

    public BaseFormalParameter getFormalParameters() {
        return formalParameters;
    }

    public BaseType getExceptionTypes() {
        return exceptionTypes;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public BaseStatement getStatements() {
        return statements;
    }

    public ElementValue getDefaultAnnotationValue() {
        return defaultAnnotationValue;
    }

    @Override
    public void accept(DeclarationVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "MethodDeclaration{" + name + " " + descriptor + "}";
    }
}
