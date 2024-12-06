/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.visitor;

import org.jd.core.v1.model.javasyntax.AbstractJavaSyntaxVisitor;
import org.jd.core.v1.model.javasyntax.statement.*;

public class RemoveLastContinueStatementVisitor extends AbstractJavaSyntaxVisitor {
    @Override
    public void visit(Statements list) {
        if (! list.isEmpty()) {
            Statement last = (Statement)list.getLast();

            if (last.getClass() == ContinueStatement.class) {
                list.removeLast();
                visit(list);
            } else {
                last.accept(this);
            }
        }
    }

    @Override public void visit(IfElseStatement statement) {
        safeAccept(statement.getStatements());
        statement.getElseStatements().accept(this);
    }

    @Override public void visit(TryStatement statement) {
        statement.getTryStatements().accept(this);
        safeAcceptListStatement(statement.getCatchClauses());
        safeAccept(statement.getFinallyStatements());
    }

    @Override public void visit(SwitchStatement statement) { acceptListStatement(statement.getBlocks()); }
    @Override public void visit(SwitchStatement.LabelBlock statement) { statement.getStatements().accept(this); }
    @Override public void visit(SwitchStatement.MultiLabelsBlock statement) { statement.getStatements().accept(this); }
    @Override public void visit(IfStatement statement) { safeAccept(statement.getStatements()); }
    @Override public void visit(SynchronizedStatement statement) { safeAccept(statement.getStatements()); }
    @Override public void visit(TryStatement.CatchClause statement) { safeAccept(statement.getStatements()); }

    @Override public void visit(DoWhileStatement statement) {}
    @Override public void visit(ForEachStatement statement) {}
    @Override public void visit(ForStatement statement) {}
    @Override public void visit(WhileStatement statement) {}
    @Override public void visit(AssertStatement statement) {}
    @Override public void visit(BreakStatement statement) {}
    @Override public void visit(ByteCodeStatement statement) {}
    @Override public void visit(CommentStatement statement) {}
    @Override public void visit(ContinueStatement statement) {}
    @Override public void visit(ExpressionStatement statement) {}
    @Override public void visit(LabelStatement statement) {}
    @Override public void visit(LambdaExpressionStatement statement) {}
    @Override public void visit(LocalVariableDeclarationStatement statement) {}
    @Override public void visit(ReturnExpressionStatement statement) {}
    @Override public void visit(ReturnStatement statement) {}
    @Override public void visit(SwitchStatement.ExpressionLabel statement) {}
    @Override public void visit(ThrowStatement statement) {}
    @Override public void visit(TypeDeclarationStatement statement) {}
    @Override public void visit(TryStatement.Resource statement) {}
}
