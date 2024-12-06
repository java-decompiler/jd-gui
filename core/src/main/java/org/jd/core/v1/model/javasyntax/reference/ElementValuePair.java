/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.reference;

public class ElementValuePair implements BaseElementValuePair {
    protected String name;
    protected ElementValue elementValue;

    public ElementValuePair(String name, ElementValue elementValue) {
        this.name = name;
        this.elementValue = elementValue;
    }

    public String getName() {
        return name;
    }

    public ElementValue getElementValue() {
        return elementValue;
    }

    @Override
    public void accept(ReferenceVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "ElementValuePair{name=" + name + ", elementValue=" + elementValue + "}";
    }
}
