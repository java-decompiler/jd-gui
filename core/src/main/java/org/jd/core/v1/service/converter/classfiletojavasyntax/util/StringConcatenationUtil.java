/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.util;

import org.jd.core.v1.model.javasyntax.expression.*;
import org.jd.core.v1.model.javasyntax.type.ObjectType;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.expression.ClassFileMethodInvocationExpression;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.expression.ClassFileNewExpression;
import org.jd.core.v1.util.DefaultList;

import java.util.Iterator;
import java.util.StringTokenizer;

public class StringConcatenationUtil {

    public static Expression create(Expression expression, int lineNumber, String typeName) {
        if (expression.isMethodInvocationExpression()) {
            MethodInvocationExpression mie = (MethodInvocationExpression) expression;

            if ((mie.getParameters() != null) && !mie.getParameters().isList() && "append".equals(mie.getName())) {
                Expression concatenatedStringExpression = mie.getParameters().getFirst();
                Expression expr = mie.getExpression();
                boolean firstParameterHaveGenericType = false;

                while (expr.isMethodInvocationExpression()) {
                    mie = (MethodInvocationExpression) expr;

                    if ((mie.getParameters() == null) || mie.getParameters().isList() || !"append".equals(mie.getName())) {
                        break;
                    }

                    firstParameterHaveGenericType = mie.getParameters().getFirst().getType().isGenericType();
                    concatenatedStringExpression = new BinaryOperatorExpression(mie.getLineNumber(), ObjectType.TYPE_STRING, (Expression) mie.getParameters(), "+", concatenatedStringExpression, 4);
                    expr = mie.getExpression();
                }

                if (expr.isNewExpression()) {
                    String internalTypeName = expr.getType().getDescriptor();

                    if ("Ljava/lang/StringBuilder;".equals(internalTypeName) || "Ljava/lang/StringBuffer;".equals(internalTypeName)) {
                        if (expr.getParameters() == null) {
                            if (!firstParameterHaveGenericType) {
                                return concatenatedStringExpression;
                            }
                        } else if (!expr.getParameters().isList()) {
                            expr = expr.getParameters().getFirst();

                            if (ObjectType.TYPE_STRING.equals(expr.getType())) {
                                return new BinaryOperatorExpression(expr.getLineNumber(), ObjectType.TYPE_STRING, expr, "+", concatenatedStringExpression, 4);
                            }
                        }
                    }
                }
            }
        }

        return new ClassFileMethodInvocationExpression(lineNumber, null, ObjectType.TYPE_STRING, expression, typeName, "toString", "()Ljava/lang/String;", null, null);
    }

    public static Expression create(String recipe, BaseExpression parameters) {
        StringTokenizer st = new StringTokenizer(recipe, "\u0001", true);

        if (st.hasMoreTokens()) {
            String token = st.nextToken();
            Expression expression = token.equals("\u0001") ? createFirstStringConcatenationItem(parameters.getFirst()) : new StringConstantExpression(token);

            if (parameters.isList()) {
                DefaultList<Expression> list = parameters.getList();
                int index = 0;

                while (st.hasMoreTokens()) {
                    token = st.nextToken();
                    Expression e = token.equals("\u0001") ? list.get(index++) : new StringConstantExpression(token);
                    expression = new BinaryOperatorExpression(expression.getLineNumber(), ObjectType.TYPE_STRING, expression, "+", e, 6);
                }
            } else {
                while (st.hasMoreTokens()) {
                    token = st.nextToken();
                    Expression e = token.equals("\u0001") ? parameters.getFirst() : new StringConstantExpression(token);
                    expression = new BinaryOperatorExpression(expression.getLineNumber(), ObjectType.TYPE_STRING, expression, "+", e, 6);
                }
            }

            return expression;
        } else {
            return StringConstantExpression.EMPTY_STRING;
        }
    }

    public static Expression create(BaseExpression parameters) {
        switch (parameters.size()) {
            case 0:
                return StringConstantExpression.EMPTY_STRING;
            case 1:
                return createFirstStringConcatenationItem(parameters.getFirst());
            default:
                Iterator<Expression> iterator = parameters.iterator();
                Expression expression = createFirstStringConcatenationItem(iterator.next());

                while (iterator.hasNext()) {
                    expression = new BinaryOperatorExpression(expression.getLineNumber(), ObjectType.TYPE_STRING, expression, "+", iterator.next(), 6);
                }

                return expression;
        }
    }

    private static Expression createFirstStringConcatenationItem(Expression expression) {
        if (!expression.getType().equals(ObjectType.TYPE_STRING)) {
            expression = new BinaryOperatorExpression(expression.getLineNumber(), ObjectType.TYPE_STRING, StringConstantExpression.EMPTY_STRING, "+", expression, 6);
        }

        return expression;
    }
}