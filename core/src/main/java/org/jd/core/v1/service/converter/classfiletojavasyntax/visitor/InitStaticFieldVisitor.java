/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.visitor;

import org.jd.core.v1.model.javasyntax.AbstractJavaSyntaxVisitor;
import org.jd.core.v1.model.javasyntax.declaration.*;
import org.jd.core.v1.model.javasyntax.expression.BinaryOperatorExpression;
import org.jd.core.v1.model.javasyntax.expression.Expression;
import org.jd.core.v1.model.javasyntax.expression.FieldReferenceExpression;
import org.jd.core.v1.model.javasyntax.statement.BaseStatement;
import org.jd.core.v1.model.javasyntax.statement.ExpressionStatement;
import org.jd.core.v1.model.javasyntax.statement.Statement;
import org.jd.core.v1.model.javasyntax.statement.Statements;
import org.jd.core.v1.model.javasyntax.type.PrimitiveType;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.declaration.ClassFileBodyDeclaration;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.declaration.ClassFileConstructorOrMethodDeclaration;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.declaration.ClassFileFieldDeclaration;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.declaration.ClassFileStaticInitializerDeclaration;
import org.jd.core.v1.util.DefaultList;

import java.util.HashMap;
import java.util.List;

public class InitStaticFieldVisitor extends AbstractJavaSyntaxVisitor {
    protected SearchFirstLineNumberVisitor searchFirstLineNumberVisitor = new SearchFirstLineNumberVisitor();
    protected SearchLocalVariableReferenceVisitor searchLocalVariableReferenceVisitor = new SearchLocalVariableReferenceVisitor();
    protected String internalTypeName;
    protected HashMap<String, FieldDeclarator> fields = new HashMap<>();
    protected List<ClassFileConstructorOrMethodDeclaration> methods;
    protected Boolean deleteStaticDeclaration;

    public void setInternalTypeName(String internalTypeName) {
        this.internalTypeName = internalTypeName;
    }

    @Override
    public void visit(AnnotationDeclaration declaration) {
        this.internalTypeName = declaration.getInternalTypeName();
        safeAccept(declaration.getBodyDeclaration());
    }

    @Override
    public void visit(ClassDeclaration declaration) {
        this.internalTypeName = declaration.getInternalTypeName();
        safeAccept(declaration.getBodyDeclaration());
    }

    @Override
    public void visit(EnumDeclaration declaration) {
        this.internalTypeName = declaration.getInternalTypeName();
        safeAccept(declaration.getBodyDeclaration());
    }

    @Override
    public void visit(InterfaceDeclaration declaration) {
        this.internalTypeName = declaration.getInternalTypeName();
        safeAccept(declaration.getBodyDeclaration());
    }

    @Override
    public void visit(BodyDeclaration declaration) {
        ClassFileBodyDeclaration bodyDeclaration = (ClassFileBodyDeclaration)declaration;

        // Store field declarations
        fields.clear();
        safeAcceptListDeclaration(bodyDeclaration.getFieldDeclarations());

        if (!fields.isEmpty()) {
            methods = bodyDeclaration.getMethodDeclarations();

            if (methods != null) {
                deleteStaticDeclaration = null;

                for (int i=0, len=methods.size(); i<len; i++) {
                    methods.get(i).accept(this);

                    if (deleteStaticDeclaration != null) {
                        if (deleteStaticDeclaration.booleanValue()) {
                            methods.remove(i);
                        }
                        break;
                    }
                }
            }
        }

        safeAcceptListDeclaration(bodyDeclaration.getInnerTypeDeclarations());
    }

    @Override
    public void visit(FieldDeclarator declaration) {
        fields.put(declaration.getName(), declaration);
    }

    @Override
    public void visit(ConstructorDeclaration declaration) {}

    @Override
    public void visit(MethodDeclaration declaration) {}

    @Override
    public void visit(InstanceInitializerDeclaration declaration) {}

    @Override
    @SuppressWarnings("unchecked")
    public void visit(StaticInitializerDeclaration declaration) {
        ClassFileStaticInitializerDeclaration sid = (ClassFileStaticInitializerDeclaration) declaration;

        BaseStatement statements = sid.getStatements();

        if (statements != null) {
            if (statements.isList()) {
                DefaultList<Statement> list = statements.getList();

                // Multiple statements
                if ((list.size() > 0) && isAssertionsDisabledStatement(list.getFirst())) {
                    // Remove assert initialization statement
                    list.removeFirst();
                }

                for (int i=0, len=list.size(); i<len; i++) {
                    if (setStaticFieldInitializer(list.get(i))) {
                        if (i > 0) {
                            // Split 'static' block
                            BaseStatement newStatements;

                            if (i == 1) {
                                newStatements = list.removeFirst();
                            } else {
                                List<Statement> subList = list.subList(0, i);
                                newStatements = new Statements(subList);
                                subList.clear();
                            }

                            // Removes statements from original list
                            len -= newStatements.size();
                            i = 0;

                            addStaticInitializerDeclaration(sid, getFirstLineNumber(newStatements), newStatements);
                        }
                        // Remove field initialization statement
                        list.remove(i--);
                        len--;
                    }
                }
            } else {
                // Single statement
                if (isAssertionsDisabledStatement(statements.getFirst())) {
                    // Remove assert initialization statement
                    statements = null;
                }
                if ((statements != null) && setStaticFieldInitializer(statements.getFirst())) {
                    // Remove field initialization statement
                    statements = null;
                }
            }

            if ((statements == null) || (statements.size() == 0)) {
                deleteStaticDeclaration = Boolean.TRUE;
            } else {
                int firstLineNumber = getFirstLineNumber(statements);
                sid.setFirstLineNumber((firstLineNumber==-1) ? 0 : firstLineNumber);
                deleteStaticDeclaration = Boolean.FALSE;
            }
        }
    }

    protected boolean isAssertionsDisabledStatement(Statement statement) {
        Expression expression = statement.getExpression();

        if (expression.getLeftExpression().isFieldReferenceExpression()) {
            FieldReferenceExpression fre = (FieldReferenceExpression) expression.getLeftExpression();

            if ((fre.getType() == PrimitiveType.TYPE_BOOLEAN) && fre.getInternalTypeName().equals(internalTypeName) && fre.getName().equals("$assertionsDisabled")) {
                return true;
            }
        }

        return false;
    }

    protected boolean setStaticFieldInitializer(Statement statement) {
        Expression expression = statement.getExpression();

        if (expression.getLeftExpression().isFieldReferenceExpression()) {
            FieldReferenceExpression fre = (FieldReferenceExpression) expression.getLeftExpression();

            if (fre.getInternalTypeName().equals(internalTypeName)) {
                FieldDeclarator fdr = fields.get(fre.getName());

                if ((fdr != null) && (fdr.getVariableInitializer() == null)) {
                    FieldDeclaration fdn = fdr.getFieldDeclaration();

                    if (((fdn.getFlags() & Declaration.FLAG_STATIC) != 0) && fdn.getType().getDescriptor().equals(fre.getDescriptor())) {
                        expression = expression.getRightExpression();

                        searchLocalVariableReferenceVisitor.init(-1);
                        expression.accept(searchLocalVariableReferenceVisitor);

                        if (!searchLocalVariableReferenceVisitor.containsReference()) {
                            fdr.setVariableInitializer(new ExpressionVariableInitializer(expression));
                            ((ClassFileFieldDeclaration)fdr.getFieldDeclaration()).setFirstLineNumber(expression.getLineNumber());
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    protected int getFirstLineNumber(BaseStatement baseStatement) {
        searchFirstLineNumberVisitor.init();
        baseStatement.accept(searchFirstLineNumberVisitor);
        return searchFirstLineNumberVisitor.getLineNumber();
    }

    protected void addStaticInitializerDeclaration(ClassFileStaticInitializerDeclaration sid, int lineNumber, BaseStatement statements) {
        methods.add(new ClassFileStaticInitializerDeclaration(
            sid.getBodyDeclaration(), sid.getClassFile(), sid.getMethod(), sid.getBindings(),
            sid.getTypeBounds(), lineNumber, statements));
    }
}
