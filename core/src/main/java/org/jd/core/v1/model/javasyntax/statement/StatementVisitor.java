/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.statement;

public interface StatementVisitor {
    void visit(AssertStatement statement);
    void visit(BreakStatement statement);
    void visit(ByteCodeStatement statement);
    void visit(CommentStatement statement);
    void visit(ContinueStatement statement);
    void visit(DoWhileStatement statement);
    void visit(ExpressionStatement statement);
    void visit(ForEachStatement statement);
    void visit(ForStatement statement);
    void visit(IfStatement statement);
    void visit(IfElseStatement statement);
    void visit(LabelStatement statement);
    void visit(LambdaExpressionStatement statement);
    void visit(LocalVariableDeclarationStatement statement);
    void visit(NoStatement statement);
    void visit(ReturnExpressionStatement statement);
    void visit(ReturnStatement statement);
    void visit(Statements statement);
    void visit(SwitchStatement statement);
    void visit(SwitchStatement.DefaultLabel statement);
    void visit(SwitchStatement.ExpressionLabel statement);
    void visit(SwitchStatement.LabelBlock statement);
    void visit(SwitchStatement.MultiLabelsBlock statement);
    void visit(SynchronizedStatement statement);
    void visit(ThrowStatement statement);
    void visit(TryStatement statement);
    void visit(TryStatement.Resource statement);
    void visit(TryStatement.CatchClause statement);
    void visit(TypeDeclarationStatement statement);
    void visit(WhileStatement statement);
}
