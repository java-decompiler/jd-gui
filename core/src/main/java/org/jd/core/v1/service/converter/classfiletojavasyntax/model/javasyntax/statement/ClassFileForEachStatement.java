/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.statement;

import org.jd.core.v1.model.javasyntax.expression.Expression;
import org.jd.core.v1.model.javasyntax.statement.BaseStatement;
import org.jd.core.v1.model.javasyntax.statement.ForEachStatement;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.localvariable.AbstractLocalVariable;

public class ClassFileForEachStatement extends ForEachStatement {
    protected AbstractLocalVariable localVariable;

    public ClassFileForEachStatement(AbstractLocalVariable localVariable, Expression expression, BaseStatement statements) {
        super(localVariable.getType(), null, expression, statements);
        this.localVariable = localVariable;
    }

    public String getName() {
        return localVariable.getName();
    }

    @Override
    public String toString() {
        return "ClassFileForEachStatement{" + type + " " + localVariable.getName() + " : " + expression + "}";
    }
}
