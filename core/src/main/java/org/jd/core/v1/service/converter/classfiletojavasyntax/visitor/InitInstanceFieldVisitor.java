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
import org.jd.core.v1.model.javasyntax.statement.ExpressionStatement;
import org.jd.core.v1.model.javasyntax.statement.Statement;
import org.jd.core.v1.model.javasyntax.statement.Statements;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.declaration.ClassFileBodyDeclaration;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.declaration.ClassFileConstructorDeclaration;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.declaration.ClassFileFieldDeclaration;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.declaration.ClassFileMethodDeclaration;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.expression.ClassFileConstructorInvocationExpression;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.expression.ClassFileSuperConstructorInvocationExpression;
import org.jd.core.v1.util.DefaultList;

import java.util.*;

import static org.jd.core.v1.api.printer.Printer.UNKNOWN_LINE_NUMBER;
import static org.jd.core.v1.model.javasyntax.declaration.Declaration.FLAG_SYNTHETIC;

public class InitInstanceFieldVisitor extends AbstractJavaSyntaxVisitor {
    protected SearchFirstLineNumberVisitor searchFirstLineNumberVisitor = new SearchFirstLineNumberVisitor();
    protected HashMap<String, FieldDeclarator> fieldDeclarators = new HashMap<>();
    protected DefaultList<Data> datas = new DefaultList<>();
    protected DefaultList<Expression> putFields = new DefaultList<>();
    protected int lineNumber = UNKNOWN_LINE_NUMBER;
    protected boolean containsLocalVariableReference;

    @Override
    public void visit(AnnotationDeclaration declaration) {
        safeAccept(declaration.getBodyDeclaration());
    }

    @Override
    public void visit(ClassDeclaration declaration) {
        safeAccept(declaration.getBodyDeclaration());
    }

    @Override
    public void visit(EnumDeclaration declaration) {
        safeAccept(declaration.getBodyDeclaration());
    }

    @Override
    public void visit(InterfaceDeclaration declaration) {
        safeAccept(declaration.getBodyDeclaration());
    }

    @Override
    public void visit(BodyDeclaration declaration) {
        ClassFileBodyDeclaration bodyDeclaration = (ClassFileBodyDeclaration)declaration;

        // Init attributes
        fieldDeclarators.clear();
        datas.clear();
        putFields.clear();
        // Visit fields
        safeAcceptListDeclaration(bodyDeclaration.getFieldDeclarations());
        // Visit methods
        safeAcceptListDeclaration(bodyDeclaration.getMethodDeclarations());
        // Init values
        updateFieldsAndConstructors();
    }

    @Override
    public void visit(FieldDeclaration declaration) {
        if ((declaration.getFlags() & FieldDeclaration.FLAG_STATIC) == 0) {
            declaration.getFieldDeclarators().accept(this);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void visit(ConstructorDeclaration declaration) {
        ClassFileConstructorDeclaration cfcd = (ClassFileConstructorDeclaration)declaration;

        if ((cfcd.getStatements() != null) && cfcd.getStatements().isStatements()) {
            Statements statements = (Statements) cfcd.getStatements();
            ListIterator<Statement> iterator = statements.listIterator();
            SuperConstructorInvocationExpression superConstructorCall = searchSuperConstructorCall(iterator);

            if (superConstructorCall != null) {
                String internalTypeName = cfcd.getClassFile().getInternalTypeName();

                datas.add(new Data(cfcd, statements, iterator.nextIndex()));

                if (datas.size() == 1) {
                    int firstLineNumber;

                    if ((cfcd.getFlags() & FLAG_SYNTHETIC) != 0) {
                        firstLineNumber = UNKNOWN_LINE_NUMBER;
                    } else {
                        firstLineNumber = superConstructorCall.getLineNumber();

                        if (superConstructorCall.getDescriptor().equals("()V") && (firstLineNumber != UNKNOWN_LINE_NUMBER) && iterator.hasNext()) {
                            if ((lineNumber == UNKNOWN_LINE_NUMBER) || (lineNumber >= firstLineNumber)) {
                                searchFirstLineNumberVisitor.init();
                                iterator.next().accept(searchFirstLineNumberVisitor);
                                iterator.previous();

                                int ln = searchFirstLineNumberVisitor.getLineNumber();

                                if ((ln != UNKNOWN_LINE_NUMBER) && (ln >= firstLineNumber)) {
                                    firstLineNumber = UNKNOWN_LINE_NUMBER;
                                }
                            }
                        }
                    }

                    initPutFields(internalTypeName, firstLineNumber, iterator);
                } else {
                    filterPutFields(internalTypeName, iterator);
                }
            }
        }
    }

    @Override
    public void visit(MethodDeclaration declaration) {
        lineNumber = ((ClassFileMethodDeclaration)declaration).getFirstLineNumber();
    }

    @Override
    public void visit(NewExpression expression) {
        safeAccept(expression.getParameters());
    }

    @Override
    public void visit(StaticInitializerDeclaration declaration) {}

    @Override
    public void visit(FieldDeclarator declaration) {
        fieldDeclarators.put(declaration.getName(), declaration);
    }

    @Override
    public void visit(LocalVariableReferenceExpression expression) {
        containsLocalVariableReference = true;
    }

    protected SuperConstructorInvocationExpression searchSuperConstructorCall(ListIterator<Statement> iterator) {
        while (iterator.hasNext()) {
            Expression expression = iterator.next().getExpression();

            if (expression.isSuperConstructorInvocationExpression()) {
                return (SuperConstructorInvocationExpression)expression;
            }

            if (expression.isConstructorInvocationExpression()) {
                break;
            }
        }

        return null;
    }

    protected void initPutFields(String internalTypeName, int firstLineNumber, ListIterator<Statement> iterator) {
        HashSet<String> fieldNames = new HashSet<>();
        Expression expression = null;

        while (iterator.hasNext()) {
            Statement statement = iterator.next();

            if (!statement.isExpressionStatement()) {
                break;
            }

            expression = statement.getExpression();

            if (!expression.isBinaryOperatorExpression()) {
                break;
            }

            if (!expression.getOperator().equals("=") || !expression.getLeftExpression().isFieldReferenceExpression()) {
                break;
            }

            FieldReferenceExpression fre = (FieldReferenceExpression)expression.getLeftExpression();

            if (!fre.getInternalTypeName().equals(internalTypeName) || !fre.getExpression().isThisExpression()) {
                break;
            }

            String fieldName = fre.getName();

            if (fieldNames.contains(fieldName)) {
                break;
            }

            containsLocalVariableReference = false;
            expression.getRightExpression().accept(this);

            if (containsLocalVariableReference) {
                break;
            }

            putFields.add(expression);
            fieldNames.add(fieldName);
            expression = null;
        }

        int lastLineNumber;

        if (expression == null) {
            lastLineNumber = (firstLineNumber == UNKNOWN_LINE_NUMBER) ? UNKNOWN_LINE_NUMBER : firstLineNumber+1;
        } else {
            lastLineNumber = expression.getLineNumber();
        }

        if (firstLineNumber < lastLineNumber) {
            Iterator<Expression> ite = putFields.iterator();

            while (ite.hasNext()) {
                int lineNumber = ite.next().getLineNumber();

                if ((firstLineNumber <= lineNumber) && (lastLineNumber <= lastLineNumber)) {
                    if (lastLineNumber == lastLineNumber) {
                        lastLineNumber++;
                    }
                    ite.remove();
                }
            }
        }
    }

    protected void filterPutFields(String internalTypeName, ListIterator<Statement> iterator) {
        Iterator<Expression> putFieldIterator = putFields.iterator();
        int index = 0;

        while (iterator.hasNext() && putFieldIterator.hasNext()) {
            Expression expression = iterator.next().getExpression();

            if (!expression.isBinaryOperatorExpression()) {
                break;
            }

            if (!expression.getOperator().equals("=") || !expression.getLeftExpression().isFieldReferenceExpression()) {
                break;
            }

            FieldReferenceExpression fre = (FieldReferenceExpression) expression.getLeftExpression();

            if (!fre.getInternalTypeName().equals(internalTypeName)) {
                break;
            }

            Expression putField = putFieldIterator.next();

            if (expression.getLineNumber() != putField.getLineNumber()) {
                break;
            }

            if (!fre.getName().equals(putField.getLeftExpression().getName())) {
                break;
            }

            index++;
        }

        if (index < putFields.size()) {
            // Cut extra putFields
            putFields.subList(index, putFields.size()).clear();
        }
    }

    protected void updateFieldsAndConstructors() {
        int count = putFields.size();

        if (count > 0) {
            // Init values
            for (Expression putField : putFields) {
                FieldDeclarator declaration = fieldDeclarators.get(putField.getLeftExpression().getName());

                if (declaration != null) {
                    Expression expression = putField.getRightExpression();
                    declaration.setVariableInitializer(new ExpressionVariableInitializer(expression));
                    ((ClassFileFieldDeclaration) declaration.getFieldDeclaration()).setFirstLineNumber(expression.getLineNumber());
                }
            }

            // Update data : remove init field statements
            for (Data data : datas) {
                data.statements.subList(data.index, data.index + count).clear();

                if (data.statements.isEmpty()) {
                    data.declaration.setStatements(null);
                    data.declaration.setFirstLineNumber(0);
                } else {
                    searchFirstLineNumberVisitor.init();
                    searchFirstLineNumberVisitor.visit(data.statements);

                    int firstLineNumber = searchFirstLineNumberVisitor.getLineNumber();

                    data.declaration.setFirstLineNumber((firstLineNumber==-1) ? 0 : firstLineNumber);
                }
            }
        }
    }

    protected static final class Data {
        public ClassFileConstructorDeclaration declaration;
        public Statements statements;
        public int index;

        public Data(ClassFileConstructorDeclaration declaration, Statements statements, int index) {
            this.declaration = declaration;
            this.statements = statements;
            this.index = index;
        }
    }
}
