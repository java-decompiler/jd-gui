/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.classfile.attribute;

public class AttributeInnerClasses implements Attribute {
    protected InnerClass[] classes;

    public AttributeInnerClasses(InnerClass[] classes) {
        this.classes = classes;
    }

    public InnerClass[] getInnerClasses() {
        return classes;
    }
}
