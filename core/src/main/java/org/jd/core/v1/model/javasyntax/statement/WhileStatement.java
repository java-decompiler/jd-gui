/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.statement;

import org.jd.core.v1.model.javasyntax.expression.Expression;

public class WhileStatement implements Statement {
    protected Expression condition;
    protected BaseStatement statements;

    public WhileStatement(Expression condition, BaseStatement statements) {
        this.condition = condition;
        this.statements = statements;
    }

    @Override
    public Expression getCondition() {
        return condition;
    }

    public void setCondition(Expression condition) {
        this.condition = condition;
    }

    @Override
    public BaseStatement getStatements() {
        return statements;
    }

    @Override
    public boolean isWhileStatement() { return true; }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }
}
