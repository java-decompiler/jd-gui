/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.util;

import org.jd.core.v1.model.classfile.ClassFile;
import org.jd.core.v1.model.classfile.ConstantPool;
import org.jd.core.v1.model.classfile.Method;
import org.jd.core.v1.model.classfile.attribute.AttributeBootstrapMethods;
import org.jd.core.v1.model.classfile.attribute.AttributeCode;
import org.jd.core.v1.model.classfile.attribute.BootstrapMethod;
import org.jd.core.v1.model.classfile.constant.*;
import org.jd.core.v1.model.javasyntax.AbstractJavaSyntaxVisitor;
import org.jd.core.v1.model.javasyntax.declaration.*;
import org.jd.core.v1.model.javasyntax.expression.*;
import org.jd.core.v1.model.javasyntax.statement.*;
import org.jd.core.v1.model.javasyntax.type.*;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.cfg.BasicBlock;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.cfg.ControlFlowGraph;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.declaration.*;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.expression.*;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.statement.ClassFileMonitorEnterStatement;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.statement.ClassFileMonitorExitStatement;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.localvariable.AbstractLocalVariable;
import org.jd.core.v1.service.converter.classfiletojavasyntax.visitor.EraseTypeArgumentVisitor;
import org.jd.core.v1.service.converter.classfiletojavasyntax.visitor.RenameLocalVariablesVisitor;
import org.jd.core.v1.service.converter.classfiletojavasyntax.visitor.SearchFirstLineNumberVisitor;
import org.jd.core.v1.util.DefaultList;
import org.jd.core.v1.util.DefaultStack;

import java.util.*;

import static org.jd.core.v1.model.javasyntax.declaration.Declaration.FLAG_PRIVATE;
import static org.jd.core.v1.model.javasyntax.declaration.Declaration.FLAG_STATIC;
import static org.jd.core.v1.model.javasyntax.declaration.Declaration.FLAG_SYNTHETIC;
import static org.jd.core.v1.model.javasyntax.statement.ReturnStatement.RETURN;
import static org.jd.core.v1.model.javasyntax.type.ObjectType.TYPE_CLASS;
import static org.jd.core.v1.model.javasyntax.type.ObjectType.TYPE_OBJECT;
import static org.jd.core.v1.model.javasyntax.type.ObjectType.TYPE_UNDEFINED_OBJECT;
import static org.jd.core.v1.model.javasyntax.type.PrimitiveType.*;

public class ByteCodeParser {
    private static final JsrReturnAddressExpression JSR_RETURN_ADDRESS_EXPRESSION = new JsrReturnAddressExpression();

    private MemberVisitor memberVisitor = new MemberVisitor();
    private SearchFirstLineNumberVisitor searchFirstLineNumberVisitor = new SearchFirstLineNumberVisitor();
    private EraseTypeArgumentVisitor eraseTypeArgumentVisitor = new EraseTypeArgumentVisitor();
    private LambdaParameterNamesVisitor lambdaParameterNamesVisitor = new LambdaParameterNamesVisitor();;
    private RenameLocalVariablesVisitor renameLocalVariablesVisitor = new RenameLocalVariablesVisitor();

    private TypeMaker typeMaker;
    private LocalVariableMaker localVariableMaker;
    protected boolean genericTypesSupported;
    private String internalTypeName;
    private AbstractTypeParametersToTypeArgumentsBinder typeParametersToTypeArgumentsBinder;
    private AttributeBootstrapMethods attributeBootstrapMethods;
    private ClassFileBodyDeclaration bodyDeclaration;
    private Map<String, BaseType> typeBounds;
    private Type returnedType;

    public ByteCodeParser(
            TypeMaker typeMaker, LocalVariableMaker localVariableMaker, ClassFile classFile,
            ClassFileBodyDeclaration bodyDeclaration, ClassFileConstructorOrMethodDeclaration comd) {
        this.typeMaker = typeMaker;
        this.localVariableMaker = localVariableMaker;
        this.genericTypesSupported = (classFile.getMajorVersion() >= 49); // (majorVersion >= Java 5)
        this.internalTypeName = classFile.getInternalTypeName();
        this.attributeBootstrapMethods = classFile.getAttribute("BootstrapMethods");
        this.bodyDeclaration = bodyDeclaration;
        this.returnedType = comd.getReturnedType();
        this.typeBounds = comd.getTypeBounds();

        if (this.genericTypesSupported) {
            this.typeParametersToTypeArgumentsBinder = new Java5TypeParametersToTypeArgumentsBinder(typeMaker, this.internalTypeName, comd);
        } else {
            this.typeParametersToTypeArgumentsBinder = new JavaTypeParametersToTypeArgumentsBinder();
        }
    }

    @SuppressWarnings("unchecked")
    public void parse(BasicBlock basicBlock, Statements statements, DefaultStack<Expression> stack) {
        ControlFlowGraph cfg = basicBlock.getControlFlowGraph();
        int fromOffset = basicBlock.getFromOffset();
        int toOffset = basicBlock.getToOffset();

        Method method = cfg.getMethod();
        ConstantPool constants = method.getConstants();
        byte[] code = method.<AttributeCode>getAttribute("Code").getCode();
        boolean syntheticFlag = (method.getAccessFlags() & FLAG_SYNTHETIC) != 0;

        Expression indexRef, arrayRef, valueRef, expression1, expression2, expression3;
        Type type1, type2, type3;
        ConstantMemberRef constantMemberRef;
        ConstantNameAndType constantNameAndType;
        String typeName, name, descriptor;
        ObjectType ot;
        int i, count, value;
        AbstractLocalVariable localVariable;

        for (int offset=fromOffset; offset<toOffset; offset++) {
            int opcode = code[offset] & 255;
            int lineNumber = syntheticFlag ? Expression.UNKNOWN_LINE_NUMBER : cfg.getLineNumber(offset);

            switch (opcode) {
                case 0: // NOP
                    break;
                case 1: // ACONST_NULL
                    stack.push(new NullExpression(lineNumber, TYPE_UNDEFINED_OBJECT));
                    break;
                case 2: // ICONST_M1
                    stack.push(new IntegerConstantExpression(lineNumber, MAYBE_NEGATIVE_BYTE_TYPE, -1));
                    break;
                case 3: case 4: // ICONST_0, ICONST_1
                    stack.push(new IntegerConstantExpression(lineNumber, MAYBE_BOOLEAN_TYPE, opcode - 3));
                    break;
                case 5: case 6: case 7: case 8: // ICONST_2 ... ICONST_5
                    stack.push(new IntegerConstantExpression(lineNumber, MAYBE_BYTE_TYPE, opcode - 3));
                    break;
                case 9: case 10: // LCONST_0, LCONST_1
                    stack.push(new LongConstantExpression(lineNumber, (long)(opcode - 9)));
                    break;
                case 11: case 12: case 13: // FCONST_0, FCONST_1, FCONST_2
                    stack.push(new FloatConstantExpression(lineNumber, (float)(opcode - 11)));
                    break;
                case 14: case 15: // DCONST_0, DCONST_1
                    stack.push(new DoubleConstantExpression(lineNumber, (double)(opcode - 14)));
                    break;
                case 16: // BIPUSH
                    value = (byte)(code[++offset] & 255);
                    stack.push(new IntegerConstantExpression(lineNumber, PrimitiveTypeUtil.getPrimitiveTypeFromValue(value), value));
                    break;
                case 17: // SIPUSH
                    value = (short)(((code[++offset] & 255) << 8) | (code[++offset] & 255));
                    stack.push(new IntegerConstantExpression(lineNumber, PrimitiveTypeUtil.getPrimitiveTypeFromValue(value), value));
                    break;
                case 18: // LDC
                    parseLDC(stack, constants, lineNumber, constants.getConstant(code[++offset] & 255));
                    break;
                case 19: case 20: // LDC_W, LDC2_W
                    parseLDC(stack, constants, lineNumber, constants.getConstant(((code[++offset] & 255) << 8) | (code[++offset] & 255)));
                    break;
                case 21: // ILOAD
                    localVariable = localVariableMaker.getLocalVariable(code[++offset] & 255, offset);
                    parseILOAD(statements, stack, lineNumber, offset, localVariable);
                    break;
                case 22: case 23: case 24: // LLOAD, FLOAD, DLOAD
                    localVariable = localVariableMaker.getLocalVariable(code[++offset] & 255, offset);
                    stack.push(new ClassFileLocalVariableReferenceExpression(lineNumber, offset, localVariable));
                    break;
                case 25: // ALOAD
                    i = code[++offset] & 255;
                    localVariable = localVariableMaker.getLocalVariable(i, offset);
                    if ((i == 0) && ((method.getAccessFlags() & FLAG_STATIC) == 0)) {
                        stack.push(new ThisExpression(lineNumber, localVariable.getType()));
                    } else {
                        stack.push(new ClassFileLocalVariableReferenceExpression(lineNumber, offset, localVariable));
                    }
                    break;
                case 26: case 27: case 28: case 29: // ILOAD_0 ... ILOAD_3
                    localVariable = localVariableMaker.getLocalVariable(opcode - 26, offset);
                    parseILOAD(statements, stack, lineNumber, offset, localVariable);
                    break;
                case 30: case 31: case 32: case 33: // LLOAD_0 ... LLOAD_3
                    localVariable = localVariableMaker.getLocalVariable(opcode - 30, offset);
                    stack.push(new ClassFileLocalVariableReferenceExpression(lineNumber, offset, localVariable));
                    break;
                case 34: case 35: case 36: case 37: // FLOAD_0 ... FLOAD_3
                    localVariable = localVariableMaker.getLocalVariable(opcode - 34, offset);
                    stack.push(new ClassFileLocalVariableReferenceExpression(lineNumber, offset, localVariable));
                    break;
                case 38: case 39: case 40: case 41: // DLOAD_0 ... DLOAD_3
                    localVariable = localVariableMaker.getLocalVariable(opcode - 38, offset);
                    stack.push(new ClassFileLocalVariableReferenceExpression(lineNumber, offset, localVariable));
                    break;
                case 42: // ALOAD_0
                    localVariable = localVariableMaker.getLocalVariable(0, offset);
                    if ((method.getAccessFlags() & FLAG_STATIC) == 0) {
                        stack.push(new ThisExpression(lineNumber, localVariable.getType()));
                    } else {
                        stack.push(new ClassFileLocalVariableReferenceExpression(lineNumber, offset, localVariable));
                    }
                    break;
                case 43: case 44: case 45: // ALOAD_1 ... ALOAD_3
                    localVariable = localVariableMaker.getLocalVariable(opcode - 42, offset);
                    stack.push(new ClassFileLocalVariableReferenceExpression(lineNumber, offset, localVariable));
                    break;
                case 46: case 47: case 48: case 49: case 50: case 51: case 52: case 53: // IALOAD, LALOAD, FALOAD, DALOAD, AALOAD, BALOAD, CALOAD, SALOAD
                    indexRef = stack.pop();
                    arrayRef = stack.pop();
                    stack.push(new ArrayExpression(lineNumber, arrayRef, indexRef));
                    break;
                case 54: case 55: case 56: case 57: // ISTORE, LSTORE, FSTORE, DSTORE
                    localVariable = getLocalVariableInAssignment(code[++offset] & 255, offset + 2, valueRef = stack.pop());
                    parseSTORE(statements, stack, lineNumber, offset, localVariable, valueRef);
                    break;
                case 58: // ASTORE
                    localVariable = getLocalVariableInAssignment(code[++offset] & 255, offset + 1, valueRef = stack.pop());
                    parseASTORE(statements, stack, lineNumber, offset, localVariable, valueRef);
                    break;
                case 59: case 60: case 61: case 62: // ISTORE_0 ... ISTORE_3
                    localVariable = getLocalVariableInAssignment(opcode - 59, offset + 1, valueRef = stack.pop());
                    parseSTORE(statements, stack, lineNumber, offset, localVariable, valueRef);
                    break;
                case 63: case 64: case 65: case 66: // LSTORE_0 ... LSTORE_3
                    localVariable = getLocalVariableInAssignment(opcode - 63, offset + 1, valueRef = stack.pop());
                    parseSTORE(statements, stack, lineNumber, offset, localVariable, valueRef);
                    break;
                case 67: case 68: case 69: case 70: // FSTORE_0 ... FSTORE_3
                    localVariable = getLocalVariableInAssignment(opcode - 67, offset + 1, valueRef = stack.pop());
                    parseSTORE(statements, stack, lineNumber, offset, localVariable, valueRef);
                    break;
                case 71: case 72: case 73: case 74: // DSTORE_0 ... DSTORE_3
                    localVariable = getLocalVariableInAssignment(opcode - 71, offset + 1, valueRef = stack.pop());
                    parseSTORE(statements, stack, lineNumber, offset, localVariable, valueRef);
                    break;
                case 75: case 76: case 77: case 78: // ASTORE_0 ... ASTORE_3
                    localVariable = getLocalVariableInAssignment(opcode - 75, offset + 1, valueRef = stack.pop());
                    parseASTORE(statements, stack, lineNumber, offset, localVariable, valueRef);
                    break;
                case 79: // IASTORE
                    valueRef = stack.pop();
                    indexRef = stack.pop();
                    arrayRef = stack.pop();
                    type1 = arrayRef.getType();
                    statements.add(new ExpressionStatement(new BinaryOperatorExpression(lineNumber, type1.createType(type1.getDimension()-1), new ArrayExpression(lineNumber, arrayRef, indexRef), "=", valueRef, 16)));
                    break;
                case 80: // LASTORE
                    valueRef = stack.pop();
                    indexRef = stack.pop();
                    arrayRef = stack.pop();
                    statements.add(new ExpressionStatement(new BinaryOperatorExpression(lineNumber, TYPE_LONG, new ArrayExpression(lineNumber, arrayRef, indexRef), "=", valueRef, 16)));
                    break;
                case 81: // FASTORE
                    valueRef = stack.pop();
                    indexRef = stack.pop();
                    arrayRef = stack.pop();
                    statements.add(new ExpressionStatement(new BinaryOperatorExpression(lineNumber, TYPE_FLOAT, new ArrayExpression(lineNumber, arrayRef, indexRef), "=", valueRef, 16)));
                    break;
                case 82: // DASTORE
                    valueRef = stack.pop();
                    indexRef = stack.pop();
                    arrayRef = stack.pop();
                    statements.add(new ExpressionStatement(new BinaryOperatorExpression(lineNumber, TYPE_DOUBLE, new ArrayExpression(lineNumber, arrayRef, indexRef), "=", valueRef, 16)));
                    break;
                case 83: // AASTORE
                    valueRef = stack.pop();
                    indexRef = stack.pop();
                    arrayRef = stack.pop();
                    type1 = arrayRef.getType();
                    type2 = type1.createType(type1.getDimension()>0 ? type1.getDimension()-1 : 0);
                    typeParametersToTypeArgumentsBinder.bindParameterTypesWithArgumentTypes(type2, valueRef);
                    statements.add(new ExpressionStatement(new BinaryOperatorExpression(lineNumber, type2, new ArrayExpression(lineNumber, arrayRef, indexRef), "=", valueRef, 16)));
                    break;
                case 84: // BASTORE
                    valueRef = stack.pop();
                    indexRef = stack.pop();
                    arrayRef = stack.pop();
                    statements.add(new ExpressionStatement(new BinaryOperatorExpression(lineNumber, TYPE_BYTE, new ArrayExpression(lineNumber, arrayRef, indexRef), "=", valueRef, 16)));
                    break;
                case 85: // CASTORE
                    valueRef = stack.pop();
                    indexRef = stack.pop();
                    arrayRef = stack.pop();
                    statements.add(new ExpressionStatement(new BinaryOperatorExpression(lineNumber, TYPE_CHAR, new ArrayExpression(lineNumber, arrayRef, indexRef), "=", valueRef, 16)));
                    break;
                case 86: // SASTORE
                    valueRef = stack.pop();
                    indexRef = stack.pop();
                    arrayRef = stack.pop();
                    statements.add(new ExpressionStatement(new BinaryOperatorExpression(lineNumber, TYPE_SHORT, new ArrayExpression(lineNumber, arrayRef, indexRef), "=", valueRef, 16)));
                    break;
                case 87: case 88: // POP, POP2
                    expression1 = stack.pop();
                    if (!expression1.isLocalVariableReferenceExpression() && !expression1.isFieldReferenceExpression()) {
                        typeParametersToTypeArgumentsBinder.bindParameterTypesWithArgumentTypes(TYPE_OBJECT, expression1);
                        statements.add(new ExpressionStatement(expression1));
                    }
                    break;
                case 89: // DUP : ..., value => ..., value, value
                    expression1 = stack.pop();
                    stack.push(expression1);
                    stack.push(expression1);
                    break;
                case 90: // DUP_X1 : ..., value2, value1 => ..., value1, value2, value1
                    expression1 = stack.pop();
                    expression2 = stack.pop();
                    stack.push(expression1);
                    stack.push(expression2);
                    stack.push(expression1);
                    break;
                case 91: // DUP_X2
                    expression1 = stack.pop();
                    expression2 = stack.pop();

                    type2 = expression2.getType();

                    if (TYPE_LONG.equals(type2) || TYPE_DOUBLE.equals(type2)) {
                        // ..., value2, value1 => ..., value1, value2, value1
                        stack.push(expression1);
                        stack.push(expression2);
                        stack.push(expression1);
                    } else {
                        // ..., value3, value2, value1 => ..., value1, value3, value2, value1
                        expression3 = stack.pop();
                        stack.push(expression1);
                        stack.push(expression3);
                        stack.push(expression2);
                        stack.push(expression1);
                    }
                    break;
                case 92: // DUP2
                    expression1 = stack.pop();

                    type1 = expression1.getType();

                    if (TYPE_LONG.equals(type1) || TYPE_DOUBLE.equals(type1)) {
                        // ..., value => ..., value, value
                        stack.push(expression1);
                        stack.push(expression1);
                    } else {
                        // ..., value2, value1 => ..., value2, value1, value2, value1
                        expression2 = stack.pop();
                        stack.push(expression2);
                        stack.push(expression1);
                        stack.push(expression2);
                        stack.push(expression1);
                    }
                    break;
                case 93: // DUP2_X1
                    expression1 = stack.pop();
                    expression2 = stack.pop();

                    type1 = expression1.getType();

                    if (TYPE_LONG.equals(type1) || TYPE_DOUBLE.equals(type1)) {
                        // ..., value2, value1 => ..., value1, value2, value1
                        stack.push(expression1);
                        stack.push(expression2);
                        stack.push(expression1);
                    } else {
                        // ..., value3, value2, value1 => ..., value2, value1, value3, value2, value1
                        expression3 = stack.pop();
                        stack.push(expression2);
                        stack.push(expression1);
                        stack.push(expression3);
                        stack.push(expression2);
                        stack.push(expression1);
                    }
                    break;
                case 94: // DUP2_X2
                    expression1 = stack.pop();
                    expression2 = stack.pop();

                    type1 = expression1.getType();

                    if (TYPE_LONG.equals(type1) || TYPE_DOUBLE.equals(type1)) {
                        type2 = expression2.getType();

                        if (TYPE_LONG.equals(type2) || TYPE_DOUBLE.equals(type2)) {
                            // ..., value2, value1 => ..., value1, value2, value1
                            stack.push(expression1);
                            stack.push(expression2);
                            stack.push(expression1);
                        } else {
                            // ..., value3, value2, value1 => ..., value1, value3, value2, value1
                            expression3 = stack.pop();
                            stack.push(expression1);
                            stack.push(expression3);
                            stack.push(expression2);
                            stack.push(expression1);
                        }
                    } else {
                        expression3 = stack.pop();
                        type3 = expression3.getType();

                        if (TYPE_LONG.equals(type3) || TYPE_DOUBLE.equals(type3)) {
                            // ..., value3, value2, value1 => ..., value2, value1, value3, value2, value1
                            stack.push(expression2);
                            stack.push(expression1);
                            stack.push(expression3);
                            stack.push(expression2);
                            stack.push(expression1);
                        } else {
                            // ..., value4, value3, value2, value1 => ..., value2, value1, value4, value3, value2, value1
                            Expression expression4 = stack.pop();
                            stack.push(expression2);
                            stack.push(expression1);
                            stack.push(expression4);
                            stack.push(expression3);
                            stack.push(expression2);
                            stack.push(expression1);
                        }
                    }
                    break;
                case 95: // SWAP : ..., value2, value1 => ..., value1, value2
                    expression1 = stack.pop();
                    expression2 = stack.pop();
                    stack.push(expression1);
                    stack.push(expression2);
                    break;
                case 96: // IADD
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(newIntegerBinaryOperatorExpression(lineNumber, expression1, "+", expression2, 6));
                    break;
                case 97: // LADD
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(new BinaryOperatorExpression(lineNumber, TYPE_LONG, expression1, "+", expression2, 6));
                    break;
                case 98: // FADD
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(new BinaryOperatorExpression(lineNumber, TYPE_FLOAT, expression1, "+", expression2, 6));
                    break;
                case 99: // DADD
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(new BinaryOperatorExpression(lineNumber, TYPE_DOUBLE, expression1, "+", expression2, 6));
                    break;
                case 100: // ISUB
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(newIntegerBinaryOperatorExpression(lineNumber, expression1, "-", expression2, 6));
                    break;
                case 101: // LSUB
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(new BinaryOperatorExpression(lineNumber, TYPE_LONG, expression1, "-", expression2, 6));
                    break;
                case 102: // FSUB
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(new BinaryOperatorExpression(lineNumber, TYPE_FLOAT, expression1, "-", expression2, 6));
                    break;
                case 103: // DSUB
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(new BinaryOperatorExpression(lineNumber, TYPE_DOUBLE, expression1, "-", expression2, 6));
                    break;
                case 104: // IMUL
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(newIntegerBinaryOperatorExpression(lineNumber, expression1, "*", expression2, 5));
                    break;
                case 105: // LMUL
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(new BinaryOperatorExpression(lineNumber, TYPE_LONG, expression1, "*", expression2, 5));
                    break;
                case 106: // FMUL
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(new BinaryOperatorExpression(lineNumber, TYPE_FLOAT, expression1, "*", expression2, 5));
                    break;
                case 107: // DMUL
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(new BinaryOperatorExpression(lineNumber, TYPE_DOUBLE, expression1, "*", expression2, 5));
                    break;
                case 108: // IDIV
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(newIntegerBinaryOperatorExpression(lineNumber, expression1, "/", expression2, 5));
                    break;
                case 109: // LDIV
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(new BinaryOperatorExpression(lineNumber, TYPE_LONG, expression1, "/", expression2, 5));
                    break;
                case 110: // FDIV
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(new BinaryOperatorExpression(lineNumber, TYPE_FLOAT, expression1, "/", expression2, 5));
                    break;
                case 111: // DDIV
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(new BinaryOperatorExpression(lineNumber, TYPE_DOUBLE, expression1, "/", expression2, 5));
                    break;
                case 112: // IREM
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(newIntegerBinaryOperatorExpression(lineNumber, expression1, "%", expression2, 5));
                    break;
                case 113: // LREM
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(new BinaryOperatorExpression(lineNumber, TYPE_LONG, expression1, "%", expression2, 5));
                    break;
                case 114: // FREM
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(new BinaryOperatorExpression(lineNumber, TYPE_FLOAT, expression1, "%", expression2, 5));
                    break;
                case 115: // DREM
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(new BinaryOperatorExpression(lineNumber, TYPE_DOUBLE, expression1, "%", expression2, 5));
                    break;
                case 116: case 117: case 118: case 119: // INEG, LNEG, FNEG, DNEG
                    stack.push(newPreArithmeticOperatorExpression(lineNumber, "-", stack.pop()));
                    break;
                case 120: // ISHL
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(newIntegerBinaryOperatorExpression(lineNumber, expression1, "<<", expression2, 7));
                    break;
                case 121: // LSHL
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(new BinaryOperatorExpression(lineNumber, TYPE_LONG, expression1, "<<", expression2, 7));
                    break;
                case 122: // ISHR
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(new BinaryOperatorExpression(lineNumber, TYPE_INT, expression1, ">>", expression2, 7));
                    break;
                case 123: // LSHR
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(new BinaryOperatorExpression(lineNumber, TYPE_LONG, expression1, ">>", expression2, 7));
                    break;
                case 124: // IUSHR
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(newIntegerBinaryOperatorExpression(lineNumber, expression1, ">>>", expression2, 7));
                    break;
                case 125: // LUSHR
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(new BinaryOperatorExpression(lineNumber, TYPE_LONG, expression1, ">>>", expression2, 7));
                    break;
                case 126: // IAND
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(newIntegerOrBooleanBinaryOperatorExpression(lineNumber, expression1, "&", expression2, 10));
                    break;
                case 127: // LAND
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(new BinaryOperatorExpression(lineNumber, TYPE_LONG, expression1, "&", expression2, 10));
                    break;
                case 128: // IOR
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(newIntegerOrBooleanBinaryOperatorExpression(lineNumber, expression1, "|", expression2, 12));
                    break;
                case 129: // LOR
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(new BinaryOperatorExpression(lineNumber, TYPE_LONG, expression1, "|", expression2, 12));
                    break;
                case 130: // IXOR
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(newIntegerOrBooleanBinaryOperatorExpression(lineNumber, expression1, "^", expression2, 11));
                    break;
                case 131: // LXOR
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(new BinaryOperatorExpression(lineNumber, TYPE_LONG, expression1, "^", expression2, 11));
                    break;
                case 132: // IINC
                    localVariable = localVariableMaker.getLocalVariable(code[++offset] & 255, offset);
                    parseIINC(statements, stack, lineNumber, offset, localVariable, (byte)(code[++offset] & 255));
                    break;
                case 133: // I2L
                    stack.push(new CastExpression(lineNumber, TYPE_LONG, stack.pop(), false));
                    break;
                case 134: // I2F
                    stack.push(new CastExpression(lineNumber, TYPE_FLOAT, stack.pop(), false));
                    break;
                case 135: // I2D
                    stack.push(new CastExpression(lineNumber, TYPE_DOUBLE, stack.pop(), false));
                    break;
                case 136: // L2I
                    stack.push(new CastExpression(lineNumber, TYPE_INT, forceExplicitCastExpression(stack.pop())));
                    break;
                case 137: // L2F
                    stack.push(new CastExpression(lineNumber, TYPE_FLOAT, forceExplicitCastExpression(stack.pop())));
                    break;
                case 138: // L2D
                    stack.push(new CastExpression(lineNumber, TYPE_DOUBLE, stack.pop(), false));
                    break;
                case 139: // F2I
                    stack.push(new CastExpression(lineNumber, TYPE_INT, forceExplicitCastExpression(stack.pop())));
                    break;
                case 140: // F2L
                    stack.push(new CastExpression(lineNumber, TYPE_LONG, forceExplicitCastExpression(stack.pop())));
                    break;
                case 141: // F2D
                    stack.push(new CastExpression(lineNumber, TYPE_DOUBLE, stack.pop(), false));
                    break;
                case 142: // D2I
                    stack.push(new CastExpression(lineNumber, TYPE_INT, forceExplicitCastExpression(stack.pop())));
                    break;
                case 143: // D2L
                    stack.push(new CastExpression(lineNumber, TYPE_LONG, forceExplicitCastExpression(stack.pop())));
                    break;
                case 144: // D2F
                    stack.push(new CastExpression(lineNumber, TYPE_FLOAT, forceExplicitCastExpression(stack.pop())));
                    break;
                case 145: // I2B
                    stack.push(new CastExpression(lineNumber, TYPE_BYTE, forceExplicitCastExpression(stack.pop())));
                    break;
                case 146: // I2C
                    stack.push(new CastExpression(lineNumber, TYPE_CHAR, forceExplicitCastExpression(stack.pop())));
                    break;
                case 147: // I2S
                    stack.push(new CastExpression(lineNumber, TYPE_SHORT, forceExplicitCastExpression(stack.pop())));
                    break;
                case 148: case 149: case 150: case 151: case 152: // LCMP, FCMPL, FCMPG, DCMPL, DCMPG
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(new ClassFileCmpExpression(lineNumber, expression1, expression2));
                    break;
                case 153: // IFEQ
                    parseIF(stack, lineNumber, basicBlock, "!=", "==", 8);
                    offset += 2; // Skip branch offset
                    break;
                case 154: // IFNE
                    parseIF(stack, lineNumber, basicBlock, "==", "!=", 8);
                    offset += 2; // Skip branch offset
                    break;
                case 155: // IFLT
                    parseIF(stack, lineNumber, basicBlock, ">=", "<", 7);
                    offset += 2; // Skip branch offset
                    break;
                case 156: // IFGE
                    parseIF(stack, lineNumber, basicBlock, "<", ">=", 7);
                    offset += 2; // Skip branch offset
                    break;
                case 157: // IFGT
                    parseIF(stack, lineNumber, basicBlock, "<=", ">", 7);
                    offset += 2; // Skip branch offset
                    break;
                case 158: // IFLE
                    parseIF(stack, lineNumber, basicBlock, ">", "<=", 7);
                    offset += 2; // Skip branch offset
                    break;
                case 159: // IF_ICMPEQ
                case 165: // IF_ACMPEQ
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(newIntegerOrBooleanComparisonOperatorExpression(lineNumber, expression1, basicBlock.mustInverseCondition() ? "!=" : "==", expression2, 9));
                    offset += 2; // Skip branch offset
                    break;
                case 160: // IF_ICMPNE
                case 166: // IF_ACMPNE
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(newIntegerOrBooleanComparisonOperatorExpression(lineNumber, expression1, basicBlock.mustInverseCondition() ? "==" : "!=", expression2, 9));
                    offset += 2; // Skip branch offset
                    break;
                case 161: // IF_ICMPLT
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(newIntegerComparisonOperatorExpression(lineNumber, expression1, basicBlock.mustInverseCondition() ? ">=" : "<", expression2, 8));
                    offset += 2; // Skip branch offset
                    break;
                case 162: // IF_ICMPGE
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(newIntegerComparisonOperatorExpression(lineNumber, expression1, basicBlock.mustInverseCondition() ? "<" : ">=", expression2, 8));
                    offset += 2; // Skip branch offset
                    break;
                case 163: // IF_ICMPGT
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(newIntegerComparisonOperatorExpression(lineNumber, expression1, basicBlock.mustInverseCondition() ? "<=" : ">", expression2, 8));
                    offset += 2; // Skip branch offset
                    break;
                case 164: // IF_ICMPLE
                    expression2 = stack.pop();
                    expression1 = stack.pop();
                    stack.push(newIntegerComparisonOperatorExpression(lineNumber, expression1, basicBlock.mustInverseCondition() ? ">" : "<=", expression2, 8));
                    offset += 2; // Skip branch offset
                    break;
                case 168: // JSR
                    stack.push(JSR_RETURN_ADDRESS_EXPRESSION);
                case 167: // GOTO
                    offset += 2; // Skip branch offset
                    break;
                case 169: // RET
                    offset++; // Skip index
                    break;
                case 170: // TABLESWITCH
                    offset = (offset+4) & 0xFFFC; // Skip padding
                    offset += 4; // Skip default offset

                    int low = ((code[offset++] & 255) << 24) | ((code[offset++] & 255) << 16) | ((code[offset++] & 255) << 8 ) |  (code[offset++] & 255);
                    int high = ((code[offset++] & 255) << 24) | ((code[offset++] & 255) << 16) | ((code[offset++] & 255) << 8 ) |  (code[offset++] & 255);

                    offset += (4 * (high - low + 1)) - 1;

                    statements.add(new SwitchStatement(stack.pop(), new DefaultList<>(high - low + 2)));
                    break;
                case 171: // LOOKUPSWITCH
                    offset = (offset+4) & 0xFFFC; // Skip padding
                    offset += 4; // Skip default offset

                    count = ((code[offset++] & 255) << 24) | ((code[offset++] & 255) << 16) | ((code[offset++] & 255) << 8 ) |  (code[offset++] & 255);

                    offset += (8 * count) - 1;

                    statements.add(new SwitchStatement(stack.pop(), new DefaultList<>(count+1)));
                    break;
                case 172: case 173: case 174: case 175: case 176: // IRETURN, LRETURN, FRETURN, DRETURN, ARETURN
                    parseXRETURN(statements, stack, lineNumber);
                    break;
                case 177: // RETURN
                    statements.add(RETURN);
                    break;
                case 178: // GETSTATIC
                    parseGetStatic(stack, constants, lineNumber, ((code[++offset] & 255) << 8) | (code[++offset] & 255));
                    break;
                case 179: // PUTSTATIC
                    parsePutStatic(statements, stack, constants, lineNumber, ((code[++offset] & 255) << 8) | (code[++offset] & 255));
                    break;
                case 180: // GETFIELD
                    parseGetField(stack, constants, lineNumber, ((code[++offset] & 255) << 8) | (code[++offset] & 255));
                    break;
                case 181: // PUTFIELD
                    parsePutField(statements, stack, constants, lineNumber, ((code[++offset] & 255) << 8) | (code[++offset] & 255));
                    break;
                case 182: case 183: case 184: case 185: // INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC, INVOKEINTERFACE
                    constantMemberRef = constants.getConstant( ((code[++offset] & 255) << 8) | (code[++offset] & 255) );
                    typeName = constants.getConstantTypeName(constantMemberRef.getClassIndex());
                    ot = typeMaker.makeFromDescriptorOrInternalTypeName(typeName);
                    constantNameAndType = constants.getConstant(constantMemberRef.getNameAndTypeIndex());
                    name = constants.getConstantUtf8(constantNameAndType.getNameIndex());
                    descriptor = constants.getConstantUtf8(constantNameAndType.getDescriptorIndex());
                    TypeMaker.MethodTypes methodTypes = makeMethodTypes(ot.getInternalName(), name, descriptor);
                    BaseExpression parameters = extractParametersFromStack(statements, stack, methodTypes.parameterTypes);

                    if (opcode == 184) { // INVOKESTATIC
                        expression1 = typeParametersToTypeArgumentsBinder.newMethodInvocationExpression(lineNumber, new ObjectTypeReferenceExpression(lineNumber, ot), ot, name, descriptor, methodTypes, parameters);
                        if (TYPE_VOID.equals(methodTypes.returnedType)) {
                            typeParametersToTypeArgumentsBinder.bindParameterTypesWithArgumentTypes(TYPE_OBJECT, expression1);
                            statements.add(new ExpressionStatement(expression1));
                        } else {
                            stack.push(expression1);
                        }
                    } else {
                        expression1 = stack.pop();
                        if (expression1.isLocalVariableReferenceExpression()) {
                            ((ClassFileLocalVariableReferenceExpression)expression1).getLocalVariable().typeOnLeft(typeBounds, ot);
                        }
                        if (opcode == 185) { // INVOKEINTERFACE
                            offset += 2; // Skip 'count' and one byte
                        }
                        if (TYPE_VOID.equals(methodTypes.returnedType)) {
                            if ((opcode == 183) && // INVOKESPECIAL
                                "<init>".equals(name)) {

                                if (expression1.isNewExpression()) {
                                    typeParametersToTypeArgumentsBinder.updateNewExpression((ClassFileNewExpression)expression1, descriptor, methodTypes, parameters);
                                } else if (ot.getDescriptor().equals(expression1.getType().getDescriptor())) {
                                    statements.add(new ExpressionStatement(typeParametersToTypeArgumentsBinder.newConstructorInvocationExpression(lineNumber, ot, descriptor, methodTypes, parameters)));
                                } else {
                                    statements.add(new ExpressionStatement(typeParametersToTypeArgumentsBinder.newSuperConstructorInvocationExpression(lineNumber, ot, descriptor, methodTypes, parameters)));
                                }
                            } else {
                                expression1 = typeParametersToTypeArgumentsBinder.newMethodInvocationExpression(
                                    lineNumber, getMethodInstanceReference(expression1, ot,  name, descriptor), ot,  name, descriptor, methodTypes, parameters);
                                typeParametersToTypeArgumentsBinder.bindParameterTypesWithArgumentTypes(TYPE_OBJECT, expression1);
                                statements.add(new ExpressionStatement(expression1));
                            }
                        } else {
                            if (opcode == 182) { // INVOKEVIRTUAL
                                if ("toString".equals(name) && "()Ljava/lang/String;".equals(descriptor)) {
                                    typeName = constants.getConstantTypeName(constantMemberRef.getClassIndex());
                                    if ("java/lang/StringBuilder".equals(typeName) || "java/lang/StringBuffer".equals(typeName)) {
                                        stack.push(StringConcatenationUtil.create(expression1, lineNumber, typeName));
                                        break;
                                    }
                                }
                            }
                            stack.push(typeParametersToTypeArgumentsBinder.newMethodInvocationExpression(
                                lineNumber, getMethodInstanceReference(expression1, ot,  name, descriptor), ot, name, descriptor, methodTypes, parameters));
                        }
                    }
                    break;
                case 186: // INVOKEDYNAMIC
                    parseInvokeDynamic(statements, stack, constants, lineNumber,  ((code[++offset] & 255) << 8) | (code[++offset] & 255));
                    offset += 2; // Skip 2 bytes
                    break;
                case 187: // NEW
                    typeName = constants.getConstantTypeName( ((code[++offset] & 255) << 8) | (code[++offset] & 255) );
                    stack.push(newNewExpression(lineNumber, typeName));
                    break;
                case 188: // NEWARRAY
                    type1 = PrimitiveTypeUtil.getPrimitiveTypeFromTag( (code[++offset] & 255) ).createType(1);
                    stack.push(new NewArray(lineNumber, type1, stack.pop()));
                    break;
                case 189: // ANEWARRAY
                    typeName = constants.getConstantTypeName( ((code[++offset] & 255) << 8) | (code[++offset] & 255) );
                    if (typeName.charAt(0) == '[') {
                        type1 = typeMaker.makeFromDescriptor(typeName);
                        type1 = type1.createType(type1.getDimension()+1);
                    } else {
                        type1 = typeMaker.makeFromInternalTypeName(typeName).createType(1);
                    }
                    if (typeName.endsWith(TYPE_CLASS.getInternalName())) {
                        ot = (ObjectType)type1;
                        if (ot.getTypeArguments() == null) {
                            type1 = ot.createType(WildcardTypeArgument.WILDCARD_TYPE_ARGUMENT);
                        }
                    }
                    stack.push(new NewArray(lineNumber, type1, stack.pop()));
                    break;
                case 190: // ARRAYLENGTH
                    stack.push(new LengthExpression(lineNumber, stack.pop()));
                    break;
                case 191: // ATHROW
                    statements.add(new ThrowStatement(stack.pop()));
                    break;
                case 192: // CHECKCAST
                    typeName = constants.getConstantTypeName( ((code[++offset] & 255) << 8) | (code[++offset] & 255) );
                    type1 = typeMaker.makeFromDescriptorOrInternalTypeName(typeName);
                    expression1 = stack.peek();
                    if (type1.isObjectType() && expression1.getType().isObjectType() && typeMaker.isRawTypeAssignable((ObjectType) type1, (ObjectType) expression1.getType())) {
                        // Ignore cast
                    } else if (expression1.isCastExpression()) {
                        // Skip double cast
                        ((CastExpression) expression1).setType(type1);
                    } else {
                        searchFirstLineNumberVisitor.init();
                        expression1.accept(searchFirstLineNumberVisitor);
                        stack.push(new CastExpression(searchFirstLineNumberVisitor.getLineNumber(), type1, forceExplicitCastExpression(stack.pop())));
                    }
                    break;
                case 193: // INSTANCEOF
                    typeName = constants.getConstantTypeName( ((code[++offset] & 255) << 8) | (code[++offset] & 255) );
                    type1 = typeMaker.makeFromDescriptorOrInternalTypeName(typeName);
                    if (type1 == null) {
                        type1 = PrimitiveTypeUtil.getPrimitiveTypeFromDescriptor(typeName);
                    }
                    stack.push(new InstanceOfExpression(lineNumber, stack.pop(), type1));
                    break;
                case 194: // MONITORENTER
                    statements.add(new ClassFileMonitorEnterStatement(stack.pop()));
                    break;
                case 195: // MONITOREXIT
                    statements.add(new ClassFileMonitorExitStatement(stack.pop()));
                    break;
                case 196: // WIDE
                    opcode = code[++offset] & 255;
                    i = ((code[++offset] & 255) << 8) | (code[++offset] & 255);

                    if (opcode == 132) { // IINC
                        count = (short)( ((code[++offset] & 255) << 8) | (code[++offset] & 255) );
                        parseIINC(statements, stack, lineNumber, offset, localVariableMaker.getLocalVariable(i, offset), count);
                    } else {
                        switch (opcode) {
                            case 21: // ILOAD
                                localVariable = localVariableMaker.getLocalVariable(i, offset + 4);
                                parseILOAD(statements, stack, offset, lineNumber, localVariable);
                                break;
                            case 22: case 23: case 24: case 25: // LLOAD, FLOAD, DLOAD, ALOAD
                                stack.push(new ClassFileLocalVariableReferenceExpression(lineNumber, offset, localVariableMaker.getLocalVariable(i, offset)));
                                break;
                            case 54: // ISTORE
                                localVariable = getLocalVariableInAssignment(i, offset + 4, valueRef = stack.pop());
                                statements.add(new ExpressionStatement(new BinaryOperatorExpression(lineNumber, localVariable.getType(), new ClassFileLocalVariableReferenceExpression(lineNumber, offset, localVariable), "=", valueRef, 16)));
                                break;
                            case 55: // LSTORE
                                localVariable = getLocalVariableInAssignment(i, offset + 4, valueRef = stack.pop());
                                statements.add(new ExpressionStatement(new BinaryOperatorExpression(lineNumber, TYPE_LONG, new ClassFileLocalVariableReferenceExpression(lineNumber, offset, localVariable), "=", valueRef, 16)));
                                break;
                            case 56: // FSTORE
                                localVariable = getLocalVariableInAssignment(i, offset + 4, valueRef = stack.pop());
                                statements.add(new ExpressionStatement(new BinaryOperatorExpression(lineNumber, TYPE_FLOAT, new ClassFileLocalVariableReferenceExpression(lineNumber, offset, localVariable), "=", valueRef, 16)));
                                break;
                            case 57: // DSTORE
                                localVariable = getLocalVariableInAssignment(i, offset + 4, valueRef = stack.pop());
                                statements.add(new ExpressionStatement(new BinaryOperatorExpression(lineNumber, TYPE_DOUBLE, new ClassFileLocalVariableReferenceExpression(lineNumber, offset, localVariable), "=", valueRef, 16)));
                                break;
                            case 58: // ASTORE
                                localVariable = getLocalVariableInAssignment(i, offset + 4, valueRef = stack.pop());
                                parseASTORE(statements, stack, lineNumber, offset, localVariable, valueRef);
                                break;
                            case 169: // RET
                                break;
                        }
                    }
                    break;
                case 197: // MULTIANEWARRAY
                    typeName = constants.getConstantTypeName( ((code[++offset] & 255) << 8) | (code[++offset] & 255) );
                    type1 = typeMaker.makeFromDescriptor(typeName);
                    i = code[++offset] & 255;

                    Expressions dimensions = new Expressions(i);

                    while (i-- > 0) {
                        dimensions.add(stack.pop());
                    }

                    Collections.reverse(dimensions);
                    stack.push(new NewArray(lineNumber, type1, dimensions));
                    break;
                case 198: // IFNULL
                    expression1 = stack.pop();
                    typeParametersToTypeArgumentsBinder.bindParameterTypesWithArgumentTypes(TYPE_OBJECT, expression1);
                    stack.push(new BinaryOperatorExpression(lineNumber, TYPE_BOOLEAN, expression1, basicBlock.mustInverseCondition() ? "!=" : "==", new NullExpression(expression1.getLineNumber(), expression1.getType()), 9));
                    offset += 2; // Skip branch offset
                    checkStack(stack, code, offset);
                    break;
                case 199: // IFNONNULL
                    expression1 = stack.pop();
                    typeParametersToTypeArgumentsBinder.bindParameterTypesWithArgumentTypes(TYPE_OBJECT, expression1);
                    stack.push(new BinaryOperatorExpression(lineNumber, TYPE_BOOLEAN, expression1, basicBlock.mustInverseCondition() ? "==" : "!=", new NullExpression(expression1.getLineNumber(), expression1.getType()), 9));
                    offset += 2; // Skip branch offset
                    checkStack(stack, code, offset);
                    break;
                case 201: // JSR_W
                    stack.push(JSR_RETURN_ADDRESS_EXPRESSION);
                case 200: // GOTO_W
                    offset += 4; // Skip branch offset
                    break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private BaseExpression extractParametersFromStack(Statements statements, DefaultStack<Expression> stack, BaseType parameterTypes) {
        if (parameterTypes == null) {
            return null;
        }

        switch (parameterTypes.size()) {
            case 0:
                return null;
            case 1:
                Expression parameter = stack.pop();
                if (parameter.isNewArray()) {
                    parameter = NewArrayMaker.make(statements, parameter);
                }
                return checkIfLastStatementIsAMultiAssignment(statements, parameter);
            default:
                Expressions parameters = new Expressions(parameterTypes.size());
                int count = parameterTypes.size() - 1;

                for (int i=count; i>=0; --i) {
                    parameter = stack.pop();
                    if (parameter.isNewArray()) {
                        parameter = NewArrayMaker.make(statements, parameter);
                    }
                    parameters.add(checkIfLastStatementIsAMultiAssignment(statements, parameter));
                }

                Collections.reverse(parameters);
                return parameters;
        }
    }

    private static Expression checkIfLastStatementIsAMultiAssignment(Statements statements, Expression parameter) {
        if (!statements.isEmpty()) {
            Expression expression = statements.getLast().getExpression();

            if (expression.isBinaryOperatorExpression() && (getLastRightExpression(expression) == parameter)) {
                // Return multi assignment expression
                statements.removeLast();
                return expression;
            }
        }

        return parameter;
    }

    private AbstractLocalVariable getLocalVariableInAssignment(int index, int offset, Expression value) {
        Type valueType = value.getType();

        if (value.isNullExpression()) {
            return localVariableMaker.getLocalVariableInNullAssignment(index, offset, valueType);
        } else if (value.isLocalVariableReferenceExpression()) {
            AbstractLocalVariable valueLocalVariable = ((ClassFileLocalVariableReferenceExpression)value).getLocalVariable();
            AbstractLocalVariable lv = localVariableMaker.getLocalVariableInAssignment(typeBounds, index, offset, valueLocalVariable);
            valueLocalVariable.variableOnLeft(typeBounds, lv);
            return lv;
        } else if (value.isMethodInvocationExpression()) {
            if (valueType.isObjectType()) {
                // Remove type arguments
                valueType = ((ObjectType)valueType).createType(null);
            } else if (valueType.isGenericType()) {
                valueType = TYPE_UNDEFINED_OBJECT;
            }
            return localVariableMaker.getLocalVariableInAssignment(typeBounds, index, offset, valueType);
        } else {
            return localVariableMaker.getLocalVariableInAssignment(typeBounds, index, offset, valueType);
        }
    }

    private void parseLDC(DefaultStack<Expression> stack, ConstantPool constants, int lineNumber, Constant constant) {
        switch (constant.getTag()) {
            case Constant.CONSTANT_Integer:
                int i = ((ConstantInteger)constant).getValue();
                stack.push(new IntegerConstantExpression(lineNumber, PrimitiveTypeUtil.getPrimitiveTypeFromValue(i), i));
                break;
            case Constant.CONSTANT_Float:
                float f = ((ConstantFloat)constant).getValue();

                if (f == Float.MIN_VALUE) {
                    stack.push(new FieldReferenceExpression(lineNumber, TYPE_FLOAT, new ObjectTypeReferenceExpression(lineNumber, ObjectType.TYPE_FLOAT), "java/lang/Float", "MIN_VALUE", "F"));
                } else if (f == Float.MAX_VALUE) {
                    stack.push(new FieldReferenceExpression(lineNumber, TYPE_FLOAT, new ObjectTypeReferenceExpression(lineNumber, ObjectType.TYPE_FLOAT), "java/lang/Float", "MAX_VALUE", "F"));
                } else if (f == Float.NEGATIVE_INFINITY) {
                    stack.push(new FieldReferenceExpression(lineNumber, TYPE_FLOAT, new ObjectTypeReferenceExpression(lineNumber, ObjectType.TYPE_FLOAT), "java/lang/Float", "NEGATIVE_INFINITY", "F"));
                } else if (f == Float.POSITIVE_INFINITY) {
                    stack.push(new FieldReferenceExpression(lineNumber, TYPE_FLOAT, new ObjectTypeReferenceExpression(lineNumber, ObjectType.TYPE_FLOAT), "java/lang/Float", "POSITIVE_INFINITY", "F"));
                } else if (Float.isNaN(f)) {
                    stack.push(new FieldReferenceExpression(lineNumber, TYPE_FLOAT, new ObjectTypeReferenceExpression(lineNumber, ObjectType.TYPE_FLOAT), "java/lang/Float", "NaN", "F"));
                } else {
                    stack.push(new FloatConstantExpression(lineNumber, f));
                }
                break;
            case Constant.CONSTANT_Class:
                int typeNameIndex = ((ConstantClass) constant).getNameIndex();
                String typeName = ((ConstantUtf8)constants.getConstant(typeNameIndex)).getValue();
                Type type = typeMaker.makeFromDescriptorOrInternalTypeName(typeName);
                if (type == null) {
                    type = PrimitiveTypeUtil.getPrimitiveTypeFromDescriptor(typeName);
                }
                stack.push(new TypeReferenceDotClassExpression(lineNumber, type));
                break;
            case Constant.CONSTANT_Long:
                long l = ((ConstantLong)constant).getValue();

                if (l == Long.MIN_VALUE) {
                    stack.push(new FieldReferenceExpression(lineNumber, TYPE_LONG, new ObjectTypeReferenceExpression(lineNumber, ObjectType.TYPE_LONG), "java/lang/Long", "MIN_VALUE", "J"));
                } else if (l == Long.MAX_VALUE) {
                    stack.push(new FieldReferenceExpression(lineNumber, TYPE_LONG, new ObjectTypeReferenceExpression(lineNumber, ObjectType.TYPE_LONG), "java/lang/Long", "MAX_VALUE", "J"));
                } else {
                    stack.push(new LongConstantExpression(lineNumber, l));
                }
                break;
            case Constant.CONSTANT_Double:
                double d = ((ConstantDouble)constant).getValue();

                if (d == Double.MIN_VALUE) {
                    stack.push(new FieldReferenceExpression(lineNumber, TYPE_DOUBLE, new ObjectTypeReferenceExpression(lineNumber, ObjectType.TYPE_DOUBLE), "java/lang/Double", "MIN_VALUE", "D"));
                } else if (d == Double.MAX_VALUE) {
                    stack.push(new FieldReferenceExpression(lineNumber, TYPE_DOUBLE, new ObjectTypeReferenceExpression(lineNumber, ObjectType.TYPE_DOUBLE), "java/lang/Double", "MAX_VALUE", "D"));
                } else if (d == Double.NEGATIVE_INFINITY) {
                    stack.push(new FieldReferenceExpression(lineNumber, TYPE_DOUBLE, new ObjectTypeReferenceExpression(lineNumber, ObjectType.TYPE_DOUBLE), "java/lang/Double", "NEGATIVE_INFINITY", "D"));
                } else if (d == Double.POSITIVE_INFINITY) {
                    stack.push(new FieldReferenceExpression(lineNumber, TYPE_DOUBLE, new ObjectTypeReferenceExpression(lineNumber, ObjectType.TYPE_DOUBLE), "java/lang/Double", "POSITIVE_INFINITY", "D"));
                } else if (Double.isNaN(d)) {
                    stack.push(new FieldReferenceExpression(lineNumber, TYPE_DOUBLE, new ObjectTypeReferenceExpression(lineNumber, ObjectType.TYPE_DOUBLE), "java/lang/Double", "NaN", "D"));
                } else if (d == Math.E) {
                    stack.push(new FieldReferenceExpression(lineNumber, TYPE_DOUBLE, new ObjectTypeReferenceExpression(lineNumber, ObjectType.TYPE_MATH), "java/lang/Math", "E", "D"));
                } else if (d == Math.PI) {
                    stack.push(new FieldReferenceExpression(lineNumber, TYPE_DOUBLE, new ObjectTypeReferenceExpression(lineNumber, ObjectType.TYPE_MATH), "java/lang/Math", "PI", "D"));
                } else {
                    stack.push(new DoubleConstantExpression(lineNumber, d));
                }
                break;
            case Constant.CONSTANT_String:
                int stringIndex = ((ConstantString)constant).getStringIndex();
                stack.push(new StringConstantExpression(lineNumber, constants.getConstantUtf8(stringIndex)));
                break;
        }
    }

    private static void parseILOAD(Statements statements, DefaultStack<Expression> stack, int lineNumber, int offset, AbstractLocalVariable localVariable) {
        if (! statements.isEmpty()) {
            Expression expression = statements.getLast().getExpression();

            if ((expression.getLineNumber() == lineNumber) && expression.isPreOperatorExpression() && expression.getExpression().isLocalVariableReferenceExpression()) {
                ClassFileLocalVariableReferenceExpression cflvre = (ClassFileLocalVariableReferenceExpression)expression.getExpression();

                if (cflvre.getLocalVariable() == localVariable) {
                    // IINC pattern found -> Remove last statement and create a pre-incrementation
                    statements.removeLast();
                    stack.push(expression);
                    return;
                }
            }
        }

        stack.push(new ClassFileLocalVariableReferenceExpression(lineNumber, offset, localVariable));
    }

    @SuppressWarnings("unchecked")
    private void parseSTORE(Statements statements, DefaultStack<Expression> stack, int lineNumber, int offset, AbstractLocalVariable localVariable, Expression valueRef) {
        ClassFileLocalVariableReferenceExpression vre = new ClassFileLocalVariableReferenceExpression(lineNumber, offset, localVariable);

        typeParametersToTypeArgumentsBinder.bindParameterTypesWithArgumentTypes(vre.getType(), valueRef);

        if ((valueRef.getLineNumber() == lineNumber) && valueRef.isBinaryOperatorExpression() && valueRef.getLeftExpression().isLocalVariableReferenceExpression()) {
            ClassFileLocalVariableReferenceExpression lvr = (ClassFileLocalVariableReferenceExpression)valueRef.getLeftExpression();

            if (lvr.getLocalVariable() == localVariable) {
                BinaryOperatorExpression boe = (BinaryOperatorExpression)valueRef;
                Expression expression;

                switch (boe.getOperator()) {
                    case "*": expression = createAssignment(boe, "*="); break;
                    case "/": expression = createAssignment(boe, "/="); break;
                    case "%": expression = createAssignment(boe, "%="); break;
                    case "<<": expression = createAssignment(boe, "<<="); break;
                    case ">>": expression = createAssignment(boe, ">>="); break;
                    case ">>>": expression = createAssignment(boe, ">>>="); break;
                    case "&": expression = createAssignment(boe, "&="); break;
                    case "^": expression = createAssignment(boe, "^="); break;
                    case "|": expression = createAssignment(boe, "|="); break;
                    case "=": expression = boe; break;
                    case "+":
                        if (isPositiveOne(boe.getRightExpression())) {
                            if (stackContainsLocalVariableReference(stack, localVariable)) {
                                stack.pop();
                                stack.push(valueRef);
                                expression = newPostArithmeticOperatorExpression(boe.getLineNumber(), boe.getLeftExpression(), "++");
                            } else {
                                expression = newPreArithmeticOperatorExpression(boe.getLineNumber(), "++", boe.getLeftExpression());
                            }
                        } else if (isNegativeOne(boe.getRightExpression())) {
                            if (stackContainsLocalVariableReference(stack, localVariable)) {
                                stack.pop();
                                stack.push(valueRef);
                                expression = newPostArithmeticOperatorExpression(boe.getLineNumber(), boe.getLeftExpression(), "--");
                            } else {
                                expression = newPreArithmeticOperatorExpression(boe.getLineNumber(), "--", boe.getLeftExpression());
                            }
                        } else {
                            expression = createAssignment(boe, "+=");
                        }
                        break;
                    case "-":
                        if (isPositiveOne(boe.getRightExpression())) {
                            if (stackContainsLocalVariableReference(stack, localVariable)) {
                                stack.pop();
                                stack.push(valueRef);
                                expression = newPostArithmeticOperatorExpression(boe.getLineNumber(), boe.getLeftExpression(), "--");
                            } else {
                                expression = newPreArithmeticOperatorExpression(boe.getLineNumber(), "--", boe.getLeftExpression());
                            }
                        } else if (isNegativeOne(boe.getRightExpression())) {
                            if (stackContainsLocalVariableReference(stack, localVariable)) {
                                stack.pop();
                                stack.push(valueRef);
                                expression = newPostArithmeticOperatorExpression(boe.getLineNumber(), boe.getLeftExpression(), "++");
                            } else {
                                expression = newPreArithmeticOperatorExpression(boe.getLineNumber(), "++", boe.getLeftExpression());
                            }
                        } else {
                            expression = createAssignment(boe, "-=");
                        }
                        break;
                    default: throw new RuntimeException("Unexpected value expression");
                }

                if (!stack.isEmpty() && (stack.peek() == valueRef)) {
                    stack.replace(valueRef, expression);
                } else {
                    statements.add(new ExpressionStatement(expression));
                }
                return;
            }
        }

        createAssignment(statements, stack, lineNumber, vre, valueRef);
    }

    private static boolean stackContainsLocalVariableReference(DefaultStack<Expression> stack, AbstractLocalVariable localVariable) {
        if (!stack.isEmpty()) {
            Expression expression = stack.peek();

            if (expression.isLocalVariableReferenceExpression()) {
                ClassFileLocalVariableReferenceExpression lvr = (ClassFileLocalVariableReferenceExpression) expression;
                return lvr.getLocalVariable() == localVariable;
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    private void parsePUT(Statements statements, DefaultStack<Expression> stack, int lineNumber, FieldReferenceExpression fr, Expression valueRef) {
        if (valueRef.isNewArray()) {
            valueRef = NewArrayMaker.make(statements, valueRef);
        }

        typeParametersToTypeArgumentsBinder.bindParameterTypesWithArgumentTypes(fr.getType(), valueRef);

        if ((valueRef.getLineNumber() == lineNumber) && valueRef.isBinaryOperatorExpression() && valueRef.getLeftExpression().isFieldReferenceExpression()) {
            FieldReferenceExpression boefr = (FieldReferenceExpression)valueRef.getLeftExpression();

            if (boefr.getName().equals(fr.getName()) && boefr.getExpression().getType().equals(fr.getExpression().getType())) {
                BinaryOperatorExpression boe = (BinaryOperatorExpression)valueRef;
                Expression expression;

                switch (boe.getOperator()) {
                    case "*": expression = createAssignment(boe, "*="); break;
                    case "/": expression = createAssignment(boe, "/="); break;
                    case "%": expression = createAssignment(boe, "%="); break;
                    case "<<": expression = createAssignment(boe, "<<="); break;
                    case ">>": expression = createAssignment(boe, ">>="); break;
                    case ">>>": expression = createAssignment(boe, ">>>="); break;
                    case "&": expression = createAssignment(boe, "&="); break;
                    case "^": expression = createAssignment(boe, "^="); break;
                    case "|": expression = createAssignment(boe, "|="); break;
                    case "=": expression = boe; break;
                    case "+":
                        if (isPositiveOne(boe.getRightExpression())) {
                            if (stackContainsFieldReference(stack, fr)) {
                                stack.pop();
                                stack.push(valueRef);
                                expression = newPostArithmeticOperatorExpression(boe.getLineNumber(), boe.getLeftExpression(), "++");
                            } else {
                                expression = newPreArithmeticOperatorExpression(boe.getLineNumber(), "++", boe.getLeftExpression());
                            }
                        } else if (isNegativeOne(boe.getRightExpression())) {
                            if (stackContainsFieldReference(stack, fr)) {
                                stack.pop();
                                stack.push(valueRef);
                                expression = newPostArithmeticOperatorExpression(boe.getLineNumber(), boe.getLeftExpression(), "--");
                            } else {
                                expression = newPreArithmeticOperatorExpression(boe.getLineNumber(), "--", boe.getLeftExpression());
                            }
                        } else {
                            expression = createAssignment(boe, "+=");
                        }
                        break;
                    case "-":
                        if (isPositiveOne(boe.getRightExpression())) {
                            if (stackContainsFieldReference(stack, fr)) {
                                stack.pop();
                                stack.push(valueRef);
                                expression = newPostArithmeticOperatorExpression(boe.getLineNumber(), boe.getLeftExpression(), "--");
                            } else {
                                expression = newPreArithmeticOperatorExpression(boe.getLineNumber(), "--", boe.getLeftExpression());
                            }
                        } else if (isPositiveOne(boe.getRightExpression())) {
                            if (stackContainsFieldReference(stack, fr)) {
                                stack.pop();
                                stack.push(valueRef);
                                expression = newPostArithmeticOperatorExpression(boe.getLineNumber(), boe.getLeftExpression(), "++");
                            } else {
                                expression = newPreArithmeticOperatorExpression(boe.getLineNumber(), "++", boe.getLeftExpression());
                            }
                        } else {
                            expression = createAssignment(boe, "-=");
                        }
                        break;
                    default: throw new RuntimeException("Unexpected value expression");
                }

                if (!stack.isEmpty() && (stack.peek() == valueRef)) {
                    stack.replace(valueRef, expression);
                } else {
                    statements.add(new ExpressionStatement(expression));
                }
                return;
            }
        }

        createAssignment(statements, stack, lineNumber, fr, valueRef);
    }

    private void parseInvokeDynamic(Statements statements, DefaultStack<Expression> stack, ConstantPool constants, int lineNumber, int index) {
        // Remove previous 'getClass()' or cast if exists
        if (! statements.isEmpty()) {
            Expression expression = statements.getLast().getExpression();

            if (expression.isMethodInvocationExpression()) {
                MethodInvocationExpression mie = (MethodInvocationExpression)expression;

                if (mie.getName().equals("getClass") && mie.getDescriptor().equals("()Ljava/lang/Class;") && mie.getInternalTypeName().equals("java/lang/Object")) {
                    statements.removeLast();
                }
            }
        }

        // Create expression
        ConstantMemberRef constantMemberRef = constants.getConstant(index);

        ConstantNameAndType indyCnat = constants.getConstant(constantMemberRef.getNameAndTypeIndex());
        String indyMethodName = constants.getConstantUtf8(indyCnat.getNameIndex());
        String indyDescriptor = constants.getConstantUtf8(indyCnat.getDescriptorIndex());
        TypeMaker.MethodTypes indyMethodTypes = typeMaker.makeMethodTypes(indyDescriptor);

        BaseExpression indyParameters = extractParametersFromStack(statements, stack, indyMethodTypes.parameterTypes);
        BootstrapMethod bootstrapMethod = attributeBootstrapMethods.getBootstrapMethods()[constantMemberRef.getClassIndex()];
        int[] bootstrapArguments = bootstrapMethod.getBootstrapArguments();

        if ("makeConcatWithConstants".equals(indyMethodName)) {
            // Create Java 9+ string concatenation
            String recipe = constants.getConstantString(bootstrapArguments[0]);
            stack.push(StringConcatenationUtil.create(recipe, indyParameters));
            return;
        } else if ("makeConcat".equals(indyMethodName)) {
            // Create Java 9+ string concatenation
            stack.push(StringConcatenationUtil.create(indyParameters));
            return;
        }

        ConstantMethodType cmt0 = constants.getConstant(bootstrapArguments[0]);
        String descriptor0 = constants.getConstantUtf8(cmt0.getDescriptorIndex());
        TypeMaker.MethodTypes methodTypes0 = typeMaker.makeMethodTypes(descriptor0);
        int parameterCount = (methodTypes0.parameterTypes == null) ? 0 : methodTypes0.parameterTypes.size();
        ConstantMethodHandle constantMethodHandle1 = constants.getConstant(bootstrapArguments[1]);
        ConstantMemberRef cmr1 = constants.getConstant(constantMethodHandle1.getReferenceIndex());
        String typeName = constants.getConstantTypeName(cmr1.getClassIndex());
        ConstantNameAndType cnat1 = constants.getConstant(cmr1.getNameAndTypeIndex());
        String name1 = constants.getConstantUtf8(cnat1.getNameIndex());
        String descriptor1 = constants.getConstantUtf8(cnat1.getDescriptorIndex());

        if (typeName.equals(internalTypeName)) {
            for (ClassFileConstructorOrMethodDeclaration methodDeclaration : bodyDeclaration.getMethodDeclarations()) {
                if (((methodDeclaration.getFlags() & (FLAG_SYNTHETIC|FLAG_PRIVATE)) == (FLAG_SYNTHETIC|FLAG_PRIVATE)) && methodDeclaration.getMethod().getName().equals(name1) && methodDeclaration.getMethod().getDescriptor().equals(descriptor1)) {
                    // Create lambda expression
                    ClassFileMethodDeclaration cfmd = (ClassFileMethodDeclaration)methodDeclaration;
                    stack.push(new LambdaIdentifiersExpression(
                            lineNumber, indyMethodTypes.returnedType, indyMethodTypes.returnedType,
                            prepareLambdaParameterNames(cfmd.getFormalParameters(), parameterCount),
                            prepareLambdaStatements(cfmd.getFormalParameters(), indyParameters, cfmd.getStatements())));
                    return;
                }
            }
        }

        if (indyParameters == null) {
            // Create static method reference
            ObjectType ot = typeMaker.makeFromInternalTypeName(typeName);

            if (name1.equals("<init>")) {
                stack.push(new ConstructorReferenceExpression(lineNumber, indyMethodTypes.returnedType, ot, descriptor1));
            } else {
                stack.push(new MethodReferenceExpression(lineNumber, indyMethodTypes.returnedType, new ObjectTypeReferenceExpression(lineNumber, ot), typeName, name1, descriptor1));
            }
            return;
        }

        // Create method reference
        stack.push(new MethodReferenceExpression(lineNumber, indyMethodTypes.returnedType, (Expression)indyParameters, typeName, name1, descriptor1));
    }

    private List<String> prepareLambdaParameterNames(BaseFormalParameter formalParameters, int parameterCount) {
        if ((formalParameters == null) || (parameterCount == 0)) {
            return null;
        } else {
            lambdaParameterNamesVisitor.init();
            formalParameters.accept(lambdaParameterNamesVisitor);
            List<String> names = lambdaParameterNamesVisitor.getNames();

            assert names.size() >= parameterCount;

            if (names.size() == parameterCount) {
                return names;
            } else {
                return names.subList(names.size() - parameterCount, names.size());
            }
        }
    }

    private BaseStatement prepareLambdaStatements(BaseFormalParameter formalParameters, BaseExpression indyParameters, BaseStatement baseStatement) {
        if (baseStatement != null) {
            if ((formalParameters != null) && (indyParameters != null)) {
                int size = indyParameters.size();

                if ((size > 0) && (size <= formalParameters.size())) {
                    HashMap<String, String> mapping = new HashMap<>();
                    Expression expression = indyParameters.getFirst();

                    if (expression.isLocalVariableReferenceExpression()) {
                        String name = formalParameters.getFirst().getName();
                        String newName = expression.getName();

                        if (!name.equals(newName)) {
                            mapping.put(name, newName);
                        }
                    }

                    if (size > 1) {
                        DefaultList<FormalParameter> formalParameterList = formalParameters.getList();
                        DefaultList<Expression> list = indyParameters.getList();

                        for (int i = 1; i < size; i++) {
                            expression = list.get(i);

                            if (expression.isLocalVariableReferenceExpression()) {
                                String name = formalParameterList.get(i).getName();
                                String newName = expression.getName();

                                if (!name.equals(newName)) {
                                    mapping.put(name, newName);
                                }
                            }
                        }
                    }

                    if (!mapping.isEmpty()) {
                        renameLocalVariablesVisitor.init(mapping);
                        baseStatement.accept(renameLocalVariablesVisitor);
                    }
                }
            }

            if (baseStatement.size() == 1) {
                Statement statement = baseStatement.getFirst();

                if (statement.isReturnExpressionStatement()) {
                    return new LambdaExpressionStatement(statement.getExpression());
                } else if (statement.isExpressionStatement()) {
                    return new LambdaExpressionStatement(statement.getExpression());
                }
            }
        }

        return baseStatement;
    }

    private static boolean stackContainsFieldReference(DefaultStack<Expression> stack, FieldReferenceExpression fr) {
        if (!stack.isEmpty()) {
            Expression expression = stack.peek();

            if (expression.isFieldReferenceExpression()) {
                return expression.getName().equals(fr.getName()) && expression.getExpression().getType().equals(fr.getExpression().getType());
            }
        }

        return false;
    }

    private static Expression createAssignment(BinaryOperatorExpression boe, String operator) {
        boe.setOperator(operator);
        boe.setPriority(16);
        return boe;
    }

    private static boolean isPositiveOne(Expression expression) {
        if (expression.isIntegerConstantExpression() && expression.getIntegerValue() == 1)
            return true;
        if (expression.isLongConstantExpression() && expression.getLongValue() == 1L)
            return true;
        if (expression.isFloatConstantExpression() && expression.getFloatValue() == 1.0F)
            return true;
        return (expression.isDoubleConstantExpression() && expression.getDoubleValue() == 1.0D);
    }

    private static boolean isNegativeOne(Expression expression) {
        if (expression.isIntegerConstantExpression() && expression.getIntegerValue() == -1)
            return true;
        if (expression.isLongConstantExpression() && expression.getLongValue() == -1L)
            return true;
        if (expression.isFloatConstantExpression() && expression.getFloatValue() == -1.0F)
            return true;
        return (expression.isDoubleConstantExpression() && expression.getDoubleValue() == -1.0D);
    }

    private void parseASTORE(Statements statements, DefaultStack<Expression> stack, int lineNumber, int offset, AbstractLocalVariable localVariable, Expression valueRef) {
        typeParametersToTypeArgumentsBinder.bindParameterTypesWithArgumentTypes(localVariable.getType(), valueRef);
        localVariable.typeOnRight(typeBounds, valueRef.getType());

        ClassFileLocalVariableReferenceExpression vre = new ClassFileLocalVariableReferenceExpression(lineNumber, offset, localVariable);
        Expression oldValueRef = valueRef;

        if (valueRef.isNewArray()) {
            valueRef = NewArrayMaker.make(statements, valueRef);
        }

        if (oldValueRef != valueRef) {
            stack.replace(oldValueRef, valueRef);
        }

        createAssignment(statements, stack, lineNumber, vre, valueRef);
    }

    @SuppressWarnings("unchecked")
    private void createAssignment(Statements statements, DefaultStack<Expression> stack, int lineNumber, Expression leftExpression, Expression rightExpression) {
        if (!stack.isEmpty() && (stack.peek() == rightExpression)) {
            stack.push(new BinaryOperatorExpression(lineNumber, leftExpression.getType(), leftExpression, "=", stack.pop(), 16));
            return;
        }

        if (!statements.isEmpty()) {
            Statement lastStatement = statements.getLast();

            if (lastStatement.isExpressionStatement()) {
                ExpressionStatement lastES = (ExpressionStatement)lastStatement;
                Expression lastExpression = lastStatement.getExpression();

                if (lastExpression.isBinaryOperatorExpression()) {
                    if (getLastRightExpression(lastExpression) == rightExpression) {
                        // Multi assignment
                        lastES.setExpression(new BinaryOperatorExpression(lineNumber, leftExpression.getType(), leftExpression, "=", lastExpression, 16));
                        return;
                    }

                    if ((lineNumber > 0) && (lastExpression.getLineNumber() == lineNumber) && (lastExpression.getLeftExpression().getClass() == rightExpression.getClass())) {
                        if (rightExpression.isLocalVariableReferenceExpression()) {
                            ClassFileLocalVariableReferenceExpression lvr1 = (ClassFileLocalVariableReferenceExpression) lastExpression.getLeftExpression();
                            ClassFileLocalVariableReferenceExpression lvr2 = (ClassFileLocalVariableReferenceExpression) rightExpression;

                            if (lvr1.getLocalVariable() == lvr2.getLocalVariable()) {
                                // Multi assignment
                                lastES.setExpression(new BinaryOperatorExpression(lineNumber, leftExpression.getType(), leftExpression, "=", lastExpression, 16));
                                return;
                            }
                        } else if (rightExpression.isFieldReferenceExpression()) {
                            FieldReferenceExpression fr1 = (FieldReferenceExpression) lastExpression.getLeftExpression();
                            FieldReferenceExpression fr2 = (FieldReferenceExpression) rightExpression;

                            if (fr1.getName().equals(fr2.getName()) && fr1.getExpression().getType().equals(fr2.getExpression().getType())) {
                                // Multi assignment
                                lastES.setExpression(new BinaryOperatorExpression(lineNumber, leftExpression.getType(), leftExpression, "=", lastExpression, 16));
                                return;
                            }
                        }
                    }
                } else if (lastExpression.isPreOperatorExpression()) {
                    if (lastExpression.getExpression().getClass() == rightExpression.getClass()) {
                        if (rightExpression.isLocalVariableReferenceExpression()) {
                            ClassFileLocalVariableReferenceExpression lvr1 = (ClassFileLocalVariableReferenceExpression)lastExpression.getExpression();
                            ClassFileLocalVariableReferenceExpression lvr2 = (ClassFileLocalVariableReferenceExpression)rightExpression;

                            if (lvr1.getLocalVariable() == lvr2.getLocalVariable()) {
                                rightExpression = newPreArithmeticOperatorExpression(lastExpression.getLineNumber(), lastExpression.getOperator(), lastExpression.getExpression());
                                statements.removeLast();
                            }
                        } else if (rightExpression.isFieldReferenceExpression()) {
                            FieldReferenceExpression fr1 = (FieldReferenceExpression)lastExpression.getExpression();
                            FieldReferenceExpression fr2 = (FieldReferenceExpression)rightExpression;

                            if (fr1.getName().equals(fr2.getName()) && fr1.getExpression().getType().equals(fr2.getExpression().getType())) {
                                rightExpression = newPreArithmeticOperatorExpression(lastExpression.getLineNumber(), lastExpression.getOperator(), lastExpression.getExpression());
                                statements.removeLast();
                            }
                        }
                    }
                } else if (lastExpression.isPostOperatorExpression()) {
                    PostOperatorExpression poe = (PostOperatorExpression)lastExpression;

                    if (poe.getExpression() == rightExpression) {
                        rightExpression = poe;
                        statements.removeLast();
                    }
                }
            }
        }

        statements.add(new ExpressionStatement(new BinaryOperatorExpression(lineNumber, leftExpression.getType(), leftExpression, "=", rightExpression, 16)));
    }

    @SuppressWarnings("unchecked")
    private void parseIINC(Statements statements, DefaultStack<Expression> stack, int lineNumber, int offset, AbstractLocalVariable localVariable, int count) {
        Expression expression;

        if (!stack.isEmpty()) {
            expression = stack.peek();

            if ((expression.getLineNumber() == lineNumber) && expression.isLocalVariableReferenceExpression()) {
                ClassFileLocalVariableReferenceExpression exp = (ClassFileLocalVariableReferenceExpression)expression;

                if (exp.getLocalVariable() == localVariable) {
                    // ILOAD found -> Create a post-incrementation
                    stack.pop();

                    if (count == 1) {
                        stack.push(newPostArithmeticOperatorExpression(lineNumber, expression, "++"));
                    } else if (count == -1) {
                        stack.push(newPostArithmeticOperatorExpression(lineNumber, expression, "--"));
                    } else {
                        assert false;
                    }

                    return;
                }
            }
        }

        expression = new ClassFileLocalVariableReferenceExpression(lineNumber, offset, localVariable);

        if (count == 1) {
            expression = newPreArithmeticOperatorExpression(lineNumber, "++", expression);
        } else if (count == -1) {
            expression = newPreArithmeticOperatorExpression(lineNumber, "--", expression);
        } else if (count >= 0) {
            expression = new BinaryOperatorExpression(lineNumber, expression.getType(), expression, "+=", new IntegerConstantExpression(lineNumber, expression.getType(), count), 16);
        } else if (count < 0) {
            expression = new BinaryOperatorExpression(lineNumber, expression.getType(), expression, "-=", new IntegerConstantExpression(lineNumber, expression.getType(), -count), 16);
        } else {
            assert false;
            expression = null;
        }

        statements.add(new ExpressionStatement(expression));
    }

    private void parseIF(DefaultStack<Expression> stack, int lineNumber, BasicBlock basicBlock, String operator1, String operator2, int priority) {
        Expression expression = stack.pop();

        if (expression.getClass() == ClassFileCmpExpression.class) {
            ClassFileCmpExpression cmp = (ClassFileCmpExpression)expression;

            typeParametersToTypeArgumentsBinder.bindParameterTypesWithArgumentTypes(cmp.getLeftExpression().getType(), cmp.getLeftExpression());
            typeParametersToTypeArgumentsBinder.bindParameterTypesWithArgumentTypes(cmp.getRightExpression().getType(), cmp.getRightExpression());

            stack.push(new BinaryOperatorExpression(lineNumber, TYPE_BOOLEAN, cmp.getLeftExpression(), (basicBlock.mustInverseCondition() ? operator1 : operator2), cmp.getRightExpression(), priority));
        } else if (expression.getType().isPrimitiveType()) {
            PrimitiveType pt = (PrimitiveType)expression.getType();

            typeParametersToTypeArgumentsBinder.bindParameterTypesWithArgumentTypes(pt, expression);

            switch (pt.getJavaPrimitiveFlags()) {
                case FLAG_BOOLEAN:
                    if (basicBlock.mustInverseCondition() ^ "==".equals(operator1))
                        stack.push(expression);
                    else
                        stack.push(new PreOperatorExpression(lineNumber, "!", expression));
                    break;
                case FLAG_FLOAT:
                    stack.push(new BinaryOperatorExpression(lineNumber, TYPE_BOOLEAN, expression, (basicBlock.mustInverseCondition() ? operator1 : operator2), new FloatConstantExpression(lineNumber, 0), 9));
                    break;
                case FLAG_DOUBLE:
                    stack.push(new BinaryOperatorExpression(lineNumber, TYPE_BOOLEAN, expression, (basicBlock.mustInverseCondition() ? operator1 : operator2), new DoubleConstantExpression(lineNumber, 0), 9));
                    break;
                case FLAG_LONG:
                    stack.push(new BinaryOperatorExpression(lineNumber, TYPE_BOOLEAN, expression, (basicBlock.mustInverseCondition() ? operator1 : operator2), new LongConstantExpression(lineNumber, 0), 9));
                    break;
                default:
                    stack.push(new BinaryOperatorExpression(lineNumber, TYPE_BOOLEAN, expression, (basicBlock.mustInverseCondition() ? operator1 : operator2), new IntegerConstantExpression(lineNumber, pt, 0), 9));
                    break;
            }
        } else {
            stack.push(new BinaryOperatorExpression(lineNumber, TYPE_BOOLEAN, expression, (basicBlock.mustInverseCondition() ? operator1 : operator2), new NullExpression(lineNumber, expression.getType()), 9));
        }
    }

    @SuppressWarnings("unchecked")
    private void parseXRETURN(Statements statements, DefaultStack<Expression> stack, int lineNumber) {
        Expression valueRef = stack.pop();

        if (valueRef.isNewArray()) {
            valueRef = NewArrayMaker.make(statements, valueRef);
        }

        typeParametersToTypeArgumentsBinder.bindParameterTypesWithArgumentTypes(returnedType, valueRef);

        if (lineNumber > valueRef.getLineNumber()) {
            lineNumber = valueRef.getLineNumber();
        }

        if (!statements.isEmpty() && valueRef.isLocalVariableReferenceExpression()) {
            Statement lastStatement = statements.getLast();

            if (lastStatement.isExpressionStatement()) {
                Expression expression = statements.getLast().getExpression();

                if ((lineNumber <= expression.getLineNumber()) && expression.isBinaryOperatorExpression() &&
                    expression.getOperator().equals("=") && expression.getLeftExpression().isLocalVariableReferenceExpression()) {
                    ClassFileLocalVariableReferenceExpression vre1 = (ClassFileLocalVariableReferenceExpression) expression.getLeftExpression();
                    ClassFileLocalVariableReferenceExpression vre2 = (ClassFileLocalVariableReferenceExpression) valueRef;

                    if (vre1.getLocalVariable() == vre2.getLocalVariable()) {
                        // Remove synthetic local variable
                        localVariableMaker.removeLocalVariable(vre1.getLocalVariable());
                        // Remove assignment statement
                        statements.removeLast();
                        statements.add(new ReturnExpressionStatement(lineNumber, expression.getRightExpression()));
                        return;
                    }
                }
            }
        }

        statements.add(new ReturnExpressionStatement(lineNumber, valueRef));
    }

    private void parseGetStatic(DefaultStack<Expression> stack, ConstantPool constants, int lineNumber, int index) {
        ConstantMemberRef constantMemberRef = constants.getConstant(index);
        String typeName = constants.getConstantTypeName(constantMemberRef.getClassIndex());
        ConstantNameAndType constantNameAndType = constants.getConstant(constantMemberRef.getNameAndTypeIndex());
        String name = constants.getConstantUtf8(constantNameAndType.getNameIndex());

        if (name.equals("TYPE") && typeName.startsWith("java/lang/")) {
            switch (typeName) {
                case "java/lang/Boolean":   stack.push(new TypeReferenceDotClassExpression(lineNumber, TYPE_BOOLEAN)); return;
                case "java/lang/Character": stack.push(new TypeReferenceDotClassExpression(lineNumber, TYPE_CHAR));    return;
                case "java/lang/Float":     stack.push(new TypeReferenceDotClassExpression(lineNumber, TYPE_FLOAT));   return;
                case "java/lang/Double":    stack.push(new TypeReferenceDotClassExpression(lineNumber, TYPE_DOUBLE));  return;
                case "java/lang/Byte":      stack.push(new TypeReferenceDotClassExpression(lineNumber, TYPE_BYTE));    return;
                case "java/lang/Short":     stack.push(new TypeReferenceDotClassExpression(lineNumber, TYPE_SHORT));   return;
                case "java/lang/Integer":   stack.push(new TypeReferenceDotClassExpression(lineNumber, TYPE_INT));     return;
                case "java/lang/Long":      stack.push(new TypeReferenceDotClassExpression(lineNumber, TYPE_LONG));    return;
                case "java/lang/Void":      stack.push(new TypeReferenceDotClassExpression(lineNumber, TYPE_VOID));    return;
            }
        }

        ObjectType ot = typeMaker.makeFromInternalTypeName(typeName);
        String descriptor = constants.getConstantUtf8(constantNameAndType.getDescriptorIndex());
        Type type = makeFieldType(ot.getInternalName(), name, descriptor);
        Expression objectRef = new ObjectTypeReferenceExpression(lineNumber, ot, !internalTypeName.equals(typeName) || localVariableMaker.containsName(name));
        stack.push(typeParametersToTypeArgumentsBinder.newFieldReferenceExpression(lineNumber, type, objectRef, ot, name, descriptor));
    }

    private void parsePutStatic(Statements statements, DefaultStack<Expression> stack, ConstantPool constants, int lineNumber, int index) {
        ConstantMemberRef constantMemberRef = constants.getConstant(index);
        String typeName = constants.getConstantTypeName(constantMemberRef.getClassIndex());
        ObjectType ot = typeMaker.makeFromInternalTypeName(typeName);
        ConstantNameAndType constantNameAndType = constants.getConstant(constantMemberRef.getNameAndTypeIndex());
        String name = constants.getConstantUtf8(constantNameAndType.getNameIndex());
        String descriptor = constants.getConstantUtf8(constantNameAndType.getDescriptorIndex());
        Type type = makeFieldType(ot.getInternalName(), name, descriptor);
        Expression valueRef = stack.pop();
        Expression objectRef = new ObjectTypeReferenceExpression(lineNumber, ot, !internalTypeName.equals(typeName) || localVariableMaker.containsName(name));
        FieldReferenceExpression fieldRef = typeParametersToTypeArgumentsBinder.newFieldReferenceExpression(lineNumber, type, objectRef, ot, name, descriptor);
        parsePUT(statements, stack, lineNumber, fieldRef, valueRef);
    }

    private void parseGetField(DefaultStack<Expression> stack, ConstantPool constants, int lineNumber, int index) {
        ConstantMemberRef constantMemberRef = constants.getConstant(index);
        String typeName = constants.getConstantTypeName(constantMemberRef.getClassIndex());
        ObjectType ot = typeMaker.makeFromInternalTypeName(typeName);
        ConstantNameAndType constantNameAndType = constants.getConstant(constantMemberRef.getNameAndTypeIndex());
        String name = constants.getConstantUtf8(constantNameAndType.getNameIndex());
        String descriptor = constants.getConstantUtf8(constantNameAndType.getDescriptorIndex());
        Type type = makeFieldType(ot.getInternalName(), name, descriptor);
        Expression objectRef = stack.pop();
        stack.push(typeParametersToTypeArgumentsBinder.newFieldReferenceExpression(lineNumber, type, getFieldInstanceReference(objectRef, ot,  name), ot, name, descriptor));
    }

    private void parsePutField(Statements statements, DefaultStack<Expression> stack, ConstantPool constants, int lineNumber, int index) {
        ConstantMemberRef constantMemberRef = constants.getConstant(index);
        String typeName = constants.getConstantTypeName(constantMemberRef.getClassIndex());
        ObjectType ot = typeMaker.makeFromInternalTypeName(typeName);
        ConstantNameAndType constantNameAndType = constants.getConstant(constantMemberRef.getNameAndTypeIndex());
        String name = constants.getConstantUtf8(constantNameAndType.getNameIndex());
        String descriptor = constants.getConstantUtf8(constantNameAndType.getDescriptorIndex());
        Type type = makeFieldType(ot.getInternalName(), name, descriptor);
        Expression valueRef = stack.pop();
        Expression objectRef = stack.pop();
        FieldReferenceExpression fieldRef = typeParametersToTypeArgumentsBinder.newFieldReferenceExpression(lineNumber, type, getFieldInstanceReference(objectRef, ot,  name), ot, name, descriptor);
        parsePUT(statements, stack, lineNumber, fieldRef, valueRef);
    }

    private static Expression getLastRightExpression(Expression boe) {
        while (true) {
            if (! boe.getOperator().equals("=")) {
                return boe;
            }

            if (! boe.getRightExpression().isBinaryOperatorExpression()) {
                return boe.getRightExpression();
            }

            boe = boe.getRightExpression();
        }
    }

    private Expression newNewExpression(int lineNumber, String internalTypeName) {
        ObjectType objectType = typeMaker.makeFromInternalTypeName(internalTypeName);

        if ((objectType.getQualifiedName() == null) && (objectType.getName() == null)) {
            ClassFileTypeDeclaration typeDeclaration = bodyDeclaration.getInnerTypeDeclaration(internalTypeName);

            if (typeDeclaration == null) {
                return new ClassFileNewExpression(lineNumber, ObjectType.TYPE_OBJECT);
            } else if (typeDeclaration.isClassDeclaration()) {
                ClassFileClassDeclaration declaration = (ClassFileClassDeclaration) typeDeclaration;
                BodyDeclaration bodyDeclaration;

                if (this.internalTypeName.equals(internalTypeName)) {
                    bodyDeclaration = null;
                } else {
                    bodyDeclaration = declaration.getBodyDeclaration();
                }

                if (declaration.getInterfaces() != null) {
                    return new ClassFileNewExpression(lineNumber, (ObjectType) declaration.getInterfaces(), bodyDeclaration, true);
                } else if (declaration.getSuperType() != null) {
                    return new ClassFileNewExpression(lineNumber, declaration.getSuperType(), bodyDeclaration, true);
                } else {
                    return new ClassFileNewExpression(lineNumber, ObjectType.TYPE_OBJECT, bodyDeclaration, true);
                }
            }
        }

        return new ClassFileNewExpression(lineNumber, objectType);
    }

    /*
     * Operators = { "+", "-", "*", "/", "%", "<<", ">>", ">>>" }
     * See "Additive Operators (+ and -) for Numeric Types": https://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.18.2
     * See "Shift Operators":                                https://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.19
     */
    private Expression newIntegerBinaryOperatorExpression(int lineNumber, Expression leftExpression, String operator, Expression rightExpression, int priority) {
        if (leftExpression.isLocalVariableReferenceExpression()) {
            AbstractLocalVariable leftVariable = ((ClassFileLocalVariableReferenceExpression)leftExpression).getLocalVariable();
            leftVariable.typeOnLeft(typeBounds, MAYBE_BYTE_TYPE);
        }

        if (rightExpression.isLocalVariableReferenceExpression()) {
            AbstractLocalVariable rightVariable = ((ClassFileLocalVariableReferenceExpression)rightExpression).getLocalVariable();
            rightVariable.typeOnLeft(typeBounds, MAYBE_BYTE_TYPE);
        }

        return new BinaryOperatorExpression(lineNumber, TYPE_INT, leftExpression, operator, rightExpression, priority);
    }

    /*
     * Operators = { "&", "|", "^" }
     * See "Binary Numeric Promotion": https://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.22.1
     */
    private Expression newIntegerOrBooleanBinaryOperatorExpression(int lineNumber, Expression leftExpression, String operator, Expression rightExpression, int priority) {
        Type type = TYPE_INT;

        if (leftExpression.isLocalVariableReferenceExpression()) {
            AbstractLocalVariable leftVariable = ((ClassFileLocalVariableReferenceExpression)leftExpression).getLocalVariable();

            if (rightExpression.isLocalVariableReferenceExpression()) {
                AbstractLocalVariable rightVariable = ((ClassFileLocalVariableReferenceExpression)rightExpression).getLocalVariable();

                if (leftVariable.isAssignableFrom(typeBounds, TYPE_BOOLEAN) || rightVariable.isAssignableFrom(typeBounds, TYPE_BOOLEAN)) {
                    leftVariable.variableOnRight(typeBounds, rightVariable);
                    rightVariable.variableOnLeft(typeBounds, leftVariable);

                    if ((leftVariable.getType() == TYPE_BOOLEAN) || (rightVariable.getType() == TYPE_BOOLEAN)) {
                        type = TYPE_BOOLEAN;
                    }
                }
            } else {
                if (rightExpression.getType() == TYPE_BOOLEAN) {
                    leftVariable.typeOnRight(typeBounds, type = TYPE_BOOLEAN);
                }
            }
        } else if (rightExpression.isLocalVariableReferenceExpression()) {
            if (leftExpression.getType() == TYPE_BOOLEAN) {
                AbstractLocalVariable rightVariable = ((ClassFileLocalVariableReferenceExpression)rightExpression).getLocalVariable();
                rightVariable.typeOnRight(typeBounds, type = TYPE_BOOLEAN);
            }
        }

        if (type == TYPE_INT) {
            if (leftExpression.getType().isPrimitiveType() && rightExpression.getType().isPrimitiveType()) {
                int leftFlags = ((PrimitiveType)leftExpression.getType()).getFlags();
                int rightFlags = ((PrimitiveType)rightExpression.getType()).getFlags();
                boolean leftBoolean = (leftFlags & FLAG_BOOLEAN) != 0;
                boolean rightBoolean = (rightFlags & FLAG_BOOLEAN) != 0;
                int commonflags = leftFlags | rightFlags;

                if (!leftBoolean || !rightBoolean) {
                    commonflags &= ~FLAG_BOOLEAN;
                }

                type = PrimitiveTypeUtil.getPrimitiveTypeFromFlags(commonflags);

                if (type == null) {
                    type = TYPE_INT;
                }
            }
        }

        typeParametersToTypeArgumentsBinder.bindParameterTypesWithArgumentTypes(leftExpression.getType(), rightExpression);
        typeParametersToTypeArgumentsBinder.bindParameterTypesWithArgumentTypes(rightExpression.getType(), leftExpression);

        return new BinaryOperatorExpression(lineNumber, type, leftExpression, operator, rightExpression, priority);
    }

    /*
     * Operators = { "==", "!=" }
     * See "Numerical Equality Operators == and !=": https://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.21.1
     */
    private Expression newIntegerOrBooleanComparisonOperatorExpression(int lineNumber, Expression leftExpression, String operator, Expression rightExpression, int priority) {
        if (leftExpression.isLocalVariableReferenceExpression()) {
            AbstractLocalVariable leftVariable = ((ClassFileLocalVariableReferenceExpression)leftExpression).getLocalVariable();

            if (rightExpression.isLocalVariableReferenceExpression()) {
                AbstractLocalVariable rightVariable = ((ClassFileLocalVariableReferenceExpression)rightExpression).getLocalVariable();

                if (leftVariable.isAssignableFrom(typeBounds, TYPE_BOOLEAN) || rightVariable.isAssignableFrom(typeBounds, TYPE_BOOLEAN)) {
                    leftVariable.variableOnRight(typeBounds, rightVariable);
                    rightVariable.variableOnLeft(typeBounds, leftVariable);
                }
            } else {
                if (rightExpression.getType() == TYPE_BOOLEAN) {
                    leftVariable.typeOnRight(typeBounds, TYPE_BOOLEAN);
                }
            }
        } else if (rightExpression.isLocalVariableReferenceExpression()) {
            if (leftExpression.getType() == TYPE_BOOLEAN) {
                AbstractLocalVariable rightVariable = ((ClassFileLocalVariableReferenceExpression)rightExpression).getLocalVariable();
                rightVariable.typeOnRight(typeBounds, TYPE_BOOLEAN);
            }
        }

        typeParametersToTypeArgumentsBinder.bindParameterTypesWithArgumentTypes(leftExpression.getType(), rightExpression);
        typeParametersToTypeArgumentsBinder.bindParameterTypesWithArgumentTypes(rightExpression.getType(), leftExpression);

        return new BinaryOperatorExpression(lineNumber, TYPE_BOOLEAN, leftExpression, operator, rightExpression, priority);
    }

    /*
     * Operators = { "==", "!=" }
     * See "Numerical Equality Operators == and !=": https://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.21.1
     */
    private Expression newIntegerComparisonOperatorExpression(int lineNumber, Expression leftExpression, String operator, Expression rightExpression, int priority) {
        if (leftExpression.isLocalVariableReferenceExpression()) {
            AbstractLocalVariable leftVariable = ((ClassFileLocalVariableReferenceExpression)leftExpression).getLocalVariable();
            leftVariable.typeOnLeft(typeBounds, MAYBE_BYTE_TYPE);
        }

        if (rightExpression.isLocalVariableReferenceExpression()) {
            AbstractLocalVariable rightVariable = ((ClassFileLocalVariableReferenceExpression)rightExpression).getLocalVariable();
            rightVariable.typeOnLeft(typeBounds, MAYBE_BYTE_TYPE);
        }

        typeParametersToTypeArgumentsBinder.bindParameterTypesWithArgumentTypes(leftExpression.getType(), rightExpression);
        typeParametersToTypeArgumentsBinder.bindParameterTypesWithArgumentTypes(rightExpression.getType(), leftExpression);

        return new BinaryOperatorExpression(lineNumber, TYPE_BOOLEAN, leftExpression, operator, rightExpression, priority);
    }

    private Expression newPreArithmeticOperatorExpression(int lineNumber, String operator, Expression expression) {
        reduceIntegerLocalVariableType(expression);
        return new PreOperatorExpression(lineNumber, operator, expression);
    }

    private Expression newPostArithmeticOperatorExpression(int lineNumber, Expression expression, String operator) {
        reduceIntegerLocalVariableType(expression);
        return new PostOperatorExpression(lineNumber, expression, operator);
    }

    private void reduceIntegerLocalVariableType(Expression expression) {
        if (expression.isLocalVariableReferenceExpression()) {
            ClassFileLocalVariableReferenceExpression lvre = (ClassFileLocalVariableReferenceExpression)expression;

            if (lvre.getLocalVariable().isPrimitiveLocalVariable()) {
                AbstractLocalVariable plv = lvre.getLocalVariable();
                if (plv.isAssignableFrom(typeBounds, MAYBE_BOOLEAN_TYPE)) {
                    plv.typeOnRight(typeBounds, MAYBE_BYTE_TYPE);
                }
            }
        }
    }

    /**
     * @return expression, 'this' or 'super'
     */
    private Expression getFieldInstanceReference(Expression expression, ObjectType ot, String name) {
        if ((bodyDeclaration.getFieldDeclarations() != null) && expression.isThisExpression()) {
            String internalName = expression.getType().getInternalName();

            if (!ot.getInternalName().equals(internalName)) {
                memberVisitor.init(name, null);
                for (ClassFileFieldDeclaration field : bodyDeclaration.getFieldDeclarations()) {
                    field.getFieldDeclarators().accept(memberVisitor);
                    if (memberVisitor.found()) {
                        return new SuperExpression(expression.getLineNumber(), expression.getType());
                    }
                }
            }
        }

        return expression;
    }

    /**
     * @return expression, 'this' or 'super'
     */
    private Expression getMethodInstanceReference(Expression expression, ObjectType ot, String name, String descriptor) {
        if ((bodyDeclaration.getMethodDeclarations() != null) && expression.isThisExpression()) {
            String internalName = expression.getType().getInternalName();

            if (!ot.getInternalName().equals(internalName)) {
                memberVisitor.init(name, descriptor);

                for (ClassFileConstructorOrMethodDeclaration member : bodyDeclaration.getMethodDeclarations()) {
                    member.accept(memberVisitor);
                    if (memberVisitor.found()) {
                        return new SuperExpression(expression.getLineNumber(), expression.getType());
                    }
                }
            }
        }

        return expression;
    }

    private static void checkStack(DefaultStack<Expression> stack, byte[] code, int offset) {
        if ((stack.size() > 1) && (offset < code.length)) {
            int opcode = code[offset+1] & 255;

            if ((opcode == 87) || (opcode == 176)) { // POP || ARETURN
                // Duplicate last expression
                Expression condition = stack.pop();
                stack.push(stack.peek());
                stack.push(condition);
            }
        }
    }

    public static boolean isAssertCondition(String internalTypeName, BasicBlock basicBlock) {
        ControlFlowGraph cfg = basicBlock.getControlFlowGraph();
        int offset = basicBlock.getFromOffset();
        int toOffset = basicBlock.getToOffset();

        if (offset + 3 > toOffset)
            return false;

        Method method = cfg.getMethod();
        byte[] code = method.<AttributeCode>getAttribute("Code").getCode();
        int opcode = code[offset] & 255;

        if (opcode != 178) // GETSTATIC
            return false;

        ConstantPool constants = method.getConstants();
        ConstantMemberRef constantMemberRef = constants.getConstant( ((code[++offset] & 255) << 8) | (code[++offset] & 255) );
        ConstantNameAndType constantNameAndType = constants.getConstant(constantMemberRef.getNameAndTypeIndex());
        String name = constants.getConstantUtf8(constantNameAndType.getNameIndex());

        if (! "$assertionsDisabled".equals(name))
            return false;

        String descriptor = constants.getConstantUtf8(constantNameAndType.getDescriptorIndex());

        if (! "Z".equals(descriptor))
            return false;

        String typeName = constants.getConstantTypeName(constantMemberRef.getClassIndex());

        return internalTypeName.equals(typeName);
    }

    public static int getExceptionLocalVariableIndex(BasicBlock basicBlock) {
        ControlFlowGraph cfg = basicBlock.getControlFlowGraph();
        int offset = basicBlock.getFromOffset();
        int toOffset = basicBlock.getToOffset();

        if (offset + 1 > toOffset) {
            assert false;
            return -1;
        }

        Method method = cfg.getMethod();
        byte[] code = method.<AttributeCode>getAttribute("Code").getCode();
        int opcode = code[offset] & 255;

        switch (opcode) {
            case 58: // ASTORE
                return code[++offset] & 255;
            case 75: case 76: case 77: case 78: // ASTORE_0 ... ASTORE_3
                return opcode - 75;
            case 87: case 88: // POP, POP2
                return -1;
            default:
                assert false;
                return -1;
        }
    }

    private static class MemberVisitor extends AbstractJavaSyntaxVisitor {
        protected String name;
        protected String descriptor;
        protected boolean found;

        public void init(String name, String descriptor) {
            this.name = name;
            this.descriptor = descriptor;
            this.found = false;
        }

        public boolean found() {
            return found;
        }

        @Override
        public void visit(FieldDeclarator declaration) {
            found |= declaration.getName().equals(name);
        }

        @Override
        public void visit(MethodDeclaration declaration) {
            found |= declaration.getName().equals(name) && declaration.getDescriptor().equals(descriptor);
        }
    }

    private static class LambdaParameterNamesVisitor extends AbstractNopDeclarationVisitor {
        protected DefaultList<String> names;

        public void init() {
            names = new DefaultList<>();
        }

        public List<String> getNames() {
            return names;
        }

        @Override public void visit(FormalParameter declaration) {
            names.add(declaration.getName());
        }

        @Override
        @SuppressWarnings("unchecked")
        public void visit(FormalParameters declarations) {
            Iterator<FormalParameter> iterator = declarations.iterator();

            while (iterator.hasNext()) {
                iterator.next().accept(this);
            }
        }
    }

    private static class JsrReturnAddressExpression extends NullExpression {
        public JsrReturnAddressExpression() {
            super(TYPE_VOID);
        }

        @Override
        public String toString() {
            return "JsrReturnAddressExpression{}";
        }
    }

    private Type makeFieldType(String internalTypeName, String fieldName, String descriptor) {
        Type type = typeMaker.makeFieldType(internalTypeName, fieldName, descriptor);

        if (!genericTypesSupported) {
            eraseTypeArgumentVisitor.init();
            type.accept(eraseTypeArgumentVisitor);
            type = (Type)eraseTypeArgumentVisitor.getBaseType();
        }

        return type;
    }

    private TypeMaker.MethodTypes makeMethodTypes(String internalTypeName, String methodName, String descriptor) {
        TypeMaker.MethodTypes methodTypes = typeMaker.makeMethodTypes(internalTypeName, methodName, descriptor);

        if (!genericTypesSupported) {
            TypeMaker.MethodTypes mt = new TypeMaker.MethodTypes();

            if (methodTypes.parameterTypes != null) {
                eraseTypeArgumentVisitor.init();
                methodTypes.parameterTypes.accept(eraseTypeArgumentVisitor);
                mt.parameterTypes = eraseTypeArgumentVisitor.getBaseType();
            }

            eraseTypeArgumentVisitor.init();
            methodTypes.returnedType.accept(eraseTypeArgumentVisitor);
            mt.returnedType = (Type)eraseTypeArgumentVisitor.getBaseType();

            if (methodTypes.exceptionTypes != null) {
                eraseTypeArgumentVisitor.init();
                methodTypes.exceptionTypes.accept(eraseTypeArgumentVisitor);
                mt.exceptionTypes = eraseTypeArgumentVisitor.getBaseType();
            }

            methodTypes = mt;
        }

        return methodTypes;
    }

    protected static Expression forceExplicitCastExpression(Expression expression) {
        // In case of downcasting, set all cast expression children as explicit to prevent missing castings
        Expression exp = expression;

        while (exp.isCastExpression()) {
            CastExpression ce = (CastExpression)exp;
            ce.setExplicit(true);
            exp = ce.getExpression();
        }

        return expression;
    }
}
