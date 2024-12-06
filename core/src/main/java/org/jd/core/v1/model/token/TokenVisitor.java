/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.token;

public interface TokenVisitor {
    void visit(BooleanConstantToken token);
    void visit(CharacterConstantToken token);
    void visit(DeclarationToken token);
    void visit(EndBlockToken token);
    void visit(EndMarkerToken token);
    void visit(KeywordToken token);
    void visit(LineNumberToken token);
    void visit(NewLineToken token);
    void visit(NumericConstantToken token);
    void visit(ReferenceToken token);
    void visit(StartBlockToken token);
    void visit(StartMarkerToken token);
    void visit(StringConstantToken token);
    void visit(TextToken token);
}
