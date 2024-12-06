/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.declaration;

import org.jd.core.v1.model.javasyntax.expression.Expression;

import static org.jd.core.v1.model.javasyntax.expression.NoExpression.NO_EXPRESSION;

public interface VariableInitializer extends Declaration {
    int getLineNumber();

    default boolean isExpressionVariableInitializer() { return false; }

    default Expression getExpression() { return NO_EXPRESSION; }
}
