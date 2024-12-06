/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.statement;

import org.jd.core.v1.model.javasyntax.declaration.BaseLocalVariableDeclarator;
import org.jd.core.v1.model.javasyntax.declaration.LocalVariableDeclaration;
import org.jd.core.v1.model.javasyntax.type.Type;

public class LocalVariableDeclarationStatement extends LocalVariableDeclaration implements Statement {

    public LocalVariableDeclarationStatement(Type type, BaseLocalVariableDeclarator localVariableDeclarators) {
        super(type, localVariableDeclarators);
    }

    @Override
    public boolean isLocalVariableDeclarationStatement() { return true; }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "LocalVariableDeclarationStatement{" + type + " " + localVariableDeclarators + "}";
    }
}
