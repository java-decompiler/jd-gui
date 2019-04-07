/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view.component;

import jd.core.Decompiler;
import jd.core.loader.Loader;
import jd.core.loader.LoaderException;
import jd.core.process.DecompilerImpl;
import org.fife.ui.rsyntaxtextarea.DocumentRange;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.jd.gui.api.API;
import org.jd.gui.api.model.Container;
import org.jd.gui.util.decompiler.ClassFileSourcePrinter;
import org.jd.gui.util.decompiler.ContainerLoader;
import org.jd.gui.util.decompiler.GuiPreferences;
import org.jd.gui.util.exception.ExceptionUtil;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.DataInputStream;
import java.util.HashMap;
import java.util.Map;

public class ClassFilePage extends TypePage {
    protected static final String ESCAPE_UNICODE_CHARACTERS   = "ClassFileViewerPreferences.escapeUnicodeCharacters";
    protected static final String OMIT_THIS_PREFIX            = "ClassFileViewerPreferences.omitThisPrefix";
    protected static final String REALIGN_LINE_NUMBERS        = "ClassFileViewerPreferences.realignLineNumbers";
    protected static final String DISPLAY_DEFAULT_CONSTRUCTOR = "ClassFileViewerPreferences.displayDefaultConstructor";

    protected static final Decompiler DECOMPILER = new DecompilerImpl();

    protected int maximumLineNumber = -1;

    static {
        // Early class loading
        String internalTypeName = ClassFilePage.class.getName().replace('.', '/');
        GuiPreferences preferences = new GuiPreferences();
        Loader loader = new Loader() {
            public DataInputStream load(String internalTypePath) throws LoaderException {
                return new DataInputStream(ClassFilePage.class.getClassLoader().getResourceAsStream(internalTypeName + ".class"));
            }
            public boolean canLoad(String internalTypePath) { return false; }
        };
        ClassFileSourcePrinter printer = new ClassFileSourcePrinter() {
            public boolean getRealignmentLineNumber() { return false; }
            public boolean isShowPrefixThis() { return false; }
            public boolean isUnicodeEscape() { return false; }
            public void append(char c) {}
            public void append(String s) {}
        };
        try {
            DECOMPILER.decompile(preferences, loader, printer, internalTypeName);
        } catch (Exception e) {
            assert ExceptionUtil.printStackTrace(e);
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
            GuiPreferences p = new GuiPreferences();
            p.setUnicodeEscape(getPreferenceValue(preferences, ESCAPE_UNICODE_CHARACTERS, false));
            p.setShowPrefixThis(! getPreferenceValue(preferences, OMIT_THIS_PREFIX, false));
            p.setShowDefaultConstructor(getPreferenceValue(preferences, DISPLAY_DEFAULT_CONSTRUCTOR, false));
            p.setRealignmentLineNumber(getPreferenceValue(preferences, REALIGN_LINE_NUMBERS, false));

            setShowMisalignment(p.getRealignmentLineNumber());
            // Init loader
            ContainerLoader loader = new ContainerLoader(entry);
            // Init printer
            Printer printer = new Printer(p);
            // Decompile class file
            DECOMPILER.decompile(p, loader, printer, entry.getPath());

            setText(printer.toString());
            // Show hyperlinks
            indexesChanged(api.getCollectionOfIndexes());
        } catch (Exception e) {
            assert ExceptionUtil.printStackTrace(e);
            setText("// INTERNAL ERROR //");
        }

        maximumLineNumber = getMaximumSourceLineNumber();
    }

    protected static boolean getPreferenceValue(Map<String, String> preferences, String key, boolean defaultValue) {
        String v = preferences.get(key);

        if (v == null) {
            return defaultValue;
        } else {
            return Boolean.valueOf(v);
        }
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

    public class Printer extends ClassFileSourcePrinter {
        protected StringBuilder stringBuffer = new StringBuilder(10*1024);
        protected boolean realignmentLineNumber;
        protected boolean showPrefixThis;
        protected boolean unicodeEscape;
        protected HashMap<String, ReferenceData> referencesCache = new HashMap<>();

        public Printer(GuiPreferences preferences) {
            this.realignmentLineNumber = preferences.getRealignmentLineNumber();
            this.showPrefixThis = preferences.isShowPrefixThis();
            this.unicodeEscape = preferences.isUnicodeEscape();
        }

        public boolean getRealignmentLineNumber() { return realignmentLineNumber; }
        public boolean isShowPrefixThis() { return showPrefixThis; }
        public boolean isUnicodeEscape() { return unicodeEscape; }

        public void append(char c) { stringBuffer.append(c); }
        public void append(String s) { stringBuffer.append(s); }

        // Manage line number and misalignment
        int textAreaLineNumber = 1;

        public void start(int maxLineNumber, int majorVersion, int minorVersion) {
            super.start(maxLineNumber, majorVersion, minorVersion);

            if (maxLineNumber == 0) {
                scrollPane.setLineNumbersEnabled(false);
            } else {
                setMaxLineNumber(maxLineNumber);
            }
        }
        public void startOfLine(int sourceLineNumber) {
            super.startOfLine(sourceLineNumber);
            setLineNumber(textAreaLineNumber, sourceLineNumber);
        }
        public void endOfLine() {
            super.endOfLine();
            textAreaLineNumber++;
        }
        public void extraLine(int count) {
            super.extraLine(count);
            if (realignmentLineNumber) {
                textAreaLineNumber += count;
            }
        }

        // --- Add strings --- //
        public void printString(String s, String scopeInternalName)  {
            strings.add(new TypePage.StringData(stringBuffer.length(), s.length(), s, scopeInternalName));
            super.printString(s, scopeInternalName);
        }

        // --- Add references --- //
        public void printTypeImport(String internalName, String name) {
            addHyperlink(new TypePage.HyperlinkReferenceData(stringBuffer.length(), name.length(), newReferenceData(internalName, null, null, null)));
            super.printTypeImport(internalName, name);
        }

        public void printType(String internalName, String name, String scopeInternalName) {
            addHyperlink(new TypePage.HyperlinkReferenceData(stringBuffer.length(), name.length(), newReferenceData(internalName, null, null, scopeInternalName)));
            super.printType(internalName, name, scopeInternalName);
        }

        public void printField(String internalName, String name, String descriptor, String scopeInternalName) {
            addHyperlink(new TypePage.HyperlinkReferenceData(stringBuffer.length(), name.length(), newReferenceData(internalName, name, descriptor, scopeInternalName)));
            super.printField(internalName, name, descriptor, scopeInternalName);
        }
        public void printStaticField(String internalName, String name, String descriptor, String scopeInternalName) {
            addHyperlink(new TypePage.HyperlinkReferenceData(stringBuffer.length(), name.length(), newReferenceData(internalName, name, descriptor, scopeInternalName)));
            super.printStaticField(internalName, name, descriptor, scopeInternalName);
        }

        public void printConstructor(String internalName, String name, String descriptor, String scopeInternalName) {
            addHyperlink(new TypePage.HyperlinkReferenceData(stringBuffer.length(), name.length(), newReferenceData(internalName, "<init>", descriptor, scopeInternalName)));
            super.printConstructor(internalName, name, descriptor, scopeInternalName);
        }

        public void printMethod(String internalName, String name, String descriptor, String scopeInternalName) {
            addHyperlink(new TypePage.HyperlinkReferenceData(stringBuffer.length(), name.length(), newReferenceData(internalName, name, descriptor, scopeInternalName)));
            super.printMethod(internalName, name, descriptor, scopeInternalName);
        }
        public void printStaticMethod(String internalName, String name, String descriptor, String scopeInternalName) {
            addHyperlink(new TypePage.HyperlinkReferenceData(stringBuffer.length(), name.length(), newReferenceData(internalName, name, descriptor, scopeInternalName)));
            super.printStaticMethod(internalName, name, descriptor, scopeInternalName);
        }

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

        // --- Add declarations --- //
        public void printTypeDeclaration(String internalName, String name) {
            TypePage.DeclarationData data = new TypePage.DeclarationData(stringBuffer.length(), name.length(), internalName, null, null);
            declarations.put(internalName, data);
            typeDeclarations.put(stringBuffer.length(), data);
            super.printTypeDeclaration(internalName, name);
        }

        public void printFieldDeclaration(String internalName, String name, String descriptor) {
            declarations.put(internalName + '-' + name + '-' + descriptor, new TypePage.DeclarationData(stringBuffer.length(), name.length(), internalName, name, descriptor));
            super.printFieldDeclaration(internalName, name, descriptor);
        }
        public void printStaticFieldDeclaration(String internalName, String name, String descriptor) {
            declarations.put(internalName + '-' + name + '-' + descriptor, new TypePage.DeclarationData(stringBuffer.length(), name.length(), internalName, name, descriptor));
            super.printStaticFieldDeclaration(internalName, name, descriptor);
        }

        public void printConstructorDeclaration(String internalName, String name, String descriptor) {
            declarations.put(internalName + "-<init>-" + descriptor, new TypePage.DeclarationData(stringBuffer.length(), name.length(), internalName, "<init>", descriptor));
            super.printConstructorDeclaration(internalName, name, descriptor);
        }
        public void printStaticConstructorDeclaration(String internalName, String name) {
            declarations.put(internalName + "-<clinit>-()V", new TypePage.DeclarationData(stringBuffer.length(), name.length(), internalName, "<clinit>", "()V"));
            super.printStaticConstructorDeclaration(internalName, name);
        }

        public void printMethodDeclaration(String internalName, String name, String descriptor) {
            declarations.put(internalName + '-' + name + '-' + descriptor, new TypePage.DeclarationData(stringBuffer.length(), name.length(), internalName, name, descriptor));
            super.printMethodDeclaration(internalName, name, descriptor);
        }
        public void printStaticMethodDeclaration(String internalName, String name, String descriptor) {
            declarations.put(internalName + '-' + name + '-' + descriptor, new TypePage.DeclarationData(stringBuffer.length(), name.length(), internalName, name, descriptor));
            super.printStaticMethodDeclaration(internalName, name, descriptor);
        }

        public String toString() { return stringBuffer.toString(); }
    }
}
