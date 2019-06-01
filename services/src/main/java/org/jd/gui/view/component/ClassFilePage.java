/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view.component;

import org.fife.ui.rsyntaxtextarea.DocumentRange;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.jd.core.v1.ClassFileToJavaSourceDecompiler;
import org.jd.gui.api.API;
import org.jd.gui.api.model.Container;
import org.jd.gui.util.decompiler.ClassPathLoader;
import org.jd.gui.util.decompiler.ContainerLoader;
import org.jd.gui.util.decompiler.NopPrinter;
import org.jd.gui.util.decompiler.StringBuilderPrinter;
import org.jd.gui.util.exception.ExceptionUtil;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ClassFilePage extends TypePage {
    protected static final String ESCAPE_UNICODE_CHARACTERS   = "ClassFileDecompilerPreferences.escapeUnicodeCharacters";
    protected static final String REALIGN_LINE_NUMBERS        = "ClassFileDecompilerPreferences.realignLineNumbers";

    protected static final ClassFileToJavaSourceDecompiler DECOMPILER = new ClassFileToJavaSourceDecompiler();

    protected int maximumLineNumber = -1;

    static {
        // Early class loading
        try {
            String internalTypeName = ClassFilePage.class.getName().replace('.', '/');
            DECOMPILER.decompile(new ClassPathLoader(), new NopPrinter(), internalTypeName);
        } catch (Throwable t) {
            assert ExceptionUtil.printStackTrace(t);
        }
    }

    public ClassFilePage(API api, Container.Entry entry) {
        super(api, entry);
        Map<String, String> preferences = api.getPreferences();
        // Init view
        setErrorForeground(Color.decode(preferences.get("JdGuiPreferences.errorBackgroundColor")));
        // Display source
        decompile(preferences);
    }

    public void decompile(Map<String, String> preferences) {
        try {
            // Clear ...
            clearHyperlinks();
            clearLineNumbers();
            declarations.clear();
            typeDeclarations.clear();
            strings.clear();

            // Init preferences
            boolean realignmentLineNumbers = getPreferenceValue(preferences, REALIGN_LINE_NUMBERS, true);
            boolean unicodeEscape = getPreferenceValue(preferences, ESCAPE_UNICODE_CHARACTERS, false);

            Map<String, Object> configuration = new HashMap<>();
            configuration.put("realignLineNumbers", realignmentLineNumbers);

            setShowMisalignment(realignmentLineNumbers);

            // Init loader
            ContainerLoader loader = new ContainerLoader(entry);

            // Init printer
            ClassFilePrinter printer = new ClassFilePrinter();
            printer.setRealignmentLineNumber(realignmentLineNumbers);
            printer.setUnicodeEscape(unicodeEscape);

            // Format internal name
            String entryPath = entry.getPath();
            assert entryPath.endsWith(".class");
            String entryInternalName = entryPath.substring(0, entryPath.length() - 6); // 6 = ".class".length()

            // Decompile class file
            DECOMPILER.decompile(loader, printer, entryInternalName, configuration);
        } catch (Throwable t) {
            assert ExceptionUtil.printStackTrace(t);
            setText("// INTERNAL ERROR //");
        }

        maximumLineNumber = getMaximumSourceLineNumber();
    }

    protected static boolean getPreferenceValue(Map<String, String> preferences, String key, boolean defaultValue) {
        String v = preferences.get(key);
        return (v == null) ? defaultValue : Boolean.valueOf(v);
    }

    public String getSyntaxStyle() { return SyntaxConstants.SYNTAX_STYLE_JAVA; }

    // --- ContentSavable --- //
    public String getFileName() {
        String path = entry.getPath();
        int index = path.lastIndexOf('.');
        return path.substring(0, index) + ".java";
    }

    // --- LineNumberNavigable --- //
    public int getMaximumLineNumber() { return maximumLineNumber; }

    public void goToLineNumber(int lineNumber) {
        int textAreaLineNumber = getTextAreaLineNumber(lineNumber);
        if (textAreaLineNumber > 0) {
            try {
                int start = textArea.getLineStartOffset(textAreaLineNumber - 1);
                int end = textArea.getLineEndOffset(textAreaLineNumber - 1);
                setCaretPositionAndCenter(new DocumentRange(start, end));
            } catch (BadLocationException e) {
                assert ExceptionUtil.printStackTrace(e);
            }
        }
    }

    public boolean checkLineNumber(int lineNumber) { return lineNumber <= maximumLineNumber; }

    // --- PreferencesChangeListener --- //
    public void preferencesChanged(Map<String, String> preferences) {
        DefaultCaret caret = (DefaultCaret)textArea.getCaret();
        int updatePolicy = caret.getUpdatePolicy();

        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        decompile(preferences);
        caret.setUpdatePolicy(updatePolicy);

        super.preferencesChanged(preferences);
    }

    public class ClassFilePrinter extends StringBuilderPrinter {
        protected HashMap<String, ReferenceData> referencesCache = new HashMap<>();

        // Manage line number and misalignment
        int textAreaLineNumber = 1;

        @Override
        public void start(int maxLineNumber, int majorVersion, int minorVersion) {
            super.start(maxLineNumber, majorVersion, minorVersion);

            if (maxLineNumber == 0) {
                scrollPane.setLineNumbersEnabled(false);
            } else {
                setMaxLineNumber(maxLineNumber);
            }
        }

        @Override
        public void end() {
            setText(stringBuffer.toString());
        }

        // --- Add strings --- //
        @Override
        public void printStringConstant(String constant, String ownerInternalName) {
            if (constant == null) constant = "null";
            if (ownerInternalName == null) ownerInternalName = "null";

            strings.add(new TypePage.StringData(stringBuffer.length(), constant.length(), constant, ownerInternalName));
            super.printStringConstant(constant, ownerInternalName);
        }

        @Override
        public void printDeclaration(int flags, String internalTypeName, String name, String descriptor) {
            if (internalTypeName == null) internalTypeName = "null";
            if (name == null) name = "null";
            if (descriptor == null) descriptor = "null";

            switch (flags) {
                case TYPE_FLAG:
                    TypePage.DeclarationData data = new TypePage.DeclarationData(stringBuffer.length(), name.length(), internalTypeName, null, null);
                    declarations.put(internalTypeName, data);
                    typeDeclarations.put(stringBuffer.length(), data);
                    break;
                case CONSTRUCTOR_FLAG:
                    declarations.put(internalTypeName + "-<init>-" + descriptor, new TypePage.DeclarationData(stringBuffer.length(), name.length(), internalTypeName, "<init>", descriptor));
                    break;
                default:
                    declarations.put(internalTypeName + '-' + name + '-' + descriptor, new TypePage.DeclarationData(stringBuffer.length(), name.length(), internalTypeName, name, descriptor));
                    break;
            }
            super.printDeclaration(flags, internalTypeName, name, descriptor);
        }

        @Override
        public void printReference(int flags, String internalTypeName, String name, String descriptor, String ownerInternalName) {
            if (internalTypeName == null) internalTypeName = "null";
            if (name == null) name = "null";
            if (descriptor == null) descriptor = "null";

            switch (flags) {
                case TYPE_FLAG:
                    addHyperlink(new TypePage.HyperlinkReferenceData(stringBuffer.length(), name.length(), newReferenceData(internalTypeName, null, null, ownerInternalName)));
                    break;
                case CONSTRUCTOR_FLAG:
                    addHyperlink(new TypePage.HyperlinkReferenceData(stringBuffer.length(), name.length(), newReferenceData(internalTypeName, "<init>", descriptor, ownerInternalName)));
                    break;
                default:
                    addHyperlink(new TypePage.HyperlinkReferenceData(stringBuffer.length(), name.length(), newReferenceData(internalTypeName, name, descriptor, ownerInternalName)));
                    break;
            }
            super.printReference(flags, internalTypeName, name, descriptor, ownerInternalName);
        }

        @Override
        public void startLine(int lineNumber) {
            super.startLine(lineNumber);
            setLineNumber(textAreaLineNumber, lineNumber);
        }
        @Override
        public void endLine() {
            super.endLine();
            textAreaLineNumber++;
        }
        public void extraLine(int count) {
            super.extraLine(count);
            if (realignmentLineNumber) {
                textAreaLineNumber += count;
            }
        }

        // --- Add references --- //
        public TypePage.ReferenceData newReferenceData(String internalName, String name, String descriptor, String scopeInternalName) {
            String key = internalName + '-' + name + '-'+ descriptor + '-' + scopeInternalName;
            ReferenceData reference = referencesCache.get(key);

            if (reference == null) {
                reference = new TypePage.ReferenceData(internalName, name, descriptor, scopeInternalName);
                referencesCache.put(key, reference);
                references.add(reference);
            }

            return reference;
        }
    }
}
