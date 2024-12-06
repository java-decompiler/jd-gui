/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.type;

import org.jd.core.v1.util.DefaultList;

import java.util.*;

public class TypeArguments extends DefaultList<TypeArgument> implements BaseTypeArgument {
    public TypeArguments() {}

    public TypeArguments(int capacity) {
        super(capacity);
    }

    public TypeArguments(Collection<TypeArgument> list) {
        super(list);
    }

    @Override
    public boolean isList() {
        return true;
    }

    @Override
    public boolean isTypeArgumentAssignableFrom(Map<String, BaseType> typeBounds, BaseTypeArgument typeArgument) {
        if (typeArgument.getClass() != TypeArguments.class) {
            return false;
        }

        TypeArguments ata = (TypeArguments)typeArgument;

        if (size() != ata.size()) {
            return false;
        }

        Iterator<TypeArgument> iterator1 = iterator();
        Iterator<TypeArgument> iterator2 = ata.iterator();

        while (iterator1.hasNext()) {
            if (!iterator1.next().isTypeArgumentAssignableFrom(typeBounds, iterator2.next())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean isTypeArgumentList() {
        return true;
    }

    @Override
    public TypeArgument getTypeArgumentFirst() {
        return getFirst();
    }

    @Override
    public DefaultList<TypeArgument> getTypeArgumentList() {
        return this;
    }

    @Override
    public int typeArgumentSize() {
        return size();
    }

    @Override
    public void accept(TypeArgumentVisitor visitor) {
        visitor.visit(this);
    }
}
