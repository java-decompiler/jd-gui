/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.reference;

public class AnnotationElementValue extends AnnotationReference implements ElementValue {

    public AnnotationElementValue(AnnotationReference annotationReference) {
        super(annotationReference.getType(),
              annotationReference.getElementValue(),
              annotationReference.getElementValuePairs());
    }

    @Override
    public void accept(ReferenceVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "AnnotationElementValue{type=" + type + ", elementValue=" + elementValue + ", elementValuePairs=" + elementValuePairs + "}";
    }
}
