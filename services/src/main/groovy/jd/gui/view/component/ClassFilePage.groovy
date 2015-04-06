/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.view.component

import groovy.transform.CompileStatic
import jd.core.Decompiler
import jd.core.loader.Loader
import jd.core.loader.LoaderException
import jd.core.process.DecompilerImpl
import jd.gui.api.API
import jd.gui.api.feature.*
import jd.gui.api.model.Container
import jd.gui.api.model.Indexes
import jd.gui.util.decompiler.ClassFileSourcePrinter
import jd.gui.util.decompiler.ContainerLoader
import jd.gui.util.decompiler.GuiPreferences
import org.fife.ui.rsyntaxtextarea.DocumentRange
import org.fife.ui.rsyntaxtextarea.SyntaxConstants

import javax.swing.text.DefaultCaret
import java.awt.Color
import java.awt.Point
import java.util.regex.Pattern

class ClassFilePage
        extends SourcePage
        implements UriGettable, ContentSavable, IndexesChangeListener, LineNumberNavigable, FocusedTypeGettable, PreferencesChangeListener {

    protected static final String ESCAPE_UNICODE_CHARACTERS = 'ClassFileViewerPreferences.escapeUnicodeCharacters'
    protected static final String OMIT_THIS_PREFIX = 'ClassFileViewerPreferences.omitThisPrefix'
    protected static final String REALIGN_LINE_NUMBERS = 'ClassFileViewerPreferences.realignLineNumbers'
    protected static final String DISPLAY_DEFAULT_CONSTRUCTOR = 'ClassFileViewerPreferences.displayDefaultConstructor'

    protected static final Decompiler DECOMPILER = new DecompilerImpl()

    protected API api
    protected Container.Entry entry
    protected Collection<Indexes> collectionOfIndexes

    protected ArrayList<ReferenceData> references = new ArrayList<>()
    protected HashMap<String, DeclarationData> declarations = new HashMap<>()
    protected TreeMap<Integer, DeclarationData> typeDeclarations = new TreeMap<>()
    protected ArrayList<StringData> strings = new ArrayList<>()

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
        // Init attributes
        this.api = api
        this.entry = entry
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

    String getSyntaxStyle() { SyntaxConstants.SYNTAX_STYLE_JAVA }

    protected boolean isHyperlinkEnabled(HyperlinkPage.HyperlinkData hyperlinkData) { hyperlinkData.reference.enabled }

    protected void openHyperlink(int x, int y, HyperlinkPage.HyperlinkData hyperlinkData) {
        if (hyperlinkData.reference.enabled) {
            // Save current position in history
            def location = textArea.getLocationOnScreen()
            int offset = textArea.viewToModel(new Point(x-location.x as int, y-location.y as int))
            def uri = entry.uri
            api.addURI(new URI(uri.scheme, uri.authority, uri.path, 'position=' + offset, null))

            // Open link
            def reference = hyperlinkData.reference
            def typeName = reference.type
            def entries = collectionOfIndexes?.collect { it.getIndex('typeDeclarations')?.get(typeName) }.flatten().grep { it!=null }
            def rootUri = entry.container.root.uri.toString()
            def sameContainerEntries = entries?.grep { it.uri.toString().startsWith(rootUri) }
            def fragment = typeName

            if (reference.name) {
                fragment += '-' + reference.name
            }
            if (reference.descriptor) {
                fragment += '-' + reference.descriptor
            }

            if (sameContainerEntries) {
                api.openURI(x, y, sameContainerEntries, null, fragment)
            } else if (entries) {
                api.openURI(x, y, entries, null, fragment)
            }
        }
    }

    // --- UriGettable --- //
    URI getUri() { entry.uri }

    // --- SourceSavable --- //
    String getFileName() {
        def path = entry.path
        int index = path.lastIndexOf('.')
        return path.substring(index+1) + '.java'
    }

    void save(API api, OutputStream os) {
        os << textArea.text
    }

    // --- IndexesChangeListener --- //
    @CompileStatic
    void indexesChanged(Collection<Indexes> collectionOfIndexes) {
        // Update the list of containers
        this.collectionOfIndexes = collectionOfIndexes
        // Refresh links
        boolean refresh = false

        for (def reference : references) {
            def typeName = reference.type
            boolean enabled

            if (reference.name) {
                typeName = searchTypeHavingMember(typeName, reference.name, reference.descriptor, entry)
                if (typeName) {
                    // Replace type with the real type containing the referenced member
                    reference.type = typeName
                    enabled = true
                } else {
                    enabled = false
                }
            } else {
                enabled = collectionOfIndexes.find { it.getIndex('typeDeclarations')?.get(typeName) } != null
            }

            if (reference.enabled != enabled) {
                reference.enabled = enabled
                refresh = true
            }
        }

        if (refresh) {
            textArea.repaint()
        }
    }

    protected String searchTypeHavingMember(String typeName, String name, String descriptor, Container.Entry entry) {
        def entries = collectionOfIndexes?.collect { it.getIndex('typeDeclarations')?.get(typeName) }.flatten().grep { it!=null }
        def rootUri = entry.container.root.uri.toString()
        def sameContainerEntries = entries?.grep { Container.Entry e -> e.uri.toString().startsWith(rootUri) }

        if (sameContainerEntries) {
            return searchTypeHavingMember(typeName, name, descriptor, sameContainerEntries)
        } else {
            return searchTypeHavingMember(typeName, name, descriptor, entries)
        }
    }

    @CompileStatic
    protected String searchTypeHavingMember(String typeName, String name, String descriptor, List<Container.Entry> entries) {
        for (def entry : entries) {
            def type = api.getTypeFactory(entry).make(api, entry, null)

            if (type) {
                if (descriptor.indexOf('(') == -1) {
                    // Search a field
                    for (def field : type.fields) {
                        if (field.name.equals(name) && field.descriptor.equals(descriptor)) {
                            // Field found
                            return typeName
                        }
                    }
                } else {
                    // Search a method
                    for (def method : type.methods) {
                        if (method.name.equals(name) && method.descriptor.equals(descriptor)) {
                            // Method found
                            return typeName
                        }
                    }
                }

                // Not found -> Search in super type
                def typeOwnerName = searchTypeHavingMember(type.superName, name, descriptor, entry)
                if (typeOwnerName) {
                    return typeOwnerName
                }
            }
        }

        return null
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

    // --- UriOpenable --- //
    boolean openUri(URI uri) {
        List<DocumentRange> ranges = []
        def fragment = uri.fragment
        def query = uri.query

        textArea.highlighter.clearMarkAllHighlights()

        if (fragment) {
            int index = fragment.indexOf('?')

            if (index == -1) {
                // Known descriptor ==> Search and high light item
                def data = declarations.get(fragment)
                if (data) {
                    ranges.add(new DocumentRange(data.startPosition, data.endPosition))
                }
            } else {
                // Unknown descriptor ==> Select all and scroll to the first one
                def prefix = fragment.substring(0, fragment.lastIndexOf('-') + 1)
                boolean method = (fragment.charAt(index - 1) == '(')
                int prefixLength = prefix.size()

                for (def entry : declarations.entrySet()) {
                    if (entry.key.startsWith(prefix)) {
                        def flag = (entry.key.charAt(prefixLength) == '(')
                        if (method == flag) {
                            ranges.add(new DocumentRange(entry.value.startPosition, entry.value.endPosition))
                        }
                    }
                }
            }
        }

        if (query) {
            Map<String, String> parameters = parseQuery(query)

            if (parameters.containsKey('lineNumber')) {
                def lineNumber = parameters.get('lineNumber')
                if (lineNumber.isNumber()) {
                    goToLineNumber(lineNumber.toInteger())
                    return true
                }
            } else if (parameters.containsKey('position')) {
                def position = parameters.get('position')
                if (position.isNumber()) {
                    int pos = position.toInteger()
                    if (textArea.document.length > pos) {
                        ranges.add(new DocumentRange(pos, pos))
                    }
                }
            } else {
                def highlightFlags = parameters.get('highlightFlags')
                def highlightPattern = parameters.get('highlightPattern')

                if (highlightFlags && highlightPattern) {
                    def highlightScope = parameters.get('highlightScope')
                    def regexp = createRegExp(parameters.get('highlightPattern'))
                    def pattern = Pattern.compile(regexp + '.*')

                    if (highlightFlags.indexOf('s') != -1) {
                        // Highlight strings
                        def patternForString = Pattern.compile(regexp)

                        for (def data : strings) {
                            if (!highlightScope || data.owner.equals(highlightScope)) {
                                def matcher = patternForString.matcher(data.text)
                                int offset = data.startPosition

                                while(matcher.find()) {
                                    ranges.add(new DocumentRange(offset + matcher.start(), offset + matcher.end()))
                                }
                            }
                        }
                    }

                    boolean t = (highlightFlags.indexOf('t') != -1) // Highlight types
                    boolean f = (highlightFlags.indexOf('f') != -1) // Highlight fields
                    boolean m = (highlightFlags.indexOf('m') != -1) // Highlight methods
                    boolean c = (highlightFlags.indexOf('c') != -1) // Highlight constructors

                    if (highlightFlags.indexOf('d') != -1) {
                        // Highlight declarations
                        for (def entry : declarations.entrySet()) {
                            def declaration = entry.value

                            if (!highlightScope || declaration.type.equals(highlightScope)) {
                                if ((t && declaration.isAType()) || (c && declaration.isAConstructor())) {
                                    matchAndAddDocumentRange(pattern, getMostInnerTypeName(declaration.type), declaration.startPosition, declaration.endPosition, ranges)
                                }
                                if ((f && declaration.isAField()) || (m && declaration.isAMethod())) {
                                    matchAndAddDocumentRange(pattern, declaration.name, declaration.startPosition, declaration.endPosition, ranges)
                                }
                            }
                        }
                    }

                    if (highlightFlags.indexOf('r') != -1) {
                        // Highlight references
                        for (def entry : hyperlinks.entrySet()) {
                            def hyperlink = entry.value
                            def reference = hyperlink.reference as ReferenceData

                            if (!highlightScope || reference.owner.equals(highlightScope)) {
                                if ((t && reference.isAType()) || (c && reference.isAConstructor())) {
                                    matchAndAddDocumentRange(pattern, getMostInnerTypeName(reference.type), hyperlink.startPosition, hyperlink.endPosition, ranges)
                                }
                                if ((f && reference.isAField()) || (m && reference.isAMethod())) {
                                    matchAndAddDocumentRange(pattern, reference.name, hyperlink.startPosition, hyperlink.endPosition, ranges)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (ranges) {
            textArea.markAllHighlightColor = searchHighlightColor
            textArea.markAll(ranges)
            setCaretPositionAndCenter(ranges.sort().get(0))
        }
    }

    @CompileStatic
    void matchAndAddDocumentRange(Pattern pattern, String text, int start, int end, List<DocumentRange> ranges) {
        if (pattern.matcher(text).matches()) {
            ranges.add(new DocumentRange(start, end))
        }
    }

    @CompileStatic
    String getMostInnerTypeName(String typeName) {
        int lastPackageSeparatorIndex = typeName.lastIndexOf('/') + 1
        int lastTypeNameSeparatorIndex = typeName.lastIndexOf('$') + 1
        int lastIndex = Math.max(lastPackageSeparatorIndex, lastTypeNameSeparatorIndex)
        return typeName.substring(lastIndex)
    }

    // --- FocusedTypeGettable --- //
    String getFocusedTypeName() { typeDeclarations.floorEntry(textArea.caretPosition)?.value?.type }

    Container.Entry getEntry() { entry }

    // --- PreferencesChangeListener --- //
    void preferencesChanged(Map<String, String> preferences) {
        def caret = textArea.caret
        int updatePolicy = caret.updatePolicy

        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        decompile(preferences)
        caret.setUpdatePolicy(updatePolicy)
    }

    @CompileStatic
    class Printer extends ClassFileSourcePrinter {
        protected StringBuffer stringBuffer
        protected boolean realignmentLineNumber
        protected boolean showPrefixThis
        protected boolean unicodeEscape
        protected HashMap<String, ReferenceData> referencesCache

        Printer(GuiPreferences preferences) {
            this.stringBuffer = new StringBuffer(10*1024)
            this.realignmentLineNumber = preferences.getRealignmentLineNumber()
            this.showPrefixThis = preferences.isShowPrefixThis()
            this.unicodeEscape = preferences.isUnicodeEscape()
            this.referencesCache = new HashMap<>()
        }

        boolean getRealignmentLineNumber() { realignmentLineNumber }
        boolean isShowPrefixThis() { showPrefixThis }
        boolean isUnicodeEscape() { unicodeEscape }

        void append(char c) { stringBuffer.append(c) }
        void append(String s) {
            stringBuffer.append(s) }

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

        // --- Manage strings --- //
        void printString(String s, String scopeInternalName)  {
            strings.add(new StringData(stringBuffer.length(), s.length(), s, scopeInternalName))
            super.printString(s, scopeInternalName)
        }

        // --- Manage references --- //
        void printTypeImport(String internalName, String name) {
            addHyperlink(new HyperlinkReferenceData(stringBuffer.length(), name.length(), newReferenceData(internalName, null, null, null)))
            super.printTypeImport(internalName, name)
        }

        void printType(String internalName, String name, String scopeInternalName) {
            addHyperlink(new HyperlinkReferenceData(stringBuffer.length(), name.length(), newReferenceData(internalName, null, null, scopeInternalName)))
            super.printType(internalName, name, scopeInternalName)
        }

        void printField(String internalName, String name, String descriptor, String scopeInternalName) {
            addHyperlink(new HyperlinkReferenceData(stringBuffer.length(), name.length(), newReferenceData(internalName, name, descriptor, scopeInternalName)))
            super.printField(internalName, name, descriptor, scopeInternalName)
        }
        void printStaticField(String internalName, String name, String descriptor, String scopeInternalName) {
            addHyperlink(new HyperlinkReferenceData(stringBuffer.length(), name.length(), newReferenceData(internalName, name, descriptor, scopeInternalName)))
            super.printStaticField(internalName, name, descriptor, scopeInternalName)
        }

        void printConstructor(String internalName, String name, String descriptor, String scopeInternalName) {
            addHyperlink(new HyperlinkReferenceData(stringBuffer.length(), name.length(), newReferenceData(internalName, "<init>", descriptor, scopeInternalName)))
            super.printConstructor(internalName, name, descriptor, scopeInternalName)
        }

        void printMethod(String internalName, String name, String descriptor, String scopeInternalName) {
            addHyperlink(new HyperlinkReferenceData(stringBuffer.length(), name.length(), newReferenceData(internalName, name, descriptor, scopeInternalName)))
            super.printMethod(internalName, name, descriptor, scopeInternalName)
        }
        void printStaticMethod(String internalName, String name, String descriptor, String scopeInternalName) {
            addHyperlink(new HyperlinkReferenceData(stringBuffer.length(), name.length(), newReferenceData(internalName, name, descriptor, scopeInternalName)))
            super.printStaticMethod(internalName, name, descriptor, scopeInternalName)
        }

        ReferenceData newReferenceData(String internalName, String name, String descriptor, String scopeInternalName) {
            def key = internalName + '-' + name + '-'+ descriptor + '-' + scopeInternalName
            def reference = referencesCache.get(key)

            if (reference == null) {
                reference = new ReferenceData(internalName, name, descriptor, scopeInternalName)
                referencesCache.put(key, reference)
                references.add(reference)
            }

            return reference
        }

        // --- Manage declarations --- //
        void printTypeDeclaration(String internalName, String name) {
            def data = new DeclarationData(stringBuffer.length(), name.length(), internalName, null, null)
            declarations.put(internalName, data)
            typeDeclarations.put(stringBuffer.length(), data)
            super.printTypeDeclaration(internalName, name)
        }

        void printFieldDeclaration(String internalName, String name, String descriptor) {
            declarations.put(internalName + '-' + name + '-' + descriptor, new DeclarationData(stringBuffer.length(), name.length(), internalName, name, descriptor))
            super.printFieldDeclaration(internalName, name, descriptor)
        }
        void printStaticFieldDeclaration(String internalName, String name, String descriptor) {
            declarations.put(internalName + '-' + name + '-' + descriptor, new DeclarationData(stringBuffer.length(), name.length(), internalName, name, descriptor))
            super.printStaticFieldDeclaration(internalName, name, descriptor)
        }

        void printConstructorDeclaration(String internalName, String name, String descriptor) {
            declarations.put(internalName + '-<init>-' + descriptor, new DeclarationData(stringBuffer.length(), name.length(), internalName, "<init>", descriptor))
            super.printConstructorDeclaration(internalName, name, descriptor)
        }

        void printMethodDeclaration(String internalName, String name, String descriptor) {
            declarations.put(internalName + '-' + name + '-' + descriptor, new DeclarationData(stringBuffer.length(), name.length(), internalName, name, descriptor))
            super.printMethodDeclaration(internalName, name, descriptor)
        }
        void printStaticMethodDeclaration(String internalName, String name, String descriptor) {
            declarations.put(internalName + '-' + name + '-' + descriptor, new DeclarationData(stringBuffer.length(), name.length(), internalName, name, descriptor))
            super.printStaticMethodDeclaration(internalName, name, descriptor)
        }

        String toString() { stringBuffer.toString() }
    }

    @CompileStatic
    static class StringData {
        int startPosition
        int endPosition
        String text
        String owner

        StringData(int startPosition, int length, String text, String owner) {
            this.startPosition = startPosition
            this.endPosition = startPosition + length
            this.text = text
            this.owner = owner
        }
    }

    @CompileStatic
    static class DeclarationData {
        int startPosition
        int endPosition
        String type
        /**
         * Field or method name or null for type
         */
        String name
        String descriptor

        DeclarationData(int startPosition, int length, String type, String name, String descriptor) {
            this.startPosition = startPosition
            this.endPosition = startPosition + length
            this.type = type
            this.name = name
            this.descriptor = descriptor
        }

        boolean isAType() { name == null }
        boolean isAField() { descriptor && descriptor.charAt(0) != '('}
        boolean isAMethod() { descriptor && descriptor.charAt(0) == '('}
        boolean isAConstructor() { "<init>".equals(name) }
    }

    @CompileStatic
    static class HyperlinkReferenceData extends HyperlinkPage.HyperlinkData {
        ReferenceData reference

        HyperlinkReferenceData(int startPosition, int length, ReferenceData reference) {
            super(startPosition, startPosition+length)
            this.reference = reference
        }
    }

    @CompileStatic
    static class ReferenceData {
        String type
        /**
         * Field or method name or null for type
         */
        String name
        /**
         * Field or method descriptor or null for type
         */
        String descriptor
        /**
         * Internal type name containing reference or null for "import" statement.
         * Used to high light items matching with URI like "file://dir1/dir2/file?highlightPattern=hello&highlightFlags=drtcmfs&highlightScope=type".
         */
        String owner
        /**
         * "Enabled" flag for link of reference
         */
        boolean enabled = false

        ReferenceData(String type, String name, String descriptor, String owner) {
            this.type = type
            this.name = name
            this.descriptor = descriptor
            this.owner = owner
        }

        boolean isAType() { name == null }
        boolean isAField() { descriptor && descriptor.charAt(0) != '('}
        boolean isAMethod() { descriptor && descriptor.charAt(0) == '('}
        boolean isAConstructor() { "<init>".equals(name) }
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
}
