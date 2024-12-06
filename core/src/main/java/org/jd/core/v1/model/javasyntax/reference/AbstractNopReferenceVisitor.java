/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.reference;

public abstract class AbstractNopReferenceVisitor implements ReferenceVisitor {
    @Override public void visit(AnnotationReference reference) {}
    @Override public void visit(AnnotationReferences references) {}
    @Override public void visit(ElementValues references) {}
    @Override public void visit(ElementValuePair reference) {}
    @Override public void visit(ElementValuePairs references) {}
    @Override public void visit(InnerObjectReference reference) {}
    @Override public void visit(ObjectReference reference) {}
}
