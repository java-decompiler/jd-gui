/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.util.decompiler;

import jd.core.printer.Printer;

public abstract class ClassFileSourcePrinter implements Printer
{
    protected static final String TAB = "  ";
    protected static final String NEWLINE = "\n";

    protected int maxLineNumber = 0;
    protected int indentationCount;
    protected boolean display;

    protected abstract boolean getRealignmentLineNumber();
    protected abstract boolean isShowPrefixThis();
    protected abstract boolean isUnicodeEscape();

    protected abstract void append(char c);
    protected abstract void append(String s);

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //

    public void print(byte b) { append(String.valueOf(b)); }
    public void print(int i) { append(String.valueOf(i)); }

    public void print(char c) {
        if (this.display)
            append(c);
    }

    public void print(String s) {
        if (this.display)
            printEscape(s);
    }

    public void printNumeric(String s) { append(s); }

    public void printString(String s, String scopeInternalName)  { append(s); }

    public void printKeyword(String s) {
        if (this.display)
            append(s);
    }

    public void printJavaWord(String s) { append(s); }

    public void printType(String internalName, String name, String scopeInternalName) {
        if (this.display)
            printEscape(name);
    }

    public void printTypeDeclaration(String internalName, String name) {
        printEscape(name);
    }

    public void printTypeImport(String internalName, String name) {
        printEscape(name);
    }

    public void printField(String internalName, String name, String descriptor, String scopeInternalName) {
        printEscape(name);
    }
    public void printFieldDeclaration(String internalName, String name, String descriptor) {
        printEscape(name);
    }

    public void printStaticField(String internalName, String name, String descriptor, String scopeInternalName) {
        printEscape(name);
    }
    public void printStaticFieldDeclaration(String internalName, String name, String descriptor) {
        printEscape(name);
    }

    public void printConstructor(String internalName, String name, String descriptor, String scopeInternalName) {
        printEscape(name);
    }
    public void printConstructorDeclaration(String internalName, String name, String descriptor) {
        printEscape(name);
    }

    public void printStaticConstructorDeclaration(String internalName, String name) {
        append(name);
    }

    public void printMethod(String internalName, String name, String descriptor, String scopeInternalName) {
        printEscape(name);
    }
    public void printMethodDeclaration(String internalName, String name, String descriptor) {
        printEscape(name);
    }

    public void printStaticMethod(String internalName, String name, String descriptor, String scopeInternalName) {
        printEscape(name);
    }
    public void printStaticMethodDeclaration(String internalName, String name, String descriptor) {
        printEscape(name);
    }

    public void start(int maxLineNumber, int majorVersion, int minorVersion) {
        this.indentationCount = 0;
        this.display = true;
        this.maxLineNumber = maxLineNumber;
    }

    public void end() {}

    public void indent() {
        this.indentationCount++;
    }
    public void desindent() {
        if (this.indentationCount > 0)
            this.indentationCount--;
    }

    public void startOfLine(int lineNumber) {
        for (int i=0; i<indentationCount; i++)
            append(TAB);
    }

    public void endOfLine() { append(NEWLINE); }

    public void extraLine(int count) {
        if (getRealignmentLineNumber()) {
            while (count-- > 0) {
                append(NEWLINE);
            }
        }
    }

    public void startOfComment() {}
    public void endOfComment() {}

    public void startOfJavadoc() {}
    public void endOfJavadoc() {}

    public void startOfXdoclet() {}
    public void endOfXdoclet() {}

    public void startOfError() {}
    public void endOfError() {}

    public void startOfImportStatements() {}
    public void endOfImportStatements() {}

    public void startOfTypeDeclaration(String internalPath) {}
    public void endOfTypeDeclaration() {}

    public void startOfAnnotationName() {}
    public void endOfAnnotationName() {}

    public void startOfOptionalPrefix() {
        if (isShowPrefixThis() == false)
            this.display = false;
    }

    public void endOfOptionalPrefix() {
        this.display = true;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //

    public void debugStartOfLayoutBlock() {}
    public void debugEndOfLayoutBlock() {}

    public void debugStartOfSeparatorLayoutBlock() {}
    public void debugEndOfSeparatorLayoutBlock(int min, int value, int max) {}

    public void debugStartOfStatementsBlockLayoutBlock() {}
    public void debugEndOfStatementsBlockLayoutBlock(int min, int value, int max) {}

    public void debugStartOfInstructionBlockLayoutBlock() {}
    public void debugEndOfInstructionBlockLayoutBlock() {}

    public void debugStartOfCommentDeprecatedLayoutBlock() {}
    public void debugEndOfCommentDeprecatedLayoutBlock() {}

    public void debugMarker(String marker) {}

    public void debugStartOfCaseBlockLayoutBlock() {}
    public void debugEndOfCaseBlockLayoutBlock() {}

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //

    protected void printEscape(String s) {
        if (isUnicodeEscape()) {
            int length = s.length();

            for (int i=0; i<length; i++) {
                char c = s.charAt(i);

                if (c == '\t') {
                    append(c);
                } else if (c < 32) {
                    // Write octal format
                    append("\\0");
                    append((char) ('0' + (c >> 3)));
                    append((char) ('0' + (c & 0x7)));
                } else if (c > 127) {
                    // Write octal format
                    append("\\u");

                    int z = (c >> 12);
                    append((char) ((z <= 9) ? ('0' + z) : (('A' - 10) + z)));
                    z = ((c >> 8) & 0xF);
                    append((char) ((z <= 9) ? ('0' + z) : (('A' - 10) + z)));
                    z = ((c >> 4) & 0xF);
                    append((char) ((z <= 9) ? ('0' + z) : (('A' - 10) + z)));
                    z = (c & 0xF);
                    append((char) ((z <= 9) ? ('0' + z) : (('A' - 10) + z)));
                } else {
                    append(c);
                }
            }
        } else {
            append(s);
        }
    }
}
