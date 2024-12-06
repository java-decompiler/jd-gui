/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.statement;

import org.jd.core.v1.model.javasyntax.expression.Expression;
import org.jd.core.v1.model.javasyntax.statement.CommentStatement;

public class ClassFileMonitorExitStatement extends CommentStatement {
    protected Expression monitor;

    public ClassFileMonitorExitStatement(Expression monitor) {
        super("/* monitor exit " + monitor + " */");
        this.monitor = monitor;
    }

    @Override
    public Expression getMonitor() {
        return monitor;
    }

    @Override
    public boolean isMonitorExitStatement() { return true; }

    @Override
    public String toString() {
        return "ClassFileMonitorExitStatement{" + monitor + "}";
    }
}
