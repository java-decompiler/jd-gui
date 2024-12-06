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

public class BindTypesToTypesVisitor extends AbstractNopTypeVisitor {
    protected TypeArgumentToTypeVisitor typeArgumentToTypeVisitor = new TypeArgumentToTypeVisitor();
    protected BindTypeArgumentsToTypeArgumentsVisitor bindTypeArgumentsToTypeArgumentsVisitor = new BindTypeArgumentsToTypeArgumentsVisitor();

    protected Map<String, TypeArgument> bindings;
    protected BaseType result;

    public void setBindings(Map<String, TypeArgument> bindings) {
        bindTypeArgumentsToTypeArgumentsVisitor.setBindings(this.bindings = bindings);
    }

    public void init() {
        this.result = null;
    }

    public BaseType getType() {
        return result;
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
            bindTypeArgumentsToTypeArgumentsVisitor.init();
            typeArguments.accept(bindTypeArgumentsToTypeArgumentsVisitor);
            BaseTypeArgument ta = bindTypeArgumentsToTypeArgumentsVisitor.getTypeArgument();

            if (typeArguments == ta) {
                result = type;
            } else {
                result = type.createType(ta);
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
                bindTypeArgumentsToTypeArgumentsVisitor.init();
                typeArguments.accept(bindTypeArgumentsToTypeArgumentsVisitor);
                BaseTypeArgument ta = bindTypeArgumentsToTypeArgumentsVisitor.getTypeArgument();

                if (typeArguments == ta) {
                    result = type;
                } else {
                    result = type.createType(ta);
                }
            }
        } else {
            ObjectType outerObjectType = (ObjectType) result;

            if (typeArguments != null) {
                bindTypeArgumentsToTypeArgumentsVisitor.init();
                typeArguments.accept(bindTypeArgumentsToTypeArgumentsVisitor);
                typeArguments = bindTypeArgumentsToTypeArgumentsVisitor.getTypeArgument();

                if (WildcardTypeArgument.WILDCARD_TYPE_ARGUMENT == typeArguments) {
                    typeArguments = null;
                }
            }

            result = new InnerObjectType(type.getInternalName(), type.getQualifiedName(), type.getName(), typeArguments, type.getDimension(), outerObjectType);
        }
    }

    @Override
    public void visit(GenericType type) {
        TypeArgument ta = bindings.get(type.getName());

        if (ta == null) {
            result = TYPE_OBJECT.createType(type.getDimension());
        } else if (ta == WildcardTypeArgument.WILDCARD_TYPE_ARGUMENT) {
            result = TYPE_OBJECT.createType(type.getDimension());
        } else {
            typeArgumentToTypeVisitor.init();
            ta.accept(typeArgumentToTypeVisitor);
            Type t = typeArgumentToTypeVisitor.getType();
            result = t.createType(t.getDimension() + type.getDimension());
        }
    }

    @Override
    public void visit(Types types) {
        int size = types.size();
        int i;

        for (i=0; i<size; i++) {
            Type t = types.get(i);
            t.accept(this);
            if (result != t) {
                break;
            }
        }

        if (i == size) {
            result = types;
        } else {
            Types newTypes = new Types(size);

            newTypes.addAll(types.subList(0, i));
            newTypes.add((Type) result);

            for (i++; i<size; i++) {
                Type t = types.get(i);
                t.accept(this);
                newTypes.add((Type) result);
            }

            result = newTypes;
        }
    }
}
