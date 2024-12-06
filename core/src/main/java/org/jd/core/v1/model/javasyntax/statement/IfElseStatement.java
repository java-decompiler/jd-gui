/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.statement;

import org.jd.core.v1.model.javasyntax.expression.Expression;

public class IfElseStatement extends IfStatement {
    protected BaseStatement elseStatements;

    public IfElseStatement(Expression condition, BaseStatement statement, BaseStatement elseStatements) {
        super(condition, statement);
        this.elseStatements = elseStatements;
    }

    @Override
    public BaseStatement getElseStatements() {
        return elseStatements;
    }

    @Override
    public boolean isIfElseStatement() { return true; }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }
}
