/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.statement;

public abstract class AbstractNopStatementVisitor implements StatementVisitor {
    @Override public void visit(AssertStatement statement) {}
    @Override public void visit(BreakStatement statement) {}
    @Override public void visit(ByteCodeStatement statement) {}
    @Override public void visit(CommentStatement statement) {}
    @Override public void visit(ContinueStatement statement) {}
    @Override public void visit(DoWhileStatement statement) {}
    @Override public void visit(ExpressionStatement statement) {}
    @Override public void visit(ForEachStatement statement) {}
    @Override public void visit(ForStatement statement) {}
    @Override public void visit(IfStatement statement) {}
    @Override public void visit(IfElseStatement statement) {}
    @Override public void visit(LabelStatement statement) {}
    @Override public void visit(LambdaExpressionStatement statement) {}
    @Override public void visit(LocalVariableDeclarationStatement statement) {}
    @Override public void visit(NoStatement statement) {}
    @Override public void visit(ReturnExpressionStatement statement) {}
    @Override public void visit(ReturnStatement statement) {}
    @Override public void visit(Statements statement) {}
    @Override public void visit(SwitchStatement statement) {}
    @Override public void visit(SwitchStatement.DefaultLabel statement) {}
    @Override public void visit(SwitchStatement.ExpressionLabel statement) {}
    @Override public void visit(SwitchStatement.LabelBlock statement) {}
    @Override public void visit(SwitchStatement.MultiLabelsBlock statement) {}
    @Override public void visit(SynchronizedStatement statement) {}
    @Override public void visit(ThrowStatement statement) {}
    @Override public void visit(TryStatement statement) {}
    @Override public void visit(TryStatement.CatchClause statement) {}
    @Override public void visit(TryStatement.Resource statement) {}
    @Override public void visit(TypeDeclarationStatement statement) {}
    @Override public void visit(WhileStatement statement) {}
}
