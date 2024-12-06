/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.visitor;

import org.jd.core.v1.model.javasyntax.type.*;

import static org.jd.core.v1.model.javasyntax.type.ObjectType.TYPE_OBJECT;

public class EraseTypeArgumentVisitor implements TypeVisitor {
    protected BaseType result;

    public void init() {
        this.result = null;
    }

    public BaseType getBaseType() {
        return result;
    }

    @Override public void visit(PrimitiveType type) { result = type; }
    @Override public void visit(ObjectType type) { result = type.createType(null); }
    @Override public void visit(GenericType type) { result = TYPE_OBJECT; }

    @Override public void visit(InnerObjectType type) {
        Type t = type.getOuterType();

        t.accept(this);

        if (result == t) {
            result = type.createType(null);
        } else {
            result = new InnerObjectType(type.getInternalName(), type.getQualifiedName(), type.getName(), null, type.getDimension(), (ObjectType)result);
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
