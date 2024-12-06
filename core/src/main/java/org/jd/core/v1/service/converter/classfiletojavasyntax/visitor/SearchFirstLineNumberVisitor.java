/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.visitor;

import org.jd.core.v1.model.javasyntax.AbstractJavaSyntaxVisitor;
import org.jd.core.v1.model.javasyntax.expression.*;
import org.jd.core.v1.model.javasyntax.statement.*;

import java.util.List;

public class SearchFirstLineNumberVisitor extends AbstractJavaSyntaxVisitor {
    protected int lineNumber;

    public SearchFirstLineNumberVisitor() {
        lineNumber = -1;
    }

    public void init() {
        lineNumber = -1;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void visit(Statements statements) {
        if (lineNumber == -1) {
            List<Statement> list = statements;

            for (Statement statement : list) {
                statement.accept(this);

                if (lineNumber != -1) {
                    break;
                }
            }
        }
    }

    @Override public void visit(ArrayExpression expression) { lineNumber = expression.getLineNumber(); }
    @Override public void visit(BinaryOperatorExpression expression) { lineNumber = expression.getLineNumber(); }
    @Override public void visit(BooleanExpression expression) { lineNumber = expression.getLineNumber(); }
    @Override public void visit(CastExpression expression) { lineNumber = expression.getLineNumber(); }
    @Override public void visit(CommentExpression expression) { lineNumber = expression.getLineNumber(); }
    @Override public void visit(ConstructorInvocationExpression expression) { lineNumber = expression.getLineNumber(); }
    @Override public void visit(ConstructorReferenceExpression expression) { lineNumber = expression.getLineNumber(); }
    @Override public void visit(DoubleConstantExpression expression) { lineNumber = expression.getLineNumber(); }
    @Override public void visit(EnumConstantReferenceExpression expression) { lineNumber = expression.getLineNumber(); }
    @Override public void visit(FieldReferenceExpression expression) { lineNumber = expression.getLineNumber(); }
    @Override public void visit(FloatConstantExpression expression) { lineNumber = expression.getLineNumber(); }
    @Override public void visit(IntegerConstantExpression expression) { lineNumber = expression.getLineNumber(); }
    @Override public void visit(InstanceOfExpression expression) { lineNumber = expression.getLineNumber(); }
    @Override public void visit(LambdaFormalParametersExpression expression) { lineNumber = expression.getLineNumber(); }
    @Override public void visit(LambdaIdentifiersExpression expression) { lineNumber = expression.getLineNumber(); }
    @Override public void visit(LengthExpression expression) { lineNumber = expression.getLineNumber(); }
    @Override public void visit(LocalVariableReferenceExpression expression) { lineNumber = expression.getLineNumber(); }
    @Override public void visit(LongConstantExpression expression) { lineNumber = expression.getLineNumber(); }
    @Override public void visit(MethodReferenceExpression expression) { lineNumber = expression.getLineNumber(); }
    @Override public void visit(NewArray expression) { lineNumber = expression.getLineNumber(); }
    @Override public void visit(NewExpression expression) { lineNumber = expression.getLineNumber(); }
    @Override public void visit(NewInitializedArray expression) { lineNumber = expression.getLineNumber(); }
    @Override public void visit(NullExpression expression) { lineNumber = expression.getLineNumber(); }
    @Override public void visit(ObjectTypeReferenceExpression expression) { lineNumber = expression.getLineNumber(); }
    @Override public void visit(ParenthesesExpression expression) { lineNumber = expression.getLineNumber(); }
    @Override public void visit(PostOperatorExpression expression) { lineNumber = expression.getLineNumber(); }
    @Override public void visit(PreOperatorExpression expression) { lineNumber = expression.getLineNumber(); }
    @Override public void visit(StringConstantExpression expression) { lineNumber = expression.getLineNumber(); }
    @Override public void visit(SuperExpression expression) { lineNumber = expression.getLineNumber(); }
    @Override public void visit(TernaryOperatorExpression expression) { lineNumber = expression.getLineNumber(); }
    @Override public void visit(ThisExpression expression) { lineNumber = expression.getLineNumber(); }
    @Override public void visit(TypeReferenceDotClassExpression expression) { lineNumber = expression.getLineNumber(); }

    @Override public void visit(AssertStatement statement) {
        statement.getCondition().accept(this);
    }

    @Override public void visit(ExpressionStatement statement) {
        statement.getExpression().accept(this);
    }

    @Override public void visit(ForEachStatement statement) {
        statement.getExpression().accept(this);
    }

    @Override public void visit(IfStatement statement) {
        statement.getCondition().accept(this);
    }

    @Override public void visit(IfElseStatement statement) {
        statement.getCondition().accept(this);
    }

    @Override public void visit(LambdaExpressionStatement statement) {
        statement.getExpression().accept(this);
    }

    @Override public void visit(MethodInvocationExpression expression) {
        expression.getExpression().accept(this);
    }

    @Override public void visit(SwitchStatement statement) {
        statement.getCondition().accept(this);
    }

    @Override public void visit(SynchronizedStatement statement) {
        statement.getMonitor().accept(this);
    }

    @Override public void visit(ThrowStatement statement) {
        statement.getExpression().accept(this);
    }

    @Override public void visit(WhileStatement statement) {
        if (statement.getCondition() != null) {
            statement.getCondition().accept(this);
        } else if (statement.getStatements() != null) {
            statement.getStatements().accept(this);
        }
    }

    @Override public void visit(DoWhileStatement statement) {
        if (statement.getStatements() != null) {
            statement.getStatements().accept(this);
        } else if (statement.getCondition() != null) {
            statement.getCondition().accept(this);
        }
    }

    @Override public void visit(ForStatement statement) {
        if (statement.getInit() != null) {
            statement.getInit().accept(this);
        } else if (statement.getCondition() != null) {
            statement.getCondition().accept(this);
        } else if (statement.getUpdate() != null) {
            statement.getUpdate().accept(this);
        } else if (statement.getStatements() != null) {
            statement.getStatements().accept(this);
        }
    }

    @Override public void visit(ReturnExpressionStatement statement) {
        statement.getExpression().accept(this);
    }

    @Override public void visit(TryStatement statement) {
        if (statement.getResources() != null) {
            acceptListStatement(statement.getResources());
        } else {
            statement.getTryStatements().accept(this);
        }
    }
}
