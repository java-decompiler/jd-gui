/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.reference;

public class ElementValueArrayInitializerElementValue implements ElementValue {
    protected BaseElementValue elementValueArrayInitializer;

    public ElementValueArrayInitializerElementValue() {
        this.elementValueArrayInitializer = null;
    }

    public ElementValueArrayInitializerElementValue(BaseElementValue elementValueArrayInitializer) {
        this.elementValueArrayInitializer = elementValueArrayInitializer;
    }

    public BaseElementValue getElementValueArrayInitializer() {
        return elementValueArrayInitializer;
    }

    @Override
    public void accept(ReferenceVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "ElementValueArrayInitializerElementValue{" + elementValueArrayInitializer + "}";
    }
}
