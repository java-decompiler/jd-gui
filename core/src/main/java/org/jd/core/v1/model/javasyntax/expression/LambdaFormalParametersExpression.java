/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.expression;

import org.jd.core.v1.model.javasyntax.declaration.BaseFormalParameter;
import org.jd.core.v1.model.javasyntax.statement.BaseStatement;
import org.jd.core.v1.model.javasyntax.type.Type;

public class LambdaFormalParametersExpression extends AbstractLambdaExpression {
    protected BaseFormalParameter formalParameters;

    public LambdaFormalParametersExpression(Type type, BaseFormalParameter formalParameters, BaseStatement statements) {
        super(type, statements);
        this.formalParameters = formalParameters;
    }

    public LambdaFormalParametersExpression(int lineNumber, Type type, BaseFormalParameter formalParameters, BaseStatement statements) {
        super(lineNumber, type, statements);
        this.formalParameters = formalParameters;
    }

    public BaseFormalParameter getFormalParameters() {
        return formalParameters;
    }

    public void setParameters(BaseFormalParameter formalParameters) {
        this.formalParameters = formalParameters;
    }

    @Override
    public void accept(ExpressionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "LambdaFormalParametersExpression{" + formalParameters + " -> " + statements + "}";
    }
}
