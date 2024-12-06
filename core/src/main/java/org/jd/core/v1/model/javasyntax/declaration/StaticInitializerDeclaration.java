/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.declaration;

import org.jd.core.v1.model.javasyntax.statement.BaseStatement;

public class StaticInitializerDeclaration implements MemberDeclaration {
    protected String descriptor;
    protected BaseStatement statements;

    public StaticInitializerDeclaration(String descriptor, BaseStatement statements) {
        this.descriptor = descriptor;
        this.statements = statements;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public BaseStatement getStatements() {
        return statements;
    }

    @Override
    public void accept(DeclarationVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "StaticInitializerDeclaration{}";
    }
}
