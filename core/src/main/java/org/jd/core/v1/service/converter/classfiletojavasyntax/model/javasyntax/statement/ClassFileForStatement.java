/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.statement;

import org.jd.core.v1.model.javasyntax.expression.BaseExpression;
import org.jd.core.v1.model.javasyntax.expression.Expression;
import org.jd.core.v1.model.javasyntax.statement.BaseStatement;
import org.jd.core.v1.model.javasyntax.statement.ForStatement;

public class ClassFileForStatement extends ForStatement {
    protected int fromOffset;
    protected int toOffset;

    public ClassFileForStatement(int fromOffset, int toOffset, BaseExpression init, Expression condition, BaseExpression update, BaseStatement statements) {
        super(init, condition, update, statements);
        this.fromOffset = fromOffset;
        this.toOffset = toOffset;
    }

    public int getFromOffset() {
        return fromOffset;
    }

    public int getToOffset() {
        return toOffset;
    }

    @Override
    public boolean isForStatement() { return true; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ClassFileForStatement{");

        if (declaration != null) {
            sb.append(declaration);
        } else {
            sb.append(init);
        }

        return sb.append("; ").append(condition).append("; ").append(update).append("}").toString();
    }
}
