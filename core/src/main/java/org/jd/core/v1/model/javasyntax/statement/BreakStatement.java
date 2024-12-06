/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.statement;

public class BreakStatement implements Statement {
    public static final BreakStatement BREAK = new BreakStatement();

    protected String label;

    protected BreakStatement() {
        this.label = null;
    }

    public BreakStatement(String label) {
        assert label != null;

        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public boolean isBreakStatement() { return true; }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }
}
