/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.util.decompiler;

public class LineNumberStringBuilderPrinter extends StringBuilderPrinter {
    protected boolean showLineNumbers = false;

    protected int maxLineNumber = 0;
    protected int digitCount = 0;

    protected String lineNumberBeginPrefix;
    protected String lineNumberEndPrefix;
    protected String unknownLineNumberPrefix;

    public void setShowLineNumbers(boolean showLineNumbers) { this.showLineNumbers = showLineNumbers; }

    protected int printDigit(int dcv, int lineNumber, int divisor, int left) {
        if (digitCount >= dcv) {
            if (lineNumber < divisor) {
                stringBuffer.append(' ');
            } else {
                int e = (lineNumber-left) / divisor;
                stringBuffer.append((char)('0' + e));
                left += e*divisor;
            }
        }

        return left;
    }

    // --- Printer --- //
    @Override
    public void start(int maxLineNumber, int majorVersion, int minorVersion) {
        super.start(maxLineNumber, majorVersion, minorVersion);

        if (showLineNumbers) {
            this.maxLineNumber = maxLineNumber;

            if (maxLineNumber > 0) {
                digitCount = 1;
                unknownLineNumberPrefix = " ";
                int maximum = 9;

                while (maximum < maxLineNumber) {
                    digitCount++;
                    unknownLineNumberPrefix += ' ';
                    maximum = maximum*10 + 9;
                }

                lineNumberBeginPrefix = "/* ";
                lineNumberEndPrefix = " */ ";
            } else {
                unknownLineNumberPrefix = "";
                lineNumberBeginPrefix = "";
                lineNumberEndPrefix = "";
            }
        } else {
            this.maxLineNumber = 0;
            unknownLineNumberPrefix = "";
            lineNumberBeginPrefix = "";
            lineNumberEndPrefix = "";
        }
    }

    @Override public void startLine(int lineNumber) {
        if (maxLineNumber > 0) {
            stringBuffer.append(lineNumberBeginPrefix);

            if (lineNumber == UNKNOWN_LINE_NUMBER) {
                stringBuffer.append(unknownLineNumberPrefix);
            } else {
                int left = 0;

                left = printDigit(5, lineNumber, 10000, left);
                left = printDigit(4, lineNumber,  1000, left);
                left = printDigit(3, lineNumber,   100, left);
                left = printDigit(2, lineNumber,    10, left);
                stringBuffer.append((char)('0' + (lineNumber-left)));
            }

            stringBuffer.append(lineNumberEndPrefix);
        }

        for (int i=0; i<indentationCount; i++) {
            stringBuffer.append(TAB);
        }
    }
    @Override public void extraLine(int count) {
        if (realignmentLineNumber) {
            while (count-- > 0) {
                if (maxLineNumber > 0) {
                    stringBuffer.append(lineNumberBeginPrefix);
                    stringBuffer.append(unknownLineNumberPrefix);
                    stringBuffer.append(lineNumberEndPrefix);
                }

                stringBuffer.append(NEWLINE);
            }
        }
    }
}
