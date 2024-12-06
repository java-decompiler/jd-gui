/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.api.printer;


public interface Printer {
    void start(int maxLineNumber, int majorVersion, int minorVersion);
    void end();

    void printText(String text);
    void printNumericConstant(String constant);
    void printStringConstant(String constant, String ownerInternalName);
    void printKeyword(String keyword);

    // Declaration & reference types
    int TYPE = 1;
    int FIELD = 2;
    int METHOD = 3;
    int CONSTRUCTOR = 4;
    int PACKAGE = 5;
    int MODULE = 6;

    void printDeclaration(int type, String internalTypeName, String name, String descriptor);
    void printReference(int type, String internalTypeName, String name, String descriptor, String ownerInternalName);

    void indent();
    void unindent();

    int UNKNOWN_LINE_NUMBER = 0;

    void startLine(int lineNumber);
    void endLine();
    void extraLine(int count);

    // Marker types
    int COMMENT = 1;
    int JAVADOC = 2;
    int ERROR = 3;
    int IMPORT_STATEMENTS = 4;

    void startMarker(int type);
    void endMarker(int type);
}
