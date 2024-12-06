/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.model.localvariable;

import org.jd.core.v1.model.javasyntax.type.BaseType;
import org.jd.core.v1.model.javasyntax.type.ObjectType;
import org.jd.core.v1.model.javasyntax.type.Type;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.TypeMaker;

import java.util.Map;

import static org.jd.core.v1.model.javasyntax.type.ObjectType.TYPE_OBJECT;
import static org.jd.core.v1.model.javasyntax.type.ObjectType.TYPE_UNDEFINED_OBJECT;

public class ObjectLocalVariable extends AbstractLocalVariable {
    protected TypeMaker typeMaker;
    protected Type type;

    public ObjectLocalVariable(TypeMaker typeMaker, int index, int offset, Type type, String name) {
        super(index, offset, name);
        this.typeMaker = typeMaker;
        this.type = type;
    }

    public ObjectLocalVariable(TypeMaker typeMaker, int index, int offset, Type type, String name, boolean declared) {
        this(typeMaker, index, offset, type, name);
        this.declared = declared;
    }

    public ObjectLocalVariable(TypeMaker typeMaker, int index, int offset, ObjectLocalVariable objectLocalVariable) {
        super(index, offset, null);
        this.typeMaker = typeMaker;
        this.type = objectLocalVariable.type;
    }

    @Override
    public Type getType() {
        return type;
    }

    public void setType(Map<String, BaseType> typeBounds, Type type) {
        if (!this.type.equals(type)) {
            this.type = type;
            fireChangeEvent(typeBounds);
        }
    }

    @Override
    public int getDimension() {
        return type.getDimension();
    }

    @Override
    public void accept(LocalVariableVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("ObjectLocalVariable{");
        
        if (type.getName() == null) {
            sb.append(type.getInternalName());
        } else {
            sb.append(type.getName());
        }

        if (type.getDimension() > 0) {
            sb.append(new String(new char[type.getDimension()]).replaceAll("\0", "[]"));
        }

        sb.append(' ').append(name).append(", index=").append(index);

        if (next != null) {
            sb.append(", next=").append(next);
        }

        return sb.append("}").toString();
    }

    @Override
    public boolean isAssignableFrom(Map<String, BaseType> typeBounds, Type type) {
        if (this.type.isObjectType()) {
            if (this.type.equals(TYPE_OBJECT)) {
                if ((type.getDimension() > 0) || !type.isPrimitiveType()) {
                    return true;
                }
            }

            if (type.isObjectType()) {
                return typeMaker.isAssignable(typeBounds, (ObjectType) this.type, (ObjectType) type);
            }
        }

        return false;
    }

    @Override
    public void typeOnRight(Map<String, BaseType> typeBounds, Type type) {
        if (type != TYPE_UNDEFINED_OBJECT) {
            if (this.type == TYPE_UNDEFINED_OBJECT) {
                this.type = type;
                fireChangeEvent(typeBounds);
            } else if ((this.type.getDimension() == 0) && (type.getDimension() == 0)) {
                assert !this.type.isPrimitiveType() && !type.isPrimitiveType() : "ObjectLocalVariable.typeOnRight(type) : unexpected type";

                if (this.type.isObjectType()) {
                    ObjectType thisObjectType = (ObjectType) this.type;

                    if (type.isObjectType()) {
                        ObjectType otherObjectType = (ObjectType) type;

                        if (thisObjectType.getInternalName().equals(otherObjectType.getInternalName())) {
                            if ((thisObjectType.getTypeArguments() == null) && (otherObjectType.getTypeArguments() != null)) {
                                // Keep type, update type arguments
                                this.type = otherObjectType;
                                fireChangeEvent(typeBounds);
                            }
                        } else if (typeMaker.isAssignable(typeBounds, thisObjectType, otherObjectType)) {
                            // Assignable types
                            if ((thisObjectType.getTypeArguments() == null) && (otherObjectType.getTypeArguments() != null)) {
                                // Keep type, update type arguments
                                this.type = thisObjectType.createType(otherObjectType.getTypeArguments());
                                fireChangeEvent(typeBounds);
                            }
                        }
                    }
                } else if (this.type.isGenericType()) {
                    if (type.isGenericType()) {
                        this.type = type;
                        fireChangeEvent(typeBounds);
                    }
                }
            }
        }
    }

    @Override
    public void typeOnLeft(Map<String, BaseType> typeBounds, Type type) {
        if ((type != TYPE_UNDEFINED_OBJECT) && !type.equals(TYPE_OBJECT)) {
            if (this.type == TYPE_UNDEFINED_OBJECT) {
                this.type = type;
                fireChangeEvent(typeBounds);
            } else if ((this.type.getDimension() == 0) && (type.getDimension() == 0) && this.type.isObjectType() && type.isObjectType()) {
                ObjectType thisObjectType = (ObjectType) this.type;
                ObjectType otherObjectType = (ObjectType) type;

                if (thisObjectType.getInternalName().equals(otherObjectType.getInternalName())) {
                    if ((thisObjectType.getTypeArguments() == null) && (otherObjectType.getTypeArguments() != null)) {
                        // Keep type, update type arguments
                        this.type = otherObjectType;
                        fireChangeEvent(typeBounds);
                    }
                } else if (typeMaker.isAssignable(typeBounds, otherObjectType, thisObjectType)) {
                    // Assignable types
                    if ((thisObjectType.getTypeArguments() == null) && (otherObjectType.getTypeArguments() != null)) {
                        // Keep type, update type arguments
                        this.type = thisObjectType.createType(otherObjectType.getTypeArguments());
                        fireChangeEvent(typeBounds);
                    }
                }
            }
        }
    }

    @Override
    public boolean isAssignableFrom(Map<String, BaseType> typeBounds, AbstractLocalVariable variable) {
        return isAssignableFrom(typeBounds, variable.getType());
    }

    @Override
    public void variableOnRight(Map<String, BaseType> typeBounds, AbstractLocalVariable variable) {
        addVariableOnRight(variable);
        typeOnRight(typeBounds, variable.getType());
    }

    @Override
    public void variableOnLeft(Map<String, BaseType> typeBounds, AbstractLocalVariable variable) {
        addVariableOnLeft(variable);
        typeOnLeft(typeBounds, variable.getType());
    }
}
