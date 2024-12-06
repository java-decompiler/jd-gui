/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.statement;

import org.jd.core.v1.model.javasyntax.expression.Expression;

import java.util.List;

public class SwitchStatement implements Statement {
    public static final DefaultLabel DEFAULT_LABEL = new DefaultLabel();

    protected Expression condition;
    protected List<Block> blocks;

    public SwitchStatement(Expression condition, List<Block> blocks) {
        this.condition = condition;
        this.blocks = blocks;
    }

    @Override
    public Expression getCondition() {
        return condition;
    }

    public void setCondition(Expression condition) {
        this.condition = condition;
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    @Override
    public boolean isSwitchStatement() { return true; }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }

    // --- Label --- //
    public interface Label extends Statement {}

    public static class DefaultLabel implements Label {
        protected DefaultLabel() {}

        @Override
        public void accept(StatementVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public String toString() {
            return "DefaultLabel";
        }
    }

    public static class ExpressionLabel implements Label {
        protected Expression expression;

        public ExpressionLabel(Expression expression) {
            this.expression = expression;
        }

        @Override
        public Expression getExpression() {
            return expression;
        }

        public void setExpression(Expression expression) {
            this.expression = expression;
        }

        @Override
        public void accept(StatementVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public String toString() {
            return "ExpressionLabel{" + expression.toString() + '}';
        }
    }

    // --- Block --- //
    public static abstract class Block implements Statement {
        protected BaseStatement statements;

        protected Block(BaseStatement statements) {
            this.statements = statements;
        }

        @Override
        public BaseStatement getStatements() {
            return statements;
        }
    }

    public static class LabelBlock extends Block {
        protected Label label;

        public LabelBlock(Label label, BaseStatement statements) {
            super(statements);
            this.label = label;
        }

        public Label getLabel() {
            return label;
        }

        @Override
        public boolean isSwitchStatementLabelBlock() { return true; }

        @Override
        public void accept(StatementVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public String toString() {
            return "LabelBlock{label=" + label.toString() + '}';
        }
    }

    public static class MultiLabelsBlock extends Block {
        protected List<Label> labels;

        public MultiLabelsBlock(List<Label> labels, BaseStatement statements) {
            super(statements);
            this.labels = labels;
        }

        public List<Label> getLabels() {
            return labels;
        }

        @Override
        public boolean isSwitchStatementMultiLabelsBlock() { return true; }

        @Override
        public void accept(StatementVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public String toString() {
            return "MultiLabelsBlock{labels=" + labels.toString() + '}';
        }
    }
}
