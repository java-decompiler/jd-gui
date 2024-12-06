/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.token;

import org.jd.core.v1.api.printer.Printer;

public class DeclarationToken implements Token {
    // Declaration & reference types
    public static final int TYPE = Printer.TYPE;
    public static final int FIELD = Printer.FIELD;
    public static final int METHOD = Printer.METHOD;
    public static final int CONSTRUCTOR = Printer.CONSTRUCTOR;
    public static final int PACKAGE = Printer.PACKAGE;
    public static final int MODULE = Printer.MODULE;

    protected int type;
    protected String internalTypeName;
    protected String name;
    protected String descriptor;

    public DeclarationToken(int type, String internalTypeName, String name, String descriptor) {
        this.type = type;
        this.internalTypeName = internalTypeName;
        this.name = name;
        this.descriptor = descriptor;
    }

    public int getType() {
        return type;
    }

    public String getInternalTypeName() {
        return internalTypeName;
    }

    public String getName() {
        return name;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public String toString() {
        return "DeclarationToken{declaration='" + name + "'}";
    }

    @Override
    public void accept(TokenVisitor visitor) {
        visitor.visit(this);
    }
}
