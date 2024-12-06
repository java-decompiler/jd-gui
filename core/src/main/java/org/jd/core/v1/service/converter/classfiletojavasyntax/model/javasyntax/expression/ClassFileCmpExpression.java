/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.expression;

import org.jd.core.v1.model.javasyntax.expression.BinaryOperatorExpression;
import org.jd.core.v1.model.javasyntax.expression.Expression;
import org.jd.core.v1.model.javasyntax.type.PrimitiveType;

public class ClassFileCmpExpression extends BinaryOperatorExpression {
    public ClassFileCmpExpression(int lineNumber, Expression leftExpression, Expression rightExpression) {
        super(lineNumber, PrimitiveType.TYPE_INT, leftExpression, "cmp", rightExpression, 7);
    }
}
