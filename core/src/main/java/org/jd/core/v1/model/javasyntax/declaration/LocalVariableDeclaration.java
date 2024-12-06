/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.declaration;

import org.jd.core.v1.model.javasyntax.type.Type;

public class LocalVariableDeclaration implements Declaration {
    protected boolean fina1 = false;
    protected Type type;
    protected BaseLocalVariableDeclarator localVariableDeclarators;

    public LocalVariableDeclaration(Type type, BaseLocalVariableDeclarator localVariableDeclarators) {
        this.type = type;
        this.localVariableDeclarators = localVariableDeclarators;
    }

    public boolean isFinal() {
        return fina1;
    }

    public void setFinal(boolean fina1) {
        this.fina1 = fina1;
    }

    public Type getType() {
        return type;
    }

    public BaseLocalVariableDeclarator getLocalVariableDeclarators() {
        return localVariableDeclarators;
    }

    public void setLocalVariableDeclarators(BaseLocalVariableDeclarator localVariableDeclarators) {
        this.localVariableDeclarators = localVariableDeclarators;
    }

    @Override
    public void accept(DeclarationVisitor visitor) {
        visitor.visit(this);
    }
}
