/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.util;

import org.jd.core.v1.model.classfile.ConstantPool;
import org.jd.core.v1.model.classfile.Method;
import org.jd.core.v1.model.classfile.attribute.AttributeCode;
import org.jd.core.v1.model.classfile.attribute.AttributeLineNumberTable;
import org.jd.core.v1.model.classfile.attribute.CodeException;
import org.jd.core.v1.model.classfile.attribute.LineNumber;
import org.jd.core.v1.model.classfile.constant.ConstantMemberRef;
import org.jd.core.v1.model.classfile.constant.ConstantNameAndType;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.cfg.BasicBlock;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.cfg.ControlFlowGraph;
import org.jd.core.v1.util.DefaultList;

import java.util.*;

import static org.jd.core.v1.service.converter.classfiletojavasyntax.model.cfg.BasicBlock.*;

public class ControlFlowGraphMaker {
    protected static final BasicBlock MARK = END;

    protected static final CodeExceptionComparator CODE_EXCEPTION_COMPARATOR = new CodeExceptionComparator();

    public static ControlFlowGraph make(Method method) {
        AttributeCode attributeCode = method.getAttribute("Code");

        if (attributeCode == null) {
            return null;
        } else {
            // Parse byte-code
            ConstantPool constants = method.getConstants();
            byte[] code = attributeCode.getCode();
            int length = code.length;

            BasicBlock[] map = new BasicBlock[length];

            char[] types = new char[length];                   // 'c' for conditional instruction, 'g' for goto, 't' for throw, 's' for switch, 'r' for return
            int[] nextOffsets = new int[length];               // Next instruction offsets
            int[] branchOffsets = new int[length];             // Branch offsets
            int[][] switchValues = new int[length][];          // Default-value and switch-values
            int[][] switchOffsets = new int[length][];         // Default-case offset and switch-case offsets

            // --- Search leaders --- //

            // The first instruction is a leader
            map[0] = MARK;

            int lastOffset = 0;
            int lastStatementOffset = -1;

            for (int offset=0; offset<length; offset++) {
                nextOffsets[lastOffset] = offset;
                lastOffset = offset;

                int opcode = code[offset] & 255;

                switch (opcode) {
                    case 16: // BIPUSH
                    case 18: // LDC
                    case 21: case 22: case 23: case 24: case 25: // ILOAD, LLOAD, FLOAD, DLOAD, ALOAD
                    case 188: // NEWARRAY
                        offset++;
                        break;
                    case 54: case 55: case 56: case 57: case 58: // ISTORE, LSTORE, FSTORE, DSTORE, ASTORE
                        offset++;
                        lastStatementOffset = offset;
                        break;
                    case 59: case 60: case 61: case 62: // ISTORE_0 .. ISTORE_3
                    case 63: case 64: case 65: case 66: // LSTORE_0 .. LSTORE_3
                    case 67: case 68: case 69: case 70: // FSTORE_0 .. FSTORE_3
                    case 71: case 72: case 73: case 74: // DSTORE_0 .. DSTORE_3
                    case 75: case 76: case 77: case 78: // ASTORE_0 .. ASTORE_3
                    case 79: case 80: case 81: case 82: case 83: case 84: case 85: case 86: // IASTORE, LASTORE, FASTORE, DASTORE, AASTORE, BASTORE, CASTORE, SASTORE
                    case 87: case 88: // POP, POP2
                    case 194: case 195: // MONITORENTER, MONITOREXIT
                        lastStatementOffset = offset;
                        break;
                    case 169: // RET
                        offset++;
                        // The instruction that immediately follows a conditional or an unconditional goto/jump instruction is a leader
                        types[offset] = 'R';
                        if (offset + 1 < length) {
                            map[offset + 1] = MARK;
                        }
                        lastStatementOffset = offset;
                        break;
                    case 179: case 181: // PUTSTATIC, PUTFIELD
                        offset += 2;
                        lastStatementOffset = offset;
                        break;
                    case 182: case 183: case 184: // INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC
                        ConstantMemberRef constantMemberRef = constants.getConstant( ((code[++offset] & 255) << 8) | (code[++offset] & 255) );
                        ConstantNameAndType constantNameAndType = constants.getConstant(constantMemberRef.getNameAndTypeIndex());
                        String descriptor = constants.getConstantUtf8(constantNameAndType.getDescriptorIndex());
                        if (descriptor.charAt(descriptor.length()-1) == 'V') {
                            lastStatementOffset = offset;
                        }
                        break;
                    case 185: case 186: // INVOKEINTERFACE, INVOKEDYNAMIC
                        constantMemberRef = constants.getConstant( ((code[++offset] & 255) << 8) | (code[++offset] & 255) );
                        constantNameAndType = constants.getConstant(constantMemberRef.getNameAndTypeIndex());
                        descriptor = constants.getConstantUtf8(constantNameAndType.getDescriptorIndex());
                        offset += 2; // Skip 2 bytes
                        if (descriptor.charAt(descriptor.length()-1) == 'V') {
                            lastStatementOffset = offset;
                        }
                        break;
                    case 132: // IINC
                        offset += 2;
                        if ((lastStatementOffset+3 == offset) && (checkILOADForIINC(code, offset, (code[offset-1] & 255)) == false)) {
                            // Last instruction is a 'statement' & the next instruction is not a matching ILOAD -> IINC as a statement
                            lastStatementOffset = offset;
                        }
                        break;
                    case 17: // SIPUSH
                    case 19: case 20: // LDC_W, LDC2_W
                    case 178: case 180: // GETSTATIC, GETFIELD
                    case 187: case 189: // NEW, ANEWARRAY
                    case 192: // CHECKCAST
                    case 193: // INSTANCEOF
                        offset += 2;
                        break;
                    case 167: // GOTO
                        char type = (lastStatementOffset+1 == offset) ? 'g' : 'G';

                        if (lastStatementOffset != -1) {
                            map[lastStatementOffset + 1] = MARK;
                        }
                        // The target of a conditional or an unconditional goto/jump instruction is a leader
                        types[offset] = type; // TODO debug, remove this line
                        int branchOffset = offset + (short)(((code[++offset] & 255) << 8) | (code[++offset] & 255));
                        map[branchOffset] = MARK;
                        types[offset] = type;
                        branchOffsets[offset] = branchOffset;
                        // The instruction that immediately follows a conditional or an unconditional goto/jump instruction is a leader
                        if (offset + 1 < length) {
                            map[offset + 1] = MARK;
                        }
                        lastStatementOffset = offset;
                        break;
                    case 168: // JSR
                        if (lastStatementOffset != -1) {
                            map[lastStatementOffset + 1] = MARK;
                        }
                        types[offset] = 'j'; // TODO debug, remove this line
                        // The target of a conditional or an unconditional goto/jump instruction is a leader
                        branchOffset = offset + (short)(((code[++offset] & 255) << 8) | (code[++offset] & 255));
                        map[branchOffset] = MARK;
                        types[offset] = 'j';
                        branchOffsets[offset] = branchOffset;
                        // The instruction that immediately follows a conditional or an unconditional goto/jump instruction is a leader
                        if (offset + 1 < length) {
                            map[offset + 1] = MARK;
                        }
                        lastStatementOffset = offset;
                        break;
                    case 153: case 154: case 155: case 156: case 157: case 158: // IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE
                    case 159: case 160: case 161: case 162: case 163: case 164: case 165: case 166: // IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ, IF_ACMPNE
                    case 198: case 199: // IFNULL, IFNONNULL
                        if (lastStatementOffset != -1) {
                            map[lastStatementOffset + 1] = MARK;
                        }
                        // The target of a conditional or an unconditional goto/jump instruction is a leader
                        branchOffset = offset + (short)(((code[++offset] & 255) << 8) | (code[++offset] & 255));
                        map[branchOffset] = MARK;
                        types[offset] = 'c';
                        branchOffsets[offset] = branchOffset;
                        // The instruction that immediately follows a conditional or an unconditional goto/jump instruction is a leader
                        if (offset + 1 < length) {
                            map[offset + 1] = MARK;
                        }
                        lastStatementOffset = offset;
                        break;
                    case 170: // TABLESWITCH
                        // Skip padding
                        int i = (offset + 4) & 0xFFFC;
                        int defaultOffset = offset + (((code[i++] & 255) << 24) | ((code[i++] & 255) << 16) | ((code[i++] & 255) << 8) | (code[i++] & 255));

                        map[defaultOffset] = MARK;

                        int low = ((code[i++] & 255) << 24) | ((code[i++] & 255) << 16) | ((code[i++] & 255) << 8) | (code[i++] & 255);
                        int high = ((code[i++] & 255) << 24) | ((code[i++] & 255) << 16) | ((code[i++] & 255) << 8) | (code[i++] & 255);
                        int[] values = new int[high - low + 2];
                        int[] offsets = new int[high - low + 2];

                        offsets[0] = defaultOffset;

                        for (int j=1, len=high-low+2; j<len; j++) {
                            values[j] = low + j - 1;
                            branchOffset = offsets[j] = offset + (((code[i++] & 255) << 24) | ((code[i++] & 255) << 16) | ((code[i++] & 255) << 8) | (code[i++] & 255));
                            map[branchOffset] = MARK;
                        }

                        offset = (i - 1);
                        types[offset] = 's';
                        switchValues[offset] = values;
                        switchOffsets[offset] = offsets;
                        lastStatementOffset = offset;
                        break;
                    case 171: // LOOKUPSWITCH
                        // Skip padding
                        i = (offset + 4) & 0xFFFC;
                        defaultOffset = offset + (((code[i++] & 255) << 24) | ((code[i++] & 255) << 16) | ((code[i++] & 255) << 8) | (code[i++] & 255));

                        map[defaultOffset] = MARK;

                        int npairs = ((code[i++] & 255) << 24) | ((code[i++] & 255) << 16) | ((code[i++] & 255) << 8) | (code[i++] & 255);

                        values = new int[npairs + 1];
                        offsets = new int[npairs + 1];

                        offsets[0] = defaultOffset;

                        for (int j=1; j<=npairs; j++) {
                            values[j] = ((code[i++] & 255) << 24) | ((code[i++] & 255) << 16) | ((code[i++] & 255) << 8) | (code[i++] & 255);
                            branchOffset = offsets[j] = offset + (((code[i++] & 255) << 24) | ((code[i++] & 255) << 16) | ((code[i++] & 255) << 8) | (code[i++] & 255));
                            map[branchOffset] = MARK;
                        }

                        offset = (i - 1);
                        types[offset] = 's';
                        switchValues[offset] = values;
                        switchOffsets[offset] = offsets;
                        lastStatementOffset = offset;
                        break;
                    case 172: case 173: case 174: case 175: case 176: // IRETURN, LRETURN, FRETURN, DRETURN, ARETURN
                        types[offset] = 'v';
                        if (offset + 1 < length) {
                            map[offset + 1] = MARK;
                        }
                        lastStatementOffset = offset;
                        break;
                    case 177: // RETURN
                        if (lastStatementOffset != -1) {
                            map[lastStatementOffset + 1] = MARK;
                        }
                        types[offset] = 'r';
                        if (offset + 1 < length) {
                            map[offset + 1] = MARK;
                        }
                        lastStatementOffset = offset;
                        break;
                    case 191: // ATHROW
                        types[offset] = 't';
                        if (offset + 1 < length) {
                            map[offset + 1] = MARK;
                        }
                        lastStatementOffset = offset;
                        break;
                    case 196: // WIDE
                        opcode = code[++offset] & 255;

                        switch (opcode) {
                            case 132: // IINC
                                offset += 4;
                                if ((lastStatementOffset+6 == offset) && (checkILOADForIINC(code, offset, ((code[offset-3] & 255) << 8) | (code[offset-2] & 255)) == false)) {
                                    // Last instruction is a 'statement' & the next instruction is not a matching ILOAD -> IINC as a statement
                                    lastStatementOffset = offset;
                                }
                                break;
                            case 169: // RET
                                offset += 2;
                                // The instruction that immediately follows a conditional or an unconditional goto/jump instruction is a leader
                                types[offset] = 'R';
                                if (offset + 1 < length) {
                                    map[offset + 1] = MARK;
                                }
                                lastStatementOffset = offset;
                                break;
                            case 54: case 55: case 56: case 57: case 58: // ISTORE, LSTORE, FSTORE, DSTORE, ASTORE
                                lastStatementOffset = offset+2;
                            default:
                                offset += 2;
                                break;
                        }
                        break;
                    case 197: // MULTIANEWARRAY
                        offset += 3;
                        break;
                    case 200: // GOTO_W
                        type = (lastStatementOffset+1 == offset) ? 'g' : 'G';

                        types[offset] = type; // TODO debug, remove this line
                        branchOffset = offset + (((code[++offset] & 255) << 24) | ((code[++offset] & 255) << 16) | ((code[++offset] & 255) << 8) | (code[++offset] & 255));
                        map[branchOffset] = MARK;
                        types[offset] = type;
                        branchOffsets[offset] = branchOffset;
                        // The instruction that immediately follows a conditional or an unconditional goto/jump instruction is a leader
                        if (offset + 1 < length) {
                            map[offset + 1] = MARK;
                        }
                        lastStatementOffset = offset;
                        break;
                    case 201: // JSR_W
                        if (lastStatementOffset != -1) {
                            map[lastStatementOffset + 1] = MARK;
                        }
                        types[offset] = 'j'; // TODO debug, remove this line
                        // The target of a conditional or an unconditional goto/jump instruction is a leader
                        branchOffset = offset + (((code[++offset] & 255) << 24) | ((code[++offset] & 255) << 16) | ((code[++offset] & 255) << 8) | (code[++offset] & 255));
                        map[branchOffset] = MARK;
                        types[offset] = 'j';
                        branchOffsets[offset] = branchOffset;
                        // The instruction that immediately follows a conditional or an unconditional goto/jump instruction is a leader
                        if (offset + 1 < length) {
                            map[offset + 1] = MARK;
                        }
                        lastStatementOffset = offset;
                        break;
                }
            }

            nextOffsets[lastOffset] = length;
            CodeException[] codeExceptions = attributeCode.getExceptionTable();

            if (codeExceptions != null) {
                for (CodeException codeException : codeExceptions) {
                    map[codeException.getStartPc()] = MARK;
                    map[codeException.getHandlerPc()] = MARK;
                }
            }

            // --- Create line numbers --- //
            ControlFlowGraph cfg = new ControlFlowGraph(method);
            AttributeLineNumberTable attributeLineNumberTable = attributeCode.getAttribute("LineNumberTable");

            if (attributeLineNumberTable != null) {
                // Parse line numbers
                LineNumber[] lineNumberTable = attributeLineNumberTable.getLineNumberTable();

                int[] offsetToLineNumbers = new int[length];
                int offset = 0;
                int lineNumber = lineNumberTable[0].getLineNumber();

                for (int i=1, len=lineNumberTable.length; i<len; i++) {
                    LineNumber lineNumberEntry = lineNumberTable[i];
                    int toIndex = lineNumberEntry.getStartPc();

                    while (offset < toIndex) offsetToLineNumbers[offset++] = lineNumber;

                    if (lineNumber > lineNumberEntry.getLineNumber()) {
                        map[offset] = MARK;
                    }

                    lineNumber = lineNumberEntry.getLineNumber();
                }

                while (offset < length) offsetToLineNumbers[offset++] = lineNumber;

                cfg.setOffsetToLineNumbers(offsetToLineNumbers);
            }

            // --- Create basic blocks --- //
            lastOffset = 0;

            // Add 'start'
            BasicBlock startBasicBlock = cfg.newBasicBlock(TYPE_START, 0, 0);

            for (int offset=nextOffsets[0]; offset<length; offset=nextOffsets[offset]) {
                if (map[offset] != null) {
                    map[lastOffset] = cfg.newBasicBlock(lastOffset, offset);
                    lastOffset = offset;
                }
            }

            map[lastOffset] = cfg.newBasicBlock(lastOffset, length);

            // --- Set type, successors and predecessors --- //
            List<BasicBlock> list = cfg.getBasicBlocks();
            List<BasicBlock> basicBlocks = new DefaultList<>(list.size());
            BasicBlock successor = list.get(1);
            startBasicBlock.setNext(successor);
            successor.getPredecessors().add(startBasicBlock);

            for (int i=1, basicBlockLength=list.size(); i<basicBlockLength; i++) {
                BasicBlock basicBlock = list.get(i);
                int lastInstructionOffset = basicBlock.getToOffset() - 1;

                switch (types[lastInstructionOffset]) {
                    case 'g': // Goto
                        basicBlock.setType(TYPE_GOTO);
                        successor = map[branchOffsets[lastInstructionOffset]];
                        basicBlock.setNext(successor);
                        successor.getPredecessors().add(basicBlock);
                        break;
                    case 'G': // Goto in ternary operator
                        basicBlock.setType(TYPE_GOTO_IN_TERNARY_OPERATOR);
                        successor = map[branchOffsets[lastInstructionOffset]];
                        basicBlock.setNext(successor);
                        successor.getPredecessors().add(basicBlock);
                        break;
                    case 't': // Throw
                        basicBlock.setType(TYPE_THROW);
                        basicBlock.setNext(END);
                        break;
                    case 'r': // Return
                        basicBlock.setType(TYPE_RETURN);
                        basicBlock.setNext(END);
                        break;
                    case 'c': // Conditional
                        basicBlock.setType(TYPE_CONDITIONAL_BRANCH);
                        successor = map[basicBlock.getToOffset()];
                        basicBlock.setNext(successor);
                        successor.getPredecessors().add(basicBlock);
                        successor = map[branchOffsets[lastInstructionOffset]];
                        basicBlock.setBranch(successor);
                        successor.getPredecessors().add(basicBlock);
                        break;
                    case 's': // Switch
                        basicBlock.setType(TYPE_SWITCH_DECLARATION);
                        int[] values = switchValues[lastInstructionOffset];
                        int[] offsets = switchOffsets[lastInstructionOffset];
                        DefaultList<SwitchCase> switchCases = new DefaultList<>(offsets.length);

                        int defaultOffset = offsets[0];
                        BasicBlock bb = map[defaultOffset];
                        switchCases.add(new SwitchCase(bb));
                        bb.getPredecessors().add(basicBlock);

                        for (int j=1, len=offsets.length; j<len; j++) {
                            int offset = offsets[j];
                            if (offset != defaultOffset) {
                                bb = map[offset];
                                switchCases.add(new SwitchCase(values[j], bb));
                                bb.getPredecessors().add(basicBlock);
                            }
                        }

                        basicBlock.setSwitchCases(switchCases);
                        break;
                    case 'j': // Jsr
                        basicBlock.setType(TYPE_JSR);
                        successor = map[basicBlock.getToOffset()];
                        basicBlock.setNext(successor);
                        successor.getPredecessors().add(basicBlock);
                        successor = map[branchOffsets[lastInstructionOffset]];
                        basicBlock.setBranch(successor);
                        successor.getPredecessors().add(basicBlock);
                        break;
                    case 'R': // Ret
                        basicBlock.setType(TYPE_RET);
                        basicBlock.setNext(END);
                        break;
                    case 'v': // Return value
                        basicBlock.setType(TYPE_RETURN_VALUE);
                        basicBlock.setNext(END);
                        break;
                    default:
                        basicBlock.setType(TYPE_STATEMENTS);
                        successor = map[basicBlock.getToOffset()];
                        basicBlock.setNext(successor);
                        successor.getPredecessors().add(basicBlock);
                        basicBlocks.add(basicBlock);
                        break;
                }
            }

            // --- Create try-catch-finally basic blocks --- //
            if (codeExceptions != null) {
                HashMap<CodeException, BasicBlock> cache = new HashMap<>();
                ConstantPool constantPool = method.getConstants();
                // Reuse arrays
                int[] handlePcToStartPc = branchOffsets;
                char[] handlePcMarks = types;

                Arrays.sort(codeExceptions, CODE_EXCEPTION_COMPARATOR);

                for (CodeException codeException : codeExceptions) {
                    int startPc = codeException.getStartPc();
                    int handlerPc = codeException.getHandlerPc();

                    if (startPc != handlerPc) {
                        if ((handlePcMarks[handlerPc] != 'T') || (startPc <= map[handlePcToStartPc[handlerPc]].getFromOffset())) {
                            int catchType = codeException.getCatchType();
                            BasicBlock tcf = cache.get(codeException);

                            if (tcf == null) {
                                int endPc = codeException.getEndPc();
                                // Check 'endPc'
                                BasicBlock start = map[startPc];

                                // Insert a new 'try-catch-finally' basic block
                                tcf = cfg.newBasicBlock(TYPE_TRY_DECLARATION, startPc, endPc);
                                tcf.setNext(start);

                                // Update predecessors
                                Set<BasicBlock> tcfPredecessors = tcf.getPredecessors();
                                Set<BasicBlock> startPredecessors = start.getPredecessors();
                                Iterator<BasicBlock> iterator = startPredecessors.iterator();

                                while (iterator.hasNext()) {
                                    BasicBlock predecessor = iterator.next();

                                    if (!start.contains(predecessor)) {
                                        predecessor.replace(start, tcf);
                                        tcfPredecessors.add(predecessor);
                                        iterator.remove();
                                    }
                                }

                                startPredecessors.add(tcf);

                                // Update map
                                map[startPc] = tcf;

                                // Store to objectTypeCache
                                cache.put(codeException, tcf);
                            }

                            String internalThrowableName = catchType == 0 ? null : constantPool.getConstantTypeName(catchType);
                            BasicBlock handlerBB = map[handlerPc];
                            tcf.addExceptionHandler(internalThrowableName, handlerBB);
                            handlerBB.getPredecessors().add(tcf);
                            handlePcToStartPc[handlerPc] = startPc;
                            handlePcMarks[handlerPc] = 'T';
                        }
                    }
                }
            }

            // --- Recheck TYPE_GOTO_IN_TERNARY_OPERATOR --- //
            for (BasicBlock bb : basicBlocks) {
                BasicBlock next = bb.getNext();
                Set<BasicBlock> predecessors;

                if ((bb.getType() == TYPE_STATEMENTS) && (next.getPredecessors().size() == 1)) {
                    if ((next.getType() == TYPE_GOTO) && (ByteCodeUtil.evalStackDepth(constants, code, bb) > 0)) {
                        // Transform STATEMENTS and GOTO to GOTO_IN_TERNARY_OPERATOR
                        bb.setType(TYPE_GOTO_IN_TERNARY_OPERATOR);
                        bb.setToOffset(next.getToOffset());
                        bb.setNext(next.getNext());
                        predecessors = next.getNext().getPredecessors();
                        predecessors.remove(next);
                        predecessors.add(bb);
                        next.setType(TYPE_DELETED);
                    } else if ((next.getType() == TYPE_CONDITIONAL_BRANCH) && (ByteCodeUtil.evalStackDepth(constants, code, bb) > 0)) {
                        // Merge STATEMENTS and CONDITIONAL_BRANCH
                        bb.setType(TYPE_CONDITIONAL_BRANCH);
                        bb.setToOffset(next.getToOffset());
                        bb.setNext(next.getNext());
                        predecessors = next.getNext().getPredecessors();
                        predecessors.remove(next);
                        predecessors.add(bb);
                        bb.setBranch(next.getBranch());
                        predecessors = next.getBranch().getPredecessors();
                        predecessors.remove(next);
                        predecessors.add(bb);
                        next.setType(TYPE_DELETED);
                    }
                }
            }

            return cfg;
        }
    }

    protected static boolean checkILOADForIINC(byte[] code, int offset, int index) {
        if (++offset < code.length) {
            int nextOpcode = code[offset] & 255;

            if (nextOpcode == 21) { // ILOAD
                if (index == (code[offset+1] & 255)) {
                    return true;
                }
            } else if (nextOpcode == 26+index) { // ILOAD_0 ... ILOAD_3
                return true;
            }
        }

        return false;
    }

    /* 1) Smaller 'startPc' first
     * 2) Smaller 'endPc' first
     */
    public static class CodeExceptionComparator implements Comparator<CodeException> {
        @Override
        public int compare(CodeException ce1, CodeException ce2) {
            int comp = ce1.getStartPc() - ce2.getStartPc();
            if (comp == 0)
                comp = ce1.getEndPc() - ce2.getEndPc();
            return comp;
        }
    }
}
