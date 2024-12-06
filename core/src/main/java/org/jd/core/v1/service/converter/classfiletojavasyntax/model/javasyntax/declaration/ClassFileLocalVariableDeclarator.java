/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.declaration;

import org.jd.core.v1.model.javasyntax.declaration.LocalVariableDeclarator;
import org.jd.core.v1.model.javasyntax.declaration.VariableInitializer;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.localvariable.AbstractLocalVariable;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.localvariable.LocalVariableReference;

public class ClassFileLocalVariableDeclarator extends LocalVariableDeclarator implements LocalVariableReference {
    protected AbstractLocalVariable localVariable;

    public ClassFileLocalVariableDeclarator(AbstractLocalVariable localVariable) {
        super(null);
        this.localVariable = localVariable;
    }

    public ClassFileLocalVariableDeclarator(int lineNumber, AbstractLocalVariable localVariable, VariableInitializer variableInitializer) {
        super(lineNumber, null, variableInitializer);
        this.localVariable = localVariable;
    }

    public String getName() {
        return localVariable.getName();
    }

    public void setName(String name) {
        localVariable.setName(name);
    }

    @Override
    public AbstractLocalVariable getLocalVariable() {
        return localVariable;
    }

    @Override
    public void setLocalVariable(AbstractLocalVariable localVariable) {
        this.localVariable = localVariable;
    }

    @Override
    public String toString() {
        return "ClassFileLocalVariableDeclarator{name=" + localVariable.getName() + ", dimension" + dimension + ", variableInitializer=" + variableInitializer + "}";
    }
}
