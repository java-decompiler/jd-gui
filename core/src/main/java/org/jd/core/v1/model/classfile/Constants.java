/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.classfile;

public interface Constants {
    // Access flags for Class, Field, Method, Nested class, Module, Module Requires, Module Exports, Module Opens
    int ACC_PUBLIC       = 0x0001; // C  F  M  N  .  .  .  .
    int ACC_PRIVATE      = 0x0002; // .  F  M  N  .  .  .  .
    int ACC_PROTECTED    = 0x0004; // .  F  M  N  .  .  .  .
    int ACC_STATIC       = 0x0008; // C  F  M  N  .  .  .  .
    int ACC_FINAL        = 0x0010; // C  F  M  N  .  .  .  .
    int ACC_SYNCHRONIZED = 0x0020; // .  .  M  .  .  .  .  .
    int ACC_SUPER        = 0x0020; // C  .  .  .  .  .  .  .
    int ACC_OPEN         = 0x0020; // .  .  .  .  Mo .  .  .
    int ACC_TRANSITIVE   = 0x0020; // .  .  .  .  .  MR .  .
    int ACC_VOLATILE     = 0x0040; // .  F  .  .  .  .  .  .
    int ACC_BRIDGE       = 0x0040; // .  .  M  .  .  .  .  .
    int ACC_STATIC_PHASE = 0x0040; // .  .  .  .  .  MR .  .
    int ACC_TRANSIENT    = 0x0080; // .  F  .  .  .  .  .  .
    int ACC_VARARGS      = 0x0080; // .  .  M  .  .  .  .  .
    int ACC_NATIVE       = 0x0100; // .  .  M  .  .  .  .  .
    int ACC_INTERFACE    = 0x0200; // C  .  .  N  .  .  .  .
    int ACC_ABSTRACT     = 0x0400; // C  .  M  N  .  .  .  .
    int ACC_STRICT       = 0x0800; // .  .  M  .  .  .  .  .
    int ACC_SYNTHETIC    = 0x1000; // C  F  M  N  Mo MR ME MO
    int ACC_ANNOTATION   = 0x2000; // C  .  .  N  .  .  .  .
    int ACC_ENUM         = 0x4000; // C  F  .  N  .  .  .  .
    int ACC_MODULE       = 0x8000; // C  .  .  .  .  .  .  .
    int ACC_MANDATED     = 0x8000; // .  .  .  .  Mo MR ME MO
}
