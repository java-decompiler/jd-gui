/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.statement;

import org.jd.core.v1.model.javasyntax.declaration.TypeDeclaration;

public class TypeDeclarationStatement implements Statement {
    protected TypeDeclaration typeDeclaration;

    public TypeDeclarationStatement(TypeDeclaration typeDeclaration) {
        this.typeDeclaration = typeDeclaration;
    }

    public TypeDeclaration getTypeDeclaration() {
        return typeDeclaration;
    }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }
}
