/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.visitor;

import org.jd.core.v1.model.javasyntax.type.AbstractNopTypeArgumentVisitor;
import org.jd.core.v1.model.javasyntax.type.GenericType;
import org.jd.core.v1.model.javasyntax.type.InnerObjectType;
import org.jd.core.v1.model.javasyntax.type.ObjectType;

import java.util.HashSet;

public class PopulateBlackListNamesVisitor extends AbstractNopTypeArgumentVisitor {
    protected HashSet<String> blackListNames;

    public PopulateBlackListNamesVisitor(HashSet<String> blackListNames) {
        this.blackListNames = blackListNames;
    }

    @Override
    public void visit(ObjectType type) {
        blackListNames.add(type.getName());
    }

    @Override
    public void visit(InnerObjectType type) {
        blackListNames.add(type.getName());
    }

    @Override
    public void visit(GenericType type) {
        blackListNames.add(type.getName());
    }
}
