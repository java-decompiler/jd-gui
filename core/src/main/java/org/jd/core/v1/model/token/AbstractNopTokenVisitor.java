/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.token;

public abstract class AbstractNopTokenVisitor implements TokenVisitor {
    @Override public void visit(BooleanConstantToken token) {}
    @Override public void visit(CharacterConstantToken token) {}
    @Override public void visit(DeclarationToken token) {}
    @Override public void visit(EndBlockToken token) {}
    @Override public void visit(EndMarkerToken token) {}
    @Override public void visit(KeywordToken token) {}
    @Override public void visit(LineNumberToken token) {}
    @Override public void visit(NewLineToken token) {}
    @Override public void visit(NumericConstantToken token) {}
    @Override public void visit(ReferenceToken token) {}
    @Override public void visit(StartBlockToken token) {}
    @Override public void visit(StartMarkerToken token) {}
    @Override public void visit(StringConstantToken token) {}
    @Override public void visit(TextToken token) {}
}
