/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.classfile.attribute;

public class InnerClass {
    protected String innerTypeName;
    protected String outerTypeName;
    protected String innerName;
    protected int innerAccessFlags;

    public InnerClass(String innerTypeName, String outerTypeName, String innerName, int innerAccessFlags) {
        this.innerTypeName = innerTypeName;
        this.outerTypeName = outerTypeName;
        this.innerName = innerName;
        this.innerAccessFlags = innerAccessFlags;
    }

    public String getInnerTypeName() {
        return innerTypeName;
    }

    public String getOuterTypeName() {
        return outerTypeName;
    }

    public String getInnerName() {
        return innerName;
    }

    public int getInnerAccessFlags() {
        return innerAccessFlags;
    }
}
