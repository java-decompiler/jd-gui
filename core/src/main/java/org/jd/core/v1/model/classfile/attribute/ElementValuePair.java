/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.classfile.attribute;

public class ElementValuePair {
	protected String elementName;
    protected ElementValue elementValue;

    public ElementValuePair(String elementName, ElementValue elementValue) {
        this.elementName = elementName;
        this.elementValue = elementValue;
    }

    public String getElementName() {
        return elementName;
    }

    @SuppressWarnings("unchecked")
    public <T extends ElementValue> T getElementValue() {
        return (T)elementValue;
    }
}
