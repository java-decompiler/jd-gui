/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.util;

import org.jd.core.v1.service.converter.classfiletojavasyntax.model.cfg.BasicBlock;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.cfg.ControlFlowGraph;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.cfg.Loop;
import org.jd.core.v1.util.DefaultList;

import java.util.*;

import static org.jd.core.v1.service.converter.classfiletojavasyntax.model.cfg.BasicBlock.*;

public class ControlFlowGraphLoopReducer {
    protected static final LoopComparator LOOP_COMPARATOR = new LoopComparator();

    public static BitSet[] buildDominatorIndexes(ControlFlowGraph cfg) {
        List<BasicBlock> list = cfg.getBasicBlocks();
        int length = list.size();
        BitSet[] arrayOfDominatorIndexes = new BitSet[length];

        BitSet initial = new BitSet(length);
        initial.set(0);
        arrayOfDominatorIndexes[0] = initial;

        for (int i=0; i<length; i++) {
            initial = new BitSet(length);
            initial.flip(0, length);
            arrayOfDominatorIndexes[i] = initial;
        }

        initial = arrayOfDominatorIndexes[0];
        initial.clear();
        initial.set(0);

        boolean change;

        do {
            change = false;

            for (BasicBlock basicBlock : list) {
                int index = basicBlock.getIndex();

                BitSet dominatorIndexes = arrayOfDominatorIndexes[index];

                initial = (BitSet)dominatorIndexes.clone();

                for (BasicBlock predecessorBB : basicBlock.getPredecessors()) {
                    dominatorIndexes.and(arrayOfDominatorIndexes[predecessorBB.getIndex()]);
                }

                dominatorIndexes.set(index);
                change |= (! initial.equals(dominatorIndexes));
            }
        } while (change);

        return arrayOfDominatorIndexes;
    }

    public static List<Loop> identifyNaturalLoops(ControlFlowGraph cfg, BitSet[] arrayOfDominatorIndexes) {
        List<BasicBlock> list = cfg.getBasicBlocks();
        int length = list.size();
        BitSet[] arrayOfMemberIndexes = new BitSet[length];

        // Identify loop members
        for (int i=0; i<length; i++) {
            BasicBlock current = list.get(i);
            BitSet dominatorIndexes = arrayOfDominatorIndexes[i];

            switch (current.getType()) {
                case TYPE_CONDITIONAL_BRANCH:
                    int index = current.getBranch().getIndex();

                    if ((index >= 0) && dominatorIndexes.get(index)) {
                        // 'branch' is a dominator -> Back edge found
                        arrayOfMemberIndexes[index] = searchLoopMemberIndexes(length, arrayOfMemberIndexes[index], current, current.getBranch());
                    }
                case TYPE_STATEMENTS:
                case TYPE_GOTO:
                    index = current.getNext().getIndex();

                    if ((index >= 0) && dominatorIndexes.get(index)) {
                        // 'next' is a dominator -> Back edge found
                        arrayOfMemberIndexes[index] = searchLoopMemberIndexes(length, arrayOfMemberIndexes[index], current, current.getNext());
                    }
                    break;
                case TYPE_SWITCH_DECLARATION:
                    for (SwitchCase switchCase : current.getSwitchCases()) {
                        index = switchCase.getBasicBlock().getIndex();

                        if ((index >= 0) && dominatorIndexes.get(index)) {
                            // 'switchCase' is a dominator -> Back edge found
                            arrayOfMemberIndexes[index] = searchLoopMemberIndexes(length, arrayOfMemberIndexes[index], current, switchCase.getBasicBlock());
                        }
                    }
                    break;
            }
        }

        // Loops & 'try' statements
        for (int i=0; i<length; i++) {
            if (arrayOfMemberIndexes[i] != null) {
                BitSet memberIndexes = arrayOfMemberIndexes[i];
                int maxOffset = -1;

                for (int j=0; j<length; j++) {
                    if (memberIndexes.get(j)) {
                        int offset = list.get(j).getFromOffset();
                        if (maxOffset < offset) {
                            maxOffset = offset;
                        }
                    }
                }

                BasicBlock start = list.get(i);
                BitSet startDominatorIndexes = arrayOfDominatorIndexes[i];

                if ((start.getType() == TYPE_TRY_DECLARATION) && (maxOffset != start.getFromOffset()) && (maxOffset < start.getExceptionHandlers().getFirst().getBasicBlock().getFromOffset())) {
                    // 'try' statement outside the loop
                    BasicBlock newStart = start.getNext();
                    HashSet<BasicBlock> newStartPredecessors = newStart.getPredecessors();

                    // Loop in 'try' statement
                    Iterator<BasicBlock> iterator = start.getPredecessors().iterator();

                    while (iterator.hasNext()) {
                        BasicBlock predecessor = iterator.next();

                        if (!startDominatorIndexes.get(predecessor.getIndex())) {
                            iterator.remove();
                            predecessor.replace(start, newStart);
                            newStartPredecessors.add(predecessor);
                        }
                    }

                    memberIndexes.clear(start.getIndex());
                    arrayOfMemberIndexes[newStart.getIndex()] = memberIndexes;
                    arrayOfMemberIndexes[i] = null;
                }
            }
        }

        // Build loops
        DefaultList<Loop> loops = new DefaultList<>();

        for (int i=0; i<length; i++) {
            if (arrayOfMemberIndexes[i] != null) {
                BitSet memberIndexes = arrayOfMemberIndexes[i];

                // Unoptimize loop
                BasicBlock start = list.get(i);
                BitSet startDominatorIndexes = arrayOfDominatorIndexes[i];

                BitSet searchZoneIndexes = new BitSet(length);
                searchZoneIndexes.or(startDominatorIndexes);
                searchZoneIndexes.flip(0, length);
                searchZoneIndexes.set(start.getIndex());

                if (start.getType() == TYPE_CONDITIONAL_BRANCH) {
                    if ((start.getNext() != start) &&
                        (start.getBranch() != start) &&
                        memberIndexes.get(start.getNext().getIndex()) &&
                        memberIndexes.get(start.getBranch().getIndex()))
                    {
                        // 'next' & 'branch' blocks are inside the loop -> Split loop ?
                        BitSet nextIndexes = new BitSet(length);
                        BitSet branchIndexes = new BitSet(length);

                        recursiveForwardSearchLoopMemberIndexes(nextIndexes, memberIndexes, start.getNext(), start);
                        recursiveForwardSearchLoopMemberIndexes(branchIndexes, memberIndexes, start.getBranch(), start);

                        BitSet commonMemberIndexes = (BitSet)nextIndexes.clone();
                        commonMemberIndexes.and(branchIndexes);

                        BitSet onlyLoopHeaderIndex = new BitSet(length);
                        onlyLoopHeaderIndex.set(i);

                        if (commonMemberIndexes.equals(onlyLoopHeaderIndex)) {
                            // Only 'start' is the common basic block -> Split loop
                            loops.add(makeLoop(list, start, searchZoneIndexes, memberIndexes));

                            branchIndexes.flip(0, length);
                            searchZoneIndexes.and(branchIndexes);
                            searchZoneIndexes.set(start.getIndex());

                            loops.add(makeLoop(list, start, searchZoneIndexes, nextIndexes));
                        } else {
                            loops.add(makeLoop(list, start, searchZoneIndexes, memberIndexes));
                        }
                    } else {
                        loops.add(makeLoop(list, start, searchZoneIndexes, memberIndexes));
                    }
                } else {
                    loops.add(makeLoop(list, start, searchZoneIndexes, memberIndexes));
                }
            }
        }

        loops.sort(LOOP_COMPARATOR);

        return loops;
    }

    protected static BitSet searchLoopMemberIndexes(int length, BitSet memberIndexes, BasicBlock current, BasicBlock start) {
        BitSet visited = new BitSet(length);

        recursiveBackwardSearchLoopMemberIndexes(visited, current, start);

        if (memberIndexes == null) {
            return visited;
        } else {
            memberIndexes.or(visited);
            return memberIndexes;
        }
    }

    protected static void recursiveBackwardSearchLoopMemberIndexes(BitSet visited, BasicBlock current, BasicBlock start) {
        if (visited.get(current.getIndex()) == false) {
            visited.set(current.getIndex());

            if (current != start) {
                for (BasicBlock predecessor : current.getPredecessors()) {
                    recursiveBackwardSearchLoopMemberIndexes(visited, predecessor, start);
                }
            }
        }
    }

    protected static Loop makeLoop(List<BasicBlock> list, BasicBlock start, BitSet searchZoneIndexes, BitSet memberIndexes) {
        int length = list.size();
        int maxOffset = -1;

        for (int i=0; i<length; i++) {
            if (memberIndexes.get(i)) {
                int offset = checkMaxOffset(list.get(i));
                if (maxOffset < offset) {
                    maxOffset = offset;
                }
            }
        }

        // Extend members
        memberIndexes.clear();
        recursiveForwardSearchLoopMemberIndexes(memberIndexes, searchZoneIndexes, start, maxOffset);

        HashSet<BasicBlock> members = new HashSet<>(memberIndexes.cardinality());

        for (int i=0; i<length; i++) {
            if (memberIndexes.get(i)) {
                members.add(list.get(i));
            }
        }

        // Search 'end' block
        BasicBlock end = END;

        if (start.getType() == TYPE_CONDITIONAL_BRANCH) {
            // First, check natural 'end' blocks
            int index = start.getBranch().getIndex();
            if (memberIndexes.get(index) == false) {
                end = start.getBranch();
            } else {
                index = start.getNext().getIndex();
                if (memberIndexes.get(index) == false) {
                    end = start.getNext();
                }
            }
        }

        if (end == END) {
            // Not found, check all member blocks
            end = searchEndBasicBlock(memberIndexes, maxOffset, members);

            if (!end.matchType(TYPE_END|TYPE_RETURN|TYPE_LOOP_START|TYPE_LOOP_CONTINUE|TYPE_LOOP_END) &&
                (end.getPredecessors().size() == 1) &&
                (end.getPredecessors().iterator().next().getLastLineNumber() + 1 >= end.getFirstLineNumber()))
            {
                HashSet<BasicBlock> set = new HashSet<>();

                if (recursiveForwardSearchLastLoopMemberIndexes(members, searchZoneIndexes, set, end, null)) {
                    members.addAll(set);

                    for (BasicBlock member : set) {
                        if (member.getIndex() >= 0) {
                            memberIndexes.set(member.getIndex());
                        }
                    }

                    end = searchEndBasicBlock(memberIndexes, maxOffset, set);
                }
            }
        }

        // Extend last member
        if (end != END) {
            HashSet<BasicBlock> m = new HashSet<>(members);
            HashSet<BasicBlock> set = new HashSet<>();

            for (BasicBlock member : m) {
                if ((member.getType() == TYPE_CONDITIONAL_BRANCH) && (member != start)) {
                    set.clear();
                    if (recursiveForwardSearchLastLoopMemberIndexes(members, searchZoneIndexes, set, member.getNext(), end)) {
                        members.addAll(set);
                    }
                    set.clear();
                    if (recursiveForwardSearchLastLoopMemberIndexes(members, searchZoneIndexes, set, member.getBranch(), end)) {
                        members.addAll(set);
                    }
                }
            }
        }

        return new Loop(start, members, end);
    }

    private static BasicBlock searchEndBasicBlock(BitSet memberIndexes, int maxOffset, Set<BasicBlock> members) {
        BasicBlock end = END;

        for (BasicBlock member : members) {
            switch (member.getType()) {
                case TYPE_CONDITIONAL_BRANCH:
                    BasicBlock bb = member.getBranch();
                    if (!memberIndexes.get(bb.getIndex()) && (maxOffset < bb.getFromOffset())) {
                        end = bb;
                        maxOffset = bb.getFromOffset();
                        break;
                    }
                case TYPE_STATEMENTS:
                case TYPE_GOTO:
                    bb = member.getNext();
                    if (!memberIndexes.get(bb.getIndex()) && (maxOffset < bb.getFromOffset())) {
                        end = bb;
                        maxOffset = bb.getFromOffset();
                    }
                    break;
                case TYPE_SWITCH_DECLARATION:
                    for (SwitchCase switchCase : member.getSwitchCases()) {
                        bb = switchCase.getBasicBlock();
                        if (!memberIndexes.get(bb.getIndex()) && (maxOffset < bb.getFromOffset())) {
                            end = bb;
                            maxOffset = bb.getFromOffset();
                        }
                    }
                    break;
                case TYPE_TRY_DECLARATION:
                    bb = member.getNext();
                    if (!memberIndexes.get(bb.getIndex()) && (maxOffset < bb.getFromOffset())) {
                        end = bb;
                        maxOffset = bb.getFromOffset();
                    }
                    for (ExceptionHandler exceptionHandler : member.getExceptionHandlers()) {
                        bb = exceptionHandler.getBasicBlock();
                        if (!memberIndexes.get(bb.getIndex()) && (maxOffset < bb.getFromOffset())) {
                            end = bb;
                            maxOffset = bb.getFromOffset();
                        }
                    }
                    break;
            }
        }

        return end;
    }

    private static int checkMaxOffset(BasicBlock basicBlock) {
        int maxOffset = basicBlock.getFromOffset();
        int offset;

        if (basicBlock.getType() == TYPE_TRY_DECLARATION) {
            for (ExceptionHandler exceptionHandler : basicBlock.getExceptionHandlers()) {
                if (exceptionHandler.getInternalThrowableName() == null) {
                    // Search throw block
                    offset = checkThrowBlockOffset(exceptionHandler.getBasicBlock());
                } else {
                    offset = checkSynchronizedBlockOffset(exceptionHandler.getBasicBlock());
                }
                if (maxOffset < offset) {
                    maxOffset = offset;
                }
            }
        } else if (basicBlock.getType() == TYPE_SWITCH_DECLARATION) {
            BasicBlock lastBB = null;
            BasicBlock previousBB = null;

            for (SwitchCase switchCase : basicBlock.getSwitchCases()) {
                BasicBlock bb = switchCase.getBasicBlock();
                if ((lastBB == null) || (lastBB.getFromOffset() < bb.getFromOffset())) {
                    previousBB = lastBB;
                    lastBB = bb;
                }
            }
            if (previousBB != null) {
                offset = checkSynchronizedBlockOffset(previousBB);
                if (maxOffset < offset) {
                    maxOffset = offset;
                }
            }
        }

        return maxOffset;
    }

    private static int checkSynchronizedBlockOffset(BasicBlock basicBlock) {
        if ((basicBlock.getNext().getType() == TYPE_TRY_DECLARATION) && (ByteCodeUtil.getLastOpcode(basicBlock) == 194)) { // MONITORENTER
            return checkThrowBlockOffset(basicBlock.getNext().getExceptionHandlers().getFirst().getBasicBlock());
        }

        return basicBlock.getFromOffset();
    }

    private static int checkThrowBlockOffset(BasicBlock basicBlock) {
        int offset = basicBlock.getFromOffset();
        BitSet watchdog = new BitSet();

        while (!basicBlock.matchType(GROUP_END) && !watchdog.get(basicBlock.getIndex())) {
            watchdog.set(basicBlock.getIndex());
            basicBlock = basicBlock.getNext();
        }

        if (basicBlock.getType() == TYPE_THROW) {
            return basicBlock.getFromOffset();
        }

        return offset;
    }

    protected static void recursiveForwardSearchLoopMemberIndexes(BitSet visited, BitSet searchZoneIndexes, BasicBlock current, BasicBlock target) {
        if (!current.matchType(GROUP_END) && (visited.get(current.getIndex()) == false) && (searchZoneIndexes.get(current.getIndex()) == true)) {
            visited.set(current.getIndex());

            if (current != target) {
                recursiveForwardSearchLoopMemberIndexes(visited, searchZoneIndexes, current.getNext(), target);
                recursiveForwardSearchLoopMemberIndexes(visited, searchZoneIndexes, current.getBranch(), target);

                for (SwitchCase switchCase : current.getSwitchCases()) {
                    recursiveForwardSearchLoopMemberIndexes(visited, searchZoneIndexes, switchCase.getBasicBlock(), target);
                }

                for (ExceptionHandler exceptionHandler : current.getExceptionHandlers()) {
                    recursiveForwardSearchLoopMemberIndexes(visited, searchZoneIndexes, exceptionHandler.getBasicBlock(), target);
                }

                if (current.getType() == TYPE_GOTO_IN_TERNARY_OPERATOR) {
                    visited.set(current.getNext().getIndex());
                }
            }
        }
    }

    protected static void recursiveForwardSearchLoopMemberIndexes(BitSet visited, BitSet searchZoneIndexes, BasicBlock current, int maxOffset) {
        if (!current.matchType(TYPE_END|TYPE_LOOP_START|TYPE_LOOP_CONTINUE|TYPE_LOOP_END|TYPE_SWITCH_BREAK) &&
            (visited.get(current.getIndex()) == false) &&
            (searchZoneIndexes.get(current.getIndex()) == true) &&
            (current.getFromOffset() <= maxOffset))
        {
            visited.set(current.getIndex());

            recursiveForwardSearchLoopMemberIndexes(visited, searchZoneIndexes, current.getNext(), maxOffset);
            recursiveForwardSearchLoopMemberIndexes(visited, searchZoneIndexes, current.getBranch(), maxOffset);

            for (SwitchCase switchCase : current.getSwitchCases()) {
                recursiveForwardSearchLoopMemberIndexes(visited, searchZoneIndexes, switchCase.getBasicBlock(), maxOffset);
            }

            for (ExceptionHandler exceptionHandler : current.getExceptionHandlers()) {
                recursiveForwardSearchLoopMemberIndexes(visited, searchZoneIndexes, exceptionHandler.getBasicBlock(), maxOffset);
            }

            if (current.getType() == TYPE_GOTO_IN_TERNARY_OPERATOR) {
                visited.set(current.getNext().getIndex());
            }
        }
    }

    protected static boolean recursiveForwardSearchLastLoopMemberIndexes(HashSet<BasicBlock> members, BitSet searchZoneIndexes, HashSet<BasicBlock> set, BasicBlock current, BasicBlock end) {
        if ((current == end) || members.contains(current) || set.contains(current)) {
            return true;
        } else if (current.matchType(GROUP_SINGLE_SUCCESSOR)) {
            if (!inSearchZone(current.getNext(), searchZoneIndexes) || !predecessorsInSearchZone(current, searchZoneIndexes)) {
                searchZoneIndexes.clear(current.getIndex());
                return true;
            } else {
                set.add(current);
                return recursiveForwardSearchLastLoopMemberIndexes(members, searchZoneIndexes, set, current.getNext(), end);
            }
        } else if (current.getType() == TYPE_CONDITIONAL_BRANCH) {
            if (!inSearchZone(current.getNext(), searchZoneIndexes) || !inSearchZone(current.getBranch(), searchZoneIndexes) || !predecessorsInSearchZone(current, searchZoneIndexes)) {
                searchZoneIndexes.clear(current.getIndex());
                return true;
            } else {
                set.add(current);
                return recursiveForwardSearchLastLoopMemberIndexes(members, searchZoneIndexes, set, current.getNext(), end) |
                       recursiveForwardSearchLastLoopMemberIndexes(members, searchZoneIndexes, set, current.getBranch(), end);
            }
        } else if (current.matchType(GROUP_END)) {
            if (!predecessorsInSearchZone(current, searchZoneIndexes)) {
                if (current.getIndex() >= 0) {
                    searchZoneIndexes.clear(current.getIndex());
                }
            } else {
                set.add(current);
            }
            return true;
        }

        return false;
    }

    protected static boolean predecessorsInSearchZone(BasicBlock basicBlock, BitSet searchZoneIndexes) {
        Set<BasicBlock> predecessors = basicBlock.getPredecessors();

        for (BasicBlock predecessor : predecessors) {
            if (!inSearchZone(predecessor, searchZoneIndexes)) {
                return false;
            }
        }

        return true;
    }

    protected static boolean inSearchZone(BasicBlock basicBlock, BitSet searchZoneIndexes) {
        return basicBlock.matchType(TYPE_END|TYPE_RETURN|TYPE_RET|TYPE_LOOP_END|TYPE_LOOP_START|TYPE_INFINITE_GOTO|TYPE_JUMP) || searchZoneIndexes.get(basicBlock.getIndex());
    }

    protected static BasicBlock recheckEndBlock(Set<BasicBlock> members, BasicBlock end) {
        do {
            boolean flag = false;

            for (BasicBlock predecessor : end.getPredecessors()) {
                if (!members.contains(predecessor)) {
                    flag = true;
                    break;
                }
            }

            if (flag) {
                break;
            }

            // Search new 'end' block
            BasicBlock newEnd = null;

            for (BasicBlock member : members) {
                if (member.matchType(GROUP_SINGLE_SUCCESSOR)) {
                    BasicBlock bb = member.getNext();
                    if ((bb != end) && !members.contains(bb)) {
                        newEnd = bb;
                        break;
                    }
                } else if (member.getType() == TYPE_CONDITIONAL_BRANCH) {
                    BasicBlock bb = member.getNext();
                    if ((bb != end) && !members.contains(bb)) {
                        newEnd = bb;
                        break;
                    }
                    bb = member.getBranch();
                    if ((bb != end) && !members.contains(bb)) {
                        newEnd = bb;
                        break;
                    }
                }
            }

            if ((newEnd == null) || (end.getFromOffset() >= newEnd.getFromOffset())) {
                break;
            }

            // Replace 'end' block
            if (end.matchType(TYPE_RETURN|TYPE_RETURN_VALUE|TYPE_THROW)) {
                members.add(end);
                end = newEnd;
            } else if (end.matchType(GROUP_SINGLE_SUCCESSOR) && (end.getNext() == newEnd)) {
                members.add(end);
                end = newEnd;
            } else {
                break;
            }
        } while (false);

        return end;
    }

    protected static BasicBlock reduceLoop(Loop loop) {
        BasicBlock start = loop.getStart();
        HashSet<BasicBlock> members = loop.getMembers();
        BasicBlock end = loop.getEnd();
        int toOffset = start.getToOffset();

        // Recheck 'end' block
        end = recheckEndBlock(members, end);

        // Build new basic block for loop
        BasicBlock loopBB = start.getControlFlowGraph().newBasicBlock(TYPE_LOOP, start.getFromOffset(), start.getToOffset());

        // Update predecessors
        Iterator<BasicBlock> startPredecessorIterator = start.getPredecessors().iterator();

        while (startPredecessorIterator.hasNext()) {
            BasicBlock predecessor = startPredecessorIterator.next();

            if (!members.contains(predecessor)) {
                predecessor.replace(start, loopBB);
                loopBB.getPredecessors().add(predecessor);
                startPredecessorIterator.remove();
            }
        }

        loopBB.setSub1(start);

        // Set LOOP_START, LOOP_END and TYPE_JUMP
        for (BasicBlock member : members) {
            if (member.matchType(GROUP_SINGLE_SUCCESSOR)) {
                BasicBlock bb = member.getNext();

                if (bb == start) {
                    member.setNext(LOOP_START);
                } else if (bb == end) {
                    member.setNext(LOOP_END);
                } else if (!members.contains(bb) && (bb.getPredecessors().size() > 1)) {
                    member.setNext(newJumpBasicBlock(member, bb));
                }
            } else if (member.getType() == TYPE_CONDITIONAL_BRANCH) {
                BasicBlock bb = member.getNext();

                if (bb == start) {
                    member.setNext(LOOP_START);
                } else if (bb == end) {
                    member.setNext(LOOP_END);
                } else if (!members.contains(bb) && (bb.getPredecessors().size() > 1)) {
                    member.setNext(newJumpBasicBlock(member, bb));
                }

                bb = member.getBranch();

                if (bb == start) {
                    member.setBranch(LOOP_START);
                } else if (bb == end) {
                    member.setBranch(LOOP_END);
                } else if (!members.contains(bb) && (bb.getPredecessors().size() > 1)) {
                    member.setBranch(newJumpBasicBlock(member, bb));
                }
            } else if (member.getType() == TYPE_SWITCH_DECLARATION) {
                for (SwitchCase switchCase : member.getSwitchCases()) {
                    BasicBlock bb = switchCase.getBasicBlock();

                    if (bb == start) {
                        switchCase.setBasicBlock(LOOP_START);
                    } else if (bb == end) {
                        switchCase.setBasicBlock(LOOP_END);
                    } else if (!members.contains(bb) && (bb.getPredecessors().size() > 1)) {
                        switchCase.setBasicBlock(newJumpBasicBlock(member, bb));
                    }
                }
            }
            if (toOffset < member.getToOffset()) {
                toOffset = member.getToOffset();
            }
        }

        if (end != null) {
            loopBB.setNext(end);
            end.replace(members, loopBB);
        }

        start.getPredecessors().clear();

        loopBB.setToOffset(toOffset);

        return loopBB;
    }

    protected static BasicBlock newJumpBasicBlock(BasicBlock bb, BasicBlock target) {
        HashSet<BasicBlock> predecessors = new HashSet<>();

        predecessors.add(bb);
        target.getPredecessors().remove(bb);

        return bb.getControlFlowGraph().newBasicBlock(TYPE_JUMP, bb.getFromOffset(), target.getFromOffset(), predecessors);
    }

    public static void reduce(ControlFlowGraph cfg) {
        BitSet[] arrayOfDominatorIndexes = buildDominatorIndexes(cfg);
        List<Loop> loops = identifyNaturalLoops(cfg, arrayOfDominatorIndexes);

        for (int i=0, loopsLength=loops.size(); i<loopsLength; i++) {
            Loop loop = loops.get(i);
            BasicBlock startBB = loop.getStart();
            BasicBlock loopBB = reduceLoop(loop);

            // Update other loops
            for (int j=loopsLength-1; j>i; j--) {
                Loop otherLoop = loops.get(j);

                if (otherLoop.getStart() == startBB) {
                    otherLoop.setStart(loopBB);
                }

                if (otherLoop.getMembers().contains(startBB)) {
                    otherLoop.getMembers().removeAll(loop.getMembers());
                    otherLoop.getMembers().add(loopBB);
                }

                if (otherLoop.getEnd() == startBB) {
                    otherLoop.setEnd(loopBB);
                }
            }
        }
    }

    /*
     * Smaller loop first
     */
    public static class LoopComparator implements Comparator<Loop> {
        @Override
        public int compare(Loop loop1, Loop loop2) {
            return loop1.getMembers().size() - loop2.getMembers().size();
        }
    }
}
