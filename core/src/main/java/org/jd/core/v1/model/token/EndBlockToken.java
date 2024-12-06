/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.token;

public class EndBlockToken implements Token {

    public static final EndBlockToken END_BLOCK = new EndBlockToken("}");
    public static final EndBlockToken END_ARRAY_BLOCK = new EndBlockToken("]");
    public static final EndBlockToken END_ARRAY_INITIALIZER_BLOCK = new EndBlockToken("}");
    public static final EndBlockToken END_PARAMETERS_BLOCK = new EndBlockToken(")");
    public static final EndBlockToken END_RESOURCES_BLOCK = new EndBlockToken(")");
    public static final EndBlockToken END_DECLARATION_OR_STATEMENT_BLOCK = new EndBlockToken("");

    protected String text;

    public EndBlockToken(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String toString() {
        return "EndBlockToken{'" + text + "'}";
    }

    @Override
    public void accept(TokenVisitor visitor) {
        visitor.visit(this);
    }
}
