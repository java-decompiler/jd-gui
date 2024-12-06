/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javafragment;

public interface JavaFragmentVisitor {
    void visit(EndBodyFragment fragment);
    void visit(EndBlockInParameterFragment fragment);
    void visit(EndBlockFragment fragment);
    void visit(EndBodyInParameterFragment fragment);
    void visit(EndMovableJavaBlockFragment fragment);
    void visit(EndSingleStatementBlockFragment fragment);
    void visit(EndStatementsBlockFragment fragment);
    void visit(ImportsFragment fragment);
    void visit(LineNumberTokensFragment fragment);
    void visit(SpacerBetweenMembersFragment fragment);
    void visit(SpacerFragment fragment);
    void visit(SpaceSpacerFragment fragment);
    void visit(StartBlockFragment fragment);
    void visit(StartBodyFragment fragment);
    void visit(StartMovableJavaBlockFragment fragment);
    void visit(StartSingleStatementBlockFragment fragment);
    void visit(StartStatementsBlockFragment fragment);
    void visit(StartStatementsDoWhileBlockFragment fragment);
    void visit(StartStatementsInfiniteForBlockFragment fragment);
    void visit(StartStatementsInfiniteWhileBlockFragment fragment);
    void visit(StartStatementsTryBlockFragment fragment);
    void visit(TokensFragment fragment);
}
