/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.statement;

import org.jd.core.v1.model.javasyntax.statement.Statement;
import org.jd.core.v1.model.javasyntax.statement.StatementVisitor;

public class ClassFileBreakContinueStatement implements Statement {
    protected int offset;
    protected int targetOffset;
    protected boolean continueLabel;
    protected Statement statement;

    public ClassFileBreakContinueStatement(int offset, int targetOffset) {
        this.offset = offset;
        this.targetOffset = targetOffset;
        this.continueLabel = false;
        this.statement = null;
    }

    public int getOffset() {
        return offset;
    }

    public int getTargetOffset() {
        return targetOffset;
    }

    public Statement getStatement() {
        return statement;
    }

    public boolean isContinueLabel() {
        return continueLabel;
    }

    public void setContinueLabel(boolean continueLabel) {
        this.continueLabel = continueLabel;
    }

    public void setStatement(Statement statement) {
        this.statement = statement;
    }

    @Override
    public void accept(StatementVisitor visitor) {
        if (statement != null) {
            statement.accept(visitor);
        }
    }

    @Override
    public String toString() {
        if (statement == null) {
            return "ClassFileBreakContinueStatement{}";
        } else {
            return "ClassFileBreakContinueStatement{" + statement + "}";
        }
    }
}
