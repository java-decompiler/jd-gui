/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.visitor;

import org.jd.core.v1.model.javasyntax.type.*;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.TypeMaker;

import java.util.Iterator;
import java.util.Map;

import static org.jd.core.v1.model.javasyntax.type.ObjectType.TYPE_CLASS;
import static org.jd.core.v1.model.javasyntax.type.ObjectType.TYPE_CLASS_WILDCARD;

public class PopulateBindingsWithTypeArgumentVisitor implements TypeArgumentVisitor {
    protected TypeArgumentToTypeVisitor typeArgumentToTypeVisitor = new TypeArgumentToTypeVisitor();
    protected TypeMaker typeMaker;
    protected Map<String, BaseType> contextualTypeBounds;
    protected Map<String, TypeArgument> bindings;
    protected Map<String, BaseType> typeBounds;
    protected BaseTypeArgument current;

    public PopulateBindingsWithTypeArgumentVisitor(TypeMaker typeMaker) {
        this.typeMaker = typeMaker;
        this.current = null;
    }

    public void init(Map<String, BaseType> contextualTypeBounds, Map<String, TypeArgument> bindings, Map<String, BaseType> typeBounds, BaseTypeArgument typeArgument) {
        this.contextualTypeBounds = contextualTypeBounds;
        this.bindings = bindings;
        this.typeBounds = typeBounds;
        this.current = typeArgument;
    }

    @Override
    public void visit(TypeArguments arguments) {
        if ((current != null) && current.isTypeArgumentList()) {
            Iterator<TypeArgument> typeArgumentIterator = arguments.iterator();
            Iterator<TypeArgument> typeGenericArgumentIterator = current.getTypeArgumentList().iterator();

            while (typeArgumentIterator.hasNext() && typeGenericArgumentIterator.hasNext()) {
                current = typeGenericArgumentIterator.next();
                typeArgumentIterator.next().accept(this);
            }
        }
    }

    @Override public void visit(GenericType type) {
        String typeName = type.getName();

        if (bindings.containsKey(typeName)) {
            TypeArgument typeArgument = bindings.get(typeName);

            if (current != null) {
                if (current.isGenericTypeArgument() && !equals(contextualTypeBounds.get(typeName), typeBounds.get(((GenericType)current).getName()))) {
                    return; // Incompatible bounds
                }

                if (typeArgument == null) {
                    bindings.put(typeName, checkTypeClassCheckDimensionAndReturnCurrentAsTypeArgument(type));
                } else if (!current.equals(typeArgument)) {
                    typeArgumentToTypeVisitor.init();
                    typeArgument.accept(typeArgumentToTypeVisitor);
                    Type t1 = typeArgumentToTypeVisitor.getType();

                    typeArgumentToTypeVisitor.init();
                    current.accept(typeArgumentToTypeVisitor);
                    Type t2 = typeArgumentToTypeVisitor.getType();

                    if (!t1.createType(0).equals(t2.createType(0))) {
                        if (t1.isObjectType() && t2.isObjectType()) {
                            ObjectType ot1 = (ObjectType)t1;
                            ObjectType ot2 = (ObjectType)t2.createType(t2.getDimension() - type.getDimension());

                            if (!typeMaker.isAssignable(typeBounds, ot1, ot2)) {
                                if (typeMaker.isAssignable(typeBounds, ot2, ot1)) {
                                    bindings.put(typeName, checkTypeClassCheckDimensionAndReturnCurrentAsTypeArgument(type));
                                } else {
                                    bindings.put(typeName, WildcardTypeArgument.WILDCARD_TYPE_ARGUMENT);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean equals(BaseType bt1, BaseType bt2) {
        return (bt2 == null) || bt2.equals(bt1);
    }

    protected TypeArgument checkTypeClassCheckDimensionAndReturnCurrentAsTypeArgument(GenericType type) {
        if (current != null) {
            Class currentClass = current.getClass();

            if (current.isObjectTypeArgument()) {
                ObjectType ot = (ObjectType) current;

                if ((ot.getTypeArguments() == null) && ot.getInternalName().equals(TYPE_CLASS.getInternalName())) {
                    return TYPE_CLASS_WILDCARD.createType(ot.getDimension() - type.getDimension());
                }

                return ot.createType(ot.getDimension() - type.getDimension());
            } else if (current.isInnerObjectTypeArgument() || current.isGenericTypeArgument() || current.isPrimitiveTypeArgument()) {
                Type t = (Type)current;
                return t.createType(t.getDimension() - type.getDimension());
            }
        }

        return current.getTypeArgumentFirst();
    }

    @Override
    public void visit(WildcardExtendsTypeArgument type) {
        if (current != null) {
            if (current.isWildcardExtendsTypeArgument()) {
                current = current.getType();
                type.getType().accept(this);
            } else {
                type.getType().accept(this);
            }
        }
    }

    @Override
    public void visit(WildcardSuperTypeArgument type) {
        if (current != null) {
            if (current.isWildcardSuperTypeArgument()) {
                current = current.getType();
                type.getType().accept(this);
            } else {
                type.getType().accept(this);
            }
        }
    }

    @Override
    public void visit(ObjectType type) {
        if ((current != null) && (type.getTypeArguments() != null)) {
            if (current.isObjectTypeArgument() || current.isInnerObjectTypeArgument()) {
                current = ((ObjectType) current).getTypeArguments();
                type.getTypeArguments().accept(this);
            }
        }
    }

    @Override
    public void visit(InnerObjectType type) {
        if ((current != null) && (type.getTypeArguments() != null) && current.isInnerObjectTypeArgument()) {
            current = ((InnerObjectType)current).getTypeArguments();
            type.getTypeArguments().accept(this);
        }
    }

    @Override public void visit(DiamondTypeArgument argument) {}
    @Override public void visit(WildcardTypeArgument type) {}
    @Override public void visit(PrimitiveType type) {}
}
