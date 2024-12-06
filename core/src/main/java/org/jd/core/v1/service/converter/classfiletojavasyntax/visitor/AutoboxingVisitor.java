/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.visitor;

import org.jd.core.v1.model.javasyntax.declaration.*;
import org.jd.core.v1.model.javasyntax.expression.Expression;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.declaration.ClassFileBodyDeclaration;

import java.util.HashMap;

public class AutoboxingVisitor extends AbstractUpdateExpressionVisitor {
    protected static final HashMap<String, String> VALUEOF_DESCRIPTOR_MAP = new HashMap<>();

    protected static final HashMap<String, String> VALUE_DESCRIPTOR_MAP = new HashMap<>();
    protected static final HashMap<String, String> VALUE_METHODNAME_MAP = new HashMap<>();

    static {
        VALUEOF_DESCRIPTOR_MAP.put("java/lang/Byte", "(B)Ljava/lang/Byte;");
        VALUEOF_DESCRIPTOR_MAP.put("java/lang/Character", "(C)Ljava/lang/Character;");
        VALUEOF_DESCRIPTOR_MAP.put("java/lang/Float", "(F)Ljava/lang/Float;");
        VALUEOF_DESCRIPTOR_MAP.put("java/lang/Integer", "(I)Ljava/lang/Integer;");
        VALUEOF_DESCRIPTOR_MAP.put("java/lang/Long", "(J)Ljava/lang/Long;");
        VALUEOF_DESCRIPTOR_MAP.put("java/lang/Short", "(S)Ljava/lang/Short;");
        VALUEOF_DESCRIPTOR_MAP.put("java/lang/Double", "(D)Ljava/lang/Double;");
        VALUEOF_DESCRIPTOR_MAP.put("java/lang/Boolean", "(Z)Ljava/lang/Boolean;");

        VALUE_DESCRIPTOR_MAP.put("java/lang/Byte", "()B");
        VALUE_DESCRIPTOR_MAP.put("java/lang/Character", "()C");
        VALUE_DESCRIPTOR_MAP.put("java/lang/Float", "()F");
        VALUE_DESCRIPTOR_MAP.put("java/lang/Integer", "()I");
        VALUE_DESCRIPTOR_MAP.put("java/lang/Long", "()J");
        VALUE_DESCRIPTOR_MAP.put("java/lang/Short", "()S");
        VALUE_DESCRIPTOR_MAP.put("java/lang/Double", "()D");
        VALUE_DESCRIPTOR_MAP.put("java/lang/Boolean", "()Z");

        VALUE_METHODNAME_MAP.put("java/lang/Byte", "byteValue");
        VALUE_METHODNAME_MAP.put("java/lang/Character", "charValue");
        VALUE_METHODNAME_MAP.put("java/lang/Float", "floatValue");
        VALUE_METHODNAME_MAP.put("java/lang/Integer", "intValue");
        VALUE_METHODNAME_MAP.put("java/lang/Long", "longValue");
        VALUE_METHODNAME_MAP.put("java/lang/Short", "shortValue");
        VALUE_METHODNAME_MAP.put("java/lang/Double", "doubleValue");
        VALUE_METHODNAME_MAP.put("java/lang/Boolean", "booleanValue");
    }

    @Override
    public void visit(BodyDeclaration declaration) {
        ClassFileBodyDeclaration cfbd = (ClassFileBodyDeclaration)declaration;
        boolean autoBoxingSupported = (cfbd.getClassFile().getMajorVersion() >= 49); // (majorVersion >= Java 5)

        if (autoBoxingSupported) {
            safeAccept(declaration.getMemberDeclarations());
        }
    }

    @Override
    protected Expression updateExpression(Expression expression) {
        if (expression.isMethodInvocationExpression() && expression.getInternalTypeName().startsWith("java/lang/")) {
            int parameterSize = (expression.getParameters() == null) ? 0 : expression.getParameters().size();

            if (expression.getExpression().isObjectTypeReferenceExpression()) {
                // static method invocation
                if ((parameterSize == 1) &&
                        expression.getName().equals("valueOf") &&
                        expression.getDescriptor().equals(VALUEOF_DESCRIPTOR_MAP.get(expression.getInternalTypeName())))
                {
                    return expression.getParameters().getFirst();
                }
            } else {
                // non-static method invocation
                if ((parameterSize == 0) &&
                        expression.getName().equals(VALUE_METHODNAME_MAP.get(expression.getInternalTypeName())) &&
                        expression.getDescriptor().equals(VALUE_DESCRIPTOR_MAP.get(expression.getInternalTypeName())))
                {
                    return expression.getExpression();
                }
            }
        }

        return expression;
    }
}
