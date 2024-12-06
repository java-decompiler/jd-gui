/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.classfile.attribute;

public class LocalVariableType {
    protected int startPc;
    protected int length;
    protected String name;
    protected String signature;
    protected int index;

    public LocalVariableType(int startPc, int length, String name, String signature, int index) {
        this.startPc = startPc;
        this.length = length;
        this.name = name;
        this.signature = signature;
        this.index = index;
    }

    public int getStartPc() {
        return startPc;
    }

    public int getLength() {
        return length;
    }

    public String getName() {
        return name;
    }

    public String getSignature() {
        return signature;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("LocalVariableType{index=").append(index);
        sb.append(", name=").append(name);
        sb.append(", signature=").append(signature);
        sb.append(", index=").append(index);
        sb.append(", startPc=").append(startPc);
        sb.append(", length=").append(length);

        return sb.append("}").toString();
    }
}
