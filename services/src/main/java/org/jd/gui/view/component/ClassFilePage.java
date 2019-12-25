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
import org.jd.gui.util.decompiler.*;
import org.jd.gui.util.exception.ExceptionUtil;
import org.jd.gui.util.io.NewlineOutputStream;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class ClassFilePage extends TypePage {
    protected static final String ESCAPE_UNICODE_CHARACTERS   = "ClassFileDecompilerPreferences.escapeUnicodeCharacters";
    protected static final String REALIGN_LINE_NUMBERS        = "ClassFileDecompilerPreferences.realignLineNumbers";
    protected static final String WRITE_LINE_NUMBERS          = "ClassFileSaverPreferences.writeLineNumbers";
    protected static final String WRITE_METADATA              = "ClassFileSaverPreferences.writeMetadata";
    protected static final String JD_CORE_VERSION             = "JdGuiPreferences.jdCoreVersion";

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
            boolean realignmentLineNumbers = getPreferenceValue(preferences, REALIGN_LINE_NUMBERS, false);
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

    @Override
    public String getSyntaxStyle() { return SyntaxConstants.SYNTAX_STYLE_JAVA; }

    // --- ContentSavable --- //
    @Override
    public String getFileName() {
        String path = entry.getPath();
        int index = path.lastIndexOf('.');
        return path.substring(0, index) + ".java";
    }

    @Override
    public void save(API api, OutputStream os) {
        try {
            // Init preferences
            Map<String, String> preferences = api.getPreferences();
            boolean realignmentLineNumbers = getPreferenceValue(preferences, REALIGN_LINE_NUMBERS, false);
            boolean unicodeEscape = getPreferenceValue(preferences, ESCAPE_UNICODE_CHARACTERS, false);
            boolean showLineNumbers = getPreferenceValue(preferences, WRITE_LINE_NUMBERS, true);

            Map<String, Object> configuration = new HashMap<>();
            configuration.put("realignLineNumbers", realignmentLineNumbers);

            // Init loader
            ContainerLoader loader = new ContainerLoader(entry);

            // Init printer
            LineNumberStringBuilderPrinter printer = new LineNumberStringBuilderPrinter();
            printer.setRealignmentLineNumber(realignmentLineNumbers);
            printer.setUnicodeEscape(unicodeEscape);
            printer.setShowLineNumbers(showLineNumbers);

            // Format internal name
            String entryPath = entry.getPath();
            assert entryPath.endsWith(".class");
            String entryInternalName = entryPath.substring(0, entryPath.length() - 6); // 6 = ".class".length()

            // Decompile class file
            DECOMPILER.decompile(loader, printer, entryInternalName, configuration);

            StringBuilder stringBuffer = printer.getStringBuffer();

            // Metadata
            if (getPreferenceValue(preferences, WRITE_METADATA, true)) {
                // Add location
                String location =
                        new File(entry.getUri()).getPath()
                                // Escape "\ u" sequence to prevent "Invalid unicode" errors
                                .replaceAll("(^|[^\\\\])\\\\u", "\\\\\\\\u");
                stringBuffer.append("\n\n/* Location:              ");
                stringBuffer.append(location);
                // Add Java compiler version
                int majorVersion = printer.getMajorVersion();

                if (majorVersion >= 45) {
                    stringBuffer.append("\n * Java compiler version: ");

                    if (majorVersion >= 49) {
                        stringBuffer.append(majorVersion - (49 - 5));
                    } else {
                        stringBuffer.append(majorVersion - (45 - 1));
                    }

                    stringBuffer.append(" (");
                    stringBuffer.append(majorVersion);
                    stringBuffer.append('.');
                    stringBuffer.append(printer.getMinorVersion());
                    stringBuffer.append(')');
                }
                // Add JD-Core version
                stringBuffer.append("\n * JD-Core Version:       ");
                stringBuffer.append(preferences.get(JD_CORE_VERSION));
                stringBuffer.append("\n */");
            }

            try (PrintStream ps = new PrintStream(new NewlineOutputStream(os), true, "UTF-8")) {
                ps.print(stringBuffer.toString());
            } catch (IOException e) {
                assert ExceptionUtil.printStackTrace(e);
            }
        } catch (Throwable t) {
            assert ExceptionUtil.printStackTrace(t);

            try (OutputStreamWriter writer = new OutputStreamWriter(os, Charset.defaultCharset())) {
                writer.write("// INTERNAL ERROR //");
            } catch (IOException ee) {
                assert ExceptionUtil.printStackTrace(ee);
            }
        }
    }

    // --- LineNumberNavigable --- //
    @Override
    public int getMaximumLineNumber() { return maximumLineNumber; }

    @Override
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

    @Override
    public boolean checkLineNumber(int lineNumber) { return lineNumber <= maximumLineNumber; }

    // --- PreferencesChangeListener --- //
    @Override
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
        public void printDeclaration(int type, String internalTypeName, String name, String descriptor) {
            if (internalTypeName == null) internalTypeName = "null";
            if (name == null) name = "null";
            if (descriptor == null) descriptor = "null";

            switch (type) {
                case TYPE:
                    TypePage.DeclarationData data = new TypePage.DeclarationData(stringBuffer.length(), name.length(), internalTypeName, null, null);
                    declarations.put(internalTypeName, data);
                    typeDeclarations.put(stringBuffer.length(), data);
                    break;
                case CONSTRUCTOR:
                    declarations.put(internalTypeName + "-<init>-" + descriptor, new TypePage.DeclarationData(stringBuffer.length(), name.length(), internalTypeName, "<init>", descriptor));
                    break;
                default:
                    declarations.put(internalTypeName + '-' + name + '-' + descriptor, new TypePage.DeclarationData(stringBuffer.length(), name.length(), internalTypeName, name, descriptor));
                    break;
            }
            super.printDeclaration(type, internalTypeName, name, descriptor);
        }

        @Override
        public void printReference(int type, String internalTypeName, String name, String descriptor, String ownerInternalName) {
            if (internalTypeName == null) internalTypeName = "null";
            if (name == null) name = "null";
            if (descriptor == null) descriptor = "null";

            switch (type) {
                case TYPE:
                    addHyperlink(new TypePage.HyperlinkReferenceData(stringBuffer.length(), name.length(), newReferenceData(internalTypeName, null, null, ownerInternalName)));
                    break;
                case CONSTRUCTOR:
                    addHyperlink(new TypePage.HyperlinkReferenceData(stringBuffer.length(), name.length(), newReferenceData(internalTypeName, "<init>", descriptor, ownerInternalName)));
                    break;
                default:
                    addHyperlink(new TypePage.HyperlinkReferenceData(stringBuffer.length(), name.length(), newReferenceData(internalTypeName, name, descriptor, ownerInternalName)));
                    break;
            }
            super.printReference(type, internalTypeName, name, descriptor, ownerInternalName);
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
        @Override
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
