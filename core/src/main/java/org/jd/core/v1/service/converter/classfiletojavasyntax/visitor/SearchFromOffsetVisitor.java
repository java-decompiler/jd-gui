/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.visitor;

import org.jd.core.v1.model.javasyntax.AbstractJavaSyntaxVisitor;
import org.jd.core.v1.model.javasyntax.expression.*;
import org.jd.core.v1.model.javasyntax.type.*;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.expression.ClassFileLocalVariableReferenceExpression;

public class SearchFromOffsetVisitor extends AbstractJavaSyntaxVisitor {
    protected int offset;

    public SearchFromOffsetVisitor() {
        offset = Integer.MAX_VALUE;
    }

    public void init() {
        offset = Integer.MAX_VALUE;
    }

    public int getOffset() {
        return this.offset;
    }

    @Override
    public void visit(LocalVariableReferenceExpression expression) {
        int offset = ((ClassFileLocalVariableReferenceExpression) expression).getOffset();

        if (this.offset > offset) {
            this.offset = offset;
        }
    }

    @Override public void visit(IntegerConstantExpression expression) {}

    @Override public void visit(TypeArguments arguments) {}
    @Override public void visit(DiamondTypeArgument argument) {}
    @Override public void visit(WildcardExtendsTypeArgument argument) {}
    @Override public void visit(WildcardSuperTypeArgument argument) {}
    @Override public void visit(WildcardTypeArgument argument) {}
    @Override public void visit(PrimitiveType type) {}
    @Override public void visit(ObjectType type) {}
    @Override public void visit(InnerObjectType type) {}
    @Override public void visit(GenericType type) {}
}
