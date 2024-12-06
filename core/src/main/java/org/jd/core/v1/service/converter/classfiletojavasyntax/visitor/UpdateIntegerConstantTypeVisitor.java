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
import org.jd.core.v1.model.javasyntax.reference.InnerObjectReference;
import org.jd.core.v1.model.javasyntax.reference.ObjectReference;
import org.jd.core.v1.model.javasyntax.statement.*;
import org.jd.core.v1.model.javasyntax.type.*;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.expression.ClassFileConstructorInvocationExpression;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.expression.ClassFileMethodInvocationExpression;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.expression.ClassFileNewExpression;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.expression.ClassFileSuperConstructorInvocationExpression;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.PrimitiveTypeUtil;
import org.jd.core.v1.util.DefaultList;

import java.util.HashMap;

import static org.jd.core.v1.model.javasyntax.type.PrimitiveType.*;

public class UpdateIntegerConstantTypeVisitor extends AbstractJavaSyntaxVisitor {
    protected static final HashMap<String, BaseType> TYPES = new HashMap<>();

    protected static final DimensionTypes DIMENSION_TYPES = new DimensionTypes();

    protected static final ObjectTypeReferenceExpression TYPE_CHARACTER_REFERENCE = new ObjectTypeReferenceExpression(ObjectType.TYPE_CHARACTER);
    protected static final ObjectTypeReferenceExpression TYPE_BYTE_REFERENCE = new ObjectTypeReferenceExpression(ObjectType.TYPE_BYTE);
    protected static final ObjectTypeReferenceExpression TYPE_SHORT_REFERENCE = new ObjectTypeReferenceExpression(ObjectType.TYPE_SHORT);
    protected static final ObjectTypeReferenceExpression TYPE_INTEGER_REFERENCE = new ObjectTypeReferenceExpression(ObjectType.TYPE_INTEGER);

    protected Type returnedType;

    static {
        BaseType c  = TYPE_CHAR;
        BaseType ci = new Types(TYPE_CHAR, TYPE_INT);

        TYPES.put("java/lang/String:indexOf(I)I", c);
        TYPES.put("java/lang/String:indexOf(II)I", ci);
        TYPES.put("java/lang/String:lastIndexOf(I)I", c);
        TYPES.put("java/lang/String:lastIndexOf(II)I", ci);
    }

    public UpdateIntegerConstantTypeVisitor(Type returnedType) {
        this.returnedType = returnedType;
    }

    @Override
    public void visit(AssertStatement statement) {
        statement.setCondition(updateBooleanExpression(statement.getCondition()));
    }

    @Override
    public void visit(DoWhileStatement statement) {
        statement.setCondition(safeUpdateBooleanExpression(statement.getCondition()));
        safeAccept(statement.getStatements());
    }

    @Override
    public void visit(ForStatement statement) {
        safeAccept(statement.getDeclaration());
        safeAccept(statement.getInit());
        statement.setCondition(safeUpdateBooleanExpression(statement.getCondition()));
        safeAccept(statement.getUpdate());
        safeAccept(statement.getStatements());
    }

    @Override
    public void visit(IfStatement statement) {
        statement.setCondition(updateBooleanExpression(statement.getCondition()));
        safeAccept(statement.getStatements());
    }

    @Override
    public void visit(IfElseStatement statement) {
        statement.setCondition(updateBooleanExpression(statement.getCondition()));
        safeAccept(statement.getStatements());
        statement.getElseStatements().accept(this);
    }

    @Override
    public void visit(WhileStatement statement) {
        statement.setCondition(updateBooleanExpression(statement.getCondition()));
        safeAccept(statement.getStatements());
    }

    @Override public void visit(ReturnExpressionStatement statement) {
        statement.setExpression(updateExpression(returnedType, statement.getExpression()));
    }

    @Override
    public void visit(BinaryOperatorExpression expression) {
        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();

        Type leftType = left.getType();
        Type rightType = right.getType();

        switch (expression.getOperator()) {
            case "&":
            case "|":
            case "^":
                if (leftType.isPrimitiveType() && rightType.isPrimitiveType()) {
                    Type type = PrimitiveTypeUtil.getCommonPrimitiveType((PrimitiveType) leftType, (PrimitiveType) rightType);
                    if (type == null) {
                        type = TYPE_INT;
                    }
                    expression.setLeftExpression(updateExpression(type, left));
                    expression.setRightExpression(updateExpression(type, right));
                }
                break;
            case "=":
                left.accept(this);
                expression.setRightExpression(updateExpression(leftType, right));
                break;
            case ">":
            case ">=":
            case "<":
            case "<=":
            case "==":
            case "!=":
                if ((leftType.getDimension() == 0) && (rightType.getDimension() == 0)) {
                    if (leftType.isPrimitiveType()) {
                        if (rightType.isPrimitiveType()) {
                            Type type;
                            if (leftType == rightType) {
                                type = leftType;
                            } else {
                                type = PrimitiveTypeUtil.getCommonPrimitiveType((PrimitiveType)leftType, (PrimitiveType)rightType);
                                if (type == null) {
                                    type = TYPE_INT;
                                }
                            }
                            expression.setLeftExpression(updateExpression(type, left));
                            expression.setRightExpression(updateExpression(type, right));
                        } else {
                            expression.setLeftExpression(updateExpression(rightType, left));
                            right.accept(this);
                        }
                        break;
                    } else if (rightType.isPrimitiveType()) {
                        left.accept(this);
                        expression.setRightExpression(updateExpression(leftType, right));
                        break;
                    }
                }

                left.accept(this);
                right.accept(this);
                break;
            default:
                expression.setRightExpression(updateExpression(expression.getType(), right));
                expression.setLeftExpression(updateExpression(expression.getType(), left));
                break;
        }
    }

    @Override
    public void visit(LambdaIdentifiersExpression expression) {
        Type rt = returnedType;
        returnedType = expression.getReturnedType();
        safeAccept(expression.getStatements());
        returnedType = rt;
    }

    @Override
    public void visit(SuperConstructorInvocationExpression expression) {
        BaseExpression parameters = expression.getParameters();

        if (parameters != null) {
            expression.setParameters(updateExpressions(((ClassFileSuperConstructorInvocationExpression)expression).getParameterTypes(), parameters));
        }
    }

    @Override
    public void visit(ConstructorInvocationExpression expression) {
        BaseExpression parameters = expression.getParameters();

        if (parameters != null) {
            expression.setParameters(updateExpressions(((ClassFileConstructorInvocationExpression)expression).getParameterTypes(), parameters));
        }
    }

    @Override
    public void visit(MethodInvocationExpression expression) {
        BaseExpression parameters = expression.getParameters();

        if (parameters != null) {
            String internalTypeName = expression.getInternalTypeName();
            String name = expression.getName();
            String descriptor = expression.getDescriptor();
            BaseType types = TYPES.get(internalTypeName + ':' + name + descriptor);

            if (types == null) {
                types = ((ClassFileMethodInvocationExpression)expression).getParameterTypes();
            }

            expression.setParameters(updateExpressions(types, parameters));
        }

        expression.getExpression().accept(this);
    }

    @Override
    public void visit(NewExpression expression) {
        BaseExpression parameters = expression.getParameters();

        if (parameters != null) {
            String internalTypeName = expression.getObjectType().getInternalName();
            String descriptor = expression.getDescriptor();
            BaseType types = TYPES.get(internalTypeName + ":<init>" + descriptor);

            if (types == null) {
                types = ((ClassFileNewExpression)expression).getParameterTypes();
            }

            expression.setParameters(updateExpressions(types, parameters));
        }
    }

    @Override
    public void visit(NewArray expression) {
        BaseExpression dimensions = expression.getDimensionExpressionList();

        if (dimensions != null) {
            updateExpressions(DIMENSION_TYPES, dimensions);
        }
    }

    @Override
    public void visit(ArrayExpression expression) {
        expression.getExpression().accept(this);
        expression.setIndex(updateExpression(TYPE_INT, expression.getIndex()));
    }

    @Override
    public void visit(CastExpression expression) {
        expression.setExpression(updateExpression(expression.getType(), expression.getExpression()));
    }

    @Override
    public void visit(TernaryOperatorExpression expression) {
        Type trueType = expression.getTrueExpression().getType();
        Type falseType = expression.getFalseExpression().getType();

        expression.setCondition(updateBooleanExpression(expression.getCondition()));

        if (trueType.isPrimitiveType()) {
            if (falseType.isPrimitiveType()) {
                expression.setTrueExpression(updateExpression(TYPE_INT, expression.getTrueExpression()));
                expression.setFalseExpression(updateExpression(TYPE_INT, expression.getFalseExpression()));
            } else {
                expression.getTrueExpression().accept(this);
                expression.setTrueExpression(updateExpression(falseType, expression.getTrueExpression()));
            }
        } else {
            if (falseType.isPrimitiveType()) {
                expression.setFalseExpression(updateExpression(trueType, expression.getFalseExpression()));
                expression.getFalseExpression().accept(this);
            } else {
                expression.getTrueExpression().accept(this);
                expression.getFalseExpression().accept(this);
            }
        }
    }

    protected Type arrayVariableInitializerType;

    @Override
    public void visit(ArrayVariableInitializer declaration) {
        Type t = arrayVariableInitializerType;
        arrayVariableInitializerType = declaration.getType();
        acceptListDeclaration(declaration);
        arrayVariableInitializerType = t;
    }

    @Override
    public void visit(LocalVariableDeclaration declaration) {
        Type t = arrayVariableInitializerType;
        arrayVariableInitializerType = declaration.getType();
        declaration.getLocalVariableDeclarators().accept(this);
        arrayVariableInitializerType = t;
    }

    @Override
    public void visit(FieldDeclaration declaration) {
        Type t = arrayVariableInitializerType;
        arrayVariableInitializerType = declaration.getType();
        declaration.getFieldDeclarators().accept(this);
        arrayVariableInitializerType = t;
    }

    @Override
    public void visit(ExpressionVariableInitializer declaration) {
        if (declaration != null) {
            declaration.setExpression(updateExpression(arrayVariableInitializerType, declaration.getExpression()));
        }
    }

    @SuppressWarnings("unchecked")
    protected BaseExpression updateExpressions(BaseType types, BaseExpression expressions) {
        if (expressions.isList()) {
            DefaultList<Type> t = types.getList();
            DefaultList<Expression> e = expressions.getList();

            for (int i=e.size()-1; i>=0; i--) {
                Type type = t.get(i);

                if ((type.getDimension() == 0) && type.isPrimitiveType()) {
                    Expression parameter = e.get(i);
                    Expression updatedParameter = updateExpression(type, parameter);

                    if (updatedParameter.isIntegerConstantExpression()) {
                        switch (((PrimitiveType)type).getJavaPrimitiveFlags()) {
                            case FLAG_BYTE:
                            case FLAG_SHORT:
                                updatedParameter = new CastExpression(type, updatedParameter);
                                break;
                        }
                    }

                    e.set(i, updatedParameter);
                }
            }
        } else {
            Type type = types.getFirst();

            if ((type.getDimension() == 0) && type.isPrimitiveType()) {
                Expression updatedParameter = updateExpression(type, (Expression)expressions);

                if (updatedParameter.isIntegerConstantExpression()) {
                    switch (((PrimitiveType)type).getJavaPrimitiveFlags()) {
                        case FLAG_BYTE:
                        case FLAG_SHORT:
                            updatedParameter = new CastExpression(type, updatedParameter);
                            break;
                    }
                }

                expressions = updatedParameter;
            }
        }

        expressions.accept(this);
        return expressions;
    }

    protected Expression updateExpression(Type type, Expression expression) {
        assert type != TYPE_VOID : "UpdateIntegerConstantTypeVisitor.updateExpression(type, expr) : try to set 'void' to a numeric expression";

        if ((type != expression.getType()) && expression.isIntegerConstantExpression()) {
            if (ObjectType.TYPE_STRING.equals(type)) {
                type = PrimitiveType.TYPE_CHAR;
            }

            if (type.isPrimitiveType()) {
                PrimitiveType primitiveType = (PrimitiveType) type;
                IntegerConstantExpression ice = (IntegerConstantExpression) expression;
                PrimitiveType icePrimitiveType = (PrimitiveType)ice.getType();
                int value = ice.getIntegerValue();
                int lineNumber = ice.getLineNumber();

                switch (primitiveType.getJavaPrimitiveFlags()) {
                    case FLAG_BOOLEAN:
                        return new BooleanExpression(lineNumber, value != 0);
                    case FLAG_CHAR:
                        switch (value) {
                            case Character.MIN_VALUE:
                                return new FieldReferenceExpression(lineNumber, TYPE_CHAR, TYPE_CHARACTER_REFERENCE, "java/lang/Character", "MIN_VALUE", "C");
                            case Character.MAX_VALUE:
                                return new FieldReferenceExpression(lineNumber, TYPE_CHAR, TYPE_CHARACTER_REFERENCE, "java/lang/Character", "MAX_VALUE", "C");
                            default:
                                if ((icePrimitiveType.getFlags() & primitiveType.getFlags()) != 0) {
                                    ice.setType(type);
                                } else {
                                    ice.setType(TYPE_INT);
                                }
                                break;
                        }
                        break;
                    case FLAG_BYTE:
                        switch (value) {
                            case Byte.MIN_VALUE:
                                return new FieldReferenceExpression(lineNumber, TYPE_BYTE, TYPE_BYTE_REFERENCE, "java/lang/Byte", "MIN_VALUE", "B");
                            case Byte.MAX_VALUE:
                                return new FieldReferenceExpression(lineNumber, TYPE_BYTE, TYPE_BYTE_REFERENCE, "java/lang/Byte", "MAX_VALUE", "B");
                            default:
                                if ((icePrimitiveType.getFlags() & primitiveType.getFlags()) != 0) {
                                    ice.setType(type);
                                } else {
                                    ice.setType(TYPE_INT);
                                }
                                break;
                        }
                        break;
                    case FLAG_SHORT:
                        switch (value) {
                            case Short.MIN_VALUE:
                                return new FieldReferenceExpression(lineNumber, TYPE_SHORT, TYPE_SHORT_REFERENCE, "java/lang/Short", "MIN_VALUE", "S");
                            case Short.MAX_VALUE:
                                return new FieldReferenceExpression(lineNumber, TYPE_SHORT, TYPE_SHORT_REFERENCE, "java/lang/Short", "MAX_VALUE", "S");
                            default:
                                if ((icePrimitiveType.getFlags() & primitiveType.getFlags()) != 0) {
                                    ice.setType(type);
                                } else {
                                    ice.setType(TYPE_INT);
                                }
                                break;
                        }
                        break;
                    case FLAG_INT:
                        switch (value) {
                            case Integer.MIN_VALUE:
                                return new FieldReferenceExpression(lineNumber, TYPE_INT, TYPE_INTEGER_REFERENCE, "java/lang/Integer", "MIN_VALUE", "I");
                            case Integer.MAX_VALUE:
                                return new FieldReferenceExpression(lineNumber, TYPE_INT, TYPE_INTEGER_REFERENCE, "java/lang/Integer", "MAX_VALUE", "I");
                            default:
                                if ((icePrimitiveType.getFlags() & primitiveType.getFlags()) != 0) {
                                    ice.setType(type);
                                } else {
                                    ice.setType(TYPE_INT);
                                }
                                break;
                        }
                        break;
                    case FLAG_LONG:
                        return new LongConstantExpression(ice.getLineNumber(), ice.getIntegerValue());
                }

                return expression;
            }
        }

        if (type.isPrimitiveType() && expression.isTernaryOperatorExpression()) {
            TernaryOperatorExpression toe = (TernaryOperatorExpression) expression;

            toe.setType(type);
            toe.setCondition(updateBooleanExpression(toe.getCondition()));
            toe.setTrueExpression(updateExpression(type, toe.getTrueExpression()));
            toe.setFalseExpression(updateExpression(type, toe.getFalseExpression()));

            return expression;
        }

        expression.accept(this);
        return expression;
    }

    protected Expression safeUpdateBooleanExpression(Expression expression) {
        return (expression == null) ? null : updateBooleanExpression(expression);
    }

    protected Expression updateBooleanExpression(Expression expression) {
        if (TYPE_BOOLEAN != expression.getType()) {
            if (expression.isIntegerConstantExpression()) {
                return new BooleanExpression(expression.getLineNumber(), expression.getIntegerValue()!=0);
            } else if (expression.isTernaryOperatorExpression()) {
                TernaryOperatorExpression toe = (TernaryOperatorExpression) expression;

                toe.setType(TYPE_BOOLEAN);
                toe.setCondition(updateBooleanExpression(toe.getCondition()));
                toe.setTrueExpression(updateBooleanExpression(toe.getTrueExpression()));
                toe.setFalseExpression(updateBooleanExpression(toe.getFalseExpression()));

                return expression;
            }
        }

        expression.accept(this);
        return expression;
    }

    @Override public void visit(FloatConstantExpression expression) {}
    @Override public void visit(IntegerConstantExpression expression) {}
    @Override public void visit(ConstructorReferenceExpression expression) {}
    @Override public void visit(DoubleConstantExpression expression) {}
    @Override public void visit(EnumConstantReferenceExpression expression) {}
    @Override public void visit(LocalVariableReferenceExpression expression) {}
    @Override public void visit(LongConstantExpression expression) {}
    @Override public void visit(BreakStatement statement) {}
    @Override public void visit(ByteCodeStatement statement) {}
    @Override public void visit(ContinueStatement statement) {}
    @Override public void visit(NullExpression expression) {}
    @Override public void visit(ObjectTypeReferenceExpression expression) {}
    @Override public void visit(SuperExpression expression) {}
    @Override public void visit(ThisExpression expression) {}
    @Override public void visit(TypeReferenceDotClassExpression expression) {}
    @Override public void visit(ObjectReference reference) {}
    @Override public void visit(InnerObjectReference reference) {}
    @Override public void visit(TypeArguments type) {}
    @Override public void visit(WildcardExtendsTypeArgument type) {}
    @Override public void visit(ObjectType type) {}
    @Override public void visit(InnerObjectType type) {}
    @Override public void visit(WildcardSuperTypeArgument type) {}
    @Override public void visit(Types list) {}
    @Override public void visit(TypeParameterWithTypeBounds type) {}
    @Override public void visit(BodyDeclaration declaration) {}

    protected static class DimensionTypes extends Types {
        @Override public Type getFirst() { return PrimitiveType.TYPE_INT; }
        @Override public Type getLast() { return PrimitiveType.TYPE_INT; }
        @Override public Type get(int i) { return PrimitiveType.TYPE_INT; }
        @Override public int size() { return 0; }
    }
}
