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
import org.jd.core.v1.model.javasyntax.declaration.StaticInitializerDeclaration;
import org.jd.core.v1.model.javasyntax.statement.BaseStatement;
import org.jd.core.v1.model.javasyntax.type.BaseType;
import org.jd.core.v1.model.javasyntax.type.BaseTypeParameter;
import org.jd.core.v1.model.javasyntax.type.Type;
import org.jd.core.v1.model.javasyntax.type.TypeArgument;

import java.util.Map;

public class ClassFileStaticInitializerDeclaration extends StaticInitializerDeclaration implements ClassFileConstructorOrMethodDeclaration {
    protected ClassFileBodyDeclaration bodyDeclaration;
    protected ClassFile classFile;
    protected Method method;
    protected Map<String, TypeArgument> bindings;
    protected Map<String, BaseType> typeBounds;
    protected int firstLineNumber;

    public ClassFileStaticInitializerDeclaration(
            ClassFileBodyDeclaration bodyDeclaration, ClassFile classFile, Method method, Map<String, TypeArgument> bindings,
            Map<String, BaseType> typeBounds, int firstLineNumber) {
        super(method.getDescriptor(), null);
        this.bodyDeclaration = bodyDeclaration;
        this.classFile = classFile;
        this.method = method;
        this.bindings = bindings;
        this.typeBounds = typeBounds;
        this.firstLineNumber = firstLineNumber;
    }

    public ClassFileStaticInitializerDeclaration(
            ClassFileBodyDeclaration bodyDeclaration, ClassFile classFile, Method method, Map<String, TypeArgument> bindings,
            Map<String, BaseType> typeBounds, int firstLineNumber, BaseStatement statements) {
        super(method.getDescriptor(), statements);
        this.bodyDeclaration = bodyDeclaration;
        this.classFile = classFile;
        this.method = method;
        this.bindings = bindings;
        this.typeBounds = typeBounds;
        this.firstLineNumber = firstLineNumber;
    }

    @Override
    public int getFlags() { return 0; }

    @Override
    public void setFlags(int flags) {}

    @Override
    public void setFormalParameters(BaseFormalParameter formalParameters) {}

    @Override
    public void setStatements(BaseStatement statements) {
        this.statements = statements;
    }

    @Override
    public ClassFile getClassFile() {
        return classFile;
    }

    @Override
    public Method getMethod() { return method; }

    @Override
    public BaseTypeParameter getTypeParameters() {
        return null;
    }

    @Override
    public BaseType getParameterTypes() {
        return null;
    }

    @Override
    public Type getReturnedType() {
        return null;
    }

    @Override
    public ClassFileBodyDeclaration getBodyDeclaration() {
        return bodyDeclaration;
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

    public void setFirstLineNumber(int firstLineNumber) {
        this.firstLineNumber = firstLineNumber;
    }

    @Override
    public String toString() {
        return "ClassFileConstructorDeclaration{" + descriptor + ", firstLineNumber=" + firstLineNumber + "}";
    }
}
