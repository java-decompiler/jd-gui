/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.statement;

import org.jd.core.v1.model.javasyntax.expression.BaseExpression;
import org.jd.core.v1.model.javasyntax.expression.Expression;
import org.jd.core.v1.util.Base;
import org.jd.core.v1.util.DefaultList;

import static org.jd.core.v1.model.javasyntax.expression.Expression.UNKNOWN_LINE_NUMBER;
import static org.jd.core.v1.model.javasyntax.expression.NoExpression.NO_EXPRESSION;
import static org.jd.core.v1.model.javasyntax.statement.NoStatement.NO_STATEMENT;

public interface BaseStatement extends Base<Statement> {
    void accept(StatementVisitor visitor);

    default boolean isBreakStatement() { return false; }
    default boolean isContinueStatement() { return false; }
    default boolean isExpressionStatement() { return false; }
    default boolean isForStatement() { return false; }
    default boolean isIfStatement() { return false; }
    default boolean isIfElseStatement() { return false; }
    default boolean isLabelStatement() { return false; }
    default boolean isLambdaExpressionStatement() { return false; }
    default boolean isLocalVariableDeclarationStatement() { return false; }
    default boolean isMonitorEnterStatement() { return false; }
    default boolean isMonitorExitStatement() { return false; }
    default boolean isReturnStatement() { return false; }
    default boolean isReturnExpressionStatement() { return false; }
    default boolean isStatements() { return false; }
    default boolean isSwitchStatement() { return false; }
    default boolean isSwitchStatementLabelBlock() { return false; }
    default boolean isSwitchStatementMultiLabelsBlock() { return false; }
    default boolean isThrowStatement() { return false; }
    default boolean isTryStatement() { return false; }
    default boolean isWhileStatement() { return false; }

    default Expression getCondition() { return NO_EXPRESSION; }
    default Expression getExpression() { return NO_EXPRESSION; }
    default Expression getMonitor() { return NO_EXPRESSION; }

    default BaseStatement getElseStatements() { return NO_STATEMENT; }
    default BaseStatement getFinallyStatements() { return NO_STATEMENT; }
    default BaseStatement getStatements() { return NO_STATEMENT; }
    default BaseStatement getTryStatements() { return NO_STATEMENT; }

    default BaseExpression getInit() { return NO_EXPRESSION; }
    default BaseExpression getUpdate() { return NO_EXPRESSION; }

    default DefaultList<TryStatement.CatchClause> getCatchClauses() { return DefaultList.emptyList(); }

    default int getLineNumber() { return UNKNOWN_LINE_NUMBER; }
}
