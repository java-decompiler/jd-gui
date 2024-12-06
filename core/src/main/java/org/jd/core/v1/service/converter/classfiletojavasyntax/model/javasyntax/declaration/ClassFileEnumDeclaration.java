/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.declaration;

import org.jd.core.v1.model.javasyntax.declaration.BodyDeclaration;
import org.jd.core.v1.model.javasyntax.declaration.EnumDeclaration;
import org.jd.core.v1.model.javasyntax.expression.BaseExpression;
import org.jd.core.v1.model.javasyntax.reference.BaseAnnotationReference;
import org.jd.core.v1.model.javasyntax.type.BaseType;

import java.util.List;

public class ClassFileEnumDeclaration extends EnumDeclaration implements ClassFileTypeDeclaration {
    protected int firstLineNumber;

    public ClassFileEnumDeclaration(BaseAnnotationReference annotationReferences, int flags, String internalName, String name, BaseType interfaces, ClassFileBodyDeclaration bodyDeclaration) {
        super(annotationReferences, flags, internalName, name, interfaces, null, bodyDeclaration);
        this.firstLineNumber = bodyDeclaration==null ? 0 : bodyDeclaration.getFirstLineNumber();
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public void setConstants(List<Constant> constants) {
        this.constants = constants;
    }

    @Override
    public int getFirstLineNumber() {
        return firstLineNumber;
    }

    @Override
    public String toString() {
        return "ClassFileEnumDeclaration{" + internalTypeName + ", firstLineNumber=" + firstLineNumber + "}";
    }

    public static class ClassFileConstant extends Constant {
        protected int index;

        public ClassFileConstant(int lineNumber, String name, int index, BaseExpression arguments, BodyDeclaration bodyDeclaration) {
            super(lineNumber, name, arguments, bodyDeclaration);
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public String toString() {
            return "ClassFileConstant{" + name + " : " + index + "}";
        }
    }
}
