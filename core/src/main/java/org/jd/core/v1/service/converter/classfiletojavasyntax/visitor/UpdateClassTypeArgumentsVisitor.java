/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.visitor;

import org.jd.core.v1.model.javasyntax.type.*;

import static org.jd.core.v1.model.javasyntax.type.ObjectType.TYPE_CLASS;
import static org.jd.core.v1.model.javasyntax.type.ObjectType.TYPE_CLASS_WILDCARD;
import static org.jd.core.v1.model.javasyntax.type.ObjectType.TYPE_OBJECT;

public class UpdateClassTypeArgumentsVisitor extends AbstractTypeArgumentVisitor {
    protected BaseTypeArgument result;

    public void init() {
        this.result = null;
    }

    public BaseTypeArgument getTypeArgument() {
        return result;
    }

    @Override
    public void visit(WildcardExtendsTypeArgument argument) {
        Type type = argument.getType();

        type.accept(this);

        result = (result == type) ? argument : new WildcardExtendsTypeArgument((Type)result);
    }

    @Override
    public void visit(WildcardSuperTypeArgument argument) {
        Type type = argument.getType();

        type.accept(this);

        result = (result == type) ? argument : new WildcardSuperTypeArgument((Type)result);
    }

    @Override public void visit(DiamondTypeArgument argument) { result = argument; }
    @Override public void visit(WildcardTypeArgument argument) { result = argument; }

    @Override public void visit(PrimitiveType type) { result = type; }
    @Override public void visit(GenericType type) { result = type; }

    @Override
    public void visit(ObjectType type) {
        BaseTypeArgument typeArguments = type.getTypeArguments();

        if (typeArguments == null) {
            if (type.getInternalName().equals(TYPE_CLASS.getInternalName())) {
                result = TYPE_CLASS_WILDCARD;
            } else {
                result = type;
            }
        } else {
            typeArguments.accept(this);

            if (result == typeArguments) {
                result = type;
            } else {
                result = type.createType(typeArguments);
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
                result = (result == typeArguments) ? type : type.createType(result);
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
                    newTypes.add((TypeArgument) result);
                }

                result = newTypes;
            }
        }
    }
}
