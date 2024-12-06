/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.visitor;

import org.jd.core.v1.model.javasyntax.expression.BooleanExpression;
import org.jd.core.v1.model.javasyntax.statement.*;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.statement.ClassFileTryStatement;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.LocalVariableMaker;
import org.jd.core.v1.util.DefaultList;

import java.util.List;

public class RemoveFinallyStatementsVisitor implements StatementVisitor {
    protected DeclaredSyntheticLocalVariableVisitor declaredSyntheticLocalVariableVisitor = new DeclaredSyntheticLocalVariableVisitor();
    protected LocalVariableMaker localVariableMaker;
    protected int statementCountInFinally;
    protected int statementCountToRemove;
    protected boolean lastFinallyStatementIsATryStatement;

    public RemoveFinallyStatementsVisitor(LocalVariableMaker localVariableMaker) {
        this.localVariableMaker = localVariableMaker;
    }

    public void init() {
        this.statementCountInFinally = 0;
        this.statementCountToRemove = 0;
        this.lastFinallyStatementIsATryStatement = false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void visit(Statements statements) {
        DefaultList<Statement> stmts = statements;
        int size = statements.size();

        if (size > 0) {
            int i = size;
            int oldStatementCountToRemove = statementCountToRemove;

            // Check last statement
            Statement lastStatement = stmts.getLast();

            if (lastStatement.isReturnExpressionStatement() || lastStatement.isReturnStatement()) {
                statementCountToRemove = statementCountInFinally;
                i--;
            } else if (lastStatement.isThrowStatement()) {
                statementCountToRemove = 0;
                i--;
            } else if (lastStatement.isContinueStatement() || lastStatement.isBreakStatement()) {
                i--;
            } else {
                WhileStatement whileStatement = getInfiniteWhileStatement(lastStatement);

                if (whileStatement != null) {
                    // Infinite loop => Do not remove any statement
                    statementCountToRemove = 0;
                    i--;
                    whileStatement.getStatements().accept(this);
                }
            }

            // Remove 'finally' statements
            if (statementCountToRemove > 0) {
                if (!lastFinallyStatementIsATryStatement && (i > 0) && stmts.get(i-1).isTryStatement()) {
                    stmts.get(i-1).accept(this);
                    statementCountToRemove = 0;
                    i--;
                } else {
                    declaredSyntheticLocalVariableVisitor.init();

                    // Remove 'finally' statements
                    if (i > statementCountToRemove) {
                        List<Statement> list = statements.subList(i - statementCountToRemove, i);

                        for (Statement statement : list) {
                            statement.accept(declaredSyntheticLocalVariableVisitor);
                        }

                        lastStatement.accept(declaredSyntheticLocalVariableVisitor);
                        list.clear();
                        i -= statementCountToRemove;
                        statementCountToRemove = 0;
                    } else {
                        List<Statement> list = statements;

                        for (Statement statement : list) {
                            statement.accept(declaredSyntheticLocalVariableVisitor);
                        }

                        list.clear();
                        if (i < size) {
                            list.add(lastStatement);
                        }
                        statementCountToRemove -= i;
                        i = 0;
                    }
                }
            }

            // Recursive visit
            while (i-- > 0) {
                stmts.get(i).accept(this);

                if (statementCountToRemove > 0) {
                    if (i + statementCountToRemove < statements.size()) {
                        statements.subList(i + 1, i + 1 + statementCountToRemove).clear();
                        statementCountToRemove = 0;
                    } else {
                        //assert false : "Error. Eclipse try-finally ?";
                    }
                }
            }

            statementCountToRemove = oldStatementCountToRemove;
        }
    }

    private static WhileStatement getInfiniteWhileStatement(Statement statement) {
        if (statement.isLabelStatement()) {
            statement = ((LabelStatement)statement).getStatement();
        }

        if ((statement == null) || !statement.isWhileStatement()) {
            return null;
        }

        if (!statement.getCondition().isBooleanExpression()) {
            return null;
        }

        BooleanExpression booleanExpression = (BooleanExpression)statement.getCondition();

        if (booleanExpression.isFalse()) {
            return null;
        }

        return (WhileStatement)statement;
    }

    @Override
    public void visit(IfElseStatement statement) {
        statement.getStatements().accept(this);
        statement.getElseStatements().accept(this);
    }

    @Override
    public void visit(SwitchStatement statement) {
        for (SwitchStatement.Block block : statement.getBlocks()) {
            block.getStatements().accept(this);
        }
    }

    @Override
    public void visit(TryStatement statement) {
        boolean oldLastFinallyStatementIsTryStatement = lastFinallyStatementIsATryStatement;
        ClassFileTryStatement ts = (ClassFileTryStatement)statement;
        Statements tryStatements = (Statements)ts.getTryStatements();
        Statements finallyStatements = (Statements)ts.getFinallyStatements();

        if (finallyStatements != null) {
            switch (finallyStatements.size()) {
                case 0: break;
                case 1: finallyStatements.getFirst().accept(this); break;
                default: for (Statement stmt : finallyStatements) stmt.accept(this); break;
            }

            if ((statementCountInFinally == 0) && (finallyStatements.size() > 0)) {
                lastFinallyStatementIsATryStatement = finallyStatements.getLast().isTryStatement();
            }
        }

        if (ts.isJsr() || (finallyStatements == null) || (finallyStatements.size() == 0)) {
            tryStatements.accept(this);
            safeAcceptListStatement(statement.getCatchClauses());
        } else if (ts.isEclipse()) {
            List<TryStatement.CatchClause> catchClauses = statement.getCatchClauses();
            int oldStatementCountInFinally = statementCountInFinally;
            int finallyStatementsSize = finallyStatements.size();

            statementCountInFinally += finallyStatementsSize;

            tryStatements.accept(this);

            statementCountToRemove = finallyStatementsSize;

            if (catchClauses != null) {
                for (TryStatement.CatchClause cc : catchClauses) {
                    cc.getStatements().accept(this);
                }
            }

            statementCountInFinally = oldStatementCountInFinally;

            if (statement.getResources() != null) {
                ts.setFinallyStatements(null);
            }
        } else {
            List<TryStatement.CatchClause> catchClauses = statement.getCatchClauses();
            int oldStatementCountInFinally = statementCountInFinally;
            int oldStatementCountToRemove = statementCountToRemove;
            int finallyStatementsSize = finallyStatements.size();

            statementCountInFinally += finallyStatementsSize;
            statementCountToRemove += finallyStatementsSize;

            tryStatements.accept(this);

            if (catchClauses != null) {
                for (TryStatement.CatchClause cc : catchClauses) {
                    cc.getStatements().accept(this);
                }
            }

            statementCountInFinally = oldStatementCountInFinally;
            statementCountToRemove = oldStatementCountToRemove;

            if (statement.getResources() != null) {
                ts.setFinallyStatements(null);
            }
        }

        lastFinallyStatementIsATryStatement = oldLastFinallyStatementIsTryStatement;
    }

    @Override public void visit(DoWhileStatement statement) { safeAccept(statement.getStatements()); }
    @Override public void visit(ForEachStatement statement) { safeAccept(statement.getStatements()); }
    @Override public void visit(ForStatement statement) { safeAccept(statement.getStatements()); }
    @Override public void visit(IfStatement statement) { safeAccept(statement.getStatements()); }
    @Override public void visit(SynchronizedStatement statement) { safeAccept(statement.getStatements()); }
    @Override public void visit(TryStatement.CatchClause statement) { safeAccept(statement.getStatements()); }
    @Override public void visit(WhileStatement statement) { safeAccept(statement.getStatements()); }

    @Override public void visit(SwitchStatement.LabelBlock statement) { statement.getStatements().accept(this); }
    @Override public void visit(SwitchStatement.MultiLabelsBlock statement) { statement.getStatements().accept(this); }

    @Override public void visit(AssertStatement statement) {}
    @Override public void visit(BreakStatement statement) {}
    @Override public void visit(ByteCodeStatement statement) {}
    @Override public void visit(CommentStatement statement) {}
    @Override public void visit(ContinueStatement statement) {}
    @Override public void visit(ExpressionStatement statement) {}
    @Override public void visit(LabelStatement statement) {}
    @Override public void visit(LambdaExpressionStatement statement) {}
    @Override public void visit(LocalVariableDeclarationStatement statement) {}
    @Override public void visit(NoStatement statement) {}
    @Override public void visit(ReturnExpressionStatement statement) {}
    @Override public void visit(ReturnStatement statement) {}
    @Override public void visit(SwitchStatement.DefaultLabel statement) {}
    @Override public void visit(SwitchStatement.ExpressionLabel statement) {}
    @Override public void visit(ThrowStatement statement) {}
    @Override public void visit(TryStatement.Resource statement) {}
    @Override public void visit(TypeDeclarationStatement statement) {}

    protected void safeAccept(BaseStatement list) {
        if (list != null)
            list.accept(this);
    }

    protected void safeAcceptListStatement(List<? extends Statement> list) {
        if (list != null) {
            for (Statement statement : list)
                statement.accept(this);
        }
    }
}
