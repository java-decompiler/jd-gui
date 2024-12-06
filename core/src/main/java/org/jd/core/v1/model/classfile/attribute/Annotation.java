/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.classfile.attribute;

public class Annotation {
    protected String descriptor;
    protected ElementValuePair[] elementValuePairs;

    public Annotation(String descriptor, ElementValuePair[] elementValuePairs) {
        this.descriptor = descriptor;
        this.elementValuePairs = elementValuePairs;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public ElementValuePair[] getElementValuePairs() {
        return elementValuePairs;
    }
}
