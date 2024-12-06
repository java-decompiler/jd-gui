/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.statement;

public class LabelStatement implements Statement {
    protected String label;
    protected Statement statement;

    public LabelStatement(String label, Statement statement) {
        this.label = label;
        this.statement = statement;
    }

    public String getLabel() {
        return label;
    }

    public Statement getStatement() {
        return statement;
    }

    @Override
    public boolean isLabelStatement() { return true; }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "LabelStatement{" + label + ": " + statement + "}";
    }
}
