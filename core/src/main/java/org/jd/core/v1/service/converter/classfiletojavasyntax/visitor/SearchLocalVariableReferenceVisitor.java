/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.visitor;

import org.jd.core.v1.model.javasyntax.AbstractJavaSyntaxVisitor;
import org.jd.core.v1.model.javasyntax.expression.LocalVariableReferenceExpression;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.expression.ClassFileLocalVariableReferenceExpression;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.localvariable.AbstractLocalVariable;

public class SearchLocalVariableReferenceVisitor extends AbstractJavaSyntaxVisitor {
    protected int index;
    protected boolean found;

    public void init(int index) {
        this.index = index;
        this.found = false;
    }

    public boolean containsReference() {
        return found;
    }

    @Override
    public void visit(LocalVariableReferenceExpression expression) {
        if (index < 0) {
            found = true;
        } else {
            ClassFileLocalVariableReferenceExpression referenceExpression = (ClassFileLocalVariableReferenceExpression) expression;
            found |= referenceExpression.getLocalVariable().getIndex() == index;
        }
    }
}
