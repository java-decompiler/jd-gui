/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.declaration;

import org.jd.core.v1.model.javasyntax.type.Type;
import org.jd.core.v1.util.DefaultList;

public class ArrayVariableInitializer extends DefaultList<VariableInitializer> implements VariableInitializer {
    protected Type type;

    public ArrayVariableInitializer(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public int getLineNumber() {
        return isEmpty() ? 0 : get(0).getLineNumber();
    }

    @Override
    public void accept(DeclarationVisitor visitor) {
        visitor.visit(this);
    }
}
