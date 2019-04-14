/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.util.decompiler;

import org.jd.core.v1.api.printer.Printer;

public class NopPrinter implements Printer {
	@Override public void start(int maxLineNumber, int majorVersion, int minorVersion) {}
	@Override public void end() {}

	@Override public void printText(String text) {}
	@Override public void printNumericConstant(String constant) {}
	@Override public void printStringConstant(String constant, String ownerInternalName) {}
	@Override public void printKeyword(String keyword) {}

	@Override public void printDeclaration(int flags, String internalTypeName, String name, String descriptor) {}
	@Override public void printReference(int flags, String internalTypeName, String name, String descriptor, String ownerInternalName) {}

	@Override public void indent() {}
	@Override public void unindent() {}

	@Override public void startLine(int lineNumber) {}
	@Override public void endLine() {}
	@Override public void extraLine(int count) {}

	@Override public void startMarker(int type) {}
	@Override public void endMarker(int type) {}
}
