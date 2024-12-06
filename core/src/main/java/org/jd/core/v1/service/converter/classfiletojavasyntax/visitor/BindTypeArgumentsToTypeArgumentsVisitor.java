/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.visitor;

import org.jd.core.v1.model.javasyntax.type.*;

import java.util.Map;

import static org.jd.core.v1.model.javasyntax.type.ObjectType.TYPE_OBJECT;

public class BindTypeArgumentsToTypeArgumentsVisitor extends AbstractTypeArgumentVisitor {
    protected TypeArgumentToTypeVisitor typeArgumentToTypeVisitor = new TypeArgumentToTypeVisitor();
    protected Map<String, TypeArgument> bindings;
    protected BaseTypeArgument result;

    public void setBindings(Map<String, TypeArgument> bindings) {
        this.bindings = bindings;
    }

    public void init() {
        this.result = null;
    }

    public BaseTypeArgument getTypeArgument() {
        if ((result == null) || TYPE_OBJECT.equals(result)) {
            return null;
        }
        return result;
    }

    @Override
    public void visit(TypeArguments arguments) {
        int size = arguments.size();
        int i;

        for (i=0; i<size; i++) {
            TypeArgument ta = arguments.get(i);
            ta.accept(this);
            if (result != ta) {
                break;
            }
        }

        if (result != null) {
            if (i == size) {
                result = arguments;
            } else {
                TypeArguments newTypes = new TypeArguments(size);

                newTypes.addAll(arguments.subList(0, i));
                newTypes.add((TypeArgument) result);

                for (i++; i < size; i++) {
                    TypeArgument ta = arguments.get(i);
                    ta.accept(this);
                    if (result == null) {
                        return;
                    }
                    newTypes.add((TypeArgument) result);
                }

                result = newTypes;
            }
        }
    }

    @Override
    public void visit(DiamondTypeArgument argument) {
        result = argument;
    }

    @Override
    public void visit(WildcardExtendsTypeArgument argument) {
        argument.getType().accept(this);

        if (result == WildcardTypeArgument.WILDCARD_TYPE_ARGUMENT) {
            result = WildcardTypeArgument.WILDCARD_TYPE_ARGUMENT;
        } else if (result == argument.getType()) {
            result = argument;
        } else if (TYPE_OBJECT.equals(result)) {
            result = WildcardTypeArgument.WILDCARD_TYPE_ARGUMENT;
        } else if (result != null) {
            typeArgumentToTypeVisitor.init();
            result.accept(typeArgumentToTypeVisitor);
            BaseType bt = typeArgumentToTypeVisitor.getType();

            if (TYPE_OBJECT.equals(bt)) {
                result = WildcardTypeArgument.WILDCARD_TYPE_ARGUMENT;
            } else {
                result = new WildcardExtendsTypeArgument((Type)bt);
            }
        }
    }

    @Override
    public void visit(PrimitiveType type) {
        result = type;
    }

    @Override
    public void visit(ObjectType type) {
        BaseTypeArgument typeArguments = type.getTypeArguments();

        if (typeArguments == null) {
            result = type;
        } else {
            typeArguments.accept(this);

            if (typeArguments == result) {
                result = type;
            } else if (result != null) {
                result = type.createType(result);
            }
        }
    }

    @Override
    public void visit(InnerObjectType type) {
        type.getOuterType().accept(this);

        BaseTypeArgument typeArguments = type.getTypeArguments();

        if (type.getOuterType() == result) {
            if (typeArguments == null) {
                result = type;
            } else {
                typeArguments.accept(this);

                if (typeArguments == result) {
                    result = type;
                } else if (result != null) {
                    result = type.createType(result);
                }
            }
        } else {
            ObjectType outerObjectType = (ObjectType) result;

            if (typeArguments != null) {
                typeArguments.accept(this);
                typeArguments = result;
            }

            result = new InnerObjectType(type.getInternalName(), type.getQualifiedName(), type.getName(), typeArguments, type.getDimension(), outerObjectType);
        }
    }

    @Override
    public void visit(WildcardSuperTypeArgument argument) {
        argument.getType().accept(this);

        if (result == WildcardTypeArgument.WILDCARD_TYPE_ARGUMENT) {
            result = WildcardTypeArgument.WILDCARD_TYPE_ARGUMENT;
        } else if (result == argument.getType()) {
            result = argument;
        } else if (result != null) {
            typeArgumentToTypeVisitor.init();
            result.accept(typeArgumentToTypeVisitor);
            result = new WildcardSuperTypeArgument(typeArgumentToTypeVisitor.getType());
        }
    }

    @Override
    public void visit(GenericType type) {
        TypeArgument ta = bindings.get(type.getName());

        if (ta == null) {
            result = null;
        } else if (ta instanceof Type) {
            result = ((Type)ta).createType(type.getDimension());
        } else {
            result = ta;
        }
    }

    @Override
    public void visit(WildcardTypeArgument argument) {
        result = argument;
    }
}
