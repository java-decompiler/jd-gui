/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.declaration;

import org.jd.core.v1.model.classfile.ClassFile;
import org.jd.core.v1.model.classfile.Method;
import org.jd.core.v1.model.javasyntax.declaration.BaseFormalParameter;
import org.jd.core.v1.model.javasyntax.declaration.MethodDeclaration;
import org.jd.core.v1.model.javasyntax.reference.BaseAnnotationReference;
import org.jd.core.v1.model.javasyntax.reference.ElementValue;
import org.jd.core.v1.model.javasyntax.statement.BaseStatement;
import org.jd.core.v1.model.javasyntax.type.BaseType;
import org.jd.core.v1.model.javasyntax.type.BaseTypeParameter;
import org.jd.core.v1.model.javasyntax.type.Type;
import org.jd.core.v1.model.javasyntax.type.TypeArgument;

import java.util.Map;

public class ClassFileMethodDeclaration extends MethodDeclaration implements ClassFileConstructorOrMethodDeclaration {
    protected ClassFileBodyDeclaration bodyDeclaration;
    protected ClassFile classFile;
    protected Method method;
    protected BaseType parameterTypes;
    protected Map<String, TypeArgument> bindings;
    protected Map<String, BaseType> typeBounds;
    protected int firstLineNumber;

    public ClassFileMethodDeclaration(
            ClassFileBodyDeclaration bodyDeclaration, ClassFile classFile, Method method, String name,
            Type returnedType, BaseType parameterTypes, Map<String, TypeArgument> bindings,
            Map<String, BaseType> typeBounds) {
        super(null, method.getAccessFlags(), name, null, returnedType, null, null, method.getDescriptor(), null, null);
        this.bodyDeclaration = bodyDeclaration;
        this.classFile = classFile;
        this.parameterTypes = parameterTypes;
        this.method = method;
        this.bindings = bindings;
        this.typeBounds = typeBounds;
    }

    public ClassFileMethodDeclaration(
            ClassFileBodyDeclaration bodyDeclaration, ClassFile classFile, Method method, String name,
            Type returnedType, BaseType parameterTypes, Map<String, TypeArgument> bindings,
            Map<String, BaseType> typeBounds, int firstLineNumber) {
        super(null, method.getAccessFlags(), name, null, returnedType, null, null, method.getDescriptor(), null, null);
        this.bodyDeclaration = bodyDeclaration;
        this.classFile = classFile;
        this.method = method;
        this.parameterTypes = parameterTypes;
        this.bindings = bindings;
        this.typeBounds = typeBounds;
        this.firstLineNumber = firstLineNumber;
    }

    public ClassFileMethodDeclaration(
            ClassFileBodyDeclaration bodyDeclaration, ClassFile classFile, Method method, BaseAnnotationReference annotationReferences,
            String name, BaseTypeParameter typeParameters, Type returnedType, BaseType parameterTypes, BaseType exceptionTypes,
            ElementValue defaultAnnotationValue, Map<String, TypeArgument> bindings,
            Map<String, BaseType> typeBounds, int firstLineNumber) {
        super(annotationReferences, method.getAccessFlags(), name, typeParameters, returnedType, null, exceptionTypes, method.getDescriptor(), null, defaultAnnotationValue);
        this.bodyDeclaration = bodyDeclaration;
        this.classFile = classFile;
        this.method = method;
        this.parameterTypes = parameterTypes;
        this.bindings = bindings;
        this.typeBounds = typeBounds;
        this.firstLineNumber = firstLineNumber;
    }

    @Override
    public void setFlags(int flags) {
        this.flags = flags;
    }

    @Override
    public void setFormalParameters(BaseFormalParameter formalParameters) {
        this.formalParameters = formalParameters;
    }

    @Override
    public void setStatements(BaseStatement statements) {
        this.statements = statements;
    }

    @Override
    public ClassFileBodyDeclaration getBodyDeclaration() {
        return bodyDeclaration;
    }

    @Override
    public ClassFile getClassFile() {
        return classFile;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public BaseType getParameterTypes() {
        return parameterTypes;
    }

    @Override
    public Map<String, TypeArgument> getBindings() {
        return bindings;
    }

    @Override
    public Map<String, BaseType> getTypeBounds() {
        return typeBounds;
    }

    @Override
    public int getFirstLineNumber() {
        return firstLineNumber;
    }

    @Override
    public String toString() {
        return "ClassFileMethodDeclaration{" + name + " " + descriptor + ", firstLineNumber=" + firstLineNumber + "}";
    }
}
