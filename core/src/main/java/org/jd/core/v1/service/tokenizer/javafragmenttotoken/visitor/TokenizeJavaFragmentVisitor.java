/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.tokenizer.javafragmenttotoken.visitor;

import org.jd.core.v1.api.printer.Printer;
import org.jd.core.v1.model.javafragment.*;
import org.jd.core.v1.model.token.*;
import org.jd.core.v1.util.DefaultList;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class TokenizeJavaFragmentVisitor implements JavaFragmentVisitor {
    protected static final ImportNameComparator NAME_COMPARATOR = new ImportNameComparator();

    protected static final KeywordToken DO = new KeywordToken("do");
    protected static final KeywordToken IMPORT = new KeywordToken("import");
    protected static final KeywordToken FOR = new KeywordToken("for");
    protected static final KeywordToken TRUE = new KeywordToken("true");
    protected static final KeywordToken TRY = new KeywordToken("try");
    protected static final KeywordToken WHILE = new KeywordToken("while");

    protected static final List<Token> DO_TOKENS = Arrays.asList((Token)DO);
    protected static final List<Token> EMPTY_FOR_TOKENS = Arrays.asList(FOR, TextToken.INFINITE_FOR);
    protected static final List<Token> EMPTY_WHILE_TOKENS = Arrays.asList(WHILE, TextToken.SPACE, TextToken.LEFTROUNDBRACKET, TRUE, TextToken.RIGHTROUNDBRACKET);
    protected static final List<Token> TRY_TOKENS = Arrays.asList((Token)TRY);

    protected KnownLineNumberTokenVisitor knownLineNumberTokenVisitor = new KnownLineNumberTokenVisitor();
    protected UnknownLineNumberTokenVisitor unknownLineNumberTokenVisitor = new UnknownLineNumberTokenVisitor();
    protected DefaultList<Token> tokens;

    public TokenizeJavaFragmentVisitor(int initialCapacity) {
        this.tokens = new DefaultList<>(initialCapacity);
    }

    public DefaultList<Token> getTokens() {
        return tokens;
    }

    @Override
    public void visit(EndBlockFragment fragment) {
        switch (fragment.getLineCount()) {
            case 0:
                tokens.add(TextToken.SPACE);
                tokens.add(EndBlockToken.END_BLOCK);
                break;
            case 1:
                tokens.add(NewLineToken.NEWLINE_1);
                tokens.add(EndBlockToken.END_BLOCK);
                break;
            case 2:
                tokens.add(NewLineToken.NEWLINE_2);
                tokens.add(EndBlockToken.END_BLOCK);
                break;
            default:
                tokens.add(new NewLineToken(fragment.getLineCount()));
                tokens.add(EndBlockToken.END_BLOCK);
        }
    }

    @Override
    public void visit(EndBlockInParameterFragment fragment) {
        switch (fragment.getLineCount()) {
            case 0:
                tokens.add(TextToken.SPACE);
                tokens.add(EndBlockToken.END_BLOCK);
                tokens.add(TextToken.COMMA);
                break;
            case 1:
                tokens.add(NewLineToken.NEWLINE_1);
                tokens.add(EndBlockToken.END_BLOCK);
                tokens.add(TextToken.COMMA);
                break;
            case 2:
                tokens.add(NewLineToken.NEWLINE_2);
                tokens.add(EndBlockToken.END_BLOCK);
                tokens.add(TextToken.COMMA);
                break;
            default:
                tokens.add(new NewLineToken(fragment.getLineCount()));
                tokens.add(EndBlockToken.END_BLOCK);
                tokens.add(TextToken.COMMA);
        }
    }

    @Override
    public void visit(EndBodyFragment fragment) {
        switch (fragment.getLineCount()) {
            case 0:
                tokens.add(TextToken.SPACE);
                tokens.add(EndBlockToken.END_BLOCK);
                break;
            case 1:
                if (fragment.getStartBodyFragment().getLineCount() == 0) {
                    tokens.add(TextToken.SPACE);
                    tokens.add(EndBlockToken.END_BLOCK);
                    tokens.add(NewLineToken.NEWLINE_1);
                } else {
                    tokens.add(NewLineToken.NEWLINE_1);
                    tokens.add(EndBlockToken.END_BLOCK);
                }
                break;
            case 2:
                tokens.add(NewLineToken.NEWLINE_1);
                tokens.add(EndBlockToken.END_BLOCK);
                tokens.add(NewLineToken.NEWLINE_1);
                break;
            default:
                tokens.add(new NewLineToken(fragment.getLineCount()-1));
                tokens.add(EndBlockToken.END_BLOCK);
                tokens.add(NewLineToken.NEWLINE_1);
        }
    }

    @Override
    public void visit(EndBodyInParameterFragment fragment) {
        switch (fragment.getLineCount()) {
            case 0:
                tokens.add(TextToken.SPACE);
                tokens.add(EndBlockToken.END_BLOCK);
                tokens.add(TextToken.COMMA);
                break;
            case 1:
                tokens.add(NewLineToken.NEWLINE_1);
                tokens.add(EndBlockToken.END_BLOCK);
                tokens.add(TextToken.COMMA);
                tokens.add(TextToken.SPACE);
                break;
            case 2:
                tokens.add(NewLineToken.NEWLINE_1);
                tokens.add(EndBlockToken.END_BLOCK);
                tokens.add(TextToken.COMMA);
                tokens.add(NewLineToken.NEWLINE_1);
                break;
            default:
                tokens.add(new NewLineToken(fragment.getLineCount()-1));
                tokens.add(EndBlockToken.END_BLOCK);
                tokens.add(TextToken.COMMA);
                tokens.add(NewLineToken.NEWLINE_1);
        }
    }

    @Override public void visit(EndMovableJavaBlockFragment fragment) {}

    @Override
    public void visit(EndSingleStatementBlockFragment fragment) {
        switch (fragment.getLineCount()) {
            case 0:
                switch (fragment.getStartSingleStatementBlockFragment().getLineCount()) {
                    case 0:
                    case 1:
                        tokens.add(TextToken.SPACE);
                        tokens.add(EndBlockToken.END_DECLARATION_OR_STATEMENT_BLOCK);
                        break;
                    default:
                        tokens.add(TextToken.SPACE);
                        tokens.add(EndBlockToken.END_BLOCK);
                        tokens.add(TextToken.SPACE);
                        break;
                }
                break;
            case 1:
                switch (fragment.getStartSingleStatementBlockFragment().getLineCount()) {
                    case 0:
                        tokens.add(EndBlockToken.END_DECLARATION_OR_STATEMENT_BLOCK);
                        tokens.add(NewLineToken.NEWLINE_1);
                        break;
                    default:
                        tokens.add(NewLineToken.NEWLINE_1);
                        tokens.add(EndBlockToken.END_BLOCK);
                        break;
                }
                break;
            case 2:
                switch (fragment.getStartSingleStatementBlockFragment().getLineCount()) {
                    case 0:
                        tokens.add(EndBlockToken.END_DECLARATION_OR_STATEMENT_BLOCK);
                        tokens.add(NewLineToken.NEWLINE_2);
                        break;
                    default:
                        tokens.add(NewLineToken.NEWLINE_1);
                        tokens.add(EndBlockToken.END_BLOCK);
                        tokens.add(NewLineToken.NEWLINE_1);
                        break;
                }
                break;
            default:
                switch (fragment.getStartSingleStatementBlockFragment().getLineCount()) {
                    case 0:
                        tokens.add(EndBlockToken.END_DECLARATION_OR_STATEMENT_BLOCK);
                        tokens.add(new NewLineToken(fragment.getLineCount()));
                        break;
                    default:
                        tokens.add(NewLineToken.NEWLINE_1);
                        tokens.add(EndBlockToken.END_BLOCK);
                        tokens.add(new NewLineToken(fragment.getLineCount()-1));
                        break;
                }
                break;
        }
    }

    @Override
    public void visit(EndStatementsBlockFragment fragment) {
        int minimalLineCount = fragment.getGroup().getMinimalLineCount();

        switch (fragment.getLineCount()) {
            case 0:
                tokens.add(TextToken.SPACE);
                tokens.add(EndBlockToken.END_BLOCK);
                tokens.add(TextToken.SPACE);
                break;
            case 1:
                if (minimalLineCount == 0) {
                    tokens.add(TextToken.SPACE);
                    tokens.add(EndBlockToken.END_BLOCK);
                    tokens.add(NewLineToken.NEWLINE_1);
                } else {
                    tokens.add(NewLineToken.NEWLINE_1);
                    tokens.add(EndBlockToken.END_BLOCK);
                    tokens.add(TextToken.SPACE);
                }
                break;
            case 2:
                switch (minimalLineCount) {
                    case 0:
                        tokens.add(TextToken.SPACE);
                        tokens.add(EndBlockToken.END_BLOCK);
                        tokens.add(NewLineToken.NEWLINE_2);
                        break;
                    case 1:
                        tokens.add(NewLineToken.NEWLINE_1);
                        tokens.add(EndBlockToken.END_BLOCK);
                        tokens.add(NewLineToken.NEWLINE_1);
                        break;
                    default:
                        tokens.add(new NewLineToken(fragment.getLineCount()-1));
                        tokens.add(EndBlockToken.END_BLOCK);
                        tokens.add(NewLineToken.NEWLINE_1);
                }
                break;
            default:
                switch (minimalLineCount) {
                    case 0:
                        tokens.add(TextToken.SPACE);
                        tokens.add(EndBlockToken.END_BLOCK);
                        tokens.add(new NewLineToken(fragment.getLineCount()));
                        break;
                    case 1:
                        tokens.add(new NewLineToken(fragment.getLineCount()));
                        tokens.add(EndBlockToken.END_BLOCK);
                        break;
                    default:
                        tokens.add(new NewLineToken(fragment.getLineCount()-1));
                        tokens.add(EndBlockToken.END_BLOCK);
                        tokens.add(NewLineToken.NEWLINE_1);
                }
                break;
        }
    }

    @Override
    public void visit(ImportsFragment fragment) {
        List<ImportsFragment.Import> imports = new DefaultList<>(fragment.getImports());

        imports.sort(NAME_COMPARATOR);

        tokens.add(StartMarkerToken.IMPORT_STATEMENTS);

        for (ImportsFragment.Import imp : imports) {
            tokens.add(IMPORT);
            tokens.add(TextToken.SPACE);
            tokens.add(new ReferenceToken(ReferenceToken.TYPE, imp.getInternalName(), imp.getQualifiedName(), null, null));
            tokens.add(TextToken.SEMICOLON);
            tokens.add(NewLineToken.NEWLINE_1);
        }

        tokens.add(EndMarkerToken.IMPORT_STATEMENTS);
    }

    @Override
    public void visit(LineNumberTokensFragment fragment) {
        knownLineNumberTokenVisitor.reset(fragment.getFirstLineNumber());

        for (Token token : fragment.getTokens()) {
            token.accept(knownLineNumberTokenVisitor);
        }
    }

    @Override
    public void visit(SpacerBetweenMembersFragment fragment) {
        switch (fragment.getLineCount()) {
            case 0:
                tokens.add(TextToken.SPACE);
                break;
            case 1:
                tokens.add(NewLineToken.NEWLINE_1);
                break;
            case 2:
                tokens.add(NewLineToken.NEWLINE_2);
                break;
            default:
                tokens.add(new NewLineToken(fragment.getLineCount()));
        }
    }

    @Override
    public void visit(SpacerFragment fragment) {
        switch (fragment.getLineCount()) {
            case 0:
                break;
            case 1:
                tokens.add(NewLineToken.NEWLINE_1);
                break;
            case 2:
                tokens.add(NewLineToken.NEWLINE_2);
                break;
            default:
                tokens.add(new NewLineToken(fragment.getLineCount()));
        }
    }

    @Override
    public void visit(SpaceSpacerFragment fragment) {
        switch (fragment.getLineCount()) {
            case 0:
                tokens.add(TextToken.SPACE);
                break;
            case 1:
                tokens.add(NewLineToken.NEWLINE_1);
                break;
            case 2:
                tokens.add(NewLineToken.NEWLINE_2);
                break;
            default:
                tokens.add(new NewLineToken(fragment.getLineCount()));
        }
    }

    @Override
    public void visit(StartBlockFragment fragment) {
        switch (fragment.getLineCount()) {
            case 0:
                tokens.add(StartBlockToken.START_BLOCK);
                tokens.add(TextToken.SPACE);
                break;
            case 1:
                tokens.add(StartBlockToken.START_BLOCK);
                tokens.add(NewLineToken.NEWLINE_1);
                break;
            case 2:
                tokens.add(StartBlockToken.START_BLOCK);
                tokens.add(NewLineToken.NEWLINE_2);
                break;
            default:
                tokens.add(StartBlockToken.START_BLOCK);
                tokens.add(new NewLineToken(fragment.getLineCount()));
                break;
        }
    }

    @Override
    public void visit(StartBodyFragment fragment) {
        switch (fragment.getLineCount()) {
            case 0:
                tokens.add(TextToken.SPACE);
                tokens.add(StartBlockToken.START_BLOCK);
                tokens.add(TextToken.SPACE);
                break;
            case 1:
                tokens.add(TextToken.SPACE);
                tokens.add(StartBlockToken.START_BLOCK);
                tokens.add(NewLineToken.NEWLINE_1);
                break;
            case 2:
                tokens.add(NewLineToken.NEWLINE_1);
                tokens.add(StartBlockToken.START_BLOCK);
                tokens.add(NewLineToken.NEWLINE_1);
                break;
            default:
                tokens.add(NewLineToken.NEWLINE_1);
                tokens.add(StartBlockToken.START_BLOCK);
                tokens.add(new NewLineToken(fragment.getLineCount()-1));
                break;
        }
    }

    @Override
    public void visit(StartMovableJavaBlockFragment fragment) {
    }

    @Override
    public void visit(StartSingleStatementBlockFragment fragment) {
        switch (fragment.getLineCount()) {
            case 0:
                tokens.add(StartBlockToken.START_DECLARATION_OR_STATEMENT_BLOCK);
                tokens.add(TextToken.SPACE);
                break;
            case 1:
                switch (fragment.getEndSingleStatementBlockFragment().getLineCount()) {
                    case 0:
                        tokens.add(StartBlockToken.START_DECLARATION_OR_STATEMENT_BLOCK);
                        tokens.add(NewLineToken.NEWLINE_1);
                        break;
                    default:
                        tokens.add(TextToken.SPACE);
                        tokens.add(StartBlockToken.START_BLOCK);
                        tokens.add(NewLineToken.NEWLINE_1);
                        break;
                }
                break;
            case 2:
                tokens.add(NewLineToken.NEWLINE_1);
                tokens.add(StartBlockToken.START_BLOCK);
                tokens.add(NewLineToken.NEWLINE_1);
                break;
            default:
                tokens.add(NewLineToken.NEWLINE_1);
                tokens.add(StartBlockToken.START_BLOCK);
                tokens.add(new NewLineToken(fragment.getLineCount() - 1));
                break;
        }
    }

    @Override
    public void visit(StartStatementsBlockFragment fragment) {
        int minimalLineCount = fragment.getGroup().getMinimalLineCount();

        switch (fragment.getLineCount()) {
            case 0:
                tokens.add(TextToken.SPACE);
                tokens.add(StartBlockToken.START_BLOCK);
                tokens.add(TextToken.SPACE);
                break;
            case 1:
                if (minimalLineCount == 0) {
                    tokens.add(NewLineToken.NEWLINE_1);
                    tokens.add(StartBlockToken.START_BLOCK);
                    tokens.add(TextToken.SPACE);
                } else {
                    tokens.add(TextToken.SPACE);
                    tokens.add(StartBlockToken.START_BLOCK);
                    tokens.add(NewLineToken.NEWLINE_1);
                }
                break;
            case 2:
                switch (minimalLineCount) {
                    case 0:
                        tokens.add(NewLineToken.NEWLINE_2);
                        tokens.add(StartBlockToken.START_BLOCK);
                        tokens.add(TextToken.SPACE);
                        break;
                    case 1:
                        tokens.add(TextToken.SPACE);
                        tokens.add(StartBlockToken.START_BLOCK);
                        tokens.add(NewLineToken.NEWLINE_2);
                        break;
                    default:
                        tokens.add(NewLineToken.NEWLINE_1);
                        tokens.add(StartBlockToken.START_BLOCK);
                        tokens.add(NewLineToken.NEWLINE_1);
                }
                break;
            default:
                switch (minimalLineCount) {
                    case 0:
                        tokens.add(new NewLineToken(fragment.getLineCount()));
                        tokens.add(StartBlockToken.START_BLOCK);
                        tokens.add(TextToken.SPACE);
                        break;
                    case 1:
                        tokens.add(TextToken.SPACE);
                        tokens.add(StartBlockToken.START_BLOCK);
                        tokens.add(new NewLineToken(fragment.getLineCount()));
                        break;
                    default:
                        tokens.add(NewLineToken.NEWLINE_1);
                        tokens.add(StartBlockToken.START_BLOCK);
                        tokens.add(new NewLineToken(fragment.getLineCount() - 1));
                }
                break;
        }
    }

    @Override
    public void visit(StartStatementsDoWhileBlockFragment fragment) {
        visit(fragment, DO_TOKENS);
    }

    @Override
    public void visit(StartStatementsInfiniteForBlockFragment fragment) {
        visit(fragment, EMPTY_FOR_TOKENS);
    }

    @Override
    public void visit(StartStatementsInfiniteWhileBlockFragment fragment) {
        visit(fragment, EMPTY_WHILE_TOKENS);
    }

    @Override
    public void visit(StartStatementsTryBlockFragment fragment) {
        visit(fragment, TRY_TOKENS);
    }

    @Override
    public void visit(TokensFragment fragment) {
        for (Token token : fragment.getTokens()) {
            token.accept(unknownLineNumberTokenVisitor);
        }
    }

    protected static class ImportNameComparator implements Comparator<ImportsFragment.Import> {
        @Override
        public int compare(ImportsFragment.Import tr1, ImportsFragment.Import tr2) {
            return tr1.getQualifiedName().compareTo(tr2.getQualifiedName());
        }
    }

    protected class KnownLineNumberTokenVisitor extends AbstractNopTokenVisitor {
        public int currentLineNumber;

        public void reset(int firstLineNumber) {
            this.currentLineNumber = firstLineNumber;
        }

        @Override
        public void visit(EndBlockToken token) {
            assert token != EndBlockToken.END_BLOCK : "Unexpected EndBlockToken.END_BLOCK at this step. Uses 'JavaFragmentFactory.addEnd***(fragments)' instead";
            tokens.add(token);
        }

        @Override
        public void visit(LineNumberToken token) {
            int lineNumber = token.getLineNumber();

            if (lineNumber != Printer.UNKNOWN_LINE_NUMBER) {
                if (currentLineNumber != Printer.UNKNOWN_LINE_NUMBER) {
                    switch (lineNumber - currentLineNumber) {
                        case 0:
                            break;
                        case 1:
                            tokens.add(NewLineToken.NEWLINE_1);
                            break;
                        case 2:
                            tokens.add(NewLineToken.NEWLINE_2);
                            break;
                        default:
                            tokens.add(new NewLineToken(lineNumber - currentLineNumber));
                            break;
                    }
                }

                currentLineNumber = token.getLineNumber();
                tokens.add(token);
            }
        }

        @Override
        public void visit(StartBlockToken token) {
            assert token != StartBlockToken.START_BLOCK : "Unexpected StartBlockToken.START_BLOCK at this step. Uses 'JavaFragmentFactory.addStart***(fragments)' instead";
            tokens.add(token);
        }

        @Override public void visit(BooleanConstantToken token) { tokens.add(token); }
        @Override public void visit(CharacterConstantToken token) { tokens.add(token); }
        @Override public void visit(DeclarationToken token) { tokens.add(token); }
        @Override public void visit(EndMarkerToken token) { tokens.add(token); }
        @Override public void visit(KeywordToken token) { tokens.add(token); }
        @Override public void visit(NumericConstantToken token) { tokens.add(token); }
        @Override public void visit(ReferenceToken token) { tokens.add(token); }
        @Override public void visit(StartMarkerToken token) { tokens.add(token); }
        @Override public void visit(StringConstantToken token) { tokens.add(token); }
        @Override public void visit(TextToken token) { tokens.add(token); }
    }

    protected class UnknownLineNumberTokenVisitor implements TokenVisitor {
        @Override
        public void visit(EndBlockToken token) {
            assert token != EndBlockToken.END_BLOCK : "Unexpected EndBlockToken.END_BLOCK at this step. Uses 'JavaFragmentFactory.addEnd***(fragments)' instead";
            tokens.add(token);
        }

        @Override
        public void visit(LineNumberToken token) {
            assert token.getLineNumber() == Printer.UNKNOWN_LINE_NUMBER : "LineNumberToken cannot have a known line number. Uses 'LineNumberTokensFragment' instead";
        }

        @Override
        public void visit(StartBlockToken token) {
            assert token != StartBlockToken.START_BLOCK : "Unexpected StartBlockToken.START_BLOCK at this step. Uses 'JavaFragmentFactory.addStart***(fragments)' instead";
            tokens.add(token);
        }

        @Override public void visit(BooleanConstantToken token) { tokens.add(token); }
        @Override public void visit(CharacterConstantToken token) { tokens.add(token); }
        @Override public void visit(DeclarationToken token) { tokens.add(token); }
        @Override public void visit(EndMarkerToken token) { tokens.add(token); }
        @Override public void visit(KeywordToken token) { tokens.add(token); }
        @Override public void visit(NewLineToken token) { tokens.add(token); }
        @Override public void visit(NumericConstantToken token) { tokens.add(token); }
        @Override public void visit(ReferenceToken token) { tokens.add(token); }
        @Override public void visit(StartMarkerToken token) { tokens.add(token); }
        @Override public void visit(StringConstantToken token) { tokens.add(token); }
        @Override public void visit(TextToken token) { tokens.add(token); }
    }

    protected void visit(StartStatementsBlockFragment fragment, Collection<Token> adds) {
        int minimalLineCount = fragment.getGroup().getMinimalLineCount();

        switch (fragment.getLineCount()) {
            case 0:
                tokens.addAll(adds);
                tokens.add(TextToken.SPACE);
                tokens.add(StartBlockToken.START_BLOCK);
                tokens.add(TextToken.SPACE);
                break;
            case 1:
                if (minimalLineCount == 0) {
                    tokens.add(NewLineToken.NEWLINE_1);
                    tokens.addAll(adds);
                    tokens.add(TextToken.SPACE);
                    tokens.add(StartBlockToken.START_BLOCK);
                    tokens.add(TextToken.SPACE);
                } else {
                    tokens.addAll(adds);
                    tokens.add(TextToken.SPACE);
                    tokens.add(StartBlockToken.START_BLOCK);
                    tokens.add(NewLineToken.NEWLINE_1);
                }
                break;
            case 2:
                switch (minimalLineCount) {
                    case 0:
                        tokens.add(NewLineToken.NEWLINE_2);
                        tokens.addAll(adds);
                        tokens.add(TextToken.SPACE);
                        tokens.add(StartBlockToken.START_BLOCK);
                        tokens.add(TextToken.SPACE);
                        break;
                    case 1:
                        tokens.add(NewLineToken.NEWLINE_1);
                        tokens.addAll(adds);
                        tokens.add(TextToken.SPACE);
                        tokens.add(StartBlockToken.START_BLOCK);
                        tokens.add(NewLineToken.NEWLINE_1);
                        break;
                    default:
                        tokens.addAll(adds);
                        tokens.add(NewLineToken.NEWLINE_1);
                        tokens.add(StartBlockToken.START_BLOCK);
                        tokens.add(NewLineToken.NEWLINE_1);
                }
                break;
            default:
                switch (minimalLineCount) {
                    case 0:
                        tokens.add(new NewLineToken(fragment.getLineCount()));
                        tokens.addAll(adds);
                        tokens.add(TextToken.SPACE);
                        tokens.add(StartBlockToken.START_BLOCK);
                        tokens.add(TextToken.SPACE);
                        break;
                    case 1:
                        tokens.addAll(adds);
                        tokens.add(TextToken.SPACE);
                        tokens.add(StartBlockToken.START_BLOCK);
                        tokens.add(new NewLineToken(fragment.getLineCount()));
                        break;
                    default:
                        tokens.addAll(adds);
                        tokens.add(NewLineToken.NEWLINE_1);
                        tokens.add(StartBlockToken.START_BLOCK);
                        tokens.add(new NewLineToken(fragment.getLineCount() - 1));
                }
                break;
        }
    }
}
