/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.util;

import org.jd.core.v1.model.javasyntax.expression.*;
import org.jd.core.v1.model.javasyntax.statement.*;
import org.jd.core.v1.model.javasyntax.type.ObjectType;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.declaration.ClassFileBodyDeclaration;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.declaration.ClassFileConstructorOrMethodDeclaration;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.declaration.ClassFileTypeDeclaration;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.expression.ClassFileLocalVariableReferenceExpression;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.localvariable.AbstractLocalVariable;
import org.jd.core.v1.util.DefaultList;

import java.util.HashMap;
import java.util.Iterator;

public class SwitchStatementMaker {
    protected static final Integer MINUS_ONE = Integer.valueOf(-1);

    @SuppressWarnings("unchecked")
    public static void makeSwitchString(LocalVariableMaker localVariableMaker, Statements statements, SwitchStatement switchStatement) {
        int size = statements.size();
        SwitchStatement previousSwitchStatement = (SwitchStatement)statements.get(size - 2);

        if ((previousSwitchStatement.getCondition().getLineNumber() == switchStatement.getCondition().getLineNumber()) && previousSwitchStatement.getCondition().isMethodInvocationExpression()) {
            Expression expression = previousSwitchStatement.getCondition();

            if (expression.isMethodInvocationExpression()) {
                expression = expression.getExpression();

                if (expression.isLocalVariableReferenceExpression()) {
                    AbstractLocalVariable syntheticLV1 = ((ClassFileLocalVariableReferenceExpression)expression).getLocalVariable();
                    expression = statements.get(size - 4).getExpression().getLeftExpression();

                    if (expression.isLocalVariableReferenceExpression()) {
                        AbstractLocalVariable lv2 = ((ClassFileLocalVariableReferenceExpression)expression).getLocalVariable();

                        if (syntheticLV1.equals(lv2)) {
                            expression = statements.get(size - 3).getExpression();

                            if (expression.isBinaryOperatorExpression()) {
                                Expression boe2 = expression;

                                expression = boe2.getRightExpression();

                                if (expression.isIntegerConstantExpression() && MINUS_ONE.equals(expression.getIntegerValue())) {
                                    expression = switchStatement.getCondition();

                                    if (expression.isLocalVariableReferenceExpression()) {
                                        AbstractLocalVariable syntheticLV2 = ((ClassFileLocalVariableReferenceExpression)expression).getLocalVariable();

                                        if (syntheticLV2.equals(((ClassFileLocalVariableReferenceExpression)boe2.getLeftExpression()).getLocalVariable())) {
                                            MethodInvocationExpression mie = (MethodInvocationExpression) previousSwitchStatement.getCondition();

                                            if (mie.getName().equals("hashCode") && mie.getDescriptor().equals("()I")) {
                                                // Pattern found ==> Parse cases of the synthetic switch statement 'previousSwitchStatement'
                                                HashMap<Integer, String> map = new HashMap<>();

                                                // Create map<synthetic index -> string>
                                                for (SwitchStatement.Block block : previousSwitchStatement.getBlocks()) {
                                                    BaseStatement stmts = block.getStatements();

                                                    assert (stmts != null) && stmts.isStatements() && (stmts.size() > 0);

                                                    for (Statement stmt : stmts) {
                                                        if (!stmt.isIfStatement()) {
                                                            break;
                                                        }

                                                        expression = stmt.getCondition();

                                                        if (!expression.isMethodInvocationExpression()) {
                                                            break;
                                                        }

                                                        expression = expression.getParameters().getFirst();

                                                        if (!expression.isStringConstantExpression()) {
                                                            break;
                                                        }

                                                        String string = expression.getStringValue();

                                                        expression = stmt.getStatements().getFirst().getExpression().getRightExpression();

                                                        if (!expression.isIntegerConstantExpression()) {
                                                            break;
                                                        }

                                                        Integer index = expression.getIntegerValue();
                                                        map.put(index, string);
                                                    }
                                                }

                                                // Replace synthetic index by string
                                                for (SwitchStatement.Block block : switchStatement.getBlocks()) {
                                                    if (block.isSwitchStatementLabelBlock()) {
                                                        SwitchStatement.LabelBlock lb = (SwitchStatement.LabelBlock) block;

                                                        if (lb.getLabel() != SwitchStatement.DEFAULT_LABEL) {
                                                            SwitchStatement.ExpressionLabel el = (SwitchStatement.ExpressionLabel) lb.getLabel();
                                                            IntegerConstantExpression nce = (IntegerConstantExpression) el.getExpression();
                                                            el.setExpression(new StringConstantExpression(nce.getLineNumber(), map.get(nce.getIntegerValue())));
                                                        }
                                                    } else if (block.isSwitchStatementMultiLabelsBlock()) {
                                                        SwitchStatement.MultiLabelsBlock lmb = (SwitchStatement.MultiLabelsBlock) block;

                                                        for (SwitchStatement.Label label : lmb.getLabels()) {
                                                            if (label != SwitchStatement.DEFAULT_LABEL) {
                                                                SwitchStatement.ExpressionLabel el = (SwitchStatement.ExpressionLabel) label;
                                                                IntegerConstantExpression nce = (IntegerConstantExpression) el.getExpression();
                                                                el.setExpression(new StringConstantExpression(nce.getLineNumber(), map.get(nce.getIntegerValue())));
                                                            }
                                                        }
                                                    }
                                                }

                                                // Replace synthetic key
                                                expression = statements.get(size - 4).getExpression();

                                                if (expression.isBinaryOperatorExpression()) {
                                                    switchStatement.setCondition(expression.getRightExpression());

                                                    // Remove next synthetic statements
                                                    statements.subList(size - 4, size - 1).clear();

                                                    // Remove synthetic local variables
                                                    localVariableMaker.removeLocalVariable(syntheticLV1);
                                                    localVariableMaker.removeLocalVariable(syntheticLV2);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void makeSwitchEnum(ClassFileBodyDeclaration bodyDeclaration, SwitchStatement switchStatement) {
        Expression expression = switchStatement.getCondition().getExpression();

        if (expression.isFieldReferenceExpression()) {
            FieldReferenceExpression fre = (FieldReferenceExpression)expression;

            if (fre.getDescriptor().equals("[I") && fre.getName().startsWith("$SwitchMap$")) {
                ClassFileTypeDeclaration syntheticClassDeclaration = bodyDeclaration.getInnerTypeDeclaration(fre.getInternalTypeName());

                if (syntheticClassDeclaration != null) {
                    // Javac switch-enum pattern
                    bodyDeclaration = (ClassFileBodyDeclaration) syntheticClassDeclaration.getBodyDeclaration();
                    DefaultList<Statement> statements = bodyDeclaration.getMethodDeclarations().get(0).getStatements().getList();
                    updateSwitchStatement(switchStatement, searchSwitchMap(fre, statements.iterator()));
                }
            }
        } else if (expression.isMethodInvocationExpression()) {
            MethodInvocationExpression mie = (MethodInvocationExpression)expression;
            String methodName = mie.getName();

            if (mie.getDescriptor().equals("()[I") && methodName.startsWith("$SWITCH_TABLE$")) {
                // Eclipse compiler switch-enum pattern
                for (ClassFileConstructorOrMethodDeclaration declaration : bodyDeclaration.getMethodDeclarations()) {
                     if (declaration.getMethod().getName().equals(methodName)) {
                         DefaultList<Statement> statements = declaration.getStatements().getList();
                         updateSwitchStatement(switchStatement, statements.listIterator(3));
                         break;
                     }
                }
            }
        }
    }

    protected static Iterator<Statement> searchSwitchMap(FieldReferenceExpression fre, Iterator<Statement> iterator) {
        String name = fre.getName();

        while (iterator.hasNext()) {
            Expression expression = iterator.next().getExpression().getLeftExpression();

            if (expression.isFieldReferenceExpression() && name.equals(expression.getName())) {
                return iterator;
            }
        }

        return iterator;
    }

    protected static void updateSwitchStatement(SwitchStatement switchStatement, Iterator<Statement> iterator) {
        // Create map<synthetic index -> enum name>
        HashMap<Integer, String> map = new HashMap<>();

        while (iterator.hasNext()) {
            Statement statement = iterator.next();

            if (!statement.isTryStatement()) {
                break;
            }

            BaseStatement statements = statement.getTryStatements();

            if (!statements.isList()) {
                break;
            }

            Expression expression = statements.getFirst().getExpression();

            if (!expression.isBinaryOperatorExpression()) {
                break;
            }

            Expression boe = expression;

            expression = boe.getRightExpression();

            if (!expression.isIntegerConstantExpression()) {
                break;
            }

            Integer index = expression.getIntegerValue();

            expression = boe.getLeftExpression();

            if (!expression.isArrayExpression()) {
                break;
            }

            expression = expression.getIndex();

            if (!expression.isMethodInvocationExpression()) {
                break;
            }

            expression = expression.getExpression();

            if (!expression.isFieldReferenceExpression()) {
                break;
            }

            String name = ((FieldReferenceExpression)expression).getName();

            map.put(index, name);
        }

        // Replace synthetic index by enum name
        Expression expression = switchStatement.getCondition().getIndex().getExpression();
        ObjectType type = (ObjectType)expression.getType();

        for (SwitchStatement.Block block : switchStatement.getBlocks()) {
            if (block.isSwitchStatementLabelBlock()) {
                SwitchStatement.LabelBlock lb = (SwitchStatement.LabelBlock) block;

                if (lb.getLabel() != SwitchStatement.DEFAULT_LABEL) {
                    SwitchStatement.ExpressionLabel el = (SwitchStatement.ExpressionLabel) lb.getLabel();
                    IntegerConstantExpression nce = (IntegerConstantExpression) el.getExpression();
                    el.setExpression(new EnumConstantReferenceExpression(nce.getLineNumber(), type, map.get(nce.getIntegerValue())));
                }
            } else if (block.isSwitchStatementMultiLabelsBlock()) {
                SwitchStatement.MultiLabelsBlock lmb = (SwitchStatement.MultiLabelsBlock) block;

                for (SwitchStatement.Label label : lmb.getLabels()) {
                    if (label != SwitchStatement.DEFAULT_LABEL) {
                        SwitchStatement.ExpressionLabel el = (SwitchStatement.ExpressionLabel) label;
                        IntegerConstantExpression nce = (IntegerConstantExpression) el.getExpression();
                        el.setExpression(new EnumConstantReferenceExpression(nce.getLineNumber(), type, map.get(nce.getIntegerValue())));
                    }
                }
            }
        }

        // Replace synthetic key
        switchStatement.setCondition(expression);
    }
}
