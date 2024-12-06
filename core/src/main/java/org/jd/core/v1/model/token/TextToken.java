/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.token;

public class TextToken implements Token {

    public static final TextToken AT = new TextToken("@");
    public static final TextToken COMMA = new TextToken(",");
    public static final TextToken COLON = new TextToken(":");
    public static final TextToken COLON_COLON = new TextToken("::");
    public static final TextToken COMMA_SPACE = new TextToken(", ");
    public static final TextToken DIAMOND = new TextToken("<>");
    public static final TextToken DOT = new TextToken(".");
    public static final TextToken DIMENSION_1 = new TextToken("[]");
    public static final TextToken DIMENSION_2 = new TextToken("[][]");
    public static final TextToken INFINITE_FOR = new TextToken(" (;;)");
    public static final TextToken LEFTRIGHTCURLYBRACKETS = new TextToken("{}");
    public static final TextToken LEFTROUNDBRACKET = new TextToken("(");
    public static final TextToken RIGHTROUNDBRACKET = new TextToken(")");
    public static final TextToken LEFTRIGHTROUNDBRACKETS = new TextToken("()");
    public static final TextToken LEFTANGLEBRACKET = new TextToken("<");
    public static final TextToken RIGHTANGLEBRACKET = new TextToken(">");
    public static final TextToken QUESTIONMARK = new TextToken("?");
    public static final TextToken QUESTIONMARK_SPACE = new TextToken("? ");
    public static final TextToken SPACE = new TextToken(" ");
    public static final TextToken SPACE_AND_SPACE = new TextToken(" & ");
    public static final TextToken SPACE_ARROW_SPACE = new TextToken(" -> ");
    public static final TextToken SPACE_COLON_SPACE = new TextToken(" : ");
    public static final TextToken SPACE_EQUAL_SPACE = new TextToken(" = ");
    public static final TextToken SPACE_QUESTION_SPACE = new TextToken(" ? ");
    public static final TextToken SPACE_LEFTROUNDBRACKET = new TextToken(" (");
    public static final TextToken SEMICOLON = new TextToken(";");
    public static final TextToken SEMICOLON_SPACE = new TextToken("; ");
    public static final TextToken VARARGS = new TextToken("... ");
    public static final TextToken VERTICALLINE = new TextToken("|");
    public static final TextToken EXCLAMATION = new TextToken("!");

    protected final String text;

    public TextToken(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String toString() {
        return "TextToken{'" + text + "'}";
    }

    @Override
    public void accept(TokenVisitor visitor) {
        visitor.visit(this);
    }
}
