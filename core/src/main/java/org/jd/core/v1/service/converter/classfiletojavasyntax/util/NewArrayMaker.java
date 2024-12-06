/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.util;

import org.jd.core.v1.model.javasyntax.declaration.ArrayVariableInitializer;
import org.jd.core.v1.model.javasyntax.declaration.ExpressionVariableInitializer;
import org.jd.core.v1.model.javasyntax.declaration.VariableInitializer;
import org.jd.core.v1.model.javasyntax.expression.*;
import org.jd.core.v1.model.javasyntax.statement.Statement;
import org.jd.core.v1.model.javasyntax.statement.Statements;
import org.jd.core.v1.model.javasyntax.type.PrimitiveType;
import org.jd.core.v1.model.javasyntax.type.Type;

import java.util.Collections;
import java.util.ListIterator;

public class NewArrayMaker {
    protected static final ArrayVariableInitializer EMPTY_ARRAY = new ArrayVariableInitializer(PrimitiveType.TYPE_VOID);

    @SuppressWarnings("unchecked")
    public static Expression make(Statements statements, Expression newArray) {
        if (! statements.isEmpty()) {
            Expression ae = statements.getLast().getExpression().getLeftExpression();

            if ((ae.getExpression() == newArray) && ae.getIndex().isIntegerConstantExpression()) {
                return new NewInitializedArray(newArray.getLineNumber(), newArray.getType(), createVariableInitializer(statements.listIterator(statements.size()), newArray));
            }
        }

        return newArray;
    }

    protected static ArrayVariableInitializer createVariableInitializer(ListIterator<Statement> li, Expression newArray) {
        Statement statement = li.previous();

        li.remove();

        Type type = newArray.getType();
        Expression boe = statement.getExpression();
        ArrayVariableInitializer array = new ArrayVariableInitializer(type.createType(type.getDimension()-1));
        int index = boe.getLeftExpression().getIndex().getIntegerValue();

        array.add(new ExpressionVariableInitializer(boe.getRightExpression()));

        while (li.hasPrevious()) {
            boe = li.previous().getExpression();

            if (boe.getLeftExpression().isArrayExpression()) {
                Expression ae = boe.getLeftExpression();

                if (ae.getIndex().isIntegerConstantExpression()) {
                    if (ae.getExpression() == newArray) {
                        index = ae.getIndex().getIntegerValue();
                        array.add(new ExpressionVariableInitializer(boe.getRightExpression()));
                        li.remove();
                        continue;
                    } else if (ae.getExpression().isNewArray()) {
                        Expression lastE = array.getLast().getExpression();

                        if (lastE.isNewArray() && (ae.getExpression() == lastE)) {
                            array.removeLast();
                            li.next();
                            array.add(createVariableInitializer(li, lastE));
                            continue;
                        }
                    }
                }
            }

            li.next();
            break;
        }

        // Replace 'new XXX[0][]' with '{}'
        ListIterator<VariableInitializer> vii = array.listIterator();

        while (vii.hasNext()) {
            Expression expression = vii.next().getExpression();

            if (expression.isNewArray()) {
                BaseExpression del = expression.getDimensionExpressionList();

                if (del.isIntegerConstantExpression() && (del.getIntegerValue() == 0)) {
                    Type t = expression.getType();

                    if ((type.getDimension() == t.getDimension() + 1) && (type.getDescriptor().length() == t.getDescriptor().length() + 1) && type.getDescriptor().endsWith(t.getDescriptor())) {
                        vii.set(EMPTY_ARRAY);
                    }
                }
            }
        }

        // Padding
        if (index > 0) {
            ExpressionVariableInitializer evi;

            type = type.createType(type.getDimension()-1);

            if ((type.getDimension() == 0) && type.isPrimitiveType()) {
                evi = new ExpressionVariableInitializer(new IntegerConstantExpression(type, 0));
            } else {
                evi = new ExpressionVariableInitializer(new NullExpression(type));
            }

            while (index-- > 0) {
                array.add(evi);
            }
        }

        Collections.reverse(array);

        return array;
    }
}
