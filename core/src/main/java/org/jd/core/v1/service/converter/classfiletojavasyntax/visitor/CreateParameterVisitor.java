/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.visitor;

import org.jd.core.v1.model.javasyntax.type.*;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.localvariable.AbstractLocalVariable;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.localvariable.GenericLocalVariable;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.localvariable.ObjectLocalVariable;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.localvariable.PrimitiveLocalVariable;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.TypeMaker;

public class CreateParameterVisitor extends AbstractNopTypeArgumentVisitor {
    protected TypeMaker typeMaker;
    protected int index;
    protected String name;

    protected AbstractLocalVariable localVariable;

    public CreateParameterVisitor(TypeMaker typeMaker) {
        this.typeMaker = typeMaker;
    }

    public void init(int index, String name) {
        this.index = index;
        this.name = name;
    }

    public AbstractLocalVariable getLocalVariable() {
        return localVariable;
    }

    @Override
    public void visit(PrimitiveType type) {
        if (type.getDimension() == 0) {
            localVariable = new PrimitiveLocalVariable(index, 0, type, name);
        } else {
            localVariable = new ObjectLocalVariable(typeMaker, index, 0, type, name);
        }
    }

    @Override
    public void visit(ObjectType type) {
        localVariable = new ObjectLocalVariable(typeMaker, index, 0, type, name);
    }

    @Override
    public void visit(InnerObjectType type) {
        localVariable = new ObjectLocalVariable(typeMaker, index, 0, type, name);
    }

    @Override
    public void visit(GenericType type) {
        localVariable = new GenericLocalVariable(index, 0, type, name);
    }
}
