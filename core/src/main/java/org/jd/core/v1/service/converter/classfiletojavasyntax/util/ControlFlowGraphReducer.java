/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.util;

import org.jd.core.v1.model.classfile.attribute.AttributeCode;
import org.jd.core.v1.model.javasyntax.expression.Expression;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.cfg.BasicBlock;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.cfg.ControlFlowGraph;

import java.util.*;

import static org.jd.core.v1.service.converter.classfiletojavasyntax.model.cfg.BasicBlock.*;

public class ControlFlowGraphReducer {

    public static boolean reduce(ControlFlowGraph cfg) {
        BasicBlock start = cfg.getStart();
        BitSet jsrTargets = new BitSet();
        BitSet visited = new BitSet(cfg.getBasicBlocks().size());

        return reduce(visited, start, jsrTargets);
    }

    public static boolean reduce(BitSet visited, BasicBlock basicBlock, BitSet jsrTargets) {
        if (!basicBlock.matchType(GROUP_END) && (visited.get(basicBlock.getIndex()) == false)) {
            visited.set(basicBlock.getIndex());

            switch (basicBlock.getType()) {
                case TYPE_START:
                case TYPE_STATEMENTS:
                case TYPE_IF:
                case TYPE_IF_ELSE:
                case TYPE_SWITCH:
                case TYPE_TRY:
                case TYPE_TRY_JSR:
                case TYPE_TRY_ECLIPSE:
                case TYPE_GOTO_IN_TERNARY_OPERATOR:
                    return reduce(visited, basicBlock.getNext(), jsrTargets);
                case TYPE_CONDITIONAL_BRANCH:
                case TYPE_CONDITION:
                case TYPE_CONDITION_OR:
                case TYPE_CONDITION_AND:
                case TYPE_CONDITION_TERNARY_OPERATOR:
                    return reduceConditionalBranch(visited, basicBlock, jsrTargets);
                case TYPE_SWITCH_DECLARATION:
                    return reduceSwitchDeclaration(visited, basicBlock, jsrTargets);
                case TYPE_TRY_DECLARATION:
                    return reduceTryDeclaration(visited, basicBlock, jsrTargets);
                case TYPE_JSR:
                    return reduceJsr(visited, basicBlock, jsrTargets);
                case TYPE_LOOP:
                    return reduceLoop(visited, basicBlock, jsrTargets);
            }
        }

        return true;
    }

    protected static boolean reduceConditionalBranch(BitSet visited, BasicBlock basicBlock, BitSet jsrTargets) {
        while (aggregateConditionalBranches(basicBlock));

        assert basicBlock.matchType(GROUP_CONDITION);

        if (reduce(visited, basicBlock.getNext(), jsrTargets) & reduce(visited, basicBlock.getBranch(), jsrTargets)) {
            return reduceConditionalBranch(basicBlock);
        }

        return false;
    }

    protected static boolean reduceConditionalBranch(BasicBlock basicBlock) {
        BasicBlock next = basicBlock.getNext();
        BasicBlock branch = basicBlock.getBranch();
        WatchDog watchdog = new WatchDog();

        if (next == branch) {
            // Empty 'if'
            createIf(basicBlock, END, END, branch);
            return true;
        }

        if (next.matchType(GROUP_END) && (next.getPredecessors().size() <= 1)) {
            // Create 'if'
            createIf(basicBlock, next, next, branch);
            return true;
        }

        if (next.matchType(GROUP_SINGLE_SUCCESSOR|TYPE_RETURN|TYPE_RETURN_VALUE|TYPE_THROW) && (next.getPredecessors().size() == 1)) {
            BasicBlock nextLast = next;
            BasicBlock nextNext = next.getNext();
            ControlFlowGraph cfg = next.getControlFlowGraph();
            int lineNumber = cfg.getLineNumber(basicBlock.getFromOffset());
            int maxOffset = branch.getFromOffset();

            if ((maxOffset == 0) || (next.getFromOffset() > branch.getFromOffset())) {
                maxOffset = Integer.MAX_VALUE;
            }

            while ((nextLast != nextNext) && nextNext.matchType(GROUP_SINGLE_SUCCESSOR) && (nextNext.getPredecessors().size() == 1) && (cfg.getLineNumber(nextNext.getFromOffset()) >= lineNumber) && (nextNext.getFromOffset() < maxOffset)) {
                watchdog.check(nextNext, nextNext.getNext());
                nextLast = nextNext;
                nextNext = nextNext.getNext();
            }

            if (nextNext == branch) {
                createIf(basicBlock, next, nextLast, branch);
                return true;
            }

            if (nextNext.matchType(GROUP_END) && (nextNext.getFromOffset() < maxOffset)) {
                createIf(basicBlock, next, nextNext, branch);
                return true;
            }

            if (branch.matchType(GROUP_END)) {
                if ((nextNext.getFromOffset() < maxOffset) && (nextNext.getPredecessors().size() == 1)) {
                    createIf(basicBlock, next, nextNext, branch);
                } else {
                    createIfElse(TYPE_IF_ELSE, basicBlock, next, nextLast, branch, branch, nextNext);
                }
                return true;
            }

            if (branch.matchType(GROUP_SINGLE_SUCCESSOR) && (branch.getPredecessors().size() == 1)) {
                BasicBlock branchLast = branch;
                BasicBlock branchNext = branch.getNext();

                watchdog.clear();

                while ((branchLast != branchNext) && branchNext.matchType(GROUP_SINGLE_SUCCESSOR) && (branchNext.getPredecessors().size() == 1) && (cfg.getLineNumber(branchNext.getFromOffset()) >= lineNumber)) {
                    watchdog.check(branchNext, branchNext.getNext());
                    branchLast = branchNext;
                    branchNext = branchNext.getNext();
                }

                if (nextNext == branchNext) {
                    if (nextLast.matchType(TYPE_GOTO_IN_TERNARY_OPERATOR|TYPE_TERNARY_OPERATOR)) {
                        createIfElse(TYPE_TERNARY_OPERATOR, basicBlock, next, nextLast, branch, branchLast, nextNext);
                        return true;
                    } else {
                        createIfElse(TYPE_IF_ELSE, basicBlock, next, nextLast, branch, branchLast, nextNext);
                        return true;
                    }
                } else {
                    if ((nextNext.getFromOffset() < branch.getFromOffset()) && (nextNext.getPredecessors().size() == 1)) {
                        createIf(basicBlock, next, nextNext, branch);
                        return true;
                    } else if (((nextNext.getFromOffset() > branch.getFromOffset()) && branchNext.matchType(GROUP_END))) {
                        createIfElse(TYPE_IF_ELSE, basicBlock, next, nextLast, branch, branchNext, nextNext);
                        return true;
                    }
                }
            }
        }

        if (branch.matchType(GROUP_SINGLE_SUCCESSOR|TYPE_RETURN|TYPE_RETURN_VALUE|TYPE_THROW) && (branch.getPredecessors().size() == 1)) {
            BasicBlock branchLast = branch;
            BasicBlock branchNext = branch.getNext();

            watchdog.clear();

            while ((branchLast != branchNext) && branchNext.matchType(GROUP_SINGLE_SUCCESSOR) && (branchNext.getPredecessors().size() == 1)) {
                watchdog.check(branchNext, branchNext.getNext());
                branchLast = branchNext;
                branchNext = branchNext.getNext();
            }

            if (branchNext == next) {
                basicBlock.inverseCondition();
                createIf(basicBlock, branch, branchLast, next);
                return true;
            }

            if (branchNext.matchType(GROUP_END) && (branchNext.getPredecessors().size() <= 1)) {
                // Create 'if'
                basicBlock.inverseCondition();
                createIf(basicBlock, branch, branchNext, next);
                return true;
            }
        }

        if (next.matchType(TYPE_RETURN|TYPE_RETURN_VALUE|TYPE_THROW)) {
            // Un-optimize byte code
            next = clone(basicBlock, next);
            // Create 'if'
            createIf(basicBlock, next, next, branch);
            return true;
        }

        if (next.matchType(GROUP_SINGLE_SUCCESSOR)) {
            BasicBlock nextLast = next;
            BasicBlock nextNext = next.getNext();

            watchdog.clear();

            while ((nextLast != nextNext) && nextNext.matchType(GROUP_SINGLE_SUCCESSOR) && (nextNext.getPredecessors().size() == 1)) {
                watchdog.check(nextNext, nextNext.getNext());
                nextLast = nextNext;
                nextNext = nextNext.getNext();
            }

            if (nextNext.matchType(TYPE_RETURN|TYPE_RETURN_VALUE|TYPE_THROW)) {
                createIf(basicBlock, next, nextNext, branch);
                return true;
            }
        }

        return false;
    }

    protected static void createIf(BasicBlock basicBlock, BasicBlock sub, BasicBlock last, BasicBlock next) {
        BasicBlock condition = basicBlock.getControlFlowGraph().newBasicBlock(basicBlock);

        condition.setNext(END);
        condition.setBranch(END);

        int toOffset = last.getToOffset();

        if (toOffset == 0) {
            toOffset = basicBlock.getToOffset();
        }

        // Split sequence
        last.setNext(END);
        next.getPredecessors().remove(last);
        // Create 'if'
        basicBlock.setType(TYPE_IF);
        basicBlock.setToOffset(toOffset);
        basicBlock.setCondition(condition);
        basicBlock.setSub1(sub);
        basicBlock.setSub2(null);
        basicBlock.setNext(next);
    }

    protected static void createIfElse(int type, BasicBlock basicBlock, BasicBlock sub1, BasicBlock last1, BasicBlock sub2, BasicBlock last2, BasicBlock next) {
        BasicBlock condition = basicBlock.getControlFlowGraph().newBasicBlock(basicBlock);

        condition.setNext(END);
        condition.setBranch(END);

        int toOffset = last2.getToOffset();

        if (toOffset == 0) {
            toOffset = last1.getToOffset();

            if (toOffset == 0) {
                toOffset = basicBlock.getToOffset();
            }
        }

        // Split sequences
        last1.setNext(END);
        next.getPredecessors().remove(last1);
        last2.setNext(END);
        next.getPredecessors().remove(last2);
        next.getPredecessors().add(basicBlock);
        // Create 'if-else'
        basicBlock.setType(type);
        basicBlock.setToOffset(toOffset);
        basicBlock.setCondition(condition);
        basicBlock.setSub1(sub1);
        basicBlock.setSub2(sub2);
        basicBlock.setNext(next);
    }

    protected static boolean aggregateConditionalBranches(BasicBlock basicBlock) {
        boolean change = false;

        BasicBlock next = basicBlock.getNext();
        BasicBlock branch = basicBlock.getBranch();

        if ((next.getType() == TYPE_GOTO_IN_TERNARY_OPERATOR) && (next.getPredecessors().size() == 1)) {
            BasicBlock nextNext = next.getNext();

            if (nextNext.matchType(TYPE_CONDITIONAL_BRANCH|TYPE_CONDITION)) {
                if (branch.matchType(TYPE_STATEMENTS|TYPE_GOTO_IN_TERNARY_OPERATOR) && (nextNext == branch.getNext()) && (branch.getPredecessors().size() == 1) && (nextNext.getPredecessors().size() == 2)) {
                    if (ByteCodeUtil.getMinDepth(nextNext) == -1) {
                        updateConditionTernaryOperator(basicBlock, nextNext);
                        return true;
                    }

                    BasicBlock nextNextNext = nextNext.getNext();
                    BasicBlock nextNextBranch = nextNext.getBranch();

                    if ((nextNextNext.getType() == TYPE_GOTO_IN_TERNARY_OPERATOR) && (nextNextNext.getPredecessors().size() == 1)) {
                        BasicBlock nextNextNextNext = nextNextNext.getNext();

                        if (nextNextNextNext.matchType(TYPE_CONDITIONAL_BRANCH|TYPE_CONDITION)) {
                            if (nextNextBranch.matchType(TYPE_STATEMENTS|TYPE_GOTO_IN_TERNARY_OPERATOR) && (nextNextNextNext == nextNextBranch.getNext()) && (nextNextBranch.getPredecessors().size() == 1) && (nextNextNextNext.getPredecessors().size() == 2)) {
                                if (ByteCodeUtil.getMinDepth(nextNextNextNext) == -2) {
                                    updateCondition(basicBlock, nextNext, nextNextNextNext);
                                    return true;
                                }
                            }
                        }
                    }
                }
                if ((nextNext.getNext() == branch) && checkJdk118TernaryOperatorPattern(next, nextNext, 153)) { // IFEQ
                    convertConditionalBranchToGotoInTernaryOperator(basicBlock, next, nextNext);
                    return true;
                }
                if ((nextNext.getBranch() == branch) && checkJdk118TernaryOperatorPattern(next, nextNext, 154)) { // IFNE
                    convertConditionalBranchToGotoInTernaryOperator(basicBlock, next, nextNext);
                    return true;
                }
                if (nextNext.getPredecessors().size() == 1) {
                    convertGotoInTernaryOperatorToCondition(next, nextNext);
                    return true;
                }
            }
        }

        if (next.matchType(TYPE_CONDITIONAL_BRANCH|GROUP_CONDITION)) {
            // Test line numbers
            int lineNumber1 = basicBlock.getLastLineNumber();
            int lineNumber2 = next.getFirstLineNumber();

            if ((lineNumber2-lineNumber1) <= 1) {
                change = aggregateConditionalBranches(next);

                if (next.matchType(TYPE_CONDITIONAL_BRANCH|GROUP_CONDITION) && (next.getPredecessors().size() == 1)) {
                    // Aggregate conditional branches
                    if (next.getNext() == branch) {
                        updateConditionalBranches(basicBlock, createLeftCondition(basicBlock), TYPE_CONDITION_OR, next);
                        return true;
                    } else if (next.getBranch() == branch) {
                        updateConditionalBranches(basicBlock, createLeftInverseCondition(basicBlock), TYPE_CONDITION_AND, next);
                        return true;
                    } else if (branch.matchType(TYPE_CONDITIONAL_BRANCH|GROUP_CONDITION)) {
                        change = aggregateConditionalBranches(branch);

                        if (branch.matchType(TYPE_CONDITIONAL_BRANCH|GROUP_CONDITION)) {
                            if ((next.getNext() == branch.getNext()) && (next.getBranch() == branch.getBranch())) {
                                updateConditionTernaryOperator2(basicBlock);
                                return true;
                            } else if ((next.getBranch() == branch.getNext()) && (next.getNext() == branch.getBranch())) {
                                updateConditionTernaryOperator2(basicBlock);
                                branch.inverseCondition();
                                return true;
                            }
                        }
                    }
                }
            }
        }

        if (branch.matchType(TYPE_CONDITIONAL_BRANCH|GROUP_CONDITION)) {
            // Test line numbers
            int lineNumber1 = basicBlock.getLastLineNumber();
            int lineNumber2 = branch.getFirstLineNumber();

            if ((lineNumber2-lineNumber1) <= 1) {
                change = aggregateConditionalBranches(branch);

                if (branch.matchType(TYPE_CONDITIONAL_BRANCH|GROUP_CONDITION) && (branch.getPredecessors().size() == 1)) {
                    // Aggregate conditional branches
                    if (branch.getBranch() == next) {
                        updateConditionalBranches(basicBlock, createLeftCondition(basicBlock), TYPE_CONDITION_AND, branch);
                        return true;
                    } else if (branch.getNext() == next) {
                        updateConditionalBranches(basicBlock, createLeftInverseCondition(basicBlock), TYPE_CONDITION_OR, branch);
                        return true;
                    }
                }
            }
        }

        if (basicBlock.getType() == TYPE_CONDITIONAL_BRANCH) {
            basicBlock.setType(TYPE_CONDITION);
            return true;
        }

        return change;
    }

    protected static BasicBlock createLeftCondition(BasicBlock basicBlock) {
        if (basicBlock.getType() == TYPE_CONDITIONAL_BRANCH) {
            return basicBlock.getControlFlowGraph().newBasicBlock(TYPE_CONDITION, basicBlock.getFromOffset(), basicBlock.getToOffset(), false);
        } else {
            BasicBlock left = basicBlock.getControlFlowGraph().newBasicBlock(basicBlock);
            left.inverseCondition();
            return left;
        }
    }

    protected static BasicBlock createLeftInverseCondition(BasicBlock basicBlock) {
        if (basicBlock.getType() == TYPE_CONDITIONAL_BRANCH) {
            return basicBlock.getControlFlowGraph().newBasicBlock(TYPE_CONDITION, basicBlock.getFromOffset(), basicBlock.getToOffset());
        } else {
            return basicBlock.getControlFlowGraph().newBasicBlock(basicBlock);
        }
    }

    protected static void updateConditionalBranches(BasicBlock basicBlock, BasicBlock leftBasicBlock, int operator, BasicBlock subBasicBlock) {
        basicBlock.setType(operator);
        basicBlock.setToOffset(subBasicBlock.getToOffset());
        basicBlock.setNext(subBasicBlock.getNext());
        basicBlock.setBranch(subBasicBlock.getBranch());
        basicBlock.setCondition(END);
        basicBlock.setSub1(leftBasicBlock);
        basicBlock.setSub2(subBasicBlock);

        subBasicBlock.getNext().replace(subBasicBlock, basicBlock);
        subBasicBlock.getBranch().replace(subBasicBlock, basicBlock);
    }

    protected static void updateConditionTernaryOperator(BasicBlock basicBlock, BasicBlock nextNext) {
        int fromOffset =  nextNext.getFromOffset();
        int toOffset = nextNext.getToOffset();
        BasicBlock next = nextNext.getNext();
        BasicBlock branch = nextNext.getBranch();

        if (basicBlock.getType() == TYPE_CONDITIONAL_BRANCH) {
            basicBlock.setType(TYPE_CONDITION);
        }
        if ((nextNext.getType() == TYPE_CONDITION) && !nextNext.mustInverseCondition()) {
            basicBlock.inverseCondition();
        }

        BasicBlock condition = nextNext;

        condition.setType(basicBlock.getType());
        condition.setFromOffset(basicBlock.getFromOffset());
        condition.setToOffset(basicBlock.getToOffset());
        condition.setNext(END);
        condition.setBranch(END);
        condition.setCondition(basicBlock.getCondition());
        condition.setSub1(basicBlock.getSub1());
        condition.setSub2(basicBlock.getSub2());
        condition.getPredecessors().clear();

        basicBlock.setType(TYPE_CONDITION_TERNARY_OPERATOR);
        basicBlock.setFromOffset(fromOffset);
        basicBlock.setToOffset(toOffset);
        basicBlock.setCondition(condition);
        basicBlock.setSub1(basicBlock.getNext());
        basicBlock.setSub2(basicBlock.getBranch());
        basicBlock.setNext(next);
        basicBlock.setBranch(branch);
        basicBlock.getSub1().setNext(END);
        basicBlock.getSub2().setNext(END);

        next.replace(nextNext, basicBlock);
        branch.replace(nextNext, basicBlock);

        basicBlock.getSub1().getPredecessors().clear();
        basicBlock.getSub2().getPredecessors().clear();
    }

    protected static void updateCondition(BasicBlock basicBlock, BasicBlock nextNext, BasicBlock nextNextNextNext) {
        int fromOffset =  nextNextNextNext.getFromOffset();
        int toOffset = nextNextNextNext.getToOffset();
        BasicBlock next = nextNextNextNext.getNext();
        BasicBlock branch = nextNextNextNext.getBranch();

        BasicBlock condition = basicBlock.getControlFlowGraph().newBasicBlock(basicBlock);
        condition.setType(TYPE_CONDITION);

        basicBlock.getNext().setNext(END);
        basicBlock.getNext().getPredecessors().clear();
        basicBlock.getBranch().setNext(END);
        basicBlock.getBranch().getPredecessors().clear();

        nextNextNextNext.setType(TYPE_CONDITION_TERNARY_OPERATOR);
        nextNextNextNext.setFromOffset(condition.getToOffset());
        nextNextNextNext.setToOffset(condition.getToOffset());
        nextNextNextNext.setCondition(condition);
        nextNextNextNext.setSub1(basicBlock.getNext());
        nextNextNextNext.setSub2(basicBlock.getBranch());
        nextNextNextNext.setNext(END);
        nextNextNextNext.setBranch(END);
        condition.setNext(END);
        condition.setBranch(END);

        condition = nextNext.getControlFlowGraph().newBasicBlock(nextNext);
        condition.setType(TYPE_CONDITION);

        nextNext.getNext().setNext(END);
        nextNext.getNext().getPredecessors().clear();
        nextNext.getBranch().setNext(END);
        nextNext.getBranch().getPredecessors().clear();

        nextNext.setType(TYPE_CONDITION_TERNARY_OPERATOR);
        nextNext.setFromOffset(condition.getToOffset());
        nextNext.setToOffset(condition.getToOffset());
        nextNext.setCondition(condition);
        nextNext.setSub1(nextNext.getNext());
        nextNext.setSub2(nextNext.getBranch());
        nextNext.setNext(END);
        nextNext.setBranch(END);
        condition.setNext(END);
        condition.setBranch(END);

        basicBlock.setType(TYPE_CONDITION);
        basicBlock.setFromOffset(fromOffset);
        basicBlock.setToOffset(toOffset);
        basicBlock.setSub1(nextNextNextNext);
        basicBlock.setSub2(nextNext);
        basicBlock.setNext(next);
        basicBlock.setBranch(branch);

        next.replace(nextNextNextNext, basicBlock);
        branch.replace(nextNextNextNext, basicBlock);
    }

    protected static void updateConditionTernaryOperator2(BasicBlock basicBlock) {
        BasicBlock next = basicBlock.getNext();
        BasicBlock branch = basicBlock.getBranch();

        ControlFlowGraph cfg = basicBlock.getControlFlowGraph();
        BasicBlock condition = cfg.newBasicBlock(TYPE_CONDITION, basicBlock.getFromOffset(), basicBlock.getToOffset());

        condition.setNext(END);
        condition.setBranch(END);

        basicBlock.setType(TYPE_CONDITION_TERNARY_OPERATOR);
        basicBlock.setToOffset(basicBlock.getFromOffset());
        basicBlock.setCondition(condition);
        basicBlock.setSub1(next);
        basicBlock.setSub2(branch);
        basicBlock.setNext(next.getNext());
        basicBlock.setBranch(next.getBranch());

        next.getNext().replace(next, basicBlock);
        next.getBranch().replace(next, basicBlock);
        branch.getNext().replace(branch, basicBlock);
        branch.getBranch().replace(branch, basicBlock);

        next.getPredecessors().clear();
        branch.getPredecessors().clear();
    }

    protected static void convertGotoInTernaryOperatorToCondition(BasicBlock basicBlock, BasicBlock next) {
        basicBlock.setType(TYPE_CONDITION);
        basicBlock.setNext(next.getNext());
        basicBlock.setBranch(next.getBranch());

        next.getNext().replace(next, basicBlock);
        next.getBranch().replace(next, basicBlock);

        next.setType(TYPE_DELETED);
    }

    protected static void convertConditionalBranchToGotoInTernaryOperator(BasicBlock basicBlock, BasicBlock next, BasicBlock nextNext) {
        basicBlock.setType(TYPE_GOTO_IN_TERNARY_OPERATOR);
        basicBlock.setNext(nextNext);
        basicBlock.getBranch().getPredecessors().remove(basicBlock);
        basicBlock.setBranch(END);
        basicBlock.setInverseCondition(false);

        nextNext.replace(next, basicBlock);

        next.setType(TYPE_DELETED);
    }

    protected static boolean checkJdk118TernaryOperatorPattern(BasicBlock next, BasicBlock nextNext, int ifByteCode) {
        if ((nextNext.getToOffset() - nextNext.getFromOffset()) == 3) {
            byte[] code = next.getControlFlowGraph().getMethod().<AttributeCode>getAttribute("Code").getCode();
            int nextFromOffset = next.getFromOffset();
            int nextNextFromOffset = nextNext.getFromOffset();
            return (code[nextFromOffset] == 3) &&                                                               // ICONST_0
                    (((code[nextFromOffset + 1] & 255) == 167) || ((code[nextFromOffset + 1] & 255) == 200)) && // GOTO or GOTO_W
                    ((code[nextNextFromOffset] & 255) == ifByteCode) &&                                         // IFEQ or IFNE
                    (nextNextFromOffset + 3 == nextNext.getToOffset());
        }

        return false;
    }

    protected static boolean reduceSwitchDeclaration(BitSet visited, BasicBlock basicBlock, BitSet jsrTargets) {
        SwitchCase defaultSC = null;
        SwitchCase lastSC = null;
        int maxOffset = -1;

        for (SwitchCase switchCase : basicBlock.getSwitchCases()) {
            if (maxOffset < switchCase.getOffset()) {
                maxOffset = switchCase.getOffset();
            }

            if (switchCase.isDefaultCase()) {
                defaultSC = switchCase;
            } else {
                lastSC = switchCase;
            }
        }

        if (lastSC == null) {
            lastSC = defaultSC;
        }

        BasicBlock lastSwitchCaseBasicBlock = null;
        BitSet v = new BitSet();
        HashSet<BasicBlock> ends = new HashSet<>();

        for (SwitchCase switchCase : basicBlock.getSwitchCases()) {
            BasicBlock bb = switchCase.getBasicBlock();

            if (switchCase.getOffset() == maxOffset) {
                lastSwitchCaseBasicBlock = bb;
            } else {
                visit(v, bb, maxOffset, ends);
            }
        }

        BasicBlock end = END;

        for (BasicBlock bb : ends) {
            if ((end == END) || (end.getFromOffset() < bb.getFromOffset())) {
                end = bb;
            }
        }

        if (end == END) {
            if ((lastSC.getBasicBlock() == lastSwitchCaseBasicBlock) && searchLoopStart(basicBlock, maxOffset)) {
                replaceLoopStartWithSwitchBreak(new BitSet(), basicBlock);
                defaultSC.setBasicBlock(end = LOOP_START);
            } else {
                end = lastSwitchCaseBasicBlock;
            }
        } else {
            visit(v, lastSwitchCaseBasicBlock, end.getFromOffset(), ends);
        }

        Set<BasicBlock> endPredecessors = end.getPredecessors();
        Iterator<BasicBlock> endPredecessorIterator = endPredecessors.iterator();

        while (endPredecessorIterator.hasNext()) {
            BasicBlock endPredecessor = endPredecessorIterator.next();

            if (v.get(endPredecessor.getIndex())) {
                endPredecessor.replace(end, SWITCH_BREAK);
                endPredecessorIterator.remove();
            }
        }

        if (defaultSC.getBasicBlock() == end) {
            Iterator<SwitchCase> iterator = basicBlock.getSwitchCases().iterator();
            while (iterator.hasNext()) {
                if (iterator.next().getBasicBlock() == end) {
                    iterator.remove();
                }
            }
        } else {
            for (SwitchCase switchCase : basicBlock.getSwitchCases()) {
                if (switchCase.getBasicBlock() == end) {
                    switchCase.setBasicBlock(SWITCH_BREAK);
                }
            }
        }

        boolean reduced = true;

        for (SwitchCase switchCase : basicBlock.getSwitchCases()) {
            reduced &= reduce(visited, switchCase.getBasicBlock(), jsrTargets);
        }

        for (SwitchCase switchCase : basicBlock.getSwitchCases()) {
            BasicBlock bb = switchCase.getBasicBlock();

            assert bb != end;

            Set<BasicBlock> predecessors = bb.getPredecessors();

            if (predecessors.size() > 1) {
                Iterator<BasicBlock> predecessorIterator = predecessors.iterator();

                while (predecessorIterator.hasNext()) {
                    BasicBlock predecessor = predecessorIterator.next();

                    if (predecessor != basicBlock) {
                        predecessor.replace(bb, END);
                        predecessorIterator.remove();
                    }
                }
            }
        }

        // Change type
        basicBlock.setType(TYPE_SWITCH);
        basicBlock.setNext(end);
        endPredecessors.add(basicBlock);

        return reduced & reduce(visited, basicBlock.getNext(), jsrTargets);
    }

    protected static boolean searchLoopStart(BasicBlock basicBlock, int maxOffset) {
        WatchDog watchdog = new WatchDog();

        for (SwitchCase switchCase : basicBlock.getSwitchCases()) {
            BasicBlock bb = switchCase.getBasicBlock();

            watchdog.clear();
            
            while (bb.getFromOffset() < maxOffset) {
                if (bb == LOOP_START) {
                    return true;
                }

                if (bb.matchType(GROUP_END|GROUP_CONDITION)) {
                    break;
                }

                BasicBlock next = null;

                if (bb.matchType(GROUP_SINGLE_SUCCESSOR)) {
                    next = bb.getNext();
                } else if (bb.getType() == TYPE_CONDITIONAL_BRANCH) {
                    next = bb.getBranch();
                } else if (bb.getType() == TYPE_SWITCH_DECLARATION) {
                    int max = bb.getFromOffset();

                    for (SwitchCase sc : bb.getSwitchCases()) {
                        if (max < sc.getBasicBlock().getFromOffset()) {
                            next = sc.getBasicBlock();
                            max = next.getFromOffset();
                        }
                    }
                }

                if (bb == next) {
                    break;
                }

                watchdog.check(bb, next);
                bb = next;
            }
        }

        return false;
    }

    protected static boolean reduceTryDeclaration(BitSet visited, BasicBlock basicBlock, BitSet jsrTargets) {
        boolean reduced = true;
        BasicBlock finallyBB = null;

        for (BasicBlock.ExceptionHandler exceptionHandler : basicBlock.getExceptionHandlers()) {
            if (exceptionHandler.getInternalThrowableName() == null) {
                reduced = reduce(visited, exceptionHandler.getBasicBlock(), jsrTargets);
                finallyBB = exceptionHandler.getBasicBlock();
                break;
            }
        }

        BasicBlock jsrTarget = searchJsrTarget(basicBlock, jsrTargets);

        reduced &= reduce(visited, basicBlock.getNext(), jsrTargets);

        BasicBlock tryBB = basicBlock.getNext();

        if (tryBB.matchType(GROUP_SYNTHETIC)) {
            return false;
        }

        int maxOffset = basicBlock.getFromOffset();
        boolean tryWithResourcesFlag = true;
        BasicBlock tryWithResourcesBB = null;

        for (BasicBlock.ExceptionHandler exceptionHandler : basicBlock.getExceptionHandlers()) {
            if (exceptionHandler.getInternalThrowableName() != null) {
                reduced &= reduce(visited, exceptionHandler.getBasicBlock(), jsrTargets);
            }

            BasicBlock bb = exceptionHandler.getBasicBlock();

            if (bb.matchType(GROUP_SYNTHETIC)) {
                return false;
            }

            if (maxOffset < bb.getFromOffset()) {
                maxOffset = bb.getFromOffset();
            }

            if (tryWithResourcesFlag) {
                Set<BasicBlock> predecessors = bb.getPredecessors();

                if (predecessors.size() == 1) {
                    tryWithResourcesFlag = false;
                } else {
                    assert predecessors.size() == 2;

                    if (tryWithResourcesBB == null) {
                        for (BasicBlock predecessor : predecessors) {
                            if (predecessor != basicBlock) {
                                assert predecessor.getType() == TYPE_TRY_DECLARATION;
                                tryWithResourcesBB = predecessor;
                                break;
                            }
                        }
                    } else if (!predecessors.contains(tryWithResourcesBB)) {
                        tryWithResourcesFlag = false;
                    }
                }
            }
        }

        if (tryWithResourcesFlag) {
            // One of 'try-with-resources' patterns
            for (BasicBlock.ExceptionHandler exceptionHandler : basicBlock.getExceptionHandlers()) {
                exceptionHandler.getBasicBlock().getPredecessors().remove(basicBlock);
            }
            for (BasicBlock predecessor : basicBlock.getPredecessors()) {
                predecessor.replace(basicBlock, tryBB);
                tryBB.replace(basicBlock, predecessor);
            }
            basicBlock.setType(TYPE_DELETED);
        } else if (reduced) {
            BasicBlock end = searchEndBlock(basicBlock, maxOffset);

            updateBlock(tryBB, end, maxOffset);

            if ((finallyBB != null) && (basicBlock.getExceptionHandlers().size() == 1) && (tryBB.getType() == TYPE_TRY) && (tryBB.getNext() == END) && (basicBlock.getFromOffset() == tryBB.getFromOffset()) && !containsFinally(tryBB)) {
                // Merge inner try
                basicBlock.getExceptionHandlers().addAll(0, tryBB.getExceptionHandlers());

                for (BasicBlock.ExceptionHandler exceptionHandler : tryBB.getExceptionHandlers()) {
                    Set<BasicBlock> predecessors = exceptionHandler.getBasicBlock().getPredecessors();
                    predecessors.clear();
                    predecessors.add(basicBlock);
                }

                tryBB.setType(TYPE_DELETED);
                tryBB = tryBB.getSub1();
                Set<BasicBlock> predecessors = tryBB.getPredecessors();
                predecessors.clear();
                predecessors.add(basicBlock);
            }

            // Update blocks
            int toOffset = maxOffset;

            for (BasicBlock.ExceptionHandler exceptionHandler : basicBlock.getExceptionHandlers()) {
                BasicBlock bb = exceptionHandler.getBasicBlock();

                if (bb == end) {
                    exceptionHandler.setBasicBlock(END);
                } else {
                    int offset = (bb.getFromOffset() == maxOffset) ? end.getFromOffset() : maxOffset;

                    if (offset == 0) {
                        offset = Integer.MAX_VALUE;
                    }

                    BasicBlock last = updateBlock(bb, end, offset);

                    if (toOffset < last.getToOffset()) {
                        toOffset = last.getToOffset();
                    }
                }
            }

            basicBlock.setSub1(tryBB);
            basicBlock.setNext(end);
            end.getPredecessors().add(basicBlock);

            if (jsrTarget == null) {
                // Change type
                if ((finallyBB != null) && checkEclipseFinallyPattern(basicBlock, finallyBB, maxOffset)) {
                    basicBlock.setType(TYPE_TRY_ECLIPSE);
                } else {
                    basicBlock.setType(TYPE_TRY);
                }
            } else {
                // Change type
                basicBlock.setType(TYPE_TRY_JSR);
                // Merge 1.1 to 1.4 sub try block
                removeJsrAndMergeSubTry(basicBlock);
            }

            basicBlock.setToOffset(toOffset);
        }

        return reduced;
    }

    protected static boolean containsFinally(BasicBlock basicBlock) {
        for (ExceptionHandler exceptionHandler : basicBlock.getExceptionHandlers()) {
            if (exceptionHandler.getInternalThrowableName() == null) {
                return true;
            }
        }

        return false;
    }

    protected static boolean checkEclipseFinallyPattern(BasicBlock basicBlock, BasicBlock finallyBB, int maxOffset) {
        int nextOpcode = ByteCodeUtil.searchNextOpcode(basicBlock, maxOffset);

        if ((nextOpcode == 0)   ||
            (nextOpcode == 167) || // GOTO
            (nextOpcode == 200)) { // GOTO_W
            return true;
        }

        BasicBlock next = basicBlock.getNext();

        if (!next.matchType(GROUP_END) && (finallyBB.getFromOffset() < next.getFromOffset())) {
            ControlFlowGraph cfg = finallyBB.getControlFlowGraph();
            int toLineNumber = cfg.getLineNumber(finallyBB.getToOffset()-1);
            int fromLineNumber = cfg.getLineNumber(next.getFromOffset());

            if (fromLineNumber < toLineNumber) {
                return true;
            }
        }

        return false;
    }

    protected static BasicBlock searchJsrTarget(BasicBlock basicBlock, BitSet jsrTargets) {
        for (BasicBlock.ExceptionHandler exceptionHandler : basicBlock.getExceptionHandlers()) {
            if (exceptionHandler.getInternalThrowableName() == null) {
                BasicBlock bb = exceptionHandler.getBasicBlock();

                if (bb.getType() == TYPE_STATEMENTS) {
                    bb = bb.getNext();

                    if ((bb.getType() == TYPE_JSR) && (bb.getNext().getType() == TYPE_THROW)) {
                        // Java 1.1 to 1.4 finally pattern found
                        BasicBlock jsrTarget = bb.getBranch();
                        jsrTargets.set(jsrTarget.getIndex());
                        return jsrTarget;
                    }
                }
            }
        }

        return null;
    }

    protected static BasicBlock searchEndBlock(BasicBlock basicBlock, int maxOffset) {
        BasicBlock end = null;
        BasicBlock last = splitSequence(basicBlock.getNext(), maxOffset);

        if (!last.matchType(GROUP_END)) {
            BasicBlock next = last.getNext();

            if ((next.getFromOffset() >= maxOffset) || (!next.matchType(TYPE_END|TYPE_RETURN|TYPE_SWITCH_BREAK|TYPE_LOOP_START|TYPE_LOOP_CONTINUE|TYPE_LOOP_END) && (next.getToOffset() < basicBlock.getFromOffset()))) {
                return next;
            }

            end = next;
        }

        for (BasicBlock.ExceptionHandler exceptionHandler : basicBlock.getExceptionHandlers()) {
            BasicBlock bb = exceptionHandler.getBasicBlock();

            if (bb.getFromOffset() < maxOffset) {
                last = splitSequence(bb, maxOffset);

                if (!last.matchType(GROUP_END)) {
                    BasicBlock next = last.getNext();

                    if ((next.getFromOffset() >= maxOffset) || (!next.matchType(TYPE_END | TYPE_RETURN | TYPE_SWITCH_BREAK | TYPE_LOOP_START | TYPE_LOOP_CONTINUE | TYPE_LOOP_END) && (next.getToOffset() < basicBlock.getFromOffset()))) {
                        return next;
                    }

                    if (end == null) {
                        end = next;
                    } else if (end != next) {
                        end = END;
                    }
                }
            } else {
                // Last handler block
                ControlFlowGraph cfg = bb.getControlFlowGraph();
                int lineNumber = cfg.getLineNumber(bb.getFromOffset());
                WatchDog watchdog = new WatchDog();
                BasicBlock next = bb.getNext();

                last = bb;

                while ((last != next) && last.matchType(GROUP_SINGLE_SUCCESSOR) && (next.getPredecessors().size() == 1) && (lineNumber <= cfg.getLineNumber(next.getFromOffset()))) {
                    watchdog.check(next, next.getNext());
                    last = next;
                    next = next.getNext();
                }

                if (!last.matchType(GROUP_END)) {
                    if ((last != next) && ((next.getPredecessors().size() > 1) || !next.matchType(GROUP_END))) {
                        return next;
                    }

                    if ((end != next) && (exceptionHandler.getInternalThrowableName() != null)) {
                        end = END;
                    }
                }
            }
        }

        if ((end != null) && end.matchType(TYPE_SWITCH_BREAK|TYPE_LOOP_START|TYPE_LOOP_CONTINUE|TYPE_LOOP_END)) {
            return end;
        }

        return END;
    }

    protected static BasicBlock splitSequence(BasicBlock basicBlock, int maxOffset) {
        BasicBlock next = basicBlock.getNext();
        WatchDog watchdog = new WatchDog();

        while ((next.getFromOffset() < maxOffset) && next.matchType(GROUP_SINGLE_SUCCESSOR)) {
            watchdog.check(next, next.getNext());
            basicBlock = next;
            next = next.getNext();
        }

        if ((basicBlock.getToOffset() > maxOffset) && (basicBlock.getType() == TYPE_TRY)) {
            // Split last try block
            List<ExceptionHandler> exceptionHandlers = basicBlock.getExceptionHandlers();
            BasicBlock bb = exceptionHandlers.get(exceptionHandlers.size() - 1).getBasicBlock();
            BasicBlock last = splitSequence(bb, maxOffset);

            next = last.getNext();
            last.setNext(END);

            basicBlock.setToOffset(last.getToOffset());
            basicBlock.setNext(next);

            next.getPredecessors().remove(last);
            next.getPredecessors().add(basicBlock);
        }

        return basicBlock;
    }

    protected static BasicBlock updateBlock(BasicBlock basicBlock, BasicBlock end, int maxOffset) {
        WatchDog watchdog = new WatchDog();

        while (basicBlock.matchType(GROUP_SINGLE_SUCCESSOR)) {
            watchdog.check(basicBlock, basicBlock.getNext());
            BasicBlock next = basicBlock.getNext();

            if ((next == end) || (next.getFromOffset() > maxOffset)) {
                next.getPredecessors().remove(basicBlock);
                basicBlock.setNext(END);
                break;
            }

            basicBlock = next;
        }

        return basicBlock;
    }

    protected static void removeJsrAndMergeSubTry(BasicBlock basicBlock) {
        if (basicBlock.getExceptionHandlers().size() == 1) {
            BasicBlock subTry = basicBlock.getSub1();

            if (subTry.matchType(TYPE_TRY|TYPE_TRY_JSR|TYPE_TRY_ECLIPSE)) {
                for (BasicBlock.ExceptionHandler exceptionHandler : subTry.getExceptionHandlers()) {
                    if (exceptionHandler.getInternalThrowableName() == null)
                        return;
                }

                // Append 'catch' handlers
                for (BasicBlock.ExceptionHandler exceptionHandler : subTry.getExceptionHandlers()) {
                    BasicBlock bb = exceptionHandler.getBasicBlock();
                    basicBlock.addExceptionHandler(exceptionHandler.getInternalThrowableName(), bb);
                    bb.replace(subTry, basicBlock);
                }

                // Move 'try' clause to parent 'try' block
                basicBlock.setSub1(subTry.getSub1());
                subTry.getSub1().replace(subTry, basicBlock);
            }
        }
    }

    protected static boolean reduceJsr(BitSet visited, BasicBlock basicBlock, BitSet jsrTargets) {
        BasicBlock branch = basicBlock.getBranch();
        boolean reduced = reduce(visited, basicBlock.getNext(), jsrTargets) & reduce(visited, branch, jsrTargets);

        if ((branch.getIndex() >= 0) && jsrTargets.get(branch.getIndex())) {
            // Reduce JSR
            int delta = basicBlock.getToOffset() - basicBlock.getFromOffset();

            if (delta > 3) {
                int opcode = ByteCodeUtil.getLastOpcode(basicBlock);

                if (opcode == 168) { // JSR
                    basicBlock.setType(TYPE_STATEMENTS);
                    basicBlock.setToOffset(basicBlock.getToOffset() - 3);
                    branch.getPredecessors().remove(basicBlock);
                    return true;
                } else if (delta > 5) { // JSR_W
                    basicBlock.setType(TYPE_STATEMENTS);
                    basicBlock.setToOffset(basicBlock.getToOffset() - 5);
                    branch.getPredecessors().remove(basicBlock);
                    return true;
                }
            }

            // Delete JSR
            basicBlock.setType(TYPE_DELETED);
            branch.getPredecessors().remove(basicBlock);
            Set<BasicBlock> nextPredecessors = basicBlock.getNext().getPredecessors();
            nextPredecessors.remove(basicBlock);

            for (BasicBlock predecessor : basicBlock.getPredecessors()) {
                predecessor.replace(basicBlock, basicBlock.getNext());
                nextPredecessors.add(predecessor);
            }

            return true;
        }

        if (basicBlock.getBranch().getPredecessors().size() > 1) {
            // Aggregate JSR
            BasicBlock next = basicBlock.getNext();
            Iterator<BasicBlock> iterator = basicBlock.getBranch().getPredecessors().iterator();

            while (iterator.hasNext()) {
                BasicBlock predecessor = iterator.next();

                if ((predecessor != basicBlock) && (predecessor.getType() == TYPE_JSR) && (predecessor.getNext() == next)) {
                    for (BasicBlock predecessorPredecessor : predecessor.getPredecessors()) {
                        predecessorPredecessor.replace(predecessor, basicBlock);
                        basicBlock.getPredecessors().add(predecessorPredecessor);
                    }
                    next.getPredecessors().remove(predecessor);
                    iterator.remove();
                    reduced = true;
                }
            }
        }

        return reduced;
    }

    protected static boolean reduceLoop(BitSet visited, BasicBlock basicBlock, BitSet jsrTargets) {
        Object clone = visited.clone();
        boolean reduced = reduce(visited, basicBlock.getSub1(), jsrTargets);

        if (reduced == false) {
            BitSet visitedMembers = new BitSet();
            BasicBlock updateBasicBlock = searchUpdateBlockAndCreateContinueLoop(visitedMembers, basicBlock.getSub1());

            visited = (BitSet)((BitSet)clone).clone();
            reduced = reduce(visited, basicBlock.getSub1(), jsrTargets);

            if (updateBasicBlock != null) {
                removeLastContinueLoop(basicBlock.getSub1().getSub1());

                BasicBlock ifBasicBlock = basicBlock.getControlFlowGraph().newBasicBlock(TYPE_IF, basicBlock.getSub1().getFromOffset(), basicBlock.getToOffset());

                ifBasicBlock.setCondition(END);
                ifBasicBlock.setSub1(basicBlock.getSub1());
                ifBasicBlock.setNext(updateBasicBlock);
                updateBasicBlock.getPredecessors().add(ifBasicBlock);
                basicBlock.setSub1(ifBasicBlock);
            }

            if (reduced == false) {
                visitedMembers.clear();

                BasicBlock conditionalBranch = getLastConditionalBranch(visitedMembers, basicBlock.getSub1());

                if ((conditionalBranch != null) && (conditionalBranch.getNext() == LOOP_START)) {
                    visitedMembers.clear();
                    visitedMembers.set(conditionalBranch.getIndex());
                    changeEndLoopToJump(visitedMembers, basicBlock.getNext(), basicBlock.getSub1());

                    BasicBlock newLoopBB = basicBlock.getControlFlowGraph().newBasicBlock(basicBlock);
                    Set<BasicBlock> predecessors = conditionalBranch.getPredecessors();

                    for (BasicBlock predecessor : predecessors) {
                        predecessor.replace(conditionalBranch, LOOP_END);
                    }

                    newLoopBB.setNext(conditionalBranch);
                    predecessors.clear();
                    predecessors.add(newLoopBB);
                    basicBlock.setSub1(newLoopBB);

                    visitedMembers.clear();
                    reduced = reduce(visitedMembers, newLoopBB, jsrTargets);
                }
            }
        }

        return reduced & reduce(visited, basicBlock.getNext(), jsrTargets);
    }

    protected static void removeLastContinueLoop(BasicBlock basicBlock) {
        BitSet visited = new BitSet();
        BasicBlock next = basicBlock.getNext();

        while (!next.matchType(GROUP_END) && (visited.get(next.getIndex()) == false)) {
            visited.set(next.getIndex());
            basicBlock = next;
            next = basicBlock.getNext();
        }

        if (next == LOOP_CONTINUE) {
            basicBlock.setNext(END);
        }
    }

    protected static BasicBlock getLastConditionalBranch(BitSet visited, BasicBlock basicBlock) {
        if (!basicBlock.matchType(GROUP_END) && (visited.get(basicBlock.getIndex()) == false)) {
            visited.set(basicBlock.getIndex());

            switch (basicBlock.getType()) {
                case TYPE_START:
                case TYPE_STATEMENTS:
                case TYPE_SWITCH_DECLARATION:
                case TYPE_TRY_DECLARATION:
                case TYPE_JSR:
                case TYPE_LOOP:
                case TYPE_IF_ELSE:
                case TYPE_SWITCH:
                case TYPE_TRY:
                case TYPE_TRY_JSR:
                case TYPE_TRY_ECLIPSE:
                    return getLastConditionalBranch(visited, basicBlock.getNext());
                case TYPE_IF:
                case TYPE_CONDITIONAL_BRANCH:
                case TYPE_CONDITION:
                case TYPE_CONDITION_OR:
                case TYPE_CONDITION_AND:
                    BasicBlock bb = getLastConditionalBranch(visited, basicBlock.getBranch());

                    if (bb != null) return bb;

                    bb = getLastConditionalBranch(visited, basicBlock.getNext());

                    if (bb != null) return bb;

                    return basicBlock;
            }
        }

        return null;
    }

    protected static void visit(BitSet visited, BasicBlock basicBlock, int maxOffset, HashSet<BasicBlock> ends) {
        if (basicBlock.getFromOffset() >= maxOffset) {
            ends.add(basicBlock);
        } else if ((basicBlock.getIndex() >= 0) && (visited.get(basicBlock.getIndex()) == false)) {
            visited.set(basicBlock.getIndex());

            switch (basicBlock.getType()) {
                case TYPE_CONDITIONAL_BRANCH:
                case TYPE_JSR:
                case TYPE_CONDITION:
                    visit(visited, basicBlock.getBranch(), maxOffset, ends);
                case TYPE_START:
                case TYPE_STATEMENTS:
                case TYPE_GOTO:
                case TYPE_GOTO_IN_TERNARY_OPERATOR:
                case TYPE_LOOP:
                    visit(visited, basicBlock.getNext(), maxOffset, ends);
                    break;
                case TYPE_TRY:
                case TYPE_TRY_JSR:
                case TYPE_TRY_ECLIPSE:
                    visit(visited, basicBlock.getSub1(), maxOffset, ends);
                case TYPE_TRY_DECLARATION:
                    for (BasicBlock.ExceptionHandler exceptionHandler : basicBlock.getExceptionHandlers()) {
                        visit(visited, exceptionHandler.getBasicBlock(), maxOffset, ends);
                    }
                    visit(visited, basicBlock.getNext(), maxOffset, ends);
                    break;
                case TYPE_IF_ELSE:
                case TYPE_TERNARY_OPERATOR:
                    visit(visited, basicBlock.getSub2(), maxOffset, ends);
                case TYPE_IF:
                    visit(visited, basicBlock.getSub1(), maxOffset, ends);
                    visit(visited, basicBlock.getNext(), maxOffset, ends);
                    break;
                case TYPE_CONDITION_OR:
                case TYPE_CONDITION_AND:
                    visit(visited, basicBlock.getSub1(), maxOffset, ends);
                    visit(visited, basicBlock.getSub2(), maxOffset, ends);
                    break;
                case TYPE_SWITCH:
                    visit(visited, basicBlock.getNext(), maxOffset, ends);
                case TYPE_SWITCH_DECLARATION:
                    for (SwitchCase switchCase : basicBlock.getSwitchCases()) {
                        visit(visited, switchCase.getBasicBlock(), maxOffset, ends);
                    }
                    break;
            }
        }
    }

    protected static void replaceLoopStartWithSwitchBreak(BitSet visited, BasicBlock basicBlock) {
        if (!basicBlock.matchType(GROUP_END) && (visited.get(basicBlock.getIndex()) == false)) {
            visited.set(basicBlock.getIndex());
            basicBlock.replace(LOOP_START, SWITCH_BREAK);

            switch (basicBlock.getType()) {
                case TYPE_CONDITIONAL_BRANCH:
                case TYPE_JSR:
                case TYPE_CONDITION:
                    replaceLoopStartWithSwitchBreak(visited, basicBlock.getBranch());
                case TYPE_START:
                case TYPE_STATEMENTS:
                case TYPE_GOTO:
                case TYPE_GOTO_IN_TERNARY_OPERATOR:
                case TYPE_LOOP:
                    replaceLoopStartWithSwitchBreak(visited, basicBlock.getNext());
                    break;
                case TYPE_TRY:
                case TYPE_TRY_JSR:
                case TYPE_TRY_ECLIPSE:
                    replaceLoopStartWithSwitchBreak(visited, basicBlock.getSub1());
                case TYPE_TRY_DECLARATION:
                    for (BasicBlock.ExceptionHandler exceptionHandler : basicBlock.getExceptionHandlers()) {
                        replaceLoopStartWithSwitchBreak(visited, exceptionHandler.getBasicBlock());
                    }
                    break;
                case TYPE_IF_ELSE:
                case TYPE_TERNARY_OPERATOR:
                    replaceLoopStartWithSwitchBreak(visited, basicBlock.getSub2());
                case TYPE_IF:
                    replaceLoopStartWithSwitchBreak(visited, basicBlock.getSub1());
                    replaceLoopStartWithSwitchBreak(visited, basicBlock.getNext());
                    break;
                case TYPE_CONDITION_OR:
                case TYPE_CONDITION_AND:
                    replaceLoopStartWithSwitchBreak(visited, basicBlock.getSub1());
                    replaceLoopStartWithSwitchBreak(visited, basicBlock.getSub2());
                    break;
                case TYPE_SWITCH:
                    replaceLoopStartWithSwitchBreak(visited, basicBlock.getNext());
                case TYPE_SWITCH_DECLARATION:
                    for (SwitchCase switchCase : basicBlock.getSwitchCases()) {
                        replaceLoopStartWithSwitchBreak(visited, switchCase.getBasicBlock());
                    }
                    break;
            }
        }
    }

    protected static BasicBlock searchUpdateBlockAndCreateContinueLoop(BitSet visited, BasicBlock basicBlock) {
        BasicBlock updateBasicBlock = null;

        if (!basicBlock.matchType(GROUP_END) && (visited.get(basicBlock.getIndex()) == false)) {
            visited.set(basicBlock.getIndex());

            switch (basicBlock.getType()) {
                case TYPE_CONDITIONAL_BRANCH:
                case TYPE_JSR:
                case TYPE_CONDITION:
                case TYPE_CONDITION_TERNARY_OPERATOR:
                    updateBasicBlock = searchUpdateBlockAndCreateContinueLoop(visited, basicBlock, basicBlock.getBranch());
                case TYPE_START:
                case TYPE_STATEMENTS:
                case TYPE_GOTO:
                case TYPE_GOTO_IN_TERNARY_OPERATOR:
                case TYPE_LOOP:
                    if (updateBasicBlock == null) {
                        updateBasicBlock = searchUpdateBlockAndCreateContinueLoop(visited, basicBlock, basicBlock.getNext());
                    }
                    break;
                case TYPE_TRY:
                case TYPE_TRY_JSR:
                case TYPE_TRY_ECLIPSE:
                    updateBasicBlock = searchUpdateBlockAndCreateContinueLoop(visited, basicBlock, basicBlock.getSub1());
                case TYPE_TRY_DECLARATION:
                    for (BasicBlock.ExceptionHandler exceptionHandler : basicBlock.getExceptionHandlers()) {
                        if (updateBasicBlock == null) {
                            updateBasicBlock = searchUpdateBlockAndCreateContinueLoop(visited, basicBlock, exceptionHandler.getBasicBlock());
                        }
                    }
                    if (updateBasicBlock == null) {
                        updateBasicBlock = searchUpdateBlockAndCreateContinueLoop(visited, basicBlock, basicBlock.getNext());
                    }
                    break;
                case TYPE_IF_ELSE:
                case TYPE_TERNARY_OPERATOR:
                    updateBasicBlock = searchUpdateBlockAndCreateContinueLoop(visited, basicBlock, basicBlock.getSub2());
                case TYPE_IF:
                    if (updateBasicBlock == null) {
                        updateBasicBlock = searchUpdateBlockAndCreateContinueLoop(visited, basicBlock, basicBlock.getSub1());
                    }
                    if (updateBasicBlock == null) {
                        updateBasicBlock = searchUpdateBlockAndCreateContinueLoop(visited, basicBlock, basicBlock.getNext());
                    }
                    break;
                case TYPE_CONDITION_OR:
                case TYPE_CONDITION_AND:
                    updateBasicBlock = searchUpdateBlockAndCreateContinueLoop(visited, basicBlock, basicBlock.getSub1());
                    if (updateBasicBlock == null) {
                        updateBasicBlock = searchUpdateBlockAndCreateContinueLoop(visited, basicBlock, basicBlock.getSub2());
                    }
                    break;
                case TYPE_SWITCH:
                    updateBasicBlock = searchUpdateBlockAndCreateContinueLoop(visited, basicBlock, basicBlock.getNext());
                case TYPE_SWITCH_DECLARATION:
                    for (SwitchCase switchCase : basicBlock.getSwitchCases()) {
                        if (updateBasicBlock == null) {
                            updateBasicBlock = searchUpdateBlockAndCreateContinueLoop(visited, basicBlock, switchCase.getBasicBlock());
                        }
                    }
                    break;
            }
        }

        return updateBasicBlock;
    }

    protected static BasicBlock searchUpdateBlockAndCreateContinueLoop(BitSet visited, BasicBlock basicBlock, BasicBlock subBasicBlock) {
        if (subBasicBlock != null) {
            if (basicBlock.getFromOffset() < subBasicBlock.getFromOffset()) {

                if (basicBlock.getFirstLineNumber() == Expression.UNKNOWN_LINE_NUMBER) {
                    if (subBasicBlock.matchType(GROUP_SINGLE_SUCCESSOR) && (subBasicBlock.getNext().getType() == TYPE_LOOP_START)) {
                        int stackDepth = ByteCodeUtil.evalStackDepth(subBasicBlock);

                        while (stackDepth != 0) {
                            Set<BasicBlock> predecessors = subBasicBlock.getPredecessors();
                            if (predecessors.size() != 1) {
                                break;
                            }
                            stackDepth += ByteCodeUtil.evalStackDepth(subBasicBlock = predecessors.iterator().next());
                        }

                        removePredecessors(subBasicBlock);
                        return subBasicBlock;
                    }
                } else if (basicBlock.getFirstLineNumber() > subBasicBlock.getFirstLineNumber()) {
                    removePredecessors(subBasicBlock);
                    return subBasicBlock;
                }
            }

            return searchUpdateBlockAndCreateContinueLoop(visited, subBasicBlock);
        }

        return null;
    }

    protected static void removePredecessors(BasicBlock basicBlock) {
        Set<BasicBlock> predecessors = basicBlock.getPredecessors();
        Iterator<BasicBlock> iterator = predecessors.iterator();

        while (iterator.hasNext()) {
            iterator.next().replace(basicBlock, LOOP_CONTINUE);
        }

        predecessors.clear();
    }

    protected static void changeEndLoopToJump(BitSet visited, BasicBlock target, BasicBlock basicBlock) {
        if (!basicBlock.matchType(GROUP_END) && (visited.get(basicBlock.getIndex()) == false)) {
            visited.set(basicBlock.getIndex());

            switch (basicBlock.getType()) {
                case TYPE_CONDITIONAL_BRANCH:
                case TYPE_JSR:
                case TYPE_CONDITION:
                    if (basicBlock.getBranch() == LOOP_END) {
                        basicBlock.setBranch(newJumpBasicBlock(basicBlock, target));
                    } else {
                        changeEndLoopToJump(visited, target, basicBlock.getBranch());
                    }
                case TYPE_START:
                case TYPE_STATEMENTS:
                case TYPE_GOTO:
                case TYPE_GOTO_IN_TERNARY_OPERATOR:
                case TYPE_LOOP:
                    if (basicBlock.getNext() == LOOP_END) {
                        basicBlock.setNext(newJumpBasicBlock(basicBlock, target));
                    } else {
                        changeEndLoopToJump(visited, target, basicBlock.getNext());
                    }
                    break;
                case TYPE_TRY:
                case TYPE_TRY_JSR:
                case TYPE_TRY_ECLIPSE:
                    if (basicBlock.getSub1() == LOOP_END) {
                        basicBlock.setSub1(newJumpBasicBlock(basicBlock, target));
                    } else {
                        changeEndLoopToJump(visited, target, basicBlock.getSub1());
                    }
                case TYPE_TRY_DECLARATION:
                    for (BasicBlock.ExceptionHandler exceptionHandler : basicBlock.getExceptionHandlers()) {
                        if (exceptionHandler.getBasicBlock() == LOOP_END) {
                            exceptionHandler.setBasicBlock(newJumpBasicBlock(basicBlock, target));
                        } else {
                            changeEndLoopToJump(visited, target, exceptionHandler.getBasicBlock());
                        }
                    }
                    break;
                case TYPE_IF_ELSE:
                case TYPE_TERNARY_OPERATOR:
                    if (basicBlock.getSub2() == LOOP_END) {
                        basicBlock.setSub2(newJumpBasicBlock(basicBlock, target));
                    } else {
                        changeEndLoopToJump(visited, target, basicBlock.getSub2());
                    }
                case TYPE_IF:
                    if (basicBlock.getSub1() == LOOP_END) {
                        basicBlock.setSub1(newJumpBasicBlock(basicBlock, target));
                    } else {
                        changeEndLoopToJump(visited, target, basicBlock.getSub1());
                    }
                    if (basicBlock.getNext() == LOOP_END) {
                        basicBlock.setNext(newJumpBasicBlock(basicBlock, target));
                    } else {
                        changeEndLoopToJump(visited, target, basicBlock.getNext());
                    }
                    break;
                case TYPE_CONDITION_OR:
                case TYPE_CONDITION_AND:
                    if (basicBlock.getSub1() == LOOP_END) {
                        basicBlock.setSub1(newJumpBasicBlock(basicBlock, target));
                    } else {
                        changeEndLoopToJump(visited, target, basicBlock.getSub1());
                    }
                    if (basicBlock.getSub2() == LOOP_END) {
                        basicBlock.setSub2(newJumpBasicBlock(basicBlock, target));
                    } else {
                        changeEndLoopToJump(visited, target, basicBlock.getSub2());
                    }
                    break;
                case TYPE_SWITCH:
                    if (basicBlock.getNext() == LOOP_END) {
                        basicBlock.setNext(newJumpBasicBlock(basicBlock, target));
                    } else {
                        changeEndLoopToJump(visited, target, basicBlock.getNext());
                    }
                case TYPE_SWITCH_DECLARATION:
                    for (SwitchCase switchCase : basicBlock.getSwitchCases()) {
                        if (switchCase.getBasicBlock() == LOOP_END) {
                            switchCase.setBasicBlock(newJumpBasicBlock(basicBlock, target));
                        } else {
                            changeEndLoopToJump(visited, target, switchCase.getBasicBlock());
                        }
                    }
                    break;
            }
        }
    }

    protected static BasicBlock newJumpBasicBlock(BasicBlock bb, BasicBlock target) {
        HashSet<BasicBlock> predecessors = new HashSet<>();

        predecessors.add(bb);
        target.getPredecessors().remove(bb);

        return bb.getControlFlowGraph().newBasicBlock(TYPE_JUMP, bb.getFromOffset(), target.getFromOffset(), predecessors);
    }

    protected static BasicBlock clone(BasicBlock bb, BasicBlock next) {
        BasicBlock clone = next.getControlFlowGraph().newBasicBlock(next.getType(), next.getFromOffset(), next.getToOffset());
        clone.setNext(END);
        clone.getPredecessors().add(bb);
        next.getPredecessors().remove(bb);
        bb.setNext(clone);
        return clone;
    }
}
