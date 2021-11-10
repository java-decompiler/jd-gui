/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.util.decompiler;

import org.jd.core.v1.api.printer.Printer;

public class StringBuilderPrinter implements Printer {
    protected static final String TAB = "  ";
    protected static final String NEWLINE = "\n";

    protected StringBuilder stringBuffer = new StringBuilder(10*1024);

    protected boolean unicodeEscape = true;
    protected boolean realignmentLineNumber = false;

    protected int majorVersion = 0;
    protected int minorVersion = 0;
    protected int indentationCount;

    public void setUnicodeEscape(boolean unicodeEscape) { this.unicodeEscape = unicodeEscape; }
    public void setRealignmentLineNumber(boolean realignmentLineNumber) { this.realignmentLineNumber = realignmentLineNumber; }

    public int getMajorVersion() { return majorVersion; }
    public int getMinorVersion() { return minorVersion; }
    public StringBuilder getStringBuffer() { return stringBuffer; }

    protected void escape(String s) {
        if (unicodeEscape && (s != null)) {
            int length = s.length();

            for (int i=0; i<length; i++) {
                char c = s.charAt(i);

                if (c == '\t') {
                    stringBuffer.append(c);
                } else if (c < 32) {
                    // Write octal format
                    stringBuffer.append("\\0");
                    stringBuffer.append((char) ('0' + (c >> 3)));
                    stringBuffer.append((char) ('0' + (c & 0x7)));
                } else if (c > 127) {
                    // Write octal format
                    stringBuffer.append("\\u");

                    int z = (c >> 12);
                    stringBuffer.append((char) ((z <= 9) ? ('0' + z) : (('A' - 10) + z)));
                    z = ((c >> 8) & 0xF);
                    stringBuffer.append((char) ((z <= 9) ? ('0' + z) : (('A' - 10) + z)));
                    z = ((c >> 4) & 0xF);
                    stringBuffer.append((char) ((z <= 9) ? ('0' + z) : (('A' - 10) + z)));
                    z = (c & 0xF);
                    stringBuffer.append((char) ((z <= 9) ? ('0' + z) : (('A' - 10) + z)));
                } else {
                    stringBuffer.append(c);
                }
            }
        } else {
            stringBuffer.append(s);
        }
    }

    // --- Printer --- //
    @Override
    public void start(int maxLineNumber, int majorVersion, int minorVersion) {
        this.stringBuffer.setLength(0);
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.indentationCount = 0;
    }

    @Override public void end() {}

    @Override public void printText(String text) { escape(text); }
    @Override public void printNumericConstant(String constant) { escape(constant); }
    @Override public void printStringConstant(String constant, String ownerInternalName) { escape(constant); }
    @Override public void printKeyword(String keyword) { stringBuffer.append(keyword); }

    @Override public void printDeclaration(int type, String internalTypeName, String name, String descriptor) { escape(name); }
    @Override public void printReference(int type, String internalTypeName, String name, String descriptor, String ownerInternalName) { escape(name); }

    @Override public void indent() { indentationCount++; }
    @Override public void unindent() { if (indentationCount > 0) indentationCount--; }

    @Override public void startLine(int lineNumber) { for (int i=0; i<indentationCount; i++) stringBuffer.append(TAB); }
    @Override public void endLine() { stringBuffer.append(NEWLINE); }
    @Override public void extraLine(int count) { if (realignmentLineNumber) while (count-- > 0) stringBuffer.append(NEWLINE); }

    @Override public void startMarker(int type) {}
    @Override public void endMarker(int type) {}
}
