/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.classfile.constant;

public class ConstantLong extends ConstantValue {
    protected long value;

    public ConstantLong(long value) {
        super(CONSTANT_Long);
        this.value = value;
    }

    public long getValue() {
        return value;
    }
}
