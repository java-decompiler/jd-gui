/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.visitor;

import org.jd.core.v1.model.javasyntax.type.*;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.localvariable.*;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.TypeMaker;

public class CreateLocalVariableVisitor extends AbstractNopTypeArgumentVisitor implements LocalVariableVisitor {
    protected TypeMaker typeMaker;
    protected int index;
    protected int offset;

    protected AbstractLocalVariable localVariable;

    public CreateLocalVariableVisitor(TypeMaker typeMaker) {
        this.typeMaker = typeMaker;
    }

    public void init(int index, int offset) {
        this.index = index;
        this.offset = offset;
    }

    public AbstractLocalVariable getLocalVariable() {
        return localVariable;
    }

    @Override
    public void visit(PrimitiveType type) {
        if (type.getDimension() == 0) {
            localVariable = new PrimitiveLocalVariable(index, offset, type, null);
        } else {
            localVariable = new ObjectLocalVariable(typeMaker, index, offset, type, null);
        }
    }

    @Override
    public void visit(ObjectType type) {
        localVariable = new ObjectLocalVariable(typeMaker, index, offset, type, null);
    }

    @Override
    public void visit(InnerObjectType type) {
        localVariable = new ObjectLocalVariable(typeMaker, index, offset, type, null);
    }

    @Override
    public void visit(GenericType type) {
        localVariable = new GenericLocalVariable(index, offset, type);
    }

    @Override
    public void visit(GenericLocalVariable lv) {
        localVariable = new GenericLocalVariable(index, offset, lv.getType());
    }

    @Override
    public void visit(ObjectLocalVariable lv) {
        localVariable = new ObjectLocalVariable(typeMaker, index, offset, lv);
    }

    @Override
    public void visit(PrimitiveLocalVariable lv) {
        if (lv.getDimension() == 0) {
            localVariable = new PrimitiveLocalVariable(index, offset, lv);
        } else {
            localVariable = new ObjectLocalVariable(typeMaker, index, offset, lv.getType(), null);
        }
    }
}
