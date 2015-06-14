/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */
package jd.gui.view.component

import groovy.transform.CompileStatic
import jd.gui.api.API
import jd.gui.api.feature.FocusedTypeGettable
import jd.gui.api.feature.IndexesChangeListener
import jd.gui.api.feature.UriGettable
import jd.gui.api.feature.UriOpenable
import jd.gui.api.model.Container
import jd.gui.api.model.Indexes
import jd.gui.util.matcher.DescriptorMatcher
import org.fife.ui.rsyntaxtextarea.DocumentRange

import java.awt.Point
import java.util.regex.Pattern

abstract class TypePage extends CustomLineNumbersPage implements UriGettable, UriOpenable, IndexesChangeListener, FocusedTypeGettable {

    protected API api
    protected Container.Entry entry
    protected Collection<Indexes> collectionOfIndexes

    protected HashMap<String, DeclarationData> declarations = new HashMap<>()
    protected TreeMap<Integer, DeclarationData> typeDeclarations = new TreeMap<>()
    protected ArrayList<ReferenceData> references = new ArrayList<>()
    protected ArrayList<StringData> strings = new ArrayList<>()

    TypePage(API api, Container.Entry entry) {
        // Init attributes
        this.api = api
        this.entry = entry
    }

    protected boolean isHyperlinkEnabled(HyperlinkPage.HyperlinkData hyperlinkData) { hyperlinkData.reference.enabled }

    protected void openHyperlink(int x, int y, HyperlinkPage.HyperlinkData hyperlinkData) {
        if (hyperlinkData.reference.enabled) {
            // Save current position in history
            def location = textArea.getLocationOnScreen()
            int offset = textArea.viewToModel(new Point(x-location.x as int, y-location.y as int))
            def uri = entry.uri
            api.addURI(new URI(uri.scheme, uri.authority, uri.path, 'position=' + offset, null))

            // Open link
            ReferenceData reference = hyperlinkData.reference
            def typeName = reference.typeName
            def entries = collectionOfIndexes?.collect { it.getIndex('typeDeclarations')?.get(typeName) }.flatten().grep { it != null }
            def fragment = typeName

            if (reference.name) {
                fragment += '-' + reference.name
            }
            if (reference.descriptor) {
                fragment += '-' + reference.descriptor
            }

            if (entries.contains(entry)) {
                api.openURI(new URI(uri.scheme, uri.authority, uri.path, fragment))
            } else {
                def rootUri = entry.container.root.uri.toString()
                def sameContainerEntries = entries?.grep { it.uri.toString().startsWith(rootUri) }

                if (sameContainerEntries) {
                    api.openURI(x, y, sameContainerEntries, null, fragment)
                } else if (entries) {
                    api.openURI(x, y, entries, null, fragment)
                }
            }
        }
    }

    // --- UriGettable --- //
    URI getUri() { entry.uri }

    // --- UriOpenable --- //
    /**
     * @param uri for URI format, @see jd.gui.api.feature.UriOpenable
     */
    boolean openUri(URI uri) {
        List<DocumentRange> ranges = []
        def fragment = uri.fragment
        def query = uri.query

        textArea.highlighter.clearMarkAllHighlights()

        if (fragment) {
            matchFragmentAndAddDocumentRange(fragment, declarations, ranges)
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
                matchQueryAndAddDocumentRange(parameters, declarations, hyperlinks, strings, ranges)
            }
        }

        if (ranges) {
            textArea.markAllHighlightColor = SELECT_HIGHLIGHT_COLOR
            textArea.markAll(ranges)
            setCaretPositionAndCenter(ranges.sort().get(0))
        }

        return true
    }

    @CompileStatic
    static void matchFragmentAndAddDocumentRange(
            String fragment, HashMap<String, DeclarationData> declarations, List<DocumentRange> ranges) {

        if ((fragment.indexOf('?') != -1) || (fragment.indexOf('*') != -1)) {
            // Unknown type and/or descriptor ==> Select all and scroll to the first one
            int lastDash = fragment.lastIndexOf('-')

            if (lastDash == -1) {
                // Search types
                String slashAndTypeName = fragment.substring(1)
                String typeName = fragment.substring(2)

                for (def entry : declarations.entrySet()) {
                    if (entry.key.endsWith(slashAndTypeName) || entry.key.equals(typeName)) {
                        ranges.add(new DocumentRange(entry.value.startPosition, entry.value.endPosition))
                    }
                }
            } else {
                def prefix = fragment.substring(0, lastDash+1)
                def suffix = fragment.substring(lastDash+1)
                def addRangeClosure

                if (suffix.charAt(0) == '(') {
                    addRangeClosure = { String key, DeclarationData value ->
                        int index = key.lastIndexOf('-') + 1
                        if (DescriptorMatcher.matchMethodDescriptors(suffix, key.substring(index))) {
                            ranges.add(new DocumentRange(value.startPosition, value.endPosition))
                        }
                    }
                } else {
                    addRangeClosure = { String key, DeclarationData value ->
                        int index = key.lastIndexOf('-') + 1
                        if (DescriptorMatcher.matchFieldDescriptors(suffix, key.substring(index))) {
                            ranges.add(new DocumentRange(value.startPosition, value.endPosition))
                        }
                    }
                }

                if (fragment.charAt(0) == '*') {
                    // Unknown type
                    String slashAndTypeNameAndName = prefix.substring(1)
                    String typeNameAndName = prefix.substring(2)

                    for (def entry : declarations.entrySet()) {
                        if ((entry.key.indexOf(slashAndTypeNameAndName) != -1) || (entry.key.startsWith(typeNameAndName))) {
                            addRangeClosure(entry.key, entry.value)
                        }
                    }
                } else {
                    // Known type
                    for (def entry : declarations.entrySet()) {
                        if (entry.key.startsWith(prefix)) {
                            addRangeClosure(entry.key, entry.value)
                        }
                    }
                }
            }
        } else {
            // Known type and descriptor ==> Search and high light item
            def data = declarations.get(fragment)
            if (data) {
                ranges.add(new DocumentRange(data.startPosition, data.endPosition))
            }
        }
    }

    @CompileStatic
    static void matchQueryAndAddDocumentRange(
            Map<String, String> parameters,
            HashMap<String, DeclarationData> declarations, TreeMap<Integer, HyperlinkPage.HyperlinkData> hyperlinks, ArrayList<StringData> strings,
            List<DocumentRange> ranges) {

        def highlightFlags = parameters.get('highlightFlags')
        def highlightPattern = parameters.get('highlightPattern')

        if (highlightFlags && highlightPattern) {
            def highlightScope = parameters.get('highlightScope')
            def regexp = createRegExp(highlightPattern)
            def pattern = Pattern.compile(regexp + '.*')

            if (highlightFlags.indexOf('s') != -1) {
                // Highlight strings
                def patternForString = Pattern.compile(regexp)

                for (def data : strings) {
                    if (matchScope(highlightScope, data.owner)) {
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

                    if (matchScope(highlightScope, declaration.typeName)) {
                        if ((t && declaration.isAType()) || (c && declaration.isAConstructor())) {
                            matchAndAddDocumentRange(pattern, getMostInnerTypeName(declaration.typeName), declaration.startPosition, declaration.endPosition, ranges)
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
                    def reference = ((HyperlinkReferenceData)hyperlink).reference

                    if (matchScope(highlightScope, reference.owner)) {
                        if ((t && reference.isAType()) || (c && reference.isAConstructor())) {
                            matchAndAddDocumentRange(pattern, getMostInnerTypeName(reference.typeName), hyperlink.startPosition, hyperlink.endPosition, ranges)
                        }
                        if ((f && reference.isAField()) || (m && reference.isAMethod())) {
                            matchAndAddDocumentRange(pattern, reference.name, hyperlink.startPosition, hyperlink.endPosition, ranges)
                        }
                    }
                }
            }
        }
    }

    @CompileStatic
    static boolean matchScope(String scope, String type) {
        if (!scope)
            return true
        if (scope.charAt(0) == '*')
            return type.endsWith(scope.substring(1)) || type.equals(scope.substring(2))
        return type.equals(scope)
    }

    @CompileStatic
    static void matchAndAddDocumentRange(Pattern pattern, String text, int start, int end, List<DocumentRange> ranges) {
        if (pattern.matcher(text).matches()) {
            ranges.add(new DocumentRange(start, end))
        }
    }

    @CompileStatic
    static String getMostInnerTypeName(String typeName) {
        int lastPackageSeparatorIndex = typeName.lastIndexOf('/') + 1
        int lastTypeNameSeparatorIndex = typeName.lastIndexOf('$') + 1
        int lastIndex = Math.max(lastPackageSeparatorIndex, lastTypeNameSeparatorIndex)
        return typeName.substring(lastIndex)
    }

    // --- FocusedTypeGettable --- //
    String getFocusedTypeName() { typeDeclarations.floorEntry(textArea.caretPosition)?.value?.typeName }

    Container.Entry getEntry() { entry }

    // --- IndexesChangeListener --- //
    @CompileStatic
    void indexesChanged(Collection<Indexes> collectionOfIndexes) {
        // Update the list of containers
        this.collectionOfIndexes = collectionOfIndexes
        // Refresh links
        boolean refresh = false

        for (def reference : references) {
            def typeName = reference.typeName
            boolean enabled

            if (reference.name) {
                typeName = searchTypeHavingMember(typeName, reference.name, reference.descriptor, entry)
                if (typeName) {
                    // Replace type with the real type containing the referenced member
                    reference.typeName = typeName
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
            def type = api.getTypeFactory(entry).make(api, entry, typeName)

            if (type) {
                if (descriptor.indexOf('(') == -1) {
                    // Search a field
                    for (def field : type.fields) {
                        if (field.name.equals(name) && DescriptorMatcher.matchFieldDescriptors(field.descriptor, descriptor)) {
                            // Field found
                            return typeName
                        }
                    }
                } else {
                    // Search a method
                    for (def method : type.methods) {
                        if (method.name.equals(name) && DescriptorMatcher.matchMethodDescriptors(method.descriptor, descriptor)) {
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
        String typeName
        /**
         * Field or method name or null for type
         */
        String name
        String descriptor

        DeclarationData(int startPosition, int length, String typeName, String name, String descriptor) {
            this.startPosition = startPosition
            this.endPosition = startPosition + length
            this.typeName = typeName
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
        String typeName
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

        ReferenceData(String typeName, String name, String descriptor, String owner) {
            this.typeName = typeName
            this.name = name
            this.descriptor = descriptor
            this.owner = owner
        }

        boolean isAType() { name == null }
        boolean isAField() { descriptor && descriptor.charAt(0) != '('}
        boolean isAMethod() { descriptor && descriptor.charAt(0) == '('}
        boolean isAConstructor() { "<init>".equals(name) }
    }
}
