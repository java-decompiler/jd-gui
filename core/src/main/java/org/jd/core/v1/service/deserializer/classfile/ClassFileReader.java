/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.deserializer.classfile;

import java.io.UTFDataFormatException;

public class ClassFileReader {
    public static final int JAVA_MAGIC_NUMBER = 0xCafeBabe;

    protected byte[] data;
    protected int    offset = 0;

    public ClassFileReader(byte[] data) {
        this.data = data;
    }

    public void skip(int length) {
        offset += length;
    }

    public byte readByte() {
        return data[offset++];
    }

    public int readUnsignedByte() {
        return (data[offset++] & 0xff);
    }

    public int readUnsignedShort() {
        return ((data[offset++] & 0xff) << 8) | (data[offset++] & 0xff);
    }

    public final int readInt() {
        return ((data[offset++] & 0xff) << 24) | ((data[offset++] & 0xff) << 16) | ((data[offset++] & 0xff) << 8) | (data[offset++] & 0xff);
    }

    public final float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    public long readLong() {
        long hi  = ((data[offset++] & 0xff) << 24) | ((data[offset++] & 0xff) << 16) | ((data[offset++] & 0xff) << 8) | (data[offset++] & 0xff);
        long low = ((data[offset++] & 0xff) << 24) | ((data[offset++] & 0xff) << 16) | ((data[offset++] & 0xff) << 8) | (data[offset++] & 0xff);
        return ((hi & 0xffffffffL) << 32) | (low  & 0xffffffffL);
    }

    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    public void readFully(byte target[]) {
        int length = target.length;
        System.arraycopy(data, offset, target, 0, length);
        offset += length;
    }

    public String readUTF8() throws UTFDataFormatException {
        int utflenx = readUnsignedShort();

        char[] charArray = new char[utflenx];
        int maxOffset = offset + utflenx;
        int c, char2, char3;
        int charArrayOffset = 0;

        while (offset < maxOffset) {
            c = (int) data[offset++] & 0xff;
            switch (c >> 4) {
                case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
                    /* 0xxxxxxx*/
                    charArray[charArrayOffset++] = (char)c;
                    break;
                case 12: case 13:
                    /* 110x xxxx   10xx xxxx*/
                    if (offset+1 > maxOffset)
                        throw new UTFDataFormatException("malformed input: partial character at end");
                    char2 = (int)data[offset++];
                    if ((char2 & 0xC0) != 0x80)
                        throw new UTFDataFormatException("malformed input around byte " + offset);
                    charArray[charArrayOffset++] = (char)(((c & 0x1F) << 6) | (char2 & 0x3F));
                    break;
                case 14:
                    /* 1110 xxxx  10xx xxxx  10xx xxxx */
                    if (offset+2 > maxOffset)
                        throw new UTFDataFormatException("malformed input: partial character at end");
                    char2 = (int)data[offset++];
                    char3 = (int)data[offset++];
                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                        throw new UTFDataFormatException("malformed input around byte " + (offset-1));
                    charArray[charArrayOffset++] = (char)(((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
                    break;
                default:
                    /* 10xx xxxx,  1111 xxxx */
                    throw new UTFDataFormatException("malformed input around byte " + offset);
            }
        }

        // The number of chars produced may be less than utflen
        return new String(charArray, 0, charArrayOffset);
    }
}
