/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.cfg;

import org.jd.core.v1.model.classfile.Method;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.cfg.BasicBlock;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.cfg.ControlFlowGraph;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.ByteCodeWriter;
import org.jd.core.v1.util.DefaultList;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.Deflater;

import static org.jd.core.v1.service.converter.classfiletojavasyntax.model.cfg.BasicBlock.*;

/**
 * A state diagram writer.
 *
 * http://plantuml.com/state.html
 * http://plantuml.com/plantuml
 */
public class ControlFlowGraphPlantUMLWriter {
    protected static final int MAX_OFFSET = Integer.MAX_VALUE;

    //protected static final String PLANTUML_URL_PREFIX   = "http://plantuml.com/plantuml/png/";
    protected static final String PLANTUML_URL_PREFIX   = "http://plantuml.com/plantuml/svg/";
    protected static final char[] PLANTUML_ENCODE_6_BIT = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_".toCharArray();
    protected static final BasicBlockComparator BASIC_BLOCK_COMPARATOR = new BasicBlockComparator();

    public static String write(ControlFlowGraph cfg) {
        if (cfg.getBasicBlocks() == null) {
            return null;
        } else {
            boolean dark = true;
            HashSet<BasicBlock> set = new HashSet<>();

            search(set, cfg.getStart());

            DefaultList<BasicBlock> list = new DefaultList<>(set);
            list.sort(BASIC_BLOCK_COMPARATOR);

            StringBuilder sb = new StringBuilder();

            //sb.append("scale 0.6\n");
            //sb.append("scale max 2000 width\n");
            //sb.append("skinparam shadowing false\n");
            sb.append("skinparam state {\n");
            sb.append("  BackgroundColor<<Reduced>> #BBD7B7\n");
            sb.append("  BorderColor<<Reduced>> Green\n");
            sb.append("  BackgroundColor<<Synthetic>> PowderBlue\n");
            sb.append("  BorderColor<<Synthetic>> DodgerBlue\n");
            sb.append("  BackgroundColor<<ToReduce>> Orange\n");
            sb.append("  BorderColor<<ToReduce>> #FF740E\n");
            sb.append("}\n");

            if (dark) {
                sb.append("skinparam BackgroundColor #2B2B2B\n");
                sb.append("skinparam state {\n");
                sb.append("  StartColor #999999\n");
                sb.append("  BackgroundColor #D6BF55\n");
                sb.append("  BorderColor #F6DF57\n");
                sb.append("}\n");
                sb.append("skinparam sequence {\n");
                sb.append("  ArrowColor #999999\n");
                sb.append("  ArrowFontColor #AAAAAA\n");
                sb.append("}\n");
            } else {
                //sb.append("skinparam BackgroundColor #DDDDDD\n");
                sb.append("skinparam state {\n");
                sb.append("  BorderColor Gold\n");
                sb.append("}\n");
                sb.append("skinparam sequence {\n");
                sb.append("  ArrowColor Black\n");
                sb.append("}\n");
            }

            Method method = cfg.getMethod();

            for (BasicBlock basicBlock : list) {
                writeState(sb, method, basicBlock);
            }

            for (BasicBlock basicBlock : list) {
                writeLink(sb, basicBlock);
            }

            return sb.toString();
        }
    }

    protected static void search(HashSet<BasicBlock> set, BasicBlock basicBlock) {
        if (set.contains(basicBlock) == false) {
            set.add(basicBlock);

            switch (basicBlock.getType()) {
                case TYPE_START:
                case TYPE_STATEMENTS:
                case TYPE_LOOP:
                case TYPE_GOTO:
                case TYPE_GOTO_IN_TERNARY_OPERATOR:
                    search(set, basicBlock.getNext());
                    break;
                case TYPE_CONDITIONAL_BRANCH:
                case TYPE_JSR:
                    search(set, basicBlock.getNext());
                    search(set, basicBlock.getBranch());
                    break;
                case TYPE_SWITCH:
                    search(set, basicBlock.getNext());
                case TYPE_SWITCH_DECLARATION:
                    for (SwitchCase switchCase : basicBlock.getSwitchCases()) {
                        search(set, switchCase.getBasicBlock());
                    }
                    break;
                case TYPE_TRY:
                case TYPE_TRY_JSR:
                case TYPE_TRY_ECLIPSE:
                    search(set, basicBlock.getSub1());
                case TYPE_TRY_DECLARATION:
                    search(set, basicBlock.getNext());
                    for (BasicBlock.ExceptionHandler exceptionHandler : basicBlock.getExceptionHandlers()) {
                        search(set, exceptionHandler.getBasicBlock());
                    }
                    break;
                case TYPE_IF:
                    search(set, basicBlock.getCondition());
                    search(set, basicBlock.getNext());
                    search(set, basicBlock.getSub1());
                    break;
                case TYPE_IF_ELSE:
                case TYPE_TERNARY_OPERATOR:
                    search(set, basicBlock.getNext());
                case TYPE_CONDITION_TERNARY_OPERATOR:
                    search(set, basicBlock.getCondition());
                case TYPE_CONDITION:
                case TYPE_CONDITION_OR:
                case TYPE_CONDITION_AND:
                    search(set, basicBlock.getSub1());
                    search(set, basicBlock.getSub2());
                    break;
            }
        }
    }

    protected static void writeState(StringBuilder sb, Method method, BasicBlock basicBlock) {
        if (basicBlock.getFromOffset() > MAX_OFFSET) {
            return;
        }

        String id = getStateId(basicBlock);

        switch (basicBlock.getType()) {
            case TYPE_STATEMENTS:
            case TYPE_IF:
            case TYPE_IF_ELSE:
            case TYPE_TERNARY_OPERATOR:
            case TYPE_TRY:
            case TYPE_TRY_JSR:
            case TYPE_TRY_ECLIPSE:
                sb.append("state \"").append(basicBlock.getTypeName()).append(" : ").append(basicBlock.getIndex()).append("\" as ").append(id).append("<<Reduced>>\n");
                writeStateOffsets(sb, id, basicBlock);
                writeLineNumbers(sb, id, basicBlock);
                writeStateEnd(sb, id, basicBlock.getNext(), "next");
                writeStatePredecessors(sb, id, basicBlock);
                writeStateCode(sb, id, method, basicBlock);
                break;
            case TYPE_RETURN:
                if (basicBlock == RETURN) {
                    break;
                }
            case TYPE_THROW:
            case TYPE_RETURN_VALUE:
            case TYPE_RET:
            case TYPE_SWITCH:
            case TYPE_INFINITE_GOTO:
            case TYPE_GOTO_IN_TERNARY_OPERATOR:
                sb.append("state \"").append(basicBlock.getTypeName()).append(" : ").append(basicBlock.getIndex()).append("\" as ").append(id).append("<<Reduced>>\n");
                writeStateOffsets(sb, id, basicBlock);
                writeLineNumbers(sb, id, basicBlock);
                writeStatePredecessors(sb, id, basicBlock);
                writeStateCode(sb, id, method, basicBlock);
                break;
            case TYPE_CONDITION:
            case TYPE_CONDITION_OR:
            case TYPE_CONDITION_AND:
            case TYPE_CONDITION_TERNARY_OPERATOR:
                sb.append("state \"").append(basicBlock.getTypeName()).append(" : ").append(basicBlock.getIndex()).append("\" as ").append(id).append("<<Reduced>>\n");
                writeStateOffsets(sb, id, basicBlock);
                writeLineNumbers(sb, id, basicBlock);
                writeInverseCondition(sb, id, basicBlock);
                writeStateCode(sb, id, method, basicBlock);
                break;
            case TYPE_JSR:
                sb.append("state \"").append(basicBlock.getTypeName()).append(" : ").append(basicBlock.getIndex()).append("\" as ").append(id).append('\n');
                writeStateOffsets(sb, id, basicBlock);
                writeLineNumbers(sb, id, basicBlock);
                writeStateEnd(sb, id, basicBlock.getNext(), "next");
                writeStateEnd(sb, id, basicBlock.getBranch(), "branch");
                writeStatePredecessors(sb, id, basicBlock);
                writeStateCode(sb, id, method, basicBlock);
                break;
            case TYPE_CONDITIONAL_BRANCH:
                sb.append("state \"").append(basicBlock.getTypeName()).append(" : ").append(basicBlock.getIndex()).append("\" as ").append(id).append('\n');
                writeStateOffsets(sb, id, basicBlock);
                writeLineNumbers(sb, id, basicBlock);
                writeStatePredecessors(sb, id, basicBlock);
                writeStateCode(sb, id, method, basicBlock);
                break;
            case TYPE_TRY_DECLARATION:
            case TYPE_GOTO:
                sb.append("state \"").append(basicBlock.getTypeName()).append(" : ").append(basicBlock.getIndex()).append("\" as ").append(id).append("<<ToReduce>>\n");
                writeStateOffsets(sb, id, basicBlock);
                writeLineNumbers(sb, id, basicBlock);
                writeStatePredecessors(sb, id, basicBlock);
                writeStateCode(sb, id, method, basicBlock);
                break;
            case TYPE_SWITCH_DECLARATION:
                sb.append("state \"").append(basicBlock.getTypeName()).append(" : ").append(basicBlock.getIndex()).append("\" as ").append(id).append("<<ToReduce>>\n");
                writeStateOffsets(sb, id, basicBlock);
                writeLineNumbers(sb, id, basicBlock);
                writeStatePredecessors(sb, id, basicBlock);
                writeStateCode(sb, id, method, basicBlock);
                break;
            case TYPE_LOOP:
                sb.append("state \"").append(basicBlock.getTypeName()).append(" : ").append(basicBlock.getIndex()).append("\" as ").append(id).append(" {\n");
                sb.append("[*] --> ").append(getStateId(basicBlock.getSub1())).append('\n');

                HashSet<BasicBlock> set = new HashSet<>();

                search(set, basicBlock.getSub1());

                set.remove(basicBlock);

                for (BasicBlock bb : set)
                    writeState(sb, method, bb);

                for (BasicBlock bb : set)
                    writeLink(sb, bb);

                sb.append("}\n");
                writeStateOffsets(sb, id, basicBlock);
                writeStateEnd(sb, id, basicBlock.getNext(), "next");
                writeStatePredecessors(sb, id, basicBlock);
                break;
            case TYPE_JUMP:
                sb.append("state \"").append(basicBlock.getTypeName()).append(" : ").append(basicBlock.getIndex()).append("\" as ").append(id).append("<<Synthetic>>\n");
                sb.append(id).append(" : offset = ").append(basicBlock.getFromOffset()).append("\n");
                sb.append(id).append(" : targetOffset = ").append(basicBlock.getToOffset()).append("\n");
                break;
        }
    }

    protected static void writeStateOffsets(StringBuilder sb, String id, BasicBlock basicBlock) {
        sb.append(id).append(" : fromOffset = ").append(basicBlock.getFromOffset()).append("\n");
        sb.append(id).append(" : toOffset = ").append(basicBlock.getToOffset()).append("\n");
    }

    protected static void writeStatePredecessors(StringBuilder sb, String id, BasicBlock basicBlock) {
        Set<BasicBlock> predecessors = basicBlock.getPredecessors();

        if (!predecessors.isEmpty()) {
            sb.append(id).append(" : predecessors = [");

            Iterator<BasicBlock> iterator = predecessors.iterator();

            sb.append(iterator.next().getIndex());
            while (iterator.hasNext()) {
                sb.append(", ").append(iterator.next().getIndex());
            }

            sb.append("]\n");
        }
    }

    protected static void writeInverseCondition(StringBuilder sb, String id, BasicBlock basicBlock) {
        if (basicBlock.matchType(TYPE_CONDITION|TYPE_CONDITION_TERNARY_OPERATOR|TYPE_GOTO_IN_TERNARY_OPERATOR)) {
            sb.append(id).append(" : inverseCondition = ").append(basicBlock.mustInverseCondition()).append("\n");
        }
    }

    protected static void writeStateCode(StringBuilder sb, String id, Method method, BasicBlock basicBlock) {
        if ((method != null) && basicBlock.matchType(GROUP_CODE) && (basicBlock.getFromOffset() < basicBlock.getToOffset())) {
            String byteCode = ByteCodeWriter.write("  ", method, basicBlock.getFromOffset(), basicBlock.getToOffset());

            byteCode = byteCode.substring(0, byteCode.length()-1).replaceAll("\n", "\\\\n\\\\\n");
            sb.append(id).append(" : code =\\n\\\n").append(byteCode).append("\n");
        }
    }

    protected static void writeLineNumbers(StringBuilder sb, String id, BasicBlock basicBlock) {
        if (basicBlock.getFirstLineNumber() > 0) {
            sb.append(id).append(" : firstLineNumber = ").append(basicBlock.getFirstLineNumber()).append("\n");
        }
        if (basicBlock.getLastLineNumber() > 0) {
            sb.append(id).append(" : lastLineNumber = ").append(basicBlock.getLastLineNumber()).append("\n");
        }
    }

    protected static void writeStateEnd(StringBuilder sb, String id, BasicBlock basicBlock, String label) {
        if (basicBlock == END) {
            sb.append(id).append(" : ").append(label).append(" = &#9673;\n");
        }
    }

    protected static void writeLink(StringBuilder sb, BasicBlock basicBlock) {
        if (basicBlock.getFromOffset() > MAX_OFFSET) {
            return;
        }

        String id = getStateId(basicBlock);

        switch (basicBlock.getType()) {
            case TYPE_START:
                sb.append("[*] --> ").append(getStateId(basicBlock.getNext())).append('\n');
                break;
            case TYPE_STATEMENTS:
            case TYPE_GOTO:
            case TYPE_GOTO_IN_TERNARY_OPERATOR:
                writeLink(sb, id, basicBlock.getNext(), "next");
                break;
            case TYPE_CONDITION:
                writeLink(sb, id, basicBlock.getSub1(), "sub1");
                writeLink(sb, id, basicBlock.getSub2(), "sub2");
            case TYPE_CONDITIONAL_BRANCH:
                writeLink(sb, id, basicBlock.getNext(), "next");
                writeLink(sb, id, basicBlock.getBranch(), "branch");
                break;
            case TYPE_SWITCH:
                writeLink(sb, id, basicBlock.getNext(), "next");
            case TYPE_SWITCH_DECLARATION:
                BasicBlock next = basicBlock.getNext();

                for (SwitchCase switchCase : basicBlock.getSwitchCases()) {
                    if ((switchCase.getBasicBlock() != END) && (switchCase.getBasicBlock() != next)) {
                        writeLink(sb, id, switchCase.getBasicBlock(), switchCase.isDefaultCase() ? "default" : "case: " + switchCase.getValue());
                    }
                }
                break;
            case TYPE_TRY:
            case TYPE_TRY_JSR:
            case TYPE_TRY_ECLIPSE:
                writeLink(sb, id, basicBlock.getSub1(), "try");
                writeLink(sb, id, basicBlock.getNext(), "next");

                for (ExceptionHandler exceptionHandler : basicBlock.getExceptionHandlers()) {
                    sb.append(id).append(" --> ").append(getStateId(exceptionHandler.getBasicBlock()));

                    if (exceptionHandler.getInternalThrowableName() == null) {
                        sb.append(" : finally");
                    } else {
                        sb.append(" : catch ").append(exceptionHandler.getInternalThrowableName());

                        if (exceptionHandler.getOtherInternalThrowableNames() != null) {
                            for (String name : exceptionHandler.getOtherInternalThrowableNames()) {
                                sb.append("\\ncatch ").append(name);
                            }
                        }
                    }

                    sb.append('\n');
                }
                break;
            case TYPE_TRY_DECLARATION:
                writeLink(sb, id, basicBlock.getNext(), "try");

                for (BasicBlock.ExceptionHandler exceptionHandler : basicBlock.getExceptionHandlers()) {
                    sb.append(id).append(" --> ").append(getStateId(exceptionHandler.getBasicBlock()));

                    if (exceptionHandler.getInternalThrowableName() == null) {
                        sb.append(" : finally");
                    } else {
                        sb.append(" : catch ").append(exceptionHandler.getInternalThrowableName());

                        if (exceptionHandler.getOtherInternalThrowableNames() != null) {
                            for (String name : exceptionHandler.getOtherInternalThrowableNames()) {
                                sb.append("\\ncatch ").append(name);
                            }
                        }
                    }

                    sb.append('\n');
                }
                break;
            case TYPE_JSR:
                writeLink(sb, id, basicBlock.getNext(), "next");
                writeLink(sb, id, basicBlock.getBranch(), "jsr");
                break;
            case TYPE_LOOP:
                if (basicBlock.getNext() != null) {
                    writeLink(sb, id, basicBlock.getNext(), "next");
                }
                break;
            case TYPE_IF_ELSE:
            case TYPE_TERNARY_OPERATOR:
                writeLink(sb, id, basicBlock.getSub2(), "else");
            case TYPE_IF:
                writeLink(sb, id, basicBlock.getCondition(), "condition");
                writeLink(sb, id, basicBlock.getNext(), "next");
                writeLink(sb, id, basicBlock.getSub1(), "then");
                break;
            case TYPE_CONDITION_TERNARY_OPERATOR:
                writeLink(sb, id, basicBlock.getCondition(), "condition");
            case TYPE_CONDITION_OR:
            case TYPE_CONDITION_AND:
                writeLink(sb, id, basicBlock.getSub1(), "left");
                writeLink(sb, id, basicBlock.getSub2(), "right");
                break;
        }
    }

    protected static void writeLink(StringBuilder sb, String fromId, BasicBlock to, String label) {
        if (to.getFromOffset() > MAX_OFFSET) {
            return;
        }

        if (to == SWITCH_BREAK) {
            sb.append("state \"SWITCH_BREAK\" as switch_break_").append(fromId).append('\n');
            sb.append(fromId).append(" --> switch_break_").append(fromId).append(" : ").append(label).append('\n');
        } else if (to == LOOP_START) {
            sb.append("state \"LOOP_START\" as start_loop_").append(fromId).append('\n');
            sb.append(fromId).append(" --> start_loop_").append(fromId).append(" : ").append(label).append('\n');
        } else if (to == LOOP_CONTINUE) {
            sb.append("state \"LOOP_CONTINUE\" as continue_loop_").append(fromId).append('\n');
            sb.append(fromId).append(" --> continue_loop_").append(fromId).append(" : ").append(label).append('\n');
        } else if (to == LOOP_END) {
            sb.append("state \"LOOP_END\" as end_loop_").append(fromId).append('\n');
            sb.append(fromId).append(" --> end_loop_").append(fromId).append(" : ").append(label).append('\n');
        } else if (to == RETURN) {
            sb.append("state \"RETURN\" as return_").append(fromId).append('\n');
            sb.append(fromId).append(" --> return_").append(fromId).append(" : ").append(label).append('\n');
        } else if (to != END) {
            sb.append(fromId).append(" --> state_").append(to.getIndex()).append(" : ").append(label).append('\n');
        }
    }

    protected static String getStateId(BasicBlock basicBlock) {
        return (basicBlock == END) || (basicBlock == LOOP_END) ? "[*]" : "state_" + basicBlock.getIndex();
    }

    public static String writePlantUMLUrl(String plantuml) throws Exception {
        byte[] input = plantuml.getBytes("UTF-8");

        // Compress
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION, true);

        deflater.setInput(input);
        deflater.finish();

        byte[] output = new byte[input.length * 2];
        int compressedDataLength = deflater.deflate(output);

        if (deflater.finished()) {
            // Encode
            final StringBuilder sb = new StringBuilder((output.length*4 + 2) / 3);

            for (int i=0; i<compressedDataLength; i+=3) {
                append3bytes(
                    sb,
                    output[i] & 0xFF,
                    i+1 < output.length ? output[i+1] & 0xFF : 0,
                    i+2 < output.length ? output[i+2] & 0xFF : 0);
            }

            return PLANTUML_URL_PREFIX + sb.toString();
        } else {
            return null;
        }
    }

    protected static void append3bytes(StringBuilder sb, int b1, int b2, int b3) {
        int c1 = b1 >> 2;
        int c2 = ((b1 & 0x3) << 4) | (b2 >> 4);
        int c3 = ((b2 & 0xF) << 2) | (b3 >> 6);
        int c4 = b3 & 0x3F;

        sb.append(PLANTUML_ENCODE_6_BIT[c1 & 0x3F]);
        sb.append(PLANTUML_ENCODE_6_BIT[c2 & 0x3F]);
        sb.append(PLANTUML_ENCODE_6_BIT[c3 & 0x3F]);
        sb.append(PLANTUML_ENCODE_6_BIT[c4 & 0x3F]);
    }

    public static class BasicBlockComparator implements Comparator<BasicBlock> {
        @Override
        public int compare(BasicBlock bb1, BasicBlock bb2) {
            return bb1.getIndex() - bb2.getIndex();
        }

        @Override
        public boolean equals(Object other) {
            return this==other;
        }
    }
}
