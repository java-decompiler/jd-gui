/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.visitor;

import org.jd.core.v1.model.javasyntax.AbstractJavaSyntaxVisitor;
import org.jd.core.v1.model.javasyntax.expression.BinaryOperatorExpression;
import org.jd.core.v1.model.javasyntax.statement.*;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.expression.ClassFileLocalVariableReferenceExpression;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.localvariable.AbstractLocalVariable;

import java.util.HashSet;

public class SearchUndeclaredLocalVariableVisitor extends AbstractJavaSyntaxVisitor {
    protected HashSet<AbstractLocalVariable> variables = new HashSet<>();

    public void init() {
        variables.clear();
    }

    public HashSet<AbstractLocalVariable> getVariables() {
        return variables;
    }

    @Override
    public void visit(BinaryOperatorExpression expression) {
        if (expression.getLeftExpression().isLocalVariableReferenceExpression() && (expression.getOperator().equals("="))) {
            AbstractLocalVariable lv = ((ClassFileLocalVariableReferenceExpression)expression.getLeftExpression()).getLocalVariable();

            if (!lv.isDeclared()) {
                variables.add(lv);
            }
        }

        expression.getLeftExpression().accept(this);
        expression.getRightExpression().accept(this);
    }

    @Override
    public void visit(DoWhileStatement statement) {
        safeAccept(statement.getCondition());
    }

    @Override
    public void visit(ForEachStatement statement) {
        statement.getExpression().accept(this);
    }

    @Override
    public void visit(ForStatement statement) {
        safeAccept(statement.getDeclaration());
        safeAccept(statement.getInit());
        safeAccept(statement.getCondition());
        safeAccept(statement.getUpdate());
    }

    @Override
    public void visit(IfStatement statement) {
        statement.getCondition().accept(this);
    }

    @Override
    public void visit(IfElseStatement statement) {
        statement.getCondition().accept(this);
    }

    @Override
    public void visit(LambdaExpressionStatement statement) {
        statement.getExpression().accept(this);
    }

    @Override
    public void visit(SwitchStatement statement) {
        statement.getCondition().accept(this);
    }

    @Override
    public void visit(SynchronizedStatement statement) {
        statement.getMonitor().accept(this);
    }

    @Override
    public void visit(TryStatement statement) {}

    @Override
    public void visit(WhileStatement statement) {
        statement.getCondition().accept(this);
    }
}
