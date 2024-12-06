/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.statement;

import org.jd.core.v1.model.javasyntax.expression.Expression;

public class SynchronizedStatement implements Statement {
    protected Expression monitor;
    protected BaseStatement statements;

    public SynchronizedStatement(Expression monitor, BaseStatement statements) {
        this.monitor = monitor;
        this.statements = statements;
    }

    @Override
    public Expression getMonitor() {
        return monitor;
    }

    public void setMonitor(Expression monitor) {
        this.monitor = monitor;
    }

    @Override
    public BaseStatement getStatements() {
        return statements;
    }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }
}
