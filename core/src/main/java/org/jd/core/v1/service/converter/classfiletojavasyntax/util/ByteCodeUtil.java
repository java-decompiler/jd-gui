/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.util;

import org.jd.core.v1.model.classfile.ConstantPool;
import org.jd.core.v1.model.classfile.Method;
import org.jd.core.v1.model.classfile.attribute.AttributeCode;
import org.jd.core.v1.model.classfile.constant.*;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.cfg.BasicBlock;

public class ByteCodeUtil {

    public static int searchNextOpcode(BasicBlock basicBlock, int maxOffset) {
        byte[] code = basicBlock.getControlFlowGraph().getMethod().<AttributeCode>getAttribute("Code").getCode();
        int offset = basicBlock.getFromOffset();
        int toOffset = basicBlock.getToOffset();

        if (toOffset > maxOffset) {
            toOffset = maxOffset;
        }

        for (; offset<toOffset; offset++) {
            int opcode = code[offset] & 255;

            switch (opcode) {
                case 16: case 18: // BIPUSH, LDC
                case 21: case 22: case 23: case 24: case 25: // ILOAD, LLOAD, FLOAD, DLOAD, ALOAD
                case 54: case 55: case 56: case 57: case 58: // ISTORE, LSTORE, FSTORE, DSTORE, ASTORE
                case 169: // RET
                case 188: // NEWARRAY
                    offset++;
                    break;
                case 17: // SIPUSH
                case 19: case 20: // LDC_W, LDC2_W
                case 132: // IINC
                case 178: // GETSTATIC
                case 179: // PUTSTATIC
                case 187: // NEW
                case 180: // GETFIELD
                case 181: // PUTFIELD
                case 182: case 183: // INVOKEVIRTUAL, INVOKESPECIAL
                case 184: // INVOKESTATIC
                case 189: // ANEWARRAY
                case 192: // CHECKCAST
                case 193: // INSTANCEOF
                    offset += 2;
                    break;
                case 153: case 154: case 155: case 156: case 157: case 158: // IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE
                case 159: case 160: case 161: case 162: case 163: case 164: case 165: case 166: // IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ, IF_ACMPNE
                case 167: // GOTO
                case 198: case 199: // IFNULL, IFNONNULL
                    int deltaOffset = (short)(((code[++offset] & 255) << 8) | (code[++offset] & 255));

                    if (deltaOffset > 0) {
                        offset += deltaOffset - 2 - 1;
                    }
                    break;
                case 200: // GOTO_W
                    deltaOffset = (((code[++offset] & 255) << 24) | ((code[++offset] & 255) << 16) | ((code[++offset] & 255) << 8) | (code[++offset] & 255));

                    if (deltaOffset > 0) {
                        offset += deltaOffset - 4 - 1;
                    }
                    break;
                case 168: // JSR
                    offset += 2;
                    break;
                case 197: // MULTIANEWARRAY
                    offset += 3;
                    break;
                case 185: // INVOKEINTERFACE
                case 186: // INVOKEDYNAMIC
                    offset += 4;
                    break;
                case 201: // JSR_W
                    offset += 4;
                    break;
                case 170: // TABLESWITCH
                    offset = (offset + 4) & 0xFFFC; // Skip padding
                    offset += 4; // Skip default offset

                    int low = ((code[offset++] & 255) << 24) | ((code[offset++] & 255) << 16) | ((code[offset++] & 255) << 8) | (code[offset++] & 255);
                    int high = ((code[offset++] & 255) << 24) | ((code[offset++] & 255) << 16) | ((code[offset++] & 255) << 8) | (code[offset++] & 255);

                    offset += (4 * (high - low + 1)) - 1;
                    break;
                case 171: // LOOKUPSWITCH
                    offset = (offset + 4) & 0xFFFC; // Skip padding
                    offset += 4; // Skip default offset

                    int count = ((code[offset++] & 255) << 24) | ((code[offset++] & 255) << 16) | ((code[offset++] & 255) << 8) | (code[offset++] & 255);

                    offset += (8 * count) - 1;
                    break;
                case 196: // WIDE
                    opcode = code[++offset] & 255;

                    if (opcode == 132) { // IINC
                        offset += 4;
                    } else {
                        offset += 2;
                    }
                    break;
            }
        }

        if (offset <= maxOffset) {
            return code[offset] & 255;
        } else {
            return 0;
        }
    }

    public static int getLastOpcode(BasicBlock basicBlock) {
        byte[] code = basicBlock.getControlFlowGraph().getMethod().<AttributeCode>getAttribute("Code").getCode();
        int offset = basicBlock.getFromOffset();
        int toOffset = basicBlock.getToOffset();

        if (offset >= toOffset) {
            return 0;
        }

        int lastOffset = offset;

        for (; offset<toOffset; offset++) {
            int opcode = code[offset] & 255;

            lastOffset = offset;

            switch (opcode) {
                case 16: case 18: // BIPUSH, LDC
                case 21: case 22: case 23: case 24: case 25: // ILOAD, LLOAD, FLOAD, DLOAD, ALOAD
                case 54: case 55: case 56: case 57: case 58: // ISTORE, LSTORE, FSTORE, DSTORE, ASTORE
                case 169: // RET
                case 188: // NEWARRAY
                    offset++;
                    break;
                case 17: // SIPUSH
                case 19: case 20: // LDC_W, LDC2_W
                case 132: // IINC
                case 178: // GETSTATIC
                case 179: // PUTSTATIC
                case 187: // NEW
                case 180: // GETFIELD
                case 181: // PUTFIELD
                case 182: case 183: // INVOKEVIRTUAL, INVOKESPECIAL
                case 184: // INVOKESTATIC
                case 189: // ANEWARRAY
                case 192: // CHECKCAST
                case 193: // INSTANCEOF
                    offset += 2;
                    break;
                case 153: case 154: case 155: case 156: case 157: case 158: // IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE
                case 159: case 160: case 161: case 162: case 163: case 164: case 165: case 166: // IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ, IF_ACMPNE
                case 167: // GOTO
                case 198: case 199: // IFNULL, IFNONNULL
                    int deltaOffset = (short)(((code[++offset] & 255) << 8) | (code[++offset] & 255));

                    if (deltaOffset > 0) {
                        offset += deltaOffset - 2 - 1;
                    }
                    break;
                case 200: // GOTO_W
                    deltaOffset = (((code[++offset] & 255) << 24) | ((code[++offset] & 255) << 16) | ((code[++offset] & 255) << 8) | (code[++offset] & 255));

                    if (deltaOffset > 0) {
                        offset += deltaOffset - 4 - 1;
                    }
                    break;
                case 168: // JSR
                    offset += 2;
                    break;
                case 197: // MULTIANEWARRAY
                    offset += 3;
                    break;
                case 185: // INVOKEINTERFACE
                case 186: // INVOKEDYNAMIC
                    offset += 4;
                    break;
                case 201: // JSR_W
                    offset += 4;
                    break;
                case 170: // TABLESWITCH
                    offset = (offset + 4) & 0xFFFC; // Skip padding
                    offset += 4; // Skip default offset

                    int low = ((code[offset++] & 255) << 24) | ((code[offset++] & 255) << 16) | ((code[offset++] & 255) << 8) | (code[offset++] & 255);
                    int high = ((code[offset++] & 255) << 24) | ((code[offset++] & 255) << 16) | ((code[offset++] & 255) << 8) | (code[offset++] & 255);

                    offset += (4 * (high - low + 1)) - 1;
                    break;
                case 171: // LOOKUPSWITCH
                    offset = (offset + 4) & 0xFFFC; // Skip padding
                    offset += 4; // Skip default offset

                    int count = ((code[offset++] & 255) << 24) | ((code[offset++] & 255) << 16) | ((code[offset++] & 255) << 8) | (code[offset++] & 255);

                    offset += (8 * count) - 1;
                    break;
                case 196: // WIDE
                    opcode = code[++offset] & 255;

                    if (opcode == 132) { // IINC
                        offset += 4;
                    } else {
                        offset += 2;
                    }
                    break;
            }
        }

        return code[lastOffset] & 255;
    }

    public static int evalStackDepth(BasicBlock bb) {
        Method method = bb.getControlFlowGraph().getMethod();
        ConstantPool constants = method.getConstants();
        AttributeCode attributeCode = method.getAttribute("Code");
        byte[] code = attributeCode.getCode();
        return evalStackDepth(constants, code, bb);
    }

    public static int evalStackDepth(ConstantPool constants, byte[] code, BasicBlock bb) {
        ConstantMemberRef constantMemberRef;
        ConstantNameAndType constantNameAndType;
        String descriptor;
        int depth = 0;

        for (int offset=bb.getFromOffset(), toOffset=bb.getToOffset(); offset<toOffset; offset++) {
            int opcode = code[offset] & 255;

            switch (opcode) {
                case 1: // ACONST_NULL
                case 2: case 3: case 4: case 5: case 6: case 7: case 8: // ICONST_M1, ICONST_0 ... ICONST_5
                case 9: case 10: case 11: case 12: case 13: case 14: case 15: // LCONST_0, LCONST_1, FCONST_0, FCONST_1, FCONST_2, DCONST_0, DCONST_1
                case 26: case 27: case 28: case 29: // ILOAD_0 ... ILOAD_3
                case 30: case 31: case 32: case 33: // LLOAD_0 ... LLOAD_3
                case 34: case 35: case 36: case 37: // FLOAD_0 ... FLOAD_3
                case 38: case 39: case 40: case 41: // DLOAD_0 ... DLOAD_3
                case 42: case 43: case 44: case 45: // ALOAD_0 ... ALOAD_3
                case 89: case 90: case 91: // DUP, DUP_X1, DUP_X2
                    depth++;
                    break;
                case 16: case 18: // BIPUSH, LDC
                case 21: case 22: case 23: case 24: case 25: // ILOAD, LLOAD, FLOAD, DLOAD, ALOAD
                    offset++;
                    depth++;
                    break;
                case 17: // SIPUSH
                case 19: case 20: // LDC_W, LDC2_W
                case 168: // JSR
                case 178: // GETSTATIC
                case 187: // NEW
                    offset += 2;
                    depth++;
                    break;
                case 46: case 47: case 48: case 49: case 50: case 51: case 52: case 53: // IALOAD, LALOAD, FALOAD, DALOAD, AALOAD, BALOAD, CALOAD, SALOAD
                case 59: case 60: case 61: case 62: // ISTORE_0 ... ISTORE_3
                case 63: case 64: case 65: case 66: // LSTORE_0 ... LSTORE_3
                case 67: case 68: case 69: case 70: // FSTORE_0 ... FSTORE_3
                case 71: case 72: case 73: case 74: // DSTORE_0 ... DSTORE_3
                case 75: case 76: case 77: case 78: // ASTORE_0 ... ASTORE_3
                case 87: // POP
                case 96: case 97: case 98: case 99:     // IADD, LADD, FADD, DADD
                case 100: case 101: case 102: case 103: // ISUB, LSUB, FSUB, DSUB
                case 104: case 105: case 106: case 107: // IMUL, LMUL, FMUL, DMUL
                case 108: case 109: case 110: case 111: // IDIV, LDIV, FDIV, DDIV
                case 112: case 113: case 114: case 115: // IREM, LREM, FREM, DREM
                case 120: case 121: // ISHL, LSHL
                case 122: case 123: // ISHR, LSHR
                case 124: case 125: // IUSHR, LUSHR
                case 126: case 127: // IAND, LAND
                case 128: case 129: // IOR, LOR
                case 130: case 131: // IXOR, LXOR
                case 148: case 149: case 150: case 151: case 152: // LCMP, FCMPL, FCMPG, DCMPL, DCMPG
                case 172: case 173: case 174: case 175: case 176: // IRETURN, LRETURN, FRETURN, DRETURN, ARETURN
                case 194: case 195: // MONITORENTER, MONITOREXIT
                    depth--;
                    break;
                case 153: case 154: case 155: case 156: case 157: case 158: // IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE
                case 179: // PUTSTATIC
                case 198: case 199: // IFNULL, IFNONNULL
                    offset += 2;
                    depth--;
                    break;
                case 54: case 55: case 56: case 57: case 58: // ISTORE, LSTORE, FSTORE, DSTORE, ASTORE
                    offset++;
                    depth--;
                    break;
                case 79: case 80: case 81: case 82: case 83: case 84: case 85: case 86: // IASTORE, LASTORE, FASTORE, DASTORE, AASTORE, BASTORE, CASTORE, SASTORE
                    depth -= 3;
                    break;
                case 92: case 93: case 94: // DUP2, DUP2_X1, DUP2_X2
                    depth += 2;
                    break;
                case 132: // IINC
                case 167: // GOTO
                case 180: // GETFIELD
                case 189: // ANEWARRAY
                case 192: // CHECKCAST
                case 193: // INSTANCEOF
                    offset += 2;
                    break;
                case 159: case 160: case 161: case 162: case 163: case 164: case 165: case 166: // IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ, IF_ACMPNE
                case 181: // PUTFIELD
                    offset += 2;
                    depth -= 2;
                    break;
                case 88: // POP2
                    depth -= 2;
                    break;
                case 169: // RET
                case 188: // NEWARRAY
                    offset++;
                    break;
                case 170: // TABLESWITCH
                    offset = (offset + 4) & 0xFFFC; // Skip padding
                    offset += 4; // Skip default offset

                    int low = ((code[offset++] & 255) << 24) | ((code[offset++] & 255) << 16) | ((code[offset++] & 255) << 8) | (code[offset++] & 255);
                    int high = ((code[offset++] & 255) << 24) | ((code[offset++] & 255) << 16) | ((code[offset++] & 255) << 8) | (code[offset++] & 255);

                    offset += (4 * (high - low + 1)) - 1;
                    depth--;
                    break;
                case 171: // LOOKUPSWITCH
                    offset = (offset + 4) & 0xFFFC; // Skip padding
                    offset += 4; // Skip default offset

                    int count = ((code[offset++] & 255) << 24) | ((code[offset++] & 255) << 16) | ((code[offset++] & 255) << 8) | (code[offset++] & 255);

                    offset += (8 * count) - 1;
                    depth--;
                    break;
                case 182: case 183: // INVOKEVIRTUAL, INVOKESPECIAL
                    constantMemberRef = constants.getConstant(((code[++offset] & 255) << 8) | (code[++offset] & 255));
                    constantNameAndType = constants.getConstant(constantMemberRef.getNameAndTypeIndex());
                    descriptor = constants.getConstantUtf8(constantNameAndType.getDescriptorIndex());
                    depth -= 1 + countMethodParameters(descriptor);

                    if (descriptor.charAt(descriptor.length()-1) != 'V') {
                        depth++;
                    }
                    break;
                case 184: // INVOKESTATIC
                    constantMemberRef = constants.getConstant(((code[++offset] & 255) << 8) | (code[++offset] & 255));
                    constantNameAndType = constants.getConstant(constantMemberRef.getNameAndTypeIndex());
                    descriptor = constants.getConstantUtf8(constantNameAndType.getDescriptorIndex());
                    depth -= countMethodParameters(descriptor);

                    if (descriptor.charAt(descriptor.length()-1) != 'V') {
                        depth++;
                    }
                    break;
                case 185: // INVOKEINTERFACE
                    constantMemberRef = constants.getConstant(((code[++offset] & 255) << 8) | (code[++offset] & 255));
                    constantNameAndType = constants.getConstant(constantMemberRef.getNameAndTypeIndex());
                    descriptor = constants.getConstantUtf8(constantNameAndType.getDescriptorIndex());
                    depth -= 1 + countMethodParameters(descriptor);
                    offset += 2; // Skip 'count' and one byte

                    if (descriptor.charAt(descriptor.length()-1) != 'V') {
                        depth++;
                    }
                    break;
                case 186: // INVOKEDYNAMIC
                    constantMemberRef = constants.getConstant(((code[++offset] & 255) << 8) | (code[++offset] & 255));
                    constantNameAndType = constants.getConstant(constantMemberRef.getNameAndTypeIndex());
                    descriptor = constants.getConstantUtf8(constantNameAndType.getDescriptorIndex());
                    depth -= countMethodParameters(descriptor);
                    offset += 2; // Skip 2 bytes

                    if (descriptor.charAt(descriptor.length()-1) != 'V') {
                        depth++;
                    }
                    break;
                case 196: // WIDE
                    opcode = code[++offset] & 255;

                    if (opcode == 132) { // IINC
                        offset += 4;
                    } else {
                        offset += 2;

                        switch (opcode) {
                            case 21: case 22: case 23: case 24: case 25: // ILOAD, LLOAD, FLOAD, DLOAD, ALOAD
                                depth++;
                                break;
                            case 54: case 55: case 56: case 57: case 58: // ISTORE, LSTORE, FSTORE, DSTORE, ASTORE
                                depth--;
                                break;
                            case 169: // RET
                                break;
                        }
                    }
                    break;
                case 197: // MULTIANEWARRAY
                    offset += 3;
                    depth += 1 - (code[offset] & 255);
                    break;
                case 201: // JSR_W
                    offset += 4;
                    depth++;
                case 200: // GOTO_W
                    offset += 4;
                    break;
            }
        }

        return depth;
    }

    public static int getMinDepth(BasicBlock bb) {
        Method method = bb.getControlFlowGraph().getMethod();
        ConstantPool constants = method.getConstants();
        AttributeCode attributeCode = method.getAttribute("Code");
        byte[] code = attributeCode.getCode();
        return getMinDepth(constants, code, bb);
    }

    private static int getMinDepth(ConstantPool constants, byte[] code, BasicBlock bb) {
        ConstantMemberRef constantMemberRef;
        ConstantNameAndType constantNameAndType;
        String descriptor;
        int depth = 0;
        int minDepth = 0;

        for (int offset=bb.getFromOffset(), toOffset=bb.getToOffset(); offset<toOffset; offset++) {
            int opcode = code[offset] & 255;

            switch (opcode) {
                case 1: // ACONST_NULL
                case 2: case 3: case 4: case 5: case 6: case 7: case 8: // ICONST_M1, ICONST_0 ... ICONST_5
                case 9: case 10: case 11: case 12: case 13: case 14: case 15: // LCONST_0, LCONST_1, FCONST_0, FCONST_1, FCONST_2, DCONST_0, DCONST_1
                case 26: case 27: case 28: case 29: // ILOAD_0 ... ILOAD_3
                case 30: case 31: case 32: case 33: // LLOAD_0 ... LLOAD_3
                case 34: case 35: case 36: case 37: // FLOAD_0 ... FLOAD_3
                case 38: case 39: case 40: case 41: // DLOAD_0 ... DLOAD_3
                case 42: case 43: case 44: case 45: // ALOAD_0 ... ALOAD_3
                    depth++;
                    break;
                case 89: // DUP
                    depth--;
                    if (minDepth > depth) minDepth = depth;
                    depth += 2;
                    break;
                case 90: // DUP_X1
                    depth -= 2;
                    if (minDepth > depth) minDepth = depth;
                    depth += 3;
                    break;
                case 91: // DUP_X2
                    depth -= 3;
                    if (minDepth > depth) minDepth = depth;
                    depth += 4;
                    break;
                case 16: case 18: // BIPUSH, LDC
                case 21: case 22: case 23: case 24: case 25: // ILOAD, LLOAD, FLOAD, DLOAD, ALOAD
                    offset++;
                    depth++;
                    break;
                case 17: // SIPUSH
                case 19: case 20: // LDC_W, LDC2_W
                case 168: // JSR
                case 178: // GETSTATIC
                case 187: // NEW
                    offset += 2;
                    depth++;
                    break;
                case 46: case 47: case 48: case 49: case 50: case 51: case 52: case 53: // IALOAD, LALOAD, FALOAD, DALOAD, AALOAD, BALOAD, CALOAD, SALOAD
                case 96: case 97: case 98: case 99:     // IADD, LADD, FADD, DADD
                case 100: case 101: case 102: case 103: // ISUB, LSUB, FSUB, DSUB
                case 104: case 105: case 106: case 107: // IMUL, LMUL, FMUL, DMUL
                case 108: case 109: case 110: case 111: // IDIV, LDIV, FDIV, DDIV
                case 112: case 113: case 114: case 115: // IREM, LREM, FREM, DREM
                case 120: case 121: // ISHL, LSHL
                case 122: case 123: // ISHR, LSHR
                case 124: case 125: // IUSHR, LUSHR
                case 126: case 127: // IAND, LAND
                case 128: case 129: // IOR, LOR
                case 130: case 131: // IXOR, LXOR
                case 148: case 149: case 150: case 151: case 152: // LCMP, FCMPL, FCMPG, DCMPL, DCMPG
                    depth -= 2;
                    if (minDepth > depth) minDepth = depth;
                    depth++;
                    break;
                case 59: case 60: case 61: case 62: // ISTORE_0 ... ISTORE_3
                case 63: case 64: case 65: case 66: // LSTORE_0 ... LSTORE_3
                case 67: case 68: case 69: case 70: // FSTORE_0 ... FSTORE_3
                case 71: case 72: case 73: case 74: // DSTORE_0 ... DSTORE_3
                case 75: case 76: case 77: case 78: // ASTORE_0 ... ASTORE_3
                case 87: // POP
                case 172: case 173: case 174: case 175: case 176: // IRETURN, LRETURN, FRETURN, DRETURN, ARETURN
                case 194: case 195: // MONITORENTER, MONITOREXIT
                    depth--;
                    if (minDepth > depth) minDepth = depth;
                    break;
                case 153: case 154: case 155: case 156: case 157: case 158: // IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE
                case 179: // PUTSTATIC
                case 198: case 199: // IFNULL, IFNONNULL
                    offset += 2;
                    depth--;
                    if (minDepth > depth) minDepth = depth;
                    break;
                case 54: case 55: case 56: case 57: case 58: // ISTORE, LSTORE, FSTORE, DSTORE, ASTORE
                    offset++;
                    depth--;
                    if (minDepth > depth) minDepth = depth;
                    break;
                case 79: case 80: case 81: case 82: case 83: case 84: case 85: case 86: // IASTORE, LASTORE, FASTORE, DASTORE, AASTORE, BASTORE, CASTORE, SASTORE
                    depth -= 3;
                    if (minDepth > depth) minDepth = depth;
                    break;
                case 92: // DUP2
                    depth -= 2;
                    if (minDepth > depth) minDepth = depth;
                    depth += 4;
                    break;
                case 93: // DUP2_X1
                    depth -= 3;
                    if (minDepth > depth) minDepth = depth;
                    depth += 5;
                    break;
                case 94: // DUP2_X2
                    depth -= 4;
                    if (minDepth > depth) minDepth = depth;
                    depth += 6;
                    break;
                case 132: // IINC
                case 167: // GOTO
                    offset += 2;
                    break;
                case 180: // GETFIELD
                case 189: // ANEWARRAY
                case 192: // CHECKCAST
                case 193: // INSTANCEOF
                    offset += 2;
                    depth--;
                    if (minDepth > depth) minDepth = depth;
                    depth++;
                    break;
                case 159: case 160: case 161: case 162: case 163: case 164: case 165: case 166: // IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ, IF_ACMPNE
                case 181: // PUTFIELD
                    offset += 2;
                    depth -= 2;
                    if (minDepth > depth) minDepth = depth;
                    break;
                case 88: // POP2
                    depth -= 2;
                    if (minDepth > depth) minDepth = depth;
                    break;
                case 169: // RET
                    offset++;
                    break;
                case 188: // NEWARRAY
                    offset++;
                    depth--;
                    if (minDepth > depth) minDepth = depth;
                    depth++;
                    break;
                case 170: // TABLESWITCH
                    offset = (offset + 4) & 0xFFFC; // Skip padding
                    offset += 4; // Skip default offset

                    int low = ((code[offset++] & 255) << 24) | ((code[offset++] & 255) << 16) | ((code[offset++] & 255) << 8) | (code[offset++] & 255);
                    int high = ((code[offset++] & 255) << 24) | ((code[offset++] & 255) << 16) | ((code[offset++] & 255) << 8) | (code[offset++] & 255);

                    offset += (4 * (high - low + 1)) - 1;
                    depth--;
                    if (minDepth > depth) minDepth = depth;
                    break;
                case 171: // LOOKUPSWITCH
                    offset = (offset + 4) & 0xFFFC; // Skip padding
                    offset += 4; // Skip default offset

                    int count = ((code[offset++] & 255) << 24) | ((code[offset++] & 255) << 16) | ((code[offset++] & 255) << 8) | (code[offset++] & 255);

                    offset += (8 * count) - 1;
                    depth--;
                    if (minDepth > depth) minDepth = depth;
                    break;
                case 182: case 183: // INVOKEVIRTUAL, INVOKESPECIAL
                    constantMemberRef = constants.getConstant(((code[++offset] & 255) << 8) | (code[++offset] & 255));
                    constantNameAndType = constants.getConstant(constantMemberRef.getNameAndTypeIndex());
                    descriptor = constants.getConstantUtf8(constantNameAndType.getDescriptorIndex());
                    depth -= 1 + countMethodParameters(descriptor);
                    if (minDepth > depth) minDepth = depth;

                    if (descriptor.charAt(descriptor.length()-1) != 'V') {
                        depth++;
                    }
                    break;
                case 184: // INVOKESTATIC
                    constantMemberRef = constants.getConstant(((code[++offset] & 255) << 8) | (code[++offset] & 255));
                    constantNameAndType = constants.getConstant(constantMemberRef.getNameAndTypeIndex());
                    descriptor = constants.getConstantUtf8(constantNameAndType.getDescriptorIndex());
                    depth -= countMethodParameters(descriptor);
                    if (minDepth > depth) minDepth = depth;

                    if (descriptor.charAt(descriptor.length()-1) != 'V') {
                        depth++;
                    }
                    break;
                case 185: // INVOKEINTERFACE
                    constantMemberRef = constants.getConstant(((code[++offset] & 255) << 8) | (code[++offset] & 255));
                    constantNameAndType = constants.getConstant(constantMemberRef.getNameAndTypeIndex());
                    descriptor = constants.getConstantUtf8(constantNameAndType.getDescriptorIndex());
                    depth -= 1 + countMethodParameters(descriptor);
                    if (minDepth > depth) minDepth = depth;
                    offset += 2; // Skip 'count' and one byte

                    if (descriptor.charAt(descriptor.length()-1) != 'V') {
                        depth++;
                    }
                    break;
                case 186: // INVOKEDYNAMIC
                    constantMemberRef = constants.getConstant(((code[++offset] & 255) << 8) | (code[++offset] & 255));
                    constantNameAndType = constants.getConstant(constantMemberRef.getNameAndTypeIndex());
                    descriptor = constants.getConstantUtf8(constantNameAndType.getDescriptorIndex());
                    depth -= countMethodParameters(descriptor);
                    if (minDepth > depth) minDepth = depth;
                    offset += 2; // Skip 2 bytes

                    if (descriptor.charAt(descriptor.length()-1) != 'V') {
                        depth++;
                    }
                    break;
                case 196: // WIDE
                    opcode = code[++offset] & 255;

                    if (opcode == 132) { // IINC
                        offset += 4;
                    } else {
                        offset += 2;

                        switch (opcode) {
                            case 21: case 22: case 23: case 24: case 25: // ILOAD, LLOAD, FLOAD, DLOAD, ALOAD
                                depth++;
                                break;
                            case 54: case 55: case 56: case 57: case 58: // ISTORE, LSTORE, FSTORE, DSTORE, ASTORE
                                depth--;
                                if (minDepth > depth) minDepth = depth;
                                break;
                            case 169: // RET
                                break;
                        }
                    }
                    break;
                case 197: // MULTIANEWARRAY
                    offset += 3;
                    depth -= (code[offset] & 255);
                    if (minDepth > depth) minDepth = depth;
                    depth++;
                    break;
                case 201: // JSR_W
                    offset += 4;
                    depth++;
                case 200: // GOTO_W
                    offset += 4;
                    break;
            }
        }

        return minDepth;
    }

    private static int countMethodParameters(String descriptor) {
        int count = 0;
        int i = 2;
        char c = descriptor.charAt(1);

        assert (descriptor.length() > 2) && (descriptor.charAt(0) == '(');

        while (c != ')') {
            while (c == '[') {
                c = descriptor.charAt(i++);
            }
            if (c == 'L') {
                do {
                    c = descriptor.charAt(i++);
                } while (c != ';');
            }
            c = descriptor.charAt(i++);
            count++;
        }

        return count;
    }
}
