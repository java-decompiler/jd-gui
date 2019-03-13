/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view.component

import groovy.transform.CompileStatic
import jd.core.Decompiler
import jd.core.loader.Loader
import jd.core.loader.LoaderException
import jd.core.process.DecompilerImpl
import org.jd.gui.api.API
import org.jd.gui.api.model.Container
import org.jd.gui.util.decompiler.ClassFileSourcePrinter
import org.jd.gui.util.decompiler.ContainerLoader
import org.jd.gui.util.decompiler.GuiPreferences
import org.fife.ui.rsyntaxtextarea.DocumentRange
import org.fife.ui.rsyntaxtextarea.SyntaxConstants

import javax.swing.text.DefaultCaret
import java.awt.Color

class ClassFilePage extends TypePage {

    protected static final String ESCAPE_UNICODE_CHARACTERS   = 'ClassFileViewerPreferences.escapeUnicodeCharacters'
    protected static final String OMIT_THIS_PREFIX            = 'ClassFileViewerPreferences.omitThisPrefix'
    protected static final String REALIGN_LINE_NUMBERS        = 'ClassFileViewerPreferences.realignLineNumbers'
    protected static final String DISPLAY_DEFAULT_CONSTRUCTOR = 'ClassFileViewerPreferences.displayDefaultConstructor'

    protected static final Decompiler DECOMPILER = new DecompilerImpl()

    protected int maximumLineNumber = -1

    static {
        // Early class loading
        def internalTypeName = ClassFilePage.class.name.replace('.', '/')
        def preferences = new GuiPreferences()
        def loader = new Loader() {
            DataInputStream load(String internalTypePath) throws LoaderException {
                return new DataInputStream(ClassFilePage.class.classLoader.getResourceAsStream(internalTypeName + '.class'))
            }
            boolean canLoad(String internalTypePath) { false }
        }
        def printer = new ClassFileSourcePrinter() {
            boolean getRealignmentLineNumber() { false }
            boolean isShowPrefixThis() { false }
            boolean isUnicodeEscape() { false }
            void append(char c) {}
            void append(String s) {}
        }
        DECOMPILER.decompile(preferences, loader, printer, internalTypeName)
    }

    ClassFilePage(API api, Container.Entry entry) {
        super(api, entry)
        // Init view
        errorForeground = Color.decode(api.preferences.get('JdGuiPreferences.errorBackgroundColor'))
        // Display source
        decompile(api.preferences)
    }

    void decompile(Map<String, String> preferences) {
        try {
            // Clear ...
            clearHyperlinks()
            clearLineNumbers()
            declarations.clear()
            typeDeclarations.clear()
            strings.clear()
            // Init preferences
            def p = new GuiPreferences()
            p.setUnicodeEscape(getPreferenceValue(preferences, ESCAPE_UNICODE_CHARACTERS, false))
            p.setShowPrefixThis(! getPreferenceValue(preferences, OMIT_THIS_PREFIX, false));
            p.setShowDefaultConstructor(getPreferenceValue(preferences, DISPLAY_DEFAULT_CONSTRUCTOR, false))
            p.setRealignmentLineNumber(getPreferenceValue(preferences, REALIGN_LINE_NUMBERS, false))

            setShowMisalignment(p.realignmentLineNumber)
            // Init loader
            def loader = new ContainerLoader(entry)
            // Init printer
            def printer = new Printer(p)
            // Decompile class file
            DECOMPILER.decompile(p, loader, printer, entry.path)

            setText(printer.toString())
            // Show hyperlinks
            indexesChanged(api.collectionOfIndexes)
        } catch (Exception ignore) {
            setText('// INTERNAL ERROR //')
        }

        maximumLineNumber = getMaximumSourceLineNumber()
    }

    @CompileStatic
    protected static boolean getPreferenceValue(Map<String, String> preferences, String key, boolean defaultValue) {
        String v = preferences.get(key);

        if (v == null) {
            return defaultValue;
        } else {
            return Boolean.valueOf(v);
        }
    }

    String getSyntaxStyle() { SyntaxConstants.SYNTAX_STYLE_JAVA }

    // --- ContentSavable --- //
    String getFileName() {
        def path = entry.path
        int index = path.lastIndexOf('.')
        return path.substring(0, index) + '.java'
    }

    // --- LineNumberNavigable --- //
    int getMaximumLineNumber() { maximumLineNumber }

    void goToLineNumber(int lineNumber) {
        int textAreaLineNumber = getTextAreaLineNumber(lineNumber)
        if (textAreaLineNumber > 0) {
            int start = textArea.getLineStartOffset(textAreaLineNumber-1)
            int end = textArea.getLineEndOffset(textAreaLineNumber-1)
            setCaretPositionAndCenter(new DocumentRange(start, end))
        }
    }

    boolean checkLineNumber(int lineNumber) { lineNumber <= maximumLineNumber }

    // --- PreferencesChangeListener --- //
    void preferencesChanged(Map<String, String> preferences) {
        def caret = textArea.caret
        int updatePolicy = caret.updatePolicy

        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        decompile(preferences)
        caret.setUpdatePolicy(updatePolicy)

        super.preferencesChanged(preferences)
    }

    @CompileStatic
    class Printer extends ClassFileSourcePrinter {
        protected StringBuffer stringBuffer = new StringBuffer(10*1024)
        protected boolean realignmentLineNumber
        protected boolean showPrefixThis
        protected boolean unicodeEscape
        protected HashMap<String, TypePage.ReferenceData> referencesCache = new HashMap<>()

        Printer(GuiPreferences preferences) {
            this.realignmentLineNumber = preferences.getRealignmentLineNumber()
            this.showPrefixThis = preferences.isShowPrefixThis()
            this.unicodeEscape = preferences.isUnicodeEscape()
        }

        boolean getRealignmentLineNumber() { realignmentLineNumber }
        boolean isShowPrefixThis() { showPrefixThis }
        boolean isUnicodeEscape() { unicodeEscape }

        void append(char c) { stringBuffer.append(c) }
        void append(String s) { stringBuffer.append(s) }

        // Manage line number and misalignment
        int textAreaLineNumber = 1

        void start(int maxLineNumber, int majorVersion, int minorVersion) {
            super.start(maxLineNumber, majorVersion, minorVersion)

            if (maxLineNumber == 0) {
                scrollPane.lineNumbersEnabled = false
            } else {
                setMaxLineNumber(maxLineNumber)
            }
        }
        void startOfLine(int sourceLineNumber) {
            super.startOfLine(sourceLineNumber)
            setLineNumber(textAreaLineNumber, sourceLineNumber)
        }
        void endOfLine() {
            super.endOfLine()
            textAreaLineNumber++
        }
        void extraLine(int count) {
            super.extraLine(count)
            if (realignmentLineNumber) {
                textAreaLineNumber += count
            }
        }

        // --- Add strings --- //
        void printString(String s, String scopeInternalName)  {
            strings.add(new TypePage.StringData(stringBuffer.length(), s.length(), s, scopeInternalName))
            super.printString(s, scopeInternalName)
        }

        // --- Add references --- //
        void printTypeImport(String internalName, String name) {
            addHyperlink(new TypePage.HyperlinkReferenceData(stringBuffer.length(), name.length(), newReferenceData(internalName, null, null, null)))
            super.printTypeImport(internalName, name)
        }

        void printType(String internalName, String name, String scopeInternalName) {
            addHyperlink(new TypePage.HyperlinkReferenceData(stringBuffer.length(), name.length(), newReferenceData(internalName, null, null, scopeInternalName)))
            super.printType(internalName, name, scopeInternalName)
        }

        void printField(String internalName, String name, String descriptor, String scopeInternalName) {
            addHyperlink(new TypePage.HyperlinkReferenceData(stringBuffer.length(), name.length(), newReferenceData(internalName, name, descriptor, scopeInternalName)))
            super.printField(internalName, name, descriptor, scopeInternalName)
        }
        void printStaticField(String internalName, String name, String descriptor, String scopeInternalName) {
            addHyperlink(new TypePage.HyperlinkReferenceData(stringBuffer.length(), name.length(), newReferenceData(internalName, name, descriptor, scopeInternalName)))
            super.printStaticField(internalName, name, descriptor, scopeInternalName)
        }

        void printConstructor(String internalName, String name, String descriptor, String scopeInternalName) {
            addHyperlink(new TypePage.HyperlinkReferenceData(stringBuffer.length(), name.length(), newReferenceData(internalName, "<init>", descriptor, scopeInternalName)))
            super.printConstructor(internalName, name, descriptor, scopeInternalName)
        }

        void printMethod(String internalName, String name, String descriptor, String scopeInternalName) {
            addHyperlink(new TypePage.HyperlinkReferenceData(stringBuffer.length(), name.length(), newReferenceData(internalName, name, descriptor, scopeInternalName)))
            super.printMethod(internalName, name, descriptor, scopeInternalName)
        }
        void printStaticMethod(String internalName, String name, String descriptor, String scopeInternalName) {
            addHyperlink(new TypePage.HyperlinkReferenceData(stringBuffer.length(), name.length(), newReferenceData(internalName, name, descriptor, scopeInternalName)))
            super.printStaticMethod(internalName, name, descriptor, scopeInternalName)
        }

        TypePage.ReferenceData newReferenceData(String internalName, String name, String descriptor, String scopeInternalName) {
            def key = internalName + '-' + name + '-'+ descriptor + '-' + scopeInternalName
            def reference = referencesCache.get(key)

            if (reference == null) {
                reference = new TypePage.ReferenceData(internalName, name, descriptor, scopeInternalName)
                referencesCache.put(key, reference)
                references.add(reference)
            }

            return reference
        }

        // --- Add declarations --- //
        void printTypeDeclaration(String internalName, String name) {
            def data = new TypePage.DeclarationData(stringBuffer.length(), name.length(), internalName, null, null)
            declarations.put(internalName, data)
            typeDeclarations.put(stringBuffer.length(), data)
            super.printTypeDeclaration(internalName, name)
        }

        void printFieldDeclaration(String internalName, String name, String descriptor) {
            declarations.put(internalName + '-' + name + '-' + descriptor, new TypePage.DeclarationData(stringBuffer.length(), name.length(), internalName, name, descriptor))
            super.printFieldDeclaration(internalName, name, descriptor)
        }
        void printStaticFieldDeclaration(String internalName, String name, String descriptor) {
            declarations.put(internalName + '-' + name + '-' + descriptor, new TypePage.DeclarationData(stringBuffer.length(), name.length(), internalName, name, descriptor))
            super.printStaticFieldDeclaration(internalName, name, descriptor)
        }

        void printConstructorDeclaration(String internalName, String name, String descriptor) {
            declarations.put(internalName + '-<init>-' + descriptor, new TypePage.DeclarationData(stringBuffer.length(), name.length(), internalName, "<init>", descriptor))
            super.printConstructorDeclaration(internalName, name, descriptor)
        }
        void printStaticConstructorDeclaration(String internalName, String name) {
            declarations.put(internalName + '-<clinit>-()V', new TypePage.DeclarationData(stringBuffer.length(), name.length(), internalName, "<clinit>", '()V'))
            super.printStaticConstructorDeclaration(internalName, name)
        }

        void printMethodDeclaration(String internalName, String name, String descriptor) {
            declarations.put(internalName + '-' + name + '-' + descriptor, new TypePage.DeclarationData(stringBuffer.length(), name.length(), internalName, name, descriptor))
            super.printMethodDeclaration(internalName, name, descriptor)
        }
        void printStaticMethodDeclaration(String internalName, String name, String descriptor) {
            declarations.put(internalName + '-' + name + '-' + descriptor, new TypePage.DeclarationData(stringBuffer.length(), name.length(), internalName, name, descriptor))
            super.printStaticMethodDeclaration(internalName, name, descriptor)
        }

        String toString() { stringBuffer.toString() }
    }
}
