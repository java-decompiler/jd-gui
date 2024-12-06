/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.statement;

import org.jd.core.v1.model.javasyntax.statement.BaseStatement;
import org.jd.core.v1.model.javasyntax.statement.TryStatement;
import org.jd.core.v1.model.javasyntax.type.ObjectType;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.localvariable.AbstractLocalVariable;
import org.jd.core.v1.util.DefaultList;

public class ClassFileTryStatement extends TryStatement {
    protected boolean jsr;
    protected boolean eclipse;

    public ClassFileTryStatement(BaseStatement tryStatements, DefaultList<TryStatement.CatchClause> catchClauses, BaseStatement finallyStatements, boolean jsr, boolean eclipse) {
        super(tryStatements, catchClauses, finallyStatements);
        this.jsr = jsr;
        this.eclipse = eclipse;
    }

    public ClassFileTryStatement(DefaultList<Resource> resources, BaseStatement tryStatements, DefaultList<TryStatement.CatchClause> catchClauses, BaseStatement finallyStatements, boolean jsr, boolean eclipse) {
        super(resources, tryStatements, catchClauses, finallyStatements);
        this.jsr = jsr;
        this.eclipse = eclipse;
    }

    public void addResources(DefaultList<Resource> resources) {
        if (resources != null) {
            if (this.resources == null) {
                this.resources = resources;
            } else {
                this.resources.addAll(resources);
            }
        }
    }

    public boolean isJsr() {
        return jsr;
    }

    public boolean isEclipse() {
        return eclipse;
    }

    public static class CatchClause extends TryStatement.CatchClause {
        protected AbstractLocalVariable localVariable;

        public CatchClause(int lineNumber, ObjectType type, AbstractLocalVariable localVariable, BaseStatement statements) {
            super(lineNumber, type, null, statements);
            this.localVariable = localVariable;
        }

        public AbstractLocalVariable getLocalVariable() {
            return localVariable;
        }

        @Override
        public String getName() {
            return localVariable.getName();
        }
    }
}
