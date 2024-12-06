/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.statement;

import org.jd.core.v1.model.javasyntax.declaration.LocalVariableDeclaration;
import org.jd.core.v1.model.javasyntax.expression.BaseExpression;
import org.jd.core.v1.model.javasyntax.expression.Expression;

public class ForStatement implements Statement {
    protected LocalVariableDeclaration declaration;
    protected BaseExpression init;
    protected Expression condition;
    protected BaseExpression update;
    protected BaseStatement statements;

    public ForStatement(LocalVariableDeclaration declaration, Expression condition, BaseExpression update, BaseStatement statements) {
        this.declaration = declaration;
        this.condition = condition;
        this.update = update;
        this.statements = statements;
    }

    public ForStatement(BaseExpression init, Expression condition, BaseExpression update, BaseStatement statements) {
        this.init = init;
        this.condition = condition;
        this.update = update;
        this.statements = statements;
    }

    public LocalVariableDeclaration getDeclaration() {
        return declaration;
    }

    public void setDeclaration(LocalVariableDeclaration declaration) {
        this.declaration = declaration;
    }

    @Override
    public BaseExpression getInit() {
        return init;
    }

    public void setInit(BaseExpression init) {
        this.init = init;
    }

    @Override
    public Expression getCondition() {
        return condition;
    }

    public void setCondition(Expression condition) {
        this.condition = condition;
    }

    @Override
    public BaseExpression getUpdate() {
        return update;
    }

    public void setUpdate(BaseExpression update) {
        this.update = update;
    }

    @Override
    public BaseStatement getStatements() {
        return statements;
    }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "ForStatement{" + declaration + " or " + init + "; " + condition + "; " + update + "}";
    }
}
