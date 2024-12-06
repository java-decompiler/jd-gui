/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.fragmenter.javasyntaxtojavafragment.visitor;

import org.jd.core.v1.model.javasyntax.AbstractJavaSyntaxVisitor;
import org.jd.core.v1.model.javasyntax.declaration.BodyDeclaration;
import org.jd.core.v1.model.javasyntax.declaration.LocalVariableDeclaration;
import org.jd.core.v1.model.javasyntax.expression.*;
import org.jd.core.v1.model.javasyntax.statement.*;

import java.util.List;

public class SingleLineStatementVisitor extends AbstractJavaSyntaxVisitor {
    // Minimal line number of visited statements
    protected int minLineNumber;
    // Maximal line number of visited statements
    protected int maxLineNumber;
    // Estimated number of statements if line numbers are unknown
    protected int statementCount;

    public void init() {
        minLineNumber = -2;
        maxLineNumber = -1;
        statementCount = 0;
    }

    public boolean isSingleLineStatement() {
        if (minLineNumber <= 0) {
            // Line numbers are unknown
            return statementCount <= 1;
        } else {
            return minLineNumber == maxLineNumber;
        }
    }

    // -- Statement -- //
    @Override
    public void visit(AssertStatement statement) {
        statement.getCondition().accept(this);
        safeAccept(statement.getMessage());
        minLineNumber = statement.getCondition().getLineNumber();
        statementCount = 1;
    }

    @Override
    public void visit(DoWhileStatement statement) {
        minLineNumber = statement.getCondition().getLineNumber();
        safeAccept(statement.getStatements());
        statement.getCondition().accept(this);
        statementCount = 2;
    }

    @Override
    public void visit(ExpressionStatement statement) {
        statement.getExpression().accept(this);
        minLineNumber = statement.getExpression().getLineNumber();
        statementCount = 1;
    }

    @Override
    public void visit(ForEachStatement statement) {
        statement.getExpression().accept(this);
        safeAccept(statement.getStatements());
        minLineNumber = statement.getExpression().getLineNumber();
        statementCount = 2;
    }

    @Override
    public void visit(ForStatement statement) {
        if (statement.getStatements() != null) {
            statement.getStatements().accept(this);
        } else if (statement.getUpdate() != null) {
            statement.getUpdate().accept(this);
        } else if (statement.getCondition() != null) {
            statement.getCondition().accept(this);
        } else if (statement.getInit() != null) {
            statement.getInit().accept(this);
        } else {
            maxLineNumber = 0;
        }

        if (statement.getCondition() != null) {
            minLineNumber = statement.getCondition().getLineNumber();
        } else if (statement.getCondition() != null) {
            minLineNumber = statement.getCondition().getLineNumber();
        } else {
            minLineNumber = maxLineNumber;
        }

        statementCount = 2;
    }

    @Override
    public void visit(IfStatement statement) {
        statement.getCondition().accept(this);
        safeAccept(statement.getStatements());
        minLineNumber = statement.getCondition().getLineNumber();
        statementCount = 2;
    }

    @Override
    public void visit(IfElseStatement statement) {
        statement.getCondition().accept(this);
        statement.getElseStatements().accept(this);
        minLineNumber = statement.getCondition().getLineNumber();
        statementCount = 2;
    }

    @Override
    public void visit(LabelStatement statement) {
        minLineNumber = maxLineNumber = 0;
        safeAccept(statement.getStatement());
        statementCount = 1;
    }

    @Override
    public void visit(LambdaExpressionStatement statement) {
        statement.getExpression().accept(this);
        minLineNumber = statement.getExpression().getLineNumber();
        statementCount = 1;
    }

    @Override
    public void visit(LocalVariableDeclarationStatement statement) {
        visit((LocalVariableDeclaration) statement);
        statementCount = 1;
    }

    @Override public void visit(ReturnExpressionStatement statement) {
        statement.getExpression().accept(this);
        minLineNumber = statement.getExpression().getLineNumber();
        statementCount = 1;
    }

    @Override
    public void visit(SwitchStatement statement) {
        statement.getCondition().accept(this);
        acceptListStatement(statement.getBlocks());
        minLineNumber = statement.getCondition().getLineNumber();
        statementCount = 2;
    }

    @Override
    public void visit(SynchronizedStatement statement) {
        statement.getMonitor().accept(this);
        safeAccept(statement.getStatements());
        minLineNumber = statement.getMonitor().getLineNumber();
        statementCount = 2;
    }

    @Override
    public void visit(ThrowStatement statement) {
        statement.getExpression().accept(this);
        minLineNumber = statement.getExpression().getLineNumber();
        statementCount = 1;
    }

    @Override
    public void visit(TryStatement statement) {
        statement.getTryStatements().accept(this);

        int min = minLineNumber;

        safeAcceptListStatement(statement.getCatchClauses());
        safeAccept(statement.getFinallyStatements());
        minLineNumber = min;
        statementCount = 2;
    }

    @Override
    public void visit(TryStatement.CatchClause statement) {
        safeAccept(statement.getStatements());
    }

    @Override
    public void visit(TypeDeclarationStatement statement) {
        minLineNumber = maxLineNumber = 0;
        statementCount = 1;
    }

    @Override
    public void visit(WhileStatement statement) {
        statement.getCondition().accept(this);
        safeAccept(statement.getStatements());
        minLineNumber = statement.getCondition().getLineNumber();
        statementCount = 2;
    }

    public void acceptListStatement(List<? extends Statement> list) {
        int size = list.size();

        switch (size) {
            case 0:
                minLineNumber = maxLineNumber = 0;
                break;
            case 1:
                list.get(0).accept(this);
                break;
            default:
                list.get(0).accept(this);

                int min = minLineNumber;

                list.get(size - 1).accept(this);

                minLineNumber = min;
                statementCount = size;
                break;
        }
    }

    public void safeAcceptListStatement(List<? extends Statement> list) {
        if (list == null) {
            minLineNumber = maxLineNumber = 0;
        } else {
            acceptListStatement(list);
        }
    }

    // -- Expression -- //
    @Override
    public void visit(ConstructorInvocationExpression expression) {
        BaseExpression parameters = expression.getParameters();

        if (parameters == null) {
            maxLineNumber = expression.getLineNumber();
        } else {
            parameters.accept(this);
        }
    }

    @Override
    public void visit(SuperConstructorInvocationExpression expression) {
        BaseExpression parameters = expression.getParameters();

        if (parameters == null) {
            maxLineNumber = expression.getLineNumber();
        } else {
            parameters.accept(this);
        }
    }

    @Override
    public void visit(MethodInvocationExpression expression) {
        BaseExpression parameters = expression.getParameters();

        if (parameters == null) {
            maxLineNumber = expression.getLineNumber();
        } else {
            parameters.accept(this);
        }
    }

    @Override
    public void visit(NewArray expression) {
        BaseExpression dimensionExpressionList = expression.getDimensionExpressionList();

        if (dimensionExpressionList == null) {
            maxLineNumber = expression.getLineNumber();
        } else {
            dimensionExpressionList.accept(this);
        }
    }

    @Override
    public void visit(NewExpression expression) {
        BodyDeclaration bodyDeclaration = expression.getBodyDeclaration();

        if (bodyDeclaration == null) {
            BaseExpression parameters = expression.getParameters();

            if (parameters == null) {
                maxLineNumber = expression.getLineNumber();
            } else {
                parameters.accept(this);
            }
        } else {
            maxLineNumber = expression.getLineNumber() + 1;
        }
    }

    public void acceptListExpression(List<? extends Expression> list) {
        int size = list.size();

        if (size == 0) {
            maxLineNumber = 0;
        } else {
            list.get(size - 1).accept(this);
        }
    }

    @Override public void visit(ArrayExpression expression) { expression.getIndex().accept(this); }
    @Override public void visit(BinaryOperatorExpression expression) { expression.getRightExpression().accept(this); }
    @Override public void visit(CastExpression expression) { expression.getExpression().accept(this); }
    @Override public void visit(LambdaFormalParametersExpression expression) { expression.getStatements().accept(this); }
    @Override public void visit(LambdaIdentifiersExpression expression) { safeAccept(expression.getStatements()); }
    @Override public void visit(NewInitializedArray expression) { expression.getArrayInitializer().accept(this); }
    @Override public void visit(ParenthesesExpression expression) { expression.getExpression().accept(this); }
    @Override public void visit(PostOperatorExpression expression) { expression.getExpression().accept(this); }
    @Override public void visit(PreOperatorExpression expression) { expression.getExpression().accept(this); }
    @Override public void visit(TernaryOperatorExpression expression) { expression.getFalseExpression().accept(this); }

    @Override public void visit(BooleanExpression expression) { maxLineNumber = expression.getLineNumber(); }
    @Override public void visit(ConstructorReferenceExpression expression) { maxLineNumber = expression.getLineNumber(); }
    @Override public void visit(DoubleConstantExpression expression) { maxLineNumber = expression.getLineNumber(); }
    @Override public void visit(EnumConstantReferenceExpression expression) { maxLineNumber = expression.getLineNumber(); }
    @Override public void visit(FloatConstantExpression expression) { maxLineNumber = expression.getLineNumber(); }
    @Override public void visit(IntegerConstantExpression expression) { maxLineNumber = expression.getLineNumber(); }
    @Override public void visit(FieldReferenceExpression expression) { maxLineNumber = expression.getLineNumber(); }
    @Override public void visit(InstanceOfExpression expression) { maxLineNumber = expression.getLineNumber(); }
    @Override public void visit(LengthExpression expression) { maxLineNumber = expression.getLineNumber(); }
    @Override public void visit(LocalVariableReferenceExpression expression) { maxLineNumber = expression.getLineNumber(); }
    @Override public void visit(LongConstantExpression expression) { maxLineNumber = expression.getLineNumber(); }
    @Override public void visit(MethodReferenceExpression expression) { maxLineNumber = expression.getLineNumber(); }
    @Override public void visit(NullExpression expression) { maxLineNumber = expression.getLineNumber(); }
    @Override public void visit(ObjectTypeReferenceExpression expression) { maxLineNumber = expression.getLineNumber(); }
    @Override public void visit(StringConstantExpression expression) { maxLineNumber = expression.getLineNumber(); }
    @Override public void visit(SuperExpression expression) { maxLineNumber = expression.getLineNumber(); }
    @Override public void visit(ThisExpression expression) { maxLineNumber = expression.getLineNumber(); }
    @Override public void visit(TypeReferenceDotClassExpression expression) { maxLineNumber = expression.getLineNumber(); }
}
