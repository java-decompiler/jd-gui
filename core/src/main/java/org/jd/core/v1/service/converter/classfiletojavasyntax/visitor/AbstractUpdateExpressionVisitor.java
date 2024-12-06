/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.visitor;

import org.jd.core.v1.model.javasyntax.AbstractJavaSyntaxVisitor;
import org.jd.core.v1.model.javasyntax.declaration.*;
import org.jd.core.v1.model.javasyntax.expression.*;
import org.jd.core.v1.model.javasyntax.reference.*;
import org.jd.core.v1.model.javasyntax.statement.*;
import org.jd.core.v1.model.javasyntax.type.*;

import java.util.ListIterator;

public abstract class AbstractUpdateExpressionVisitor extends AbstractJavaSyntaxVisitor {
    protected abstract Expression updateExpression(Expression expression);

    protected BaseExpression updateBaseExpression(BaseExpression baseExpression) {
        if (baseExpression == null) {
            return null;
        }

        if (baseExpression.isList()) {
            ListIterator<Expression> iterator = baseExpression.getList().listIterator();

            while (iterator.hasNext()) {
                iterator.set(updateExpression(iterator.next()));
            }

            return baseExpression;
        }

        return updateExpression(baseExpression.getFirst());
    }

    @Override
    public void visit(AnnotationDeclaration declaration) {
        safeAccept(declaration.getAnnotationDeclarators());
        safeAccept(declaration.getBodyDeclaration());
    }

    @Override
    public void visit(ClassDeclaration declaration) {
        visit((InterfaceDeclaration) declaration);
    }

    @Override
    public void visit(ConstructorInvocationExpression expression) {
        if (expression.getParameters() != null) {
            expression.setParameters(updateBaseExpression(expression.getParameters()));
            expression.getParameters().accept(this);
        }
    }

    @Override
    public void visit(ConstructorDeclaration declaration) {
        safeAccept(declaration.getStatements());
    }

    @Override
    public void visit(EnumDeclaration declaration) {
        safeAccept(declaration.getBodyDeclaration());
    }

    @Override
    public void visit(EnumDeclaration.Constant declaration) {
        if (declaration.getArguments() != null) {
            declaration.setArguments(updateBaseExpression(declaration.getArguments()));
            declaration.getArguments().accept(this);
        }
        safeAccept(declaration.getBodyDeclaration());
    }

    @Override
    public void visit(ExpressionVariableInitializer declaration) {
        if (declaration.getExpression() != null) {
            declaration.setExpression(updateExpression(declaration.getExpression()));
            declaration.getExpression().accept(this);
        }
    }

    @Override public void visit(FieldDeclaration declaration) {
        declaration.getFieldDeclarators().accept(this);
    }

    @Override
    public void visit(FieldDeclarator declaration) {
        safeAccept(declaration.getVariableInitializer());
    }

    @Override public void visit(FormalParameter declaration) {}

    @Override
    public void visit(InterfaceDeclaration declaration) {
        safeAccept(declaration.getBodyDeclaration());
    }

    @Override
    public void visit(LocalVariableDeclaration declaration) {
        declaration.getLocalVariableDeclarators().accept(this);
    }

    @Override
    public void visit(LocalVariableDeclarator declarator) {
        safeAccept(declarator.getVariableInitializer());
    }

    @Override
    public void visit(MethodDeclaration declaration) {
        safeAccept(declaration.getAnnotationReferences());
        safeAccept(declaration.getStatements());
    }

    @Override
    public void visit(ArrayExpression expression) {
        expression.setExpression(updateExpression(expression.getExpression()));
        expression.setIndex(updateExpression(expression.getIndex()));
        expression.getExpression().accept(this);
        expression.getIndex().accept(this);
    }

    @Override
    public void visit(BinaryOperatorExpression expression) {
        expression.setLeftExpression(updateExpression(expression.getLeftExpression()));
        expression.setRightExpression(updateExpression(expression.getRightExpression()));
        expression.getLeftExpression().accept(this);
        expression.getRightExpression().accept(this);
    }

    @Override
    public void visit(CastExpression expression) {
        expression.setExpression(updateExpression(expression.getExpression()));
        expression.getExpression().accept(this);
    }

    @Override
    public void visit(FieldReferenceExpression expression) {
        if (expression.getExpression() != null) {
            expression.setExpression(updateExpression(expression.getExpression()));
            expression.getExpression().accept(this);
        }
    }

    @Override
    public void visit(InstanceOfExpression expression) {
        if (expression.getExpression() != null) {
            expression.setExpression(updateExpression(expression.getExpression()));
            expression.getExpression().accept(this);
        }
    }

    @Override
    public void visit(LambdaFormalParametersExpression expression) {
        expression.getStatements().accept(this);
    }

    @Override public void visit(LengthExpression expression) {
        expression.setExpression(updateExpression(expression.getExpression()));
        expression.getExpression().accept(this);
    }

    @Override
    public void visit(MethodInvocationExpression expression) {
        expression.setExpression(updateExpression(expression.getExpression()));
        if (expression.getParameters() != null) {
            expression.setParameters(updateBaseExpression(expression.getParameters()));
            expression.getParameters().accept(this);
        }
        expression.getExpression().accept(this);
    }

    @Override
    public void visit(MethodReferenceExpression expression) {
        expression.setExpression(updateExpression(expression.getExpression()));
        expression.getExpression().accept(this);
    }

    @Override
    public void visit(NewArray expression) {
        if (expression.getDimensionExpressionList() != null) {
            expression.setDimensionExpressionList(updateBaseExpression(expression.getDimensionExpressionList()));
            expression.getDimensionExpressionList().accept(this);
        }
    }

    @Override
    public void visit(NewExpression expression) {
        if (expression.getParameters() != null) {
            expression.setParameters(updateBaseExpression(expression.getParameters()));
            expression.getParameters().accept(this);
        }
        // safeAccept(expression.getBodyDeclaration());
    }

    @Override public void visit(NewInitializedArray expression) {
        safeAccept(expression.getArrayInitializer());
    }

    @Override
    public void visit(ParenthesesExpression expression) {
        expression.setExpression(updateExpression(expression.getExpression()));
        expression.getExpression().accept(this);
    }

    @Override
    public void visit(PostOperatorExpression expression) {
        expression.setExpression(updateExpression(expression.getExpression()));
        expression.getExpression().accept(this);
    }

    @Override
    public void visit(PreOperatorExpression expression) {
        expression.setExpression(updateExpression(expression.getExpression()));
        expression.getExpression().accept(this);
    }

    @Override
    public void visit(SuperConstructorInvocationExpression expression) {
        if (expression.getParameters() != null) {
            expression.setParameters(updateBaseExpression(expression.getParameters()));
            expression.getParameters().accept(this);
        }
    }

    @Override
    public void visit(TernaryOperatorExpression expression) {
        expression.setCondition(updateExpression(expression.getCondition()));
        expression.setTrueExpression(updateExpression(expression.getTrueExpression()));
        expression.setFalseExpression(updateExpression(expression.getFalseExpression()));
        expression.getCondition().accept(this);
        expression.getTrueExpression().accept(this);
        expression.getFalseExpression().accept(this);
    }

    @Override
    public void visit(ExpressionElementValue reference) {
        reference.setExpression(updateExpression(reference.getExpression()));
        reference.getExpression().accept(this);
    }

    @Override
    public void visit(AssertStatement statement) {
        statement.setCondition(updateExpression(statement.getCondition()));
        statement.getCondition().accept(this);
        safeAccept(statement.getMessage());
    }

    @Override
    public void visit(DoWhileStatement statement) {
        statement.setCondition(updateExpression(statement.getCondition()));
        safeAccept(statement.getCondition());
        safeAccept(statement.getStatements());
    }

    @Override
    public void visit(ExpressionStatement statement) {
        statement.setExpression(updateExpression(statement.getExpression()));
        statement.getExpression().accept(this);
    }

    @Override
    public void visit(ForEachStatement statement) {
        statement.setExpression(updateExpression(statement.getExpression()));
        statement.getExpression().accept(this);
        safeAccept(statement.getStatements());
    }

    @Override
    public void visit(ForStatement statement) {
        safeAccept(statement.getDeclaration());
        if (statement.getInit() != null) {
            statement.setInit(updateBaseExpression(statement.getInit()));
            statement.getInit().accept(this);
        }
        if (statement.getCondition() != null) {
            statement.setCondition(updateExpression(statement.getCondition()));
            statement.getCondition().accept(this);
        }
        if (statement.getUpdate() != null) {
            statement.setUpdate(updateBaseExpression(statement.getUpdate()));
            statement.getUpdate().accept(this);
        }
        safeAccept(statement.getStatements());
    }

    @Override
    public void visit(IfStatement statement) {
        statement.setCondition(updateExpression(statement.getCondition()));
        statement.getCondition().accept(this);
        safeAccept(statement.getStatements());
    }

    @Override
    public void visit(IfElseStatement statement) {
        statement.setCondition(updateExpression(statement.getCondition()));
        statement.getCondition().accept(this);
        safeAccept(statement.getStatements());
        statement.getElseStatements().accept(this);
    }

    @Override
    public void visit(LambdaExpressionStatement statement) {
        statement.setExpression(updateExpression(statement.getExpression()));
        statement.getExpression().accept(this);
    }

    @Override public void visit(ReturnExpressionStatement statement) {
        statement.setExpression(updateExpression(statement.getExpression()));
        statement.getExpression().accept(this);
    }

    @Override
    public void visit(SwitchStatement statement) {
        statement.setCondition(updateExpression(statement.getCondition()));
        statement.getCondition().accept(this);
        acceptListStatement(statement.getBlocks());
    }

    @Override
    public void visit(SwitchStatement.ExpressionLabel statement) {
        statement.setExpression(updateExpression(statement.getExpression()));
        statement.getExpression().accept(this);
    }

    @Override
    public void visit(SynchronizedStatement statement) {
        statement.setMonitor(updateExpression(statement.getMonitor()));
        statement.getMonitor().accept(this);
        safeAccept(statement.getStatements());
    }

    @Override
    public void visit(ThrowStatement statement) {
        statement.setExpression(updateExpression(statement.getExpression()));
        statement.getExpression().accept(this);
    }

    @Override
    public void visit(TryStatement.CatchClause statement) {
        safeAccept(statement.getStatements());
    }

    @Override
    public void visit(TryStatement.Resource statement) {
        statement.setExpression(updateExpression(statement.getExpression()));
        statement.getExpression().accept(this);
    }

    @Override
    public void visit(WhileStatement statement) {
        statement.setCondition(updateExpression(statement.getCondition()));
        statement.getCondition().accept(this);
        safeAccept(statement.getStatements());
    }

    @Override public void visit(ConstructorReferenceExpression expression) {}
    @Override public void visit(DoubleConstantExpression expression) {}
    @Override public void visit(EnumConstantReferenceExpression expression) {}
    @Override public void visit(FloatConstantExpression expression) {}
    @Override public void visit(IntegerConstantExpression expression) {}
    @Override public void visit(LocalVariableReferenceExpression expression) {}
    @Override public void visit(LongConstantExpression expression) {}
    @Override public void visit(NullExpression expression) {}
    @Override public void visit(TypeReferenceDotClassExpression expression) {}
    @Override public void visit(ObjectTypeReferenceExpression expression) {}
    @Override public void visit(StringConstantExpression expression) {}
    @Override public void visit(SuperExpression expression) {}
    @Override public void visit(ThisExpression expression) {}
    @Override public void visit(AnnotationReference reference) {}
    @Override public void visit(ElementValueArrayInitializerElementValue reference) {}
    @Override public void visit(AnnotationElementValue reference) {}
    @Override public void visit(ObjectReference reference) {}
    @Override public void visit(BreakStatement statement) {}
    @Override public void visit(ByteCodeStatement statement) {}
    @Override public void visit(ContinueStatement statement) {}
    @Override public void visit(ReturnStatement statement) {}
    @Override public void visit(SwitchStatement.DefaultLabel statement) {}

    @Override public void visit(InnerObjectReference reference) {}
    @Override public void visit(TypeArguments type) {}
    @Override public void visit(WildcardExtendsTypeArgument type) {}
    @Override public void visit(ObjectType type) {}
    @Override public void visit(InnerObjectType type) {}
    @Override public void visit(WildcardSuperTypeArgument type) {}
    @Override public void visit(Types list) {}
    @Override public void visit(TypeParameterWithTypeBounds type) {}
}
