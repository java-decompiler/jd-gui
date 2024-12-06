/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.token;

public class StartBlockToken implements Token {

    public static final StartBlockToken START_BLOCK = new StartBlockToken("{");
    public static final StartBlockToken START_ARRAY_BLOCK = new StartBlockToken("[");
    public static final StartBlockToken START_ARRAY_INITIALIZER_BLOCK = new StartBlockToken("{");
    public static final StartBlockToken START_PARAMETERS_BLOCK = new StartBlockToken("(");
    public static final StartBlockToken START_RESOURCES_BLOCK = new StartBlockToken("(");
    public static final StartBlockToken START_DECLARATION_OR_STATEMENT_BLOCK = new StartBlockToken("");

    protected String text;

    protected StartBlockToken(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String toString() {
        return "StartBlockToken{'" + text + "'}";
    }

    @Override
    public void accept(TokenVisitor visitor) {
        visitor.visit(this);
    }
}
