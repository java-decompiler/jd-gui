/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.expression;

import org.jd.core.v1.model.javasyntax.statement.BaseStatement;
import org.jd.core.v1.model.javasyntax.type.Type;

import java.util.List;

public class LambdaIdentifiersExpression extends AbstractLambdaExpression {
    protected Type returnedType;
    protected List<String> parameterNames;

    public LambdaIdentifiersExpression(Type type, Type returnedType, List<String> parameterNames, BaseStatement statements) {
        super(type, statements);
        this.returnedType = returnedType;
        this.parameterNames = parameterNames;
    }

    public LambdaIdentifiersExpression(int lineNumber, Type type, Type returnedType, List<String> parameterNames, BaseStatement statements) {
        super(lineNumber, type, statements);
        this.returnedType = returnedType;
        this.parameterNames = parameterNames;
    }

    public Type getReturnedType() {
        return returnedType;
    }

    public List<String> getParameterNames() {
        return parameterNames;
    }

    @Override
    public void accept(ExpressionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "LambdaIdentifiersExpression{" + parameterNames + " -> " + statements + "}";
    }
}
