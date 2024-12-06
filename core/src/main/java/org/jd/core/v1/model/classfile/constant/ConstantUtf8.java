/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.classfile.constant;

public class ConstantUtf8 extends ConstantValue {
    protected String value;

    public ConstantUtf8(String value) {
        super(CONSTANT_Utf8);
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
