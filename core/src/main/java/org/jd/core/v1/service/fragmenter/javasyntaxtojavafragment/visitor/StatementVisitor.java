/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.fragmenter.javasyntaxtojavafragment.visitor;

import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.model.javafragment.ImportsFragment;
import org.jd.core.v1.model.javafragment.StartSingleStatementBlockFragment;
import org.jd.core.v1.model.javafragment.StartStatementsBlockFragment;
import org.jd.core.v1.model.javafragment.TokensFragment;
import org.jd.core.v1.model.javasyntax.expression.Expression;
import org.jd.core.v1.model.javasyntax.statement.*;
import org.jd.core.v1.model.javasyntax.type.BaseType;
import org.jd.core.v1.model.token.*;
import org.jd.core.v1.service.fragmenter.javasyntaxtojavafragment.util.JavaFragmentFactory;

import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class StatementVisitor extends ExpressionVisitor {
    public static final KeywordToken ASSERT = new KeywordToken("assert");
    public static final KeywordToken BREAK = new KeywordToken("break");
    public static final KeywordToken CASE = new KeywordToken("case");
    public static final KeywordToken CATCH = new KeywordToken("catch");
    public static final KeywordToken CONTINUE = new KeywordToken("continue");
    public static final KeywordToken DEFAULT = new KeywordToken("default");
    public static final KeywordToken DO = new KeywordToken("do");
    public static final KeywordToken ELSE = new KeywordToken("else");
    public static final KeywordToken FINAL = new KeywordToken("final");
    public static final KeywordToken FINALLY = new KeywordToken("finally");
    public static final KeywordToken FOR = new KeywordToken("for");
    public static final KeywordToken IF = new KeywordToken("if");
    public static final KeywordToken RETURN = new KeywordToken("return");
    public static final KeywordToken STRICT = new KeywordToken("strictfp");
    public static final KeywordToken SYNCHRONIZED = new KeywordToken("synchronized");
    public static final KeywordToken SWITCH = new KeywordToken("switch");
    public static final KeywordToken THROW = new KeywordToken("throw");
    public static final KeywordToken TRANSIENT = new KeywordToken("transient");
    public static final KeywordToken TRY = new KeywordToken("try");
    public static final KeywordToken VOLATILE = new KeywordToken("volatile");
    public static final KeywordToken WHILE = new KeywordToken("while");

    public StatementVisitor(Loader loader, String mainInternalTypeName, int majorVersion, ImportsFragment importsFragment) {
        super(loader, mainInternalTypeName, majorVersion, importsFragment);
    }

    @Override
    public void visit(AssertStatement statement) {
        tokens = new Tokens();
        tokens.add(StartBlockToken.START_DECLARATION_OR_STATEMENT_BLOCK);
        tokens.add(ASSERT);
        tokens.add(TextToken.SPACE);
        statement.getCondition().accept(this);

        Expression msg = statement.getMessage();

        if (msg != null) {
            tokens.add(TextToken.SPACE_COLON_SPACE);
            msg.accept(this);
        }

        tokens.add(TextToken.SEMICOLON);
        tokens.add(EndBlockToken.END_DECLARATION_OR_STATEMENT_BLOCK);
        fragments.addTokensFragment(tokens);
    }

    @Override
    public void visit(BreakStatement statement) {
        tokens = new Tokens();
        tokens.add(BREAK);

        if (statement.getLabel() != null) {
            tokens.add(TextToken.SPACE);
            tokens.add(newTextToken(statement.getLabel()));
        }

        tokens.add(TextToken.SEMICOLON);
        fragments.addTokensFragment(tokens);
    }

    @Override public void visit(ByteCodeStatement statement) {
        visitComment(statement.getText());
    }

    @Override public void visit(CommentStatement statement) {
        visitComment(statement.getText());
    }

    protected void visitComment(String text) {
        tokens = new Tokens();
        tokens.add(StartMarkerToken.COMMENT);

        StringTokenizer st = new StringTokenizer(text, "\n");

        while (st.hasMoreTokens()) {
            tokens.add(new TextToken(st.nextToken()));
            tokens.add(NewLineToken.NEWLINE_1);
        }

        tokens.remove(tokens.size()-1);
        tokens.add(EndMarkerToken.COMMENT);
        fragments.addTokensFragment(tokens);
    }

    @Override
    public void visit(ContinueStatement statement) {
        tokens = new Tokens();
        tokens.add(CONTINUE);

        if (statement.getLabel() != null) {
            tokens.add(TextToken.SPACE);
            tokens.add(newTextToken(statement.getLabel()));
        }

        tokens.add(TextToken.SEMICOLON);
        fragments.addTokensFragment(tokens);
    }

    @Override
    public void visit(DoWhileStatement statement) {
        StartStatementsBlockFragment.Group group = JavaFragmentFactory.addStartStatementsDoWhileBlock(fragments);

        safeAccept(statement.getStatements());

        JavaFragmentFactory.addEndStatementsBlock(fragments, group);

        tokens = new Tokens();
        tokens.add(WHILE);
        tokens.add(TextToken.SPACE);
        tokens.add(StartBlockToken.START_PARAMETERS_BLOCK);

        statement.getCondition().accept(this);

        tokens.add(EndBlockToken.END_PARAMETERS_BLOCK);
        tokens.add(TextToken.SEMICOLON);
        fragments.addTokensFragment(tokens);
    }

    @Override
    public void visit(ExpressionStatement statement) {
        tokens = new Tokens();
        tokens.add(StartBlockToken.START_DECLARATION_OR_STATEMENT_BLOCK);

        statement.getExpression().accept(this);

        tokens.add(TextToken.SEMICOLON);
        tokens.add(EndBlockToken.END_DECLARATION_OR_STATEMENT_BLOCK);
        fragments.addTokensFragment(tokens);
    }

    @Override
    public void visit(ForStatement statement) {
        tokens = new Tokens();
        tokens.add(FOR);
        tokens.add(TextToken.SPACE);
        tokens.add(StartBlockToken.START_PARAMETERS_BLOCK);

        safeAccept(statement.getDeclaration());
        safeAccept(statement.getInit());

        if (statement.getCondition() == null) {
            tokens.add(TextToken.SEMICOLON);
        } else {
            tokens.add(TextToken.SEMICOLON_SPACE);
            statement.getCondition().accept(this);
        }

        if (statement.getUpdate() == null) {
            tokens.add(TextToken.SEMICOLON);
        } else {
            tokens.add(TextToken.SEMICOLON_SPACE);
            statement.getUpdate().accept(this);
        }

        visitLoopStatements(statement.getStatements());
    }

    @Override
    public void visit(ForEachStatement statement) {
        tokens = new Tokens();
        tokens.add(FOR);
        tokens.add(TextToken.SPACE);
        tokens.add(StartBlockToken.START_PARAMETERS_BLOCK);

        BaseType type = statement.getType();

        type.accept(this);

        tokens.add(TextToken.SPACE);
        tokens.add(newTextToken(statement.getName()));
        tokens.add(TextToken.SPACE_COLON_SPACE);

        statement.getExpression().accept(this);

        visitLoopStatements(statement.getStatements());
    }

    protected void visitLoopStatements(BaseStatement statements) {
        tokens.add(EndBlockToken.END_PARAMETERS_BLOCK);
        fragments.addTokensFragment(tokens);

        if (statements == null) {
            tokens.add(TextToken.SEMICOLON);
        } else {
            Fragments tmp = fragments;
            fragments = new Fragments();

            statements.accept(this);

            switch (fragments.size()) {
                case 0:
                    tokens.add(TextToken.SEMICOLON);
                    break;
                case 1:
                    StartSingleStatementBlockFragment start = JavaFragmentFactory.addStartSingleStatementBlock(tmp);
                    tmp.addAll(fragments);
                    JavaFragmentFactory.addEndSingleStatementBlock(tmp, start);
                    break;
                default:
                    StartStatementsBlockFragment.Group group = JavaFragmentFactory.addStartStatementsBlock(tmp);
                    tmp.addAll(fragments);
                    JavaFragmentFactory.addEndStatementsBlock(tmp, group);
                    break;
            }

            fragments = tmp;
        }
    }

    @Override
    public void visit(IfStatement statement) {
        tokens = new Tokens();
        tokens.add(IF);
        tokens.add(TextToken.SPACE);
        tokens.add(StartBlockToken.START_PARAMETERS_BLOCK);

        statement.getCondition().accept(this);

        tokens.add(EndBlockToken.END_PARAMETERS_BLOCK);
        fragments.addTokensFragment(tokens);

        BaseStatement stmt = statement.getStatements();

        if (stmt == null) {
            fragments.add(TokensFragment.SEMICOLON);
        } else {
            Fragments tmp = fragments;
            fragments = new Fragments();

            stmt.accept(this);

            switch (stmt.size()) {
                case 0:
                    tmp.add(TokensFragment.SEMICOLON);
                    break;
                case 1:
                    StartSingleStatementBlockFragment start = JavaFragmentFactory.addStartSingleStatementBlock(tmp);
                    tmp.addAll(fragments);
                    JavaFragmentFactory.addEndSingleStatementBlock(tmp, start);
                    break;
                default:
                    StartStatementsBlockFragment.Group group = JavaFragmentFactory.addStartStatementsBlock(tmp);
                    tmp.addAll(fragments);
                    JavaFragmentFactory.addEndStatementsBlock(tmp, group);
                    break;
            }

            fragments = tmp;
        }
    }

    @Override
    public void visit(IfElseStatement statement) {
        tokens = new Tokens();
        tokens.add(IF);
        tokens.add(TextToken.SPACE);
        tokens.add(StartBlockToken.START_PARAMETERS_BLOCK);

        statement.getCondition().accept(this);

        tokens.add(EndBlockToken.END_PARAMETERS_BLOCK);
        fragments.addTokensFragment(tokens);

        StartStatementsBlockFragment.Group group = JavaFragmentFactory.addStartStatementsBlock(fragments);
        statement.getStatements().accept(this);
        JavaFragmentFactory.addEndStatementsBlock(fragments, group);
        visitElseStatements(statement.getElseStatements(), group);
    }

    protected void visitElseStatements(BaseStatement elseStatements, StartStatementsBlockFragment.Group group) {
        BaseStatement statementList = elseStatements;

        if (elseStatements.isList()) {
            if (elseStatements.size() == 1) {
                statementList = elseStatements.getFirst();
            }
        }

        tokens = new Tokens();
        tokens.add(ELSE);

        if (statementList.isIfElseStatement()) {
            tokens.add(TextToken.SPACE);
            tokens.add(IF);
            tokens.add(TextToken.SPACE);
            tokens.add(StartBlockToken.START_PARAMETERS_BLOCK);

            statementList.getCondition().accept(this);

            tokens.add(EndBlockToken.END_PARAMETERS_BLOCK);
            fragments.addTokensFragment(tokens);

            JavaFragmentFactory.addStartStatementsBlock(fragments, group);
            statementList.getStatements().accept(this);
            JavaFragmentFactory.addEndStatementsBlock(fragments, group);
            visitElseStatements(statementList.getElseStatements(), group);
        } else if (statementList.isIfStatement()) {
            tokens.add(TextToken.SPACE);
            tokens.add(IF);
            tokens.add(TextToken.SPACE);
            tokens.add(StartBlockToken.START_PARAMETERS_BLOCK);

            statementList.getCondition().accept(this);

            tokens.add(EndBlockToken.END_PARAMETERS_BLOCK);
            fragments.addTokensFragment(tokens);

            JavaFragmentFactory.addStartStatementsBlock(fragments, group);

            statementList.getStatements().accept(this);

            JavaFragmentFactory.addEndStatementsBlock(fragments, group);
        } else {
            fragments.addTokensFragment(tokens);

            JavaFragmentFactory.addStartStatementsBlock(fragments, group);

            elseStatements.accept(this);

            JavaFragmentFactory.addEndStatementsBlock(fragments, group);
        }
    }

    @Override
    public void visit(LabelStatement statement) {
        tokens = new Tokens();
        tokens.add(newTextToken(statement.getLabel()));
        tokens.add(TextToken.COLON);

        if (statement.getStatement() == null) {
            fragments.addTokensFragment(tokens);
        } else {
            tokens.add(TextToken.SPACE);
            fragments.addTokensFragment(tokens);
            statement.getStatement().accept(this);
        }
    }

    @Override
    public void visit(LambdaExpressionStatement statement) {
        statement.getExpression().accept(this);
    }

    @Override
    public void visit(LocalVariableDeclarationStatement statement) {
        tokens = new Tokens();
        tokens.add(StartBlockToken.START_DECLARATION_OR_STATEMENT_BLOCK);

        if (statement.isFinal()) {
            tokens.add(FINAL);
            tokens.add(TextToken.SPACE);
        }

        BaseType type = statement.getType();

        type.accept(this);

        tokens.add(TextToken.SPACE);

        statement.getLocalVariableDeclarators().accept(this);

        tokens.add(TextToken.SEMICOLON);
        tokens.add(EndBlockToken.END_DECLARATION_OR_STATEMENT_BLOCK);
        fragments.addTokensFragment(tokens);
    }

    @Override
    public void visit(ReturnExpressionStatement statement) {
        tokens = new Tokens();
        tokens.add(StartBlockToken.START_DECLARATION_OR_STATEMENT_BLOCK);
        tokens.addLineNumberToken(statement.getLineNumber());
        tokens.add(RETURN);
        tokens.add(TextToken.SPACE);

        statement.getExpression().accept(this);

        tokens.add(TextToken.SEMICOLON);
        tokens.add(EndBlockToken.END_DECLARATION_OR_STATEMENT_BLOCK);
        fragments.addTokensFragment(tokens);
    }

    @Override
    public void visit(ReturnStatement statement) {
        fragments.add(TokensFragment.RETURN_SEMICOLON);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void visit(Statements list) {
        int size = list.size();

        if (size > 0) {
            Iterator<Statement> iterator = list.iterator();
            iterator.next().accept(this);

            for (int i = 1; i < size; i++) {
                JavaFragmentFactory.addSpacerBetweenStatements(fragments);
                iterator.next().accept(this);
            }
        }
    }

    @Override
    public void visit(SwitchStatement statement) {
        tokens = new Tokens();
        tokens.add(SWITCH);
        tokens.add(TextToken.SPACE);
        tokens.add(StartBlockToken.START_PARAMETERS_BLOCK);

        statement.getCondition().accept(this);

        tokens.add(EndBlockToken.END_PARAMETERS_BLOCK);
        fragments.addTokensFragment(tokens);

        StartStatementsBlockFragment.Group group = JavaFragmentFactory.addStartStatementsBlock(fragments);

        Iterator<SwitchStatement.Block> iterator = statement.getBlocks().iterator();

        if (iterator.hasNext()) {
            iterator.next().accept(this);

            while (iterator.hasNext()) {
                JavaFragmentFactory.addSpacerBetweenSwitchLabelBlock(fragments);
                iterator.next().accept(this);
            }
        }

        JavaFragmentFactory.addEndStatementsBlock(fragments, group);
        JavaFragmentFactory.addSpacerAfterEndStatementsBlock(fragments);
    }

    @Override
    public void visit(SwitchStatement.LabelBlock statement) {
        statement.getLabel().accept(this);
        JavaFragmentFactory.addSpacerAfterSwitchLabel(fragments);
        statement.getStatements().accept(this);
        JavaFragmentFactory.addSpacerAfterSwitchBlock(fragments);
    }

    @Override
    public void visit(SwitchStatement.DefaultLabel statement) {
        tokens = new Tokens();
        tokens.add(DEFAULT);
        tokens.add(TextToken.COLON);
        fragments.addTokensFragment(tokens);
    }

    @Override
    public void visit(SwitchStatement.ExpressionLabel statement) {
        tokens = new Tokens();
        tokens.add(CASE);
        tokens.add(TextToken.SPACE);

        statement.getExpression().accept(this);

        tokens.add(TextToken.COLON);
        fragments.addTokensFragment(tokens);
    }

    @Override
    public void visit(SwitchStatement.MultiLabelsBlock statement) {
        Iterator<SwitchStatement.Label> iterator = statement.getLabels().iterator();

        if (iterator.hasNext()) {
            iterator.next().accept(this);

            while (iterator.hasNext()) {
                JavaFragmentFactory.addSpacerBetweenSwitchLabels(fragments);
                iterator.next().accept(this);
            }
        }

        JavaFragmentFactory.addSpacerAfterSwitchLabel(fragments);
        statement.getStatements().accept(this);
        JavaFragmentFactory.addSpacerAfterSwitchBlock(fragments);
    }

    @Override
    public void visit(SynchronizedStatement statement) {
        tokens = new Tokens();
        tokens.add(SYNCHRONIZED);
        tokens.add(TextToken.SPACE);
        tokens.add(StartBlockToken.START_PARAMETERS_BLOCK);

        statement.getMonitor().accept(this);

        tokens.add(EndBlockToken.END_PARAMETERS_BLOCK);

        BaseStatement statements = statement.getStatements();

        if (statements == null) {
            tokens.add(TextToken.SPACE);
            tokens.add(TextToken.LEFTRIGHTCURLYBRACKETS);
            fragments.addTokensFragment(tokens);
        } else {
            fragments.addTokensFragment(tokens);
            StartStatementsBlockFragment.Group group = JavaFragmentFactory.addStartStatementsBlock(fragments);
            statements.accept(this);
            JavaFragmentFactory.addEndStatementsBlock(fragments, group);
        }
    }

    @Override
    public void visit(ThrowStatement statement) {
        tokens = new Tokens();
        tokens.add(StartBlockToken.START_DECLARATION_OR_STATEMENT_BLOCK);
        tokens.add(THROW);
        tokens.add(TextToken.SPACE);

        statement.getExpression().accept(this);

        tokens.add(TextToken.SEMICOLON);
        tokens.add(EndBlockToken.END_DECLARATION_OR_STATEMENT_BLOCK);
        fragments.addTokensFragment(tokens);
    }

    @Override
    public void visit(TryStatement statement) {
        List<TryStatement.Resource> resources = statement.getResources();
        StartStatementsBlockFragment.Group group;

        if (resources == null) {
            group = JavaFragmentFactory.addStartStatementsTryBlock(fragments);
        } else {
            int size = resources.size();

            assert size > 0;

            tokens = new Tokens();
            tokens.add(TRY);
            if (size == 1) {
                tokens.add(TextToken.SPACE);
            }
            tokens.add(StartBlockToken.START_RESOURCES_BLOCK);

            resources.get(0).accept(this);

            for (int i=1; i<size; i++) {
                tokens.add(TextToken.SEMICOLON_SPACE);
                resources.get(i).accept(this);
            }

            tokens.add(EndBlockToken.END_RESOURCES_BLOCK);
            fragments.addTokensFragment(tokens);
            group = JavaFragmentFactory.addStartStatementsBlock(fragments);
        }

        visitTryStatement(statement, group);
    }

    @Override
    public void visit(TryStatement.Resource resource) {
        Expression expression = resource.getExpression();

        tokens.addLineNumberToken(expression);

        BaseType type = resource.getType();

        type.accept(this);
        tokens.add(TextToken.SPACE);
        tokens.add(newTextToken(resource.getName()));
        tokens.add(TextToken.SPACE_EQUAL_SPACE);
        expression.accept(this);
    }

    protected void visitTryStatement(TryStatement statement, StartStatementsBlockFragment.Group group) {
        int fragmentCount1 = fragments.size(), fragmentCount2 = fragmentCount1;

        statement.getTryStatements().accept(this);

        if (statement.getCatchClauses() != null) {
            for (TryStatement.CatchClause cc : statement.getCatchClauses()) {
                JavaFragmentFactory.addEndStatementsBlock(fragments, group);

                BaseType type = cc.getType();

                tokens = new Tokens();
                tokens.add(CATCH);
                tokens.add(TextToken.SPACE_LEFTROUNDBRACKET);
                type.accept(this);

                if (cc.getOtherTypes() != null) {
                    for (BaseType otherType : cc.getOtherTypes()) {
                        tokens.add(TextToken.VERTICALLINE);
                        otherType.accept(this);
                    }
                }

                tokens.add(TextToken.SPACE);
                tokens.add(newTextToken(cc.getName()));
                tokens.add(TextToken.RIGHTROUNDBRACKET);

                int lineNumber = cc.getLineNumber();

                if (lineNumber == Expression.UNKNOWN_LINE_NUMBER) {
                    fragments.addTokensFragment(tokens);
                } else {
                    tokens.addLineNumberToken(lineNumber);
                    fragments.addTokensFragment(tokens);
                }

                fragmentCount1 = fragments.size();
                JavaFragmentFactory.addStartStatementsBlock(fragments, group);
                fragmentCount2 = fragments.size();
                cc.getStatements().accept(this);
            }
        }

        if (statement.getFinallyStatements() != null) {
            JavaFragmentFactory.addEndStatementsBlock(fragments, group);

            tokens = new Tokens();
            tokens.add(FINALLY);
            fragments.addTokensFragment(tokens);

            fragmentCount1 = fragments.size();
            JavaFragmentFactory.addStartStatementsBlock(fragments, group);
            fragmentCount2 = fragments.size();
            statement.getFinallyStatements().accept(this);
        }

        if (fragmentCount2 == fragments.size()) {
            fragments.subList(fragmentCount1, fragmentCount2).clear();
            tokens.add(TextToken.SPACE);
            tokens.add(TextToken.LEFTRIGHTCURLYBRACKETS);
        } else {
            JavaFragmentFactory.addEndStatementsBlock(fragments, group);
        }
    }

    @Override
    public void visit(TypeDeclarationStatement statement) {
        statement.getTypeDeclaration().accept(this);
        fragments.add(TokensFragment.SEMICOLON);
    }

    @Override
    public void visit(WhileStatement statement) {
        tokens = new Tokens();
        tokens.add(WHILE);
        tokens.add(TextToken.SPACE);
        tokens.add(StartBlockToken.START_PARAMETERS_BLOCK);

        statement.getCondition().accept(this);

        visitLoopStatements(statement.getStatements());
    }
}
