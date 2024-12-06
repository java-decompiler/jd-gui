/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.declaration;

public interface Declaration {
    // Access flags for Class, Field, Method, Nested class, Module, Module Requires, Module Exports, Module Opens
    int FLAG_PUBLIC       = 0x0001;  // C  F  M  N  .  .  .  .
    int FLAG_PRIVATE      = 0x0002;  // .  F  M  N  .  .  .  .
    int FLAG_PROTECTED    = 0x0004;  // .  F  M  N  .  .  .  .
    int FLAG_STATIC       = 0x0008;  // C  F  M  N  .  .  .  .
    int FLAG_FINAL        = 0x0010;  // C  F  M  N  .  .  .  .
    int FLAG_SYNCHRONIZED = 0x0020;  // .  .  M  .  .  .  .  .
    int FLAG_SUPER        = 0x0020;  // C  .  .  .  .  .  .  .
    int FLAG_OPEN         = 0x0020;  // .  .  .  .  Mo .  .  .
    int FLAG_TRANSITIVE   = 0x0020;  // .  .  .  .  .  MR .  .
    int FLAG_VOLATILE     = 0x0040;  // .  F  .  .  .  .  .  .
    int FLAG_BRIDGE       = 0x0040;  // .  .  M  .  .  .  .  .
    int FLAG_STATIC_PHASE = 0x0040;  // .  .  .  .  .  MR .  .
    int FLAG_TRANSIENT    = 0x0080;  // .  F  .  .  .  .  .  .
    int FLAG_VARARGS      = 0x0080;  // .  .  M  .  .  .  .  .
    int FLAG_NATIVE       = 0x0100;  // .  .  M  .  .  .  .  .
    int FLAG_INTERFACE    = 0x0200;  // C  .  .  N  .  .  .  .
    int FLAG_ANONYMOUS    = 0x0200;  // .  .  M  .  .  .  .  . // Custom flag
    int FLAG_ABSTRACT     = 0x0400;  // C  .  M  N  .  .  .  .
    int FLAG_STRICT       = 0x0800;  // .  .  M  .  .  .  .  .
    int FLAG_SYNTHETIC    = 0x1000;  // C  F  M  N  Mo MR ME MO
    int FLAG_ANNOTATION   = 0x2000;  // C  .  .  N  .  .  .  .
    int FLAG_ENUM         = 0x4000;  // C  F  .  N  .  .  .  .
    int FLAG_MODULE       = 0x8000;  // C  .  .  .  .  .  .  .
    int FLAG_MANDATED     = 0x8000;  // .  .  .  .  Mo MR ME MO

    // Extension
    int FLAG_DEFAULT      = 0x10000; // .  .  M  .  .  .  .  .

    void accept(DeclarationVisitor visitor);
}
