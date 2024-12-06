/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.reference;

import org.jd.core.v1.model.javasyntax.type.ObjectType;

public class AnnotationReference implements BaseAnnotationReference {
    protected ObjectType type;
    protected ElementValue elementValue;
    protected BaseElementValuePair elementValuePairs;

    public AnnotationReference(ObjectType type) {
        this.type = type;
    }

    public AnnotationReference(ObjectType type, ElementValue elementValue) {
        this.type = type;
        this.elementValue = elementValue;
    }

    public AnnotationReference(ObjectType type, BaseElementValuePair elementValuePairs) {
        this.type = type;
        this.elementValuePairs = elementValuePairs;
    }

    protected AnnotationReference(ObjectType type, ElementValue elementValue, BaseElementValuePair elementValuePairs) {
        this.type = type;
        this.elementValue = elementValue;
        this.elementValuePairs = elementValuePairs;
    }

    public ObjectType getType() {
        return type;
    }

    public ElementValue getElementValue() {
        return elementValue;
    }

    public BaseElementValuePair getElementValuePairs() {
        return elementValuePairs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AnnotationReference)) return false;

        AnnotationReference that = (AnnotationReference) o;

        if (elementValue != null ? !elementValue.equals(that.elementValue) : that.elementValue != null) return false;
        if (elementValuePairs != null ? !elementValuePairs.equals(that.elementValuePairs) : that.elementValuePairs != null)
            return false;
        if (!type.equals(that.type)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = 970748295 + type.hashCode();
        result = 31 * result + (elementValue != null ? elementValue.hashCode() : 0);
        result = 31 * result + (elementValuePairs != null ? elementValuePairs.hashCode() : 0);
        return result;
    }

    @Override
    public void accept(ReferenceVisitor visitor) {
        visitor.visit(this);
    }
}
