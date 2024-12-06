/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.token;

public class NumericConstantToken implements Token {

    protected String text;

    public NumericConstantToken(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String toString() {
        return "NumericConstantToken{'" + text + "'}";
    }

    @Override
    public void accept(TokenVisitor visitor) {
        visitor.visit(this);
    }
}
