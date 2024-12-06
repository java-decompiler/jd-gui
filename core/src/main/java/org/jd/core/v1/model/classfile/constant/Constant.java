/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.classfile.constant;

public abstract class Constant {
    public static final byte CONSTANT_Unknown            = 0;
    public static final byte CONSTANT_Utf8               = 1;
    public static final byte CONSTANT_Integer            = 3;
    public static final byte CONSTANT_Float              = 4;
    public static final byte CONSTANT_Long               = 5;
    public static final byte CONSTANT_Double             = 6;
    public static final byte CONSTANT_Class              = 7;
    public static final byte CONSTANT_String             = 8;
    public static final byte CONSTANT_FieldRef           = 9;
    public static final byte CONSTANT_MethodRef          = 10;
    public static final byte CONSTANT_InterfaceMethodRef = 11;
    public static final byte CONSTANT_NameAndType        = 12;
    public static final byte CONSTANT_MethodHandle       = 15;
    public static final byte CONSTANT_MethodType         = 16;
    public static final byte CONSTANT_InvokeDynamic      = 18;
    public static final byte CONSTANT_MemberRef          = 19; // Unofficial constant

    protected byte tag;

    public Constant(byte tag) {
        this.tag = tag;
    }

    public byte getTag() {
        return tag;
    }
}
