/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.statement;

import org.jd.core.v1.model.javasyntax.expression.Expression;
import org.jd.core.v1.model.javasyntax.type.Type;

public class ForEachStatement implements Statement {
    protected Type type;
    protected String name;
    protected Expression expression;
    protected BaseStatement statements;

    public ForEachStatement(Type type, String name, Expression expression, BaseStatement statements) {
        this.type = type;
        this.name = name;
        this.expression = expression;
        this.statements = statements;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    @Override
    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
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
