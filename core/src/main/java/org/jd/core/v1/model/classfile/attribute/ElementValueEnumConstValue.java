/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.classfile.attribute;

public class ElementValueEnumConstValue implements ElementValue {
    protected String descriptor;
    protected String constName;

    public ElementValueEnumConstValue(String descriptor, String constName) {
        this.descriptor = descriptor;
        this.constName = constName;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public String getConstName() {
        return constName;
    }

    @Override
    public void accept(ElementValueVisitor visitor) {
        visitor.visit(this);
    }
}
