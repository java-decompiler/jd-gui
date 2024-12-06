/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.reference;

public interface ReferenceVisitor {
    void visit(AnnotationElementValue reference);
    void visit(AnnotationReference reference);
    void visit(AnnotationReferences references);
    void visit(ElementValueArrayInitializerElementValue reference);
    void visit(ElementValues references);
    void visit(ElementValuePair reference);
    void visit(ElementValuePairs references);
    void visit(ExpressionElementValue reference);
    void visit(InnerObjectReference reference);
    void visit(ObjectReference reference);
}
