/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.model.cfg;

import org.jd.core.v1.util.DefaultList;

import java.util.HashSet;
import java.util.Iterator;

public class BasicBlock {
    public static final int TYPE_DELETED                         = 0;
    public static final int TYPE_START                           = (1 << 0);
    public static final int TYPE_END                             = (1 << 1);
    public static final int TYPE_STATEMENTS                      = (1 << 2);
    public static final int TYPE_THROW                           = (1 << 3);
    public static final int TYPE_RETURN                          = (1 << 4);
    public static final int TYPE_RETURN_VALUE                    = (1 << 5);
    public static final int TYPE_SWITCH_DECLARATION              = (1 << 6);
    public static final int TYPE_SWITCH                          = (1 << 7);
    public static final int TYPE_SWITCH_BREAK                    = (1 << 8);
    public static final int TYPE_TRY_DECLARATION                 = (1 << 9);
    public static final int TYPE_TRY                             = (1 << 10);
    public static final int TYPE_TRY_JSR                         = (1 << 11);
    public static final int TYPE_TRY_ECLIPSE                     = (1 << 12);
    public static final int TYPE_JSR                             = (1 << 13);
    public static final int TYPE_RET                             = (1 << 14);
    public static final int TYPE_CONDITIONAL_BRANCH              = (1 << 15);
    public static final int TYPE_IF                              = (1 << 16);
    public static final int TYPE_IF_ELSE                         = (1 << 17);
    public static final int TYPE_CONDITION                       = (1 << 18);
    public static final int TYPE_CONDITION_OR                    = (1 << 19);
    public static final int TYPE_CONDITION_AND                   = (1 << 20);
    public static final int TYPE_CONDITION_TERNARY_OPERATOR      = (1 << 21);
    public static final int TYPE_LOOP                            = (1 << 22);
    public static final int TYPE_LOOP_START                      = (1 << 23);
    public static final int TYPE_LOOP_CONTINUE                   = (1 << 24);
    public static final int TYPE_LOOP_END                        = (1 << 25);
    public static final int TYPE_GOTO                            = (1 << 26);
    public static final int TYPE_INFINITE_GOTO                   = (1 << 27);
    public static final int TYPE_GOTO_IN_TERNARY_OPERATOR        = (1 << 28);
    public static final int TYPE_TERNARY_OPERATOR                = (1 << 29);
    public static final int TYPE_JUMP                            = (1 << 30);

    public static final int GROUP_SINGLE_SUCCESSOR  = TYPE_START|TYPE_STATEMENTS|TYPE_TRY_DECLARATION|TYPE_JSR|TYPE_LOOP|TYPE_IF|TYPE_IF_ELSE|TYPE_SWITCH|TYPE_TRY|TYPE_TRY_JSR|TYPE_TRY_ECLIPSE|TYPE_GOTO|TYPE_GOTO_IN_TERNARY_OPERATOR|TYPE_TERNARY_OPERATOR;
    public static final int GROUP_SYNTHETIC         = TYPE_START|TYPE_END|TYPE_CONDITIONAL_BRANCH|TYPE_SWITCH_DECLARATION|TYPE_TRY_DECLARATION|TYPE_RET|TYPE_GOTO|TYPE_JUMP;
    public static final int GROUP_CODE              = TYPE_STATEMENTS|TYPE_THROW|TYPE_RETURN|TYPE_RETURN_VALUE|TYPE_SWITCH_DECLARATION|TYPE_CONDITIONAL_BRANCH|TYPE_JSR|TYPE_RET|TYPE_SWITCH|TYPE_GOTO|TYPE_INFINITE_GOTO|TYPE_GOTO_IN_TERNARY_OPERATOR|TYPE_CONDITION|TYPE_CONDITION_TERNARY_OPERATOR;
    public static final int GROUP_END               = TYPE_END|TYPE_THROW|TYPE_RETURN|TYPE_RETURN_VALUE|TYPE_RET|TYPE_SWITCH_BREAK|TYPE_LOOP_START|TYPE_LOOP_CONTINUE|TYPE_LOOP_END|TYPE_INFINITE_GOTO|TYPE_JUMP;
    public static final int GROUP_CONDITION         = TYPE_CONDITION|TYPE_CONDITION_OR|TYPE_CONDITION_AND|TYPE_CONDITION_TERNARY_OPERATOR;

    protected static final String[] TYPE_NAMES = {
        "DELETED", "START", "END", "STATEMENTS", "THROW", "RETURN", "RETURN_VALUE", "SWITCH_DECLARATION", "SWITCH",
        "SWITCH_BREAK", "TRY_DECLARATION", "TRY", "TRY_JSR", "TYPE_TRY_ECLIPSE", "JSR", "RET", "CONDITIONAL_BRANCH",
        "IF", "IF_ELSE", "CONDITION", "CONDITION_OR", "CONDITION_AND", "CONDITION_TERNARY_OPERATOR", "LOOP",
        "LOOP_START", "LOOP_CONTINUE", "LOOP_END", "GOTO", "INFINITE_GOTO", "GOTO_IN_TERNARY_OP", "TERNARY_OP", "JUMP"
    };

    protected static final DefaultList<ExceptionHandler> EMPTY_EXCEPTION_HANDLERS = DefaultList.emptyList();
    protected static final DefaultList<SwitchCase> EMPTY_SWITCH_CASES = DefaultList.emptyList();

    public static final BasicBlock SWITCH_BREAK = new ImmutableBasicBlock(TYPE_SWITCH_BREAK);
    public static final BasicBlock LOOP_START = new ImmutableBasicBlock(TYPE_LOOP_START);
    public static final BasicBlock LOOP_CONTINUE = new ImmutableBasicBlock(TYPE_LOOP_CONTINUE);
    public static final BasicBlock LOOP_END = new ImmutableBasicBlock(TYPE_LOOP_END);
    public static final BasicBlock END = new ImmutableBasicBlock(TYPE_END);
    public static final BasicBlock RETURN = new ImmutableBasicBlock(TYPE_RETURN);


    protected ControlFlowGraph controlFlowGraph;

    protected int index;
    protected int type;

    protected int fromOffset;
    protected int toOffset;

    protected BasicBlock next;
    protected BasicBlock branch;
    protected BasicBlock condition;
    protected boolean inverseCondition;
    protected BasicBlock sub1;
    protected BasicBlock sub2;
    protected DefaultList<ExceptionHandler> exceptionHandlers = EMPTY_EXCEPTION_HANDLERS;
    protected DefaultList<SwitchCase> switchCases = EMPTY_SWITCH_CASES;
    protected HashSet<BasicBlock> predecessors;

    public BasicBlock(ControlFlowGraph controlFlowGraph, int index, BasicBlock original) {
        this(controlFlowGraph, index, original, new HashSet<>());
    }

    public BasicBlock(ControlFlowGraph controlFlowGraph, int index, BasicBlock original, HashSet<BasicBlock> predecessors) {
        this.controlFlowGraph = controlFlowGraph;
        this.index = index;
        this.type = original.type;
        this.fromOffset = original.fromOffset;
        this.toOffset = original.toOffset;
        this.next = original.next;
        this.branch = original.branch;
        this.condition = original.condition;
        this.inverseCondition = original.inverseCondition;
        this.sub1 = original.sub1;
        this.sub2 = original.sub2;
        this.exceptionHandlers = original.exceptionHandlers;
        this.switchCases = original.switchCases;
        this.predecessors = predecessors;
    }

    public BasicBlock(ControlFlowGraph controlFlowGraph, int index, int type, int fromOffset, int toOffset, boolean inverseCondition) {
        this(controlFlowGraph, index, type, fromOffset, toOffset, inverseCondition, new HashSet<>());
    }

    public BasicBlock(ControlFlowGraph controlFlowGraph, int index, int type, int fromOffset, int toOffset, boolean inverseCondition, HashSet<BasicBlock> predecessors) {
        this.controlFlowGraph = controlFlowGraph;
        this.index = index;
        this.type = type;
        this.fromOffset = fromOffset;
        this.toOffset = toOffset;
        this.next = this.branch = this.condition = this.sub1 = this.sub2 = END;
        this.predecessors = predecessors;
        this.inverseCondition = inverseCondition;
    }

    public ControlFlowGraph getControlFlowGraph() {
        return controlFlowGraph;
    }

    public int getIndex() {
        return index;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getFromOffset() {
        return fromOffset;
    }

    public void setFromOffset(int fromOffset) {
        this.fromOffset = fromOffset;
    }

    public int getToOffset() {
        return toOffset;
    }

    public void setToOffset(int toOffset) {
        this.toOffset = toOffset;
    }

    public int getFirstLineNumber() {
        return controlFlowGraph.getLineNumber(this.fromOffset);
    }

    public int getLastLineNumber() {
        return controlFlowGraph.getLineNumber(this.toOffset-1);
    }

    public BasicBlock getNext() {
        return next;
    }

    public void setNext(BasicBlock next) {
        this.next = next;
    }

    public BasicBlock getBranch() {
        return branch;
    }

    public void setBranch(BasicBlock branch) {
        this.branch = branch;
    }

    public DefaultList<ExceptionHandler> getExceptionHandlers() {
        return exceptionHandlers;
    }

    public DefaultList<SwitchCase> getSwitchCases() {
        return switchCases;
    }

    public void setSwitchCases(DefaultList<SwitchCase> switchCases) {
        this.switchCases = switchCases;
    }

    public BasicBlock getCondition() {
        return condition;
    }

    public void setCondition(BasicBlock condition) {
        this.condition = condition;
    }

    public BasicBlock getSub1() {
        return sub1;
    }

    public void setSub1(BasicBlock sub1) {
        this.sub1 = sub1;
    }

    public BasicBlock getSub2() {
        return sub2;
    }

    public void setSub2(BasicBlock sub2) {
        this.sub2 = sub2;
    }

    public HashSet<BasicBlock> getPredecessors() {
        return predecessors;
    }

    public boolean mustInverseCondition() {
        return inverseCondition;
    }

    public void setInverseCondition(boolean inverseCondition) {
        this.inverseCondition = inverseCondition;
    }

    public boolean contains(BasicBlock basicBlock) {
        if (next == basicBlock)
            return true;

        if (branch == basicBlock)
            return true;

        for (ExceptionHandler exceptionHandler : exceptionHandlers) {
            if (exceptionHandler.getBasicBlock() == basicBlock)
                return true;
        }

        for (SwitchCase switchCase : switchCases) {
            if (switchCase.getBasicBlock() == basicBlock)
                return true;
        }

        if (sub1 == basicBlock)
            return true;

        if (sub2 == basicBlock)
            return true;

        return false;
    }

    public void replace(BasicBlock old, BasicBlock nevv) {
        if (next == old)
            next = nevv;

        if (branch == old)
            branch = nevv;

        for (ExceptionHandler exceptionHandler : exceptionHandlers) {
            exceptionHandler.replace(old, nevv);
        }

        for (SwitchCase switchCase : switchCases) {
            switchCase.replace(old, nevv);
        }

        if (sub1 == old)
            sub1 = nevv;

        if (sub2 == old)
            sub2 = nevv;

        if (predecessors.contains(old)) {
            predecessors.remove(old);
            if (nevv != BasicBlock.END)
                predecessors.add(nevv);
        }
    }

    public void replace(HashSet<BasicBlock> olds, BasicBlock nevv) {
        if (olds.contains(next))
            next = nevv;

        if (olds.contains(branch))
            branch = nevv;

        for (ExceptionHandler exceptionHandler : exceptionHandlers) {
            exceptionHandler.replace(olds, nevv);
        }

        for (SwitchCase switchCase : switchCases) {
            switchCase.replace(olds, nevv);
        }

        if (olds.contains(sub1))
            sub1 = nevv;

        if (olds.contains(sub2))
            sub2 = nevv;

        predecessors.removeAll(olds);
        predecessors.add(nevv);
    }

    public void addExceptionHandler(String internalThrowableName, BasicBlock basicBlock) {
        if (exceptionHandlers == EMPTY_EXCEPTION_HANDLERS) {
            // Add a first handler
            exceptionHandlers = new DefaultList<>();
            exceptionHandlers.add(new ExceptionHandler(internalThrowableName, basicBlock));
        } else {
            for (ExceptionHandler exceptionHandler : exceptionHandlers) {
                if (exceptionHandler.getBasicBlock() == basicBlock) {
                    // Found -> Add an other 'internalThrowableName'
                    exceptionHandler.addInternalThrowableName(internalThrowableName);
                    return;
                }
            }
            // Not found -> Add a new handler
            exceptionHandlers.add(new ExceptionHandler(internalThrowableName, basicBlock));
        }
    }

    public void inverseCondition() {
        switch (type) {
            case TYPE_CONDITION:
            case TYPE_CONDITION_TERNARY_OPERATOR:
            case TYPE_GOTO_IN_TERNARY_OPERATOR:
                inverseCondition ^= true;
                break;
            case TYPE_CONDITION_AND:
                type = TYPE_CONDITION_OR;
                sub1.inverseCondition();
                sub2.inverseCondition();
                break;
            case TYPE_CONDITION_OR:
                type = TYPE_CONDITION_AND;
                sub1.inverseCondition();
                sub2.inverseCondition();
                break;
            default:
                assert false : "Invalid condition";
                break;
        }
    }

    public boolean matchType(int types) {
        return (type & types) != 0;
    }

    public String getTypeName() {
        return TYPE_NAMES[(type==0) ? 0 : Integer.numberOfTrailingZeros(type)+1];
    }

    @Override
    public String toString() {
        String s = "BasicBlock{index=" + index + ", from=" + fromOffset + ", to=" + toOffset + ", type=" + getTypeName() + ", inverseCondition=" + inverseCondition;

        if (!predecessors.isEmpty()) {
            s += ", predecessors=[";

            Iterator<BasicBlock> iterator = predecessors.iterator();

            if (iterator.hasNext()) {
                s += iterator.next().getIndex();
                while (iterator.hasNext()) {
                    s += ", " + iterator.next().getIndex();
                }
            }

            s += "]";
        }

        return s + "}";
    }

    @Override
    public int hashCode() {
        return 378887654 + index;
    }

    @Override
    public boolean equals(Object other) {
        return index == ((BasicBlock)other).index;
    }

    public static class ExceptionHandler {
        protected String internalThrowableName;
        protected DefaultList<String> otherInternalThrowableNames;
        protected BasicBlock basicBlock;

        public ExceptionHandler(String internalThrowableName, BasicBlock basicBlock) {
            this.internalThrowableName = internalThrowableName;
            this.basicBlock = basicBlock;
        }

        public String getInternalThrowableName() {
            return internalThrowableName;
        }

        public DefaultList<String> getOtherInternalThrowableNames() {
            return otherInternalThrowableNames;
        }

        public BasicBlock getBasicBlock() {
            return basicBlock;
        }

        public void setBasicBlock(BasicBlock basicBlock) {
            this.basicBlock = basicBlock;
        }

        public void addInternalThrowableName(String internalThrowableName) {
            if (otherInternalThrowableNames == null)
                otherInternalThrowableNames = new DefaultList<>();
            otherInternalThrowableNames.add(internalThrowableName);
        }

        public void replace(BasicBlock old, BasicBlock nevv) {
            if (basicBlock == old)
                basicBlock = nevv;
        }

        public void replace(HashSet<BasicBlock> olds, BasicBlock nevv) {
            if (olds.contains(basicBlock))
                basicBlock = nevv;
        }

        @Override
        public String toString() {
            if (otherInternalThrowableNames == null)
                return "BasicBlock.Handler{" + internalThrowableName + " -> " + basicBlock + "}";
            else
                return "BasicBlock.Handler{" + internalThrowableName + ", " + otherInternalThrowableNames + " -> " + basicBlock + "}";
        }
    }

    public static class SwitchCase {
        protected int value;
        protected int offset;
        protected BasicBlock basicBlock;
        protected boolean defaultCase;

        public SwitchCase(BasicBlock basicBlock) {
            this.offset = basicBlock.getFromOffset();
            this.basicBlock = basicBlock;
            this.defaultCase = true;
        }

        public SwitchCase(int value, BasicBlock basicBlock) {
            this.value = value;
            this.offset = basicBlock.getFromOffset();
            this.basicBlock = basicBlock;
            this.defaultCase = false;
        }

        public int getValue() {
            return value;
        }

        public int getOffset() {
            return offset;
        }

        public BasicBlock getBasicBlock() {
            return basicBlock;
        }

        public void setBasicBlock(BasicBlock basicBlock) {
            this.basicBlock = basicBlock;
        }

        public boolean isDefaultCase() {
            return defaultCase;
        }

        public void replace(BasicBlock old, BasicBlock nevv) {
            if (basicBlock == old)
                basicBlock = nevv;
        }

        public void replace(HashSet<BasicBlock> olds, BasicBlock nevv) {
            if (olds.contains(basicBlock))
                basicBlock = nevv;
        }

        @Override
        public String toString() {
            if (defaultCase)
                return "BasicBlock.SwitchCase{default: " + basicBlock + "}";
            else
                return "BasicBlock.SwitchCase{'" + value + "': " + basicBlock + "}";
        }
    }

    protected static class ImmutableBasicBlock extends BasicBlock {
        public ImmutableBasicBlock(int type) {
            super(
                null, -1, type, 0, 0, true,
                new HashSet<BasicBlock>() {
                    public boolean add(BasicBlock e) { return false; }
                }
            );
        }

        public int getFirstLineNumber() { return 0; }
        public int getLastLineNumber() { return 0; }
    }
}
