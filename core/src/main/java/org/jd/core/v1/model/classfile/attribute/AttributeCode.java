/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.classfile.attribute;

import java.util.Map;

public class AttributeCode implements Attribute {
    protected int maxStack;
    protected int maxLocals;
    protected byte[] code;
    protected CodeException[] exceptionTable;
    protected Map<String, Attribute> attributes;

    public AttributeCode(int maxStack, int maxLocals, byte[] code, CodeException[] exceptionTable, Map<String, Attribute> attributes) {
        this.maxStack = maxStack;
        this.maxLocals = maxLocals;
        this.code = code;
        this.exceptionTable = exceptionTable;
        this.attributes = attributes;
    }

    public int getMaxStack() {
        return maxStack;
    }

    public int getMaxLocals() {
        return maxLocals;
    }

    public byte[] getCode() {
        return code;
    }

    public CodeException[] getExceptionTable() {
        return exceptionTable;
    }

    @SuppressWarnings("unchecked")
    public <T extends Attribute> T getAttribute(String name) {
        return (attributes == null) ? null : (T)attributes.get(name);
    }
}
