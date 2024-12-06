/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.expression;

import org.jd.core.v1.model.javasyntax.type.BaseTypeArgument;
import org.jd.core.v1.model.javasyntax.type.Type;

public class MethodInvocationExpression extends MethodReferenceExpression {
    protected BaseTypeArgument nonWildcardTypeArguments;
    protected BaseExpression parameters;

    public MethodInvocationExpression(Type type, Expression expression, String internalTypeName, String name, String descriptor) {
        super(type, expression, internalTypeName, name, descriptor);
    }

    public MethodInvocationExpression(int lineNumber, Type type, Expression expression, String internalTypeName, String name, String descriptor) {
        super(lineNumber, type, expression, internalTypeName, name, descriptor);
    }

    public MethodInvocationExpression(Type type, Expression expression, String internalTypeName, String name, String descriptor, BaseExpression parameters) {
        super(type, expression, internalTypeName, name, descriptor);
        this.parameters = parameters;
    }

    public MethodInvocationExpression(int lineNumber, Type type, Expression expression, String internalTypeName, String name, String descriptor, BaseExpression parameters) {
        super(lineNumber, type, expression, internalTypeName, name, descriptor);
        this.parameters = parameters;
    }

    public BaseTypeArgument getNonWildcardTypeArguments() {
        return nonWildcardTypeArguments;
    }

    public void setNonWildcardTypeArguments(BaseTypeArgument nonWildcardTypeArguments) {
        this.nonWildcardTypeArguments = nonWildcardTypeArguments;
    }

    @Override
    public BaseExpression getParameters() {
        return parameters;
    }

    public void setParameters(BaseExpression parameters) {
        this.parameters = parameters;
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public boolean isMethodInvocationExpression() { return true; }

    @Override
    public void accept(ExpressionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "MethodInvocationExpression{call " + expression + " . " + name + "(" + descriptor + ")}";
    }
}
