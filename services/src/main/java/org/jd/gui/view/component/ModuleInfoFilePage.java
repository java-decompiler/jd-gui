/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view.component;

import org.fife.ui.rsyntaxtextarea.*;
import org.jd.gui.api.API;
import org.jd.gui.api.model.Container;
import org.jd.gui.util.decompiler.ContainerLoader;
import org.jd.gui.util.exception.ExceptionUtil;

import javax.swing.text.Segment;
import java.util.Map;

public class ModuleInfoFilePage extends ClassFilePage {
    public static final String SYNTAX_STYLE_JAVA_MODULE = "text/java-module";

    static {
        // Add a new token maker for Java 9+ module
        AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory)TokenMakerFactory.getDefaultInstance();
        atmf.putMapping(SYNTAX_STYLE_JAVA_MODULE, ModuleInfoTokenMaker.class.getName());
    }

    public ModuleInfoFilePage(API api, Container.Entry entry) {
        super(api, entry);
    }

    public void decompile(Map<String, String> preferences) {
        try {
            // Clear ...
            clearHyperlinks();
            clearLineNumbers();
            typeDeclarations.clear();

            // Init preferences
            boolean unicodeEscape = getPreferenceValue(preferences, ESCAPE_UNICODE_CHARACTERS, false);

            // Init loader
            ContainerLoader loader = new ContainerLoader(entry);

            // Init printer
            ModuleInfoFilePrinter printer = new ModuleInfoFilePrinter();
            printer.setUnicodeEscape(unicodeEscape);

            // Format internal name
            String entryPath = entry.getPath();
            assert entryPath.endsWith(".class");
            String entryInternalName = entryPath.substring(0, entryPath.length() - 6); // 6 = ".class".length()

            // Decompile class file
            DECOMPILER.decompile(loader, printer, entryInternalName);
        } catch (Throwable t) {
            assert ExceptionUtil.printStackTrace(t);
            setText("// INTERNAL ERROR //");
        }
    }

    public String getSyntaxStyle() { return SYNTAX_STYLE_JAVA_MODULE; }

    public class ModuleInfoFilePrinter extends ClassFilePrinter {
        @Override
        public void start(int maxLineNumber, int majorVersion, int minorVersion) {}

        @Override
        public void end() {
            setText(stringBuffer.toString());
            initLineNumbers();
        }
    }

    // https://github.com/bobbylight/RSyntaxTextArea/wiki/Adding-Syntax-Highlighting-for-a-new-Language
    public static class ModuleInfoTokenMaker extends AbstractTokenMaker {
        @Override
        public TokenMap getWordsToHighlight() {
            TokenMap tokenMap = new TokenMap();

            tokenMap.put("exports", Token.RESERVED_WORD);
            tokenMap.put("module", Token.RESERVED_WORD);
            tokenMap.put("open", Token.RESERVED_WORD);
            tokenMap.put("opens", Token.RESERVED_WORD);
            tokenMap.put("provides", Token.RESERVED_WORD);
            tokenMap.put("requires", Token.RESERVED_WORD);
            tokenMap.put("to", Token.RESERVED_WORD);
            tokenMap.put("transitive", Token.RESERVED_WORD);
            tokenMap.put("uses", Token.RESERVED_WORD);
            tokenMap.put("with", Token.RESERVED_WORD);

            return tokenMap;
        }

        @Override
        public void addToken(Segment segment, int start, int end, int tokenType, int startOffset) {
            // This assumes all keywords, etc. were parsed as "identifiers."
            if (tokenType==Token.IDENTIFIER) {
                int value = wordsToHighlight.get(segment, start, end);
                if (value != -1) {
                    tokenType = value;
                }
            }
            super.addToken(segment, start, end, tokenType, startOffset);
        }

        @Override
        public Token getTokenList(Segment text, int startTokenType, int startOffset) {
            resetTokenList();

            char[] array = text.array;
            int offset = text.offset;
            int end = offset + text.count;

            int newStartOffset = startOffset - offset;

            int currentTokenStart = offset;
            int currentTokenType  = startTokenType;

            for (int i=offset; i<end; i++) {
                char c = array[i];

                switch (currentTokenType) {
                    case Token.NULL:
                        currentTokenStart = i;   // Starting a new token here.
                        if (RSyntaxUtilities.isLetter(c) || c=='_') {
                            currentTokenType = Token.IDENTIFIER;
                        } else {
                            currentTokenType = Token.WHITESPACE;
                        }
                        break;
                    default: // Should never happen
                    case Token.WHITESPACE:
                        if (RSyntaxUtilities.isLetterOrDigit(c) || c=='_') {
                            addToken(text, currentTokenStart, i-1, Token.WHITESPACE, newStartOffset+currentTokenStart);
                            currentTokenStart = i;
                            currentTokenType = Token.IDENTIFIER;
                        }
                        break;
                    case Token.IDENTIFIER:
                        if (!RSyntaxUtilities.isLetterOrDigit(c) && c!='_') {
                            addToken(text, currentTokenStart, i-1, Token.IDENTIFIER, newStartOffset+currentTokenStart);
                            currentTokenStart = i;
                            currentTokenType = Token.WHITESPACE;
                        }
                        break;
                }
            }

            if (currentTokenType == Token.NULL) {
                addNullToken();
            }else {
                addToken(text, currentTokenStart,end-1, currentTokenType, newStartOffset+currentTokenStart);
                addNullToken();
            }

            return firstToken;
        }
    }
}
