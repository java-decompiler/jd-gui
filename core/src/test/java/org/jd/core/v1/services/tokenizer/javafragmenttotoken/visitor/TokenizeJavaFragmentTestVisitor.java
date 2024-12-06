/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.services.tokenizer.javafragmenttotoken.visitor;

import org.jd.core.v1.model.fragment.FlexibleFragment;
import org.jd.core.v1.model.javafragment.*;
import org.jd.core.v1.model.token.TextToken;
import org.jd.core.v1.service.tokenizer.javafragmenttotoken.visitor.TokenizeJavaFragmentVisitor;

public class TokenizeJavaFragmentTestVisitor extends TokenizeJavaFragmentVisitor {
    protected static final TextToken SUFFIX = new TextToken("-->");

    protected int size;
    protected StringBuilder sb = new StringBuilder(200);

    public TokenizeJavaFragmentTestVisitor(int initialCapacity) {
        super(initialCapacity);
    }

    @Override
    public void visit(EndBlockFragment fragment) {
        addPrefix(fragment);
        super.visit(fragment);
        addSuffix();
    }

    @Override
    public void visit(EndBodyInParameterFragment fragment) {
        addPrefix(fragment);
        super.visit(fragment);
        addSuffix();
    }

    @Override
    public void visit(SpaceSpacerFragment fragment) {
        addPrefix(fragment);
        super.visit(fragment);
        addSuffix();
    }

    @Override
    public void visit(EndBodyFragment fragment) {
        addPrefix(fragment);
        super.visit(fragment);
        addSuffix();
    }

    @Override
    public void visit(EndSingleStatementBlockFragment fragment) {
        addPrefix(fragment);
        super.visit(fragment);
        addSuffix();
    }

    @Override
    public void visit(EndStatementsBlockFragment fragment) {
        addPrefix(fragment);
        super.visit(fragment);
        addSuffix();
    }

    @Override
    public void visit(ImportsFragment fragment) {
        addPrefix(fragment);
        super.visit(fragment);
        addSuffix();
    }

    @Override
    public void visit(SpacerBetweenMembersFragment fragment) {
        addPrefix(fragment);
        super.visit(fragment);
        addSuffix();
    }

    @Override
    public void visit(SpacerFragment fragment) {
        addPrefix(fragment);
        super.visit(fragment);
        addSuffix();
    }

    @Override
    public void visit(StartBodyFragment fragment) {
        addPrefix(fragment);
        super.visit(fragment);
        addSuffix();
    }

    @Override
    public void visit(StartSingleStatementBlockFragment fragment) {
        addPrefix(fragment);
        super.visit(fragment);
        addSuffix();
    }

    @Override
    public void visit(StartBlockFragment fragment) {
        addPrefix(fragment);
        super.visit(fragment);
        addSuffix();
    }

    @Override
    public void visit(StartStatementsBlockFragment fragment) {
        addPrefix(fragment);
        super.visit(fragment);
        addSuffix();
    }

    @Override
    public void visit(StartStatementsDoWhileBlockFragment fragment) {
        addPrefix(fragment);
        super.visit(fragment);
        addSuffix();
    }

    @Override
    public void visit(StartStatementsInfiniteForBlockFragment fragment) {
        addPrefix(fragment);
        super.visit(fragment);
        addSuffix();
    }

    @Override
    public void visit(StartStatementsInfiniteWhileBlockFragment fragment) {
        addPrefix(fragment);
        super.visit(fragment);
        addSuffix();
    }

    @Override
    public void visit(StartStatementsTryBlockFragment fragment) {
        addPrefix(fragment);
        super.visit(fragment);
        addSuffix();
    }

    protected void addPrefix(FlexibleFragment fragment) {
        sb.setLength(0);
        sb.append("<-- ");
        sb.append(fragment.getMinimalLineCount());
        sb.append(',');
        sb.append(fragment.getInitialLineCount());
        sb.append(',');
        sb.append(fragment.getMaximalLineCount());
        sb.append(':');
        sb.append(fragment.getWeight());

        if (fragment.getLabel() != null) {
            sb.append(" '");
            sb.append(fragment.getLabel());
            sb.append('\'');
        }

        sb.append(' ');
        tokens.add(new TextToken(sb.toString()));
        size = tokens.size();
    }

    protected void addSuffix() {
        if (size < tokens.size()) {
            tokens.add(TextToken.SPACE);
        }
        tokens.add(SUFFIX);
    }
}
