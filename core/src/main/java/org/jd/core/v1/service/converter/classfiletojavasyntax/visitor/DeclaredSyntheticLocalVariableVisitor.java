/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.visitor;

import org.jd.core.v1.model.javasyntax.AbstractJavaSyntaxVisitor;
import org.jd.core.v1.model.javasyntax.declaration.FieldDeclaration;
import org.jd.core.v1.model.javasyntax.declaration.FormalParameter;
import org.jd.core.v1.model.javasyntax.declaration.LocalVariableDeclaration;
import org.jd.core.v1.model.javasyntax.declaration.MethodDeclaration;
import org.jd.core.v1.model.javasyntax.expression.*;
import org.jd.core.v1.model.javasyntax.statement.ForEachStatement;
import org.jd.core.v1.model.javasyntax.statement.SwitchStatement;
import org.jd.core.v1.model.javasyntax.statement.TryStatement;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.expression.ClassFileLocalVariableReferenceExpression;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.localvariable.AbstractLocalVariable;
import org.jd.core.v1.util.DefaultList;

public class DeclaredSyntheticLocalVariableVisitor extends AbstractJavaSyntaxVisitor {
    protected DefaultList<LocalVariableReferenceExpression> localVariableReferenceExpressions = new DefaultList<>();

    public void init() {
        localVariableReferenceExpressions.clear();
    }

    @Override
    public void visit(FieldDeclaration declaration) {
        safeAccept(declaration.getAnnotationReferences());
        declaration.getFieldDeclarators().accept(this);
    }

    @Override
    public void visit(FormalParameter declaration) {
        safeAccept(declaration.getAnnotationReferences());
    }

    @Override
    public void visit(LocalVariableDeclaration declaration) {
        declaration.getLocalVariableDeclarators().accept(this);
    }

    @Override
    public void visit(MethodDeclaration declaration) {
        safeAccept(declaration.getAnnotationReferences());
        safeAccept(declaration.getFormalParameters());
        safeAccept(declaration.getStatements());
    }

    @Override
    public void visit(ArrayExpression expression) {
        expression.getExpression().accept(this);
        expression.getIndex().accept(this);
    }

    @Override
    public void visit(CastExpression expression) {
        expression.getExpression().accept(this);
    }

    @Override
    public void visit(FieldReferenceExpression expression) {
        safeAccept(expression.getExpression());
    }

    @Override
    public void visit(InstanceOfExpression expression) {
        expression.getExpression().accept(this);
    }

    @Override
    public void visit(LocalVariableReferenceExpression expression) {
        AbstractLocalVariable localVariable = ((ClassFileLocalVariableReferenceExpression)expression).getLocalVariable();

        localVariableReferenceExpressions.add(expression);

        if (localVariableReferenceExpressions.containsAll(localVariable.getReferences())) {
            localVariable.setDeclared(true);
        }
    }

    @Override
    public void visit(MethodReferenceExpression expression) {
        expression.getExpression().accept(this);
    }

    @Override
    public void visit(NewArray expression) {
        safeAccept(expression.getDimensionExpressionList());
    }

    @Override
    public void visit(NewExpression expression) {
        safeAccept(expression.getParameters());
        safeAccept(expression.getBodyDeclaration());
    }

    @Override
    public void visit(NewInitializedArray expression) {
        safeAcceptListDeclaration(expression.getArrayInitializer());
    }

    @Override
    public void visit(ForEachStatement statement) {
        statement.getExpression().accept(this);
        safeAccept(statement.getStatements());
    }

    @Override
    public void visit(SwitchStatement.LabelBlock statement) {
        statement.getStatements().accept(this);
    }

    @Override
    public void visit(SwitchStatement.MultiLabelsBlock statement) {
        statement.getStatements().accept(this);
    }

    @Override
    public void visit(TryStatement.CatchClause statement) {
        safeAccept(statement.getStatements());
    }

    @Override
    public void visit(TryStatement.Resource statement) {
        statement.getExpression().accept(this);
    }

    @Override public void visit(ConstructorReferenceExpression expression) {}
    @Override public void visit(DoubleConstantExpression expression) {}
    @Override public void visit(FloatConstantExpression expression) {}
    @Override public void visit(IntegerConstantExpression expression) {}
    @Override public void visit(LongConstantExpression expression) {}
    @Override public void visit(NullExpression expression) {}
    @Override public void visit(ObjectTypeReferenceExpression expression) {}
    @Override public void visit(ThisExpression expression) {}
    @Override public void visit(TypeReferenceDotClassExpression expression) {}
}
