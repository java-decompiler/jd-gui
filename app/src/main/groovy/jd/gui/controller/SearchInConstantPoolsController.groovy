/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.controller

import groovy.swing.SwingBuilder
import groovy.transform.CompileStatic
import jd.gui.api.API
import jd.gui.api.feature.IndexesChangeListener
import jd.gui.api.model.Container
import jd.gui.api.model.Indexes
import jd.gui.model.configuration.Configuration
import jd.gui.model.container.FilteredContainerWrapper
import jd.gui.service.type.TypeFactoryService
import jd.gui.util.UriUtil
import jd.gui.view.SearchInConstantPoolsView

import java.awt.Cursor
import java.util.regex.Pattern

class SearchInConstantPoolsController implements IndexesChangeListener {
    static final int CACHE_MAX_ENTRIES = 5*20*9

    API api
    SearchInConstantPoolsView searchInConstantPoolsView
    Map<String, Map<String, Collection<Container.Entry>>> cache
    Set<FilteredContainerWrapper> filteredContainerWrappers
    Collection<Indexes> collectionOfIndexes
    Closure openClosure
    long indexesHashCode = 0L

    SearchInConstantPoolsController(SwingBuilder swing, Configuration configuration, API api) {
        this.api = api
        // Create UI
        this.searchInConstantPoolsView = new SearchInConstantPoolsView(
                swing, configuration, api,
                { pattern, flags -> updateTree(pattern, flags) },               // onPatternChangedClosure
                { uri, pattern, flags -> onTypeSelected(uri, pattern, flags) }  // onTypeSelectedClosure
        )
        // Create result cache
        this.cache = new LinkedHashMap<String, Collection<Container.Entry>>(CACHE_MAX_ENTRIES*3/2, 0.7f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, Collection<Container.Entry>> eldest) {
                return size() > CACHE_MAX_ENTRIES
            }
        }
        this.filteredContainerWrappers = new HashSet<FilteredContainerWrapper>()
    }

    void show(Collection<Indexes> collectionOfIndexes, Closure openClosure) {
        // Init attributes
        this.collectionOfIndexes = collectionOfIndexes
        this.openClosure = openClosure
        // Refresh view
        long hashCode = collectionOfIndexes.hashCode()
        if (hashCode != indexesHashCode) {
            // List of indexes has changed
            updateTree(searchInConstantPoolsView.pattern, searchInConstantPoolsView.flags)
            indexesHashCode = hashCode
        }
        // Show
        searchInConstantPoolsView.show()
    }

    void updateTree(String pattern, int flags) {
        filteredContainerWrappers.clear()

        int matchingTypeCount = 0
        int patternLength = pattern.length()

        if (patternLength > 0) {
            for (def indexes : collectionOfIndexes) {
                def matchingEntries = new HashSet<Container.Entry>()

                // Waiting the end of indexation...
                searchInConstantPoolsView.swing.searchInConstantPoolsDialog.rootPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR))
                indexes.waitIndexers()
                searchInConstantPoolsView.swing.searchInConstantPoolsDialog.rootPane.setCursor(Cursor.getDefaultCursor())
                // Find matched entries
                filter(indexes, pattern, flags, matchingEntries)

                if (! matchingEntries.isEmpty()) {
                    // Search root container with first matching entry
                    def parentEntry = matchingEntries.iterator().next()
                    def container = null

                    while (parentEntry.container.root) {
                        container = parentEntry.container
                        parentEntry = container.root.parent
                    }

                    // TODO In a future release, display matching strings, types, inner-types, fields and methods, not only matching files
                    matchingEntries = getOuterEntries(matchingEntries)

                    matchingTypeCount += matchingEntries.size()

                    // Dummy parent entry wrapper
                    def parentEntryWrapper = new Container.Entry() {
                        Collection<Container.Entry> children

                        Container getContainer() { parentEntry.container }
                        Container.Entry getParent() { null }
                        URI getUri() { parentEntry.uri }
                        String getPath() { parentEntry.path }
                        boolean isDirectory() { false }
                        long length() { 0 }
                        InputStream getInputStream() { null }
                        Collection<Container.Entry> getChildren() { children }
                    }
                    // Create a filtered container
                    def containerWrapper = new FilteredContainerWrapper(container, parentEntryWrapper, matchingEntries)
                    // Initialization of 'children' of dummy parent entry wrapper
                    parentEntryWrapper.children = containerWrapper.root.children

                    filteredContainerWrappers.add(containerWrapper)
                }
            }
        }

        searchInConstantPoolsView.updateTree(filteredContainerWrappers, matchingTypeCount)
    }

    Collection<Container.Entry> getOuterEntries(Set<Container.Entry> matchingEntries) {
        def innerTypeEntryToOuterTypeEntry = [:]
        def matchingOuterEntriesSet = new HashSet<Container.Entry>()

        for (def entry : matchingEntries) {
            def type = TypeFactoryService.instance.get(entry)?.make(api, entry, null)

            if (type?.outerName) {
                def outerTypeEntry = innerTypeEntryToOuterTypeEntry.get(entry)

                if (outerTypeEntry == null) {
                    def typeNameToEntry = [:]
                    def innerTypeNameToOuterTypeName = [:]

                    // Populate "typeNameToEntry" and "innerTypeNameToOuterTypeName"
                    for (def e : entry.parent.children) {
                        type = TypeFactoryService.instance.get(e)?.make(api, e, null)

                        if (type) {
                            typeNameToEntry.put(type.name, e)
                            if (type.outerName) {
                                innerTypeNameToOuterTypeName.put(type.name, type.outerName)
                            }
                        }
                    }

                    // Search outer type entries and populate "innerTypeEntryToOuterTypeEntry"
                    for (def e : innerTypeNameToOuterTypeName.entrySet()) {
                        def innerTypeEntry = typeNameToEntry.get(e.key)

                        if (innerTypeEntry) {
                            def outerTypeName = e.value

                            for (;;) {
                                def typeName = innerTypeNameToOuterTypeName.get(outerTypeName)
                                if (typeName) {
                                    outerTypeName = typeName
                                } else {
                                    break
                                }
                            }

                            outerTypeEntry = typeNameToEntry.get(outerTypeName)

                            if (outerTypeEntry) {
                                innerTypeEntryToOuterTypeEntry.put(innerTypeEntry, outerTypeEntry)
                            }
                        }
                    }

                    // Get outer type entry
                    outerTypeEntry = innerTypeEntryToOuterTypeEntry.get(entry) ?: entry
                }

                matchingOuterEntriesSet.add(outerTypeEntry)
            } else {
                matchingOuterEntriesSet.add(entry)
            }
        }

        // Return outer type entries sorted by path
        return matchingOuterEntriesSet.sort { e1, e2 -> e1.path.compareTo(e2.path) }
    }

    void filter(Indexes indexes, String pattern, int flags, Set<Container.Entry> matchingEntries) {
        boolean declarations = ((flags & SearchInConstantPoolsView.SEARCH_TYPE_DECLARATION) != 0)
        boolean references = ((flags & SearchInConstantPoolsView.SEARCH_TYPE_REFERENCE) != 0)

        def matchTypeEntriesWithCharClosure = { c, index -> matchTypeEntriesWithChar(c, index) }
        def matchTypeEntriesWithStringClosure = { s, index -> matchTypeEntriesWithString(s, index) }
        def matchWithCharClosure = { c, index -> matchWithChar(c, index) }
        def matchWithStringClosure = { s, index -> matchWithString(s, index) }
        def matchStringWithCharClosure = { c, index -> matchStringWithChar(c, index) }
        def matchStringWithStringClosure = { s, index -> matchStringWithString(s, index) }

        if ((flags & SearchInConstantPoolsView.SEARCH_TYPE_TYPE) != 0) {
            if (declarations)
                match(indexes, 'typeDeclarations', pattern,
                        matchTypeEntriesWithCharClosure, matchTypeEntriesWithStringClosure, matchingEntries)
            if (references)
                match(indexes, 'typeReferences', pattern,
                        matchTypeEntriesWithCharClosure, matchTypeEntriesWithStringClosure, matchingEntries)
        }

        if ((flags & SearchInConstantPoolsView.SEARCH_TYPE_CONSTRUCTOR) != 0) {
            if (declarations)
                match(indexes, 'constructorDeclarations', pattern,
                        matchTypeEntriesWithCharClosure, matchTypeEntriesWithStringClosure, matchingEntries)
            if (references)
                match(indexes, 'constructorReferences', pattern,
                        matchTypeEntriesWithCharClosure, matchTypeEntriesWithStringClosure, matchingEntries)
        }

        if ((flags & SearchInConstantPoolsView.SEARCH_TYPE_METHOD) != 0) {
            if (declarations)
                match(indexes, 'methodDeclarations', pattern,
                        matchWithCharClosure, matchWithStringClosure, matchingEntries)
            if (references)
                match(indexes, 'methodReferences', pattern,
                        matchWithCharClosure, matchWithStringClosure, matchingEntries)
        }

        if ((flags & SearchInConstantPoolsView.SEARCH_TYPE_FIELD) != 0) {
            if (declarations)
                match(indexes, 'fieldDeclarations', pattern,
                        matchWithCharClosure, matchWithStringClosure, matchingEntries)
            if (references)
                match(indexes, 'fieldReferences', pattern,
                        matchWithCharClosure, matchWithStringClosure, matchingEntries)
        }

        if ((flags & SearchInConstantPoolsView.SEARCH_TYPE_STRING) != 0) {
            if (declarations || references)
                match(indexes, 'strings', pattern,
                        matchStringWithCharClosure, matchStringWithStringClosure, matchingEntries)
        }
    }

    @CompileStatic
    void match(Indexes indexes, String indexName, String pattern,
            Closure matchWithCharClosure, Closure matchWithStringClosure, Set<Container.Entry> matchingEntries) {
        int patternLength = pattern.length()

        if (patternLength > 0) {
            String key = String.valueOf(indexes.hashCode()) + '***' + indexName + '***' + pattern
            Map<String, Collection<Container.Entry>> matchedTypes = cache.get(key)

            if (matchedTypes == null) {
                def index = indexes.getIndex(indexName)

                if (patternLength == 1) {
                    matchedTypes = (Map<String, Collection<Container.Entry>>)matchWithCharClosure(pattern.charAt(0), index)
                } else {
                    def lastKey = key.substring(0, key.length() - 1)
                    def lastMatchedTypes = cache.get(lastKey)
                    if (lastMatchedTypes) {
                        matchedTypes = (Map<String, Collection<Container.Entry>>)matchWithStringClosure(pattern, lastMatchedTypes)
                    } else {
                        matchedTypes = (Map<String, Collection<Container.Entry>>)matchWithStringClosure(pattern, index)
                    }
                }

                // Cache matchingEntries
                cache.put(key, matchedTypes)
            }

            if (matchedTypes) {
                for (def entries : matchedTypes.values()) {
                    matchingEntries.addAll(entries)
                }
            }
        }
    }

    @CompileStatic
    static Map<String, Collection<Container.Entry>> matchTypeEntriesWithChar(char c, Map<String, Collection<Container.Entry>> index) {
        if ((c == '*') || (c == '?')) {
            return Collections.emptyMap()
        } else {
            return index.findAll { String typeName, entries ->
                // Search last package separator
                int lastPackageSeparatorIndex = typeName.lastIndexOf('/') + 1
                int lastTypeNameSeparatorIndex = typeName.lastIndexOf('$') + 1
                int lastIndex = Math.max(lastPackageSeparatorIndex, lastTypeNameSeparatorIndex)
                return (lastIndex < typeName.length()) && (typeName.charAt(lastIndex) == c)
            }
        }
    }

    @CompileStatic
    static Map<String, Collection<Container.Entry>> matchTypeEntriesWithString(String pattern, Map<String, Collection<Container.Entry>> index) {
        def p = createPattern(pattern)
        return index.findAll { String typeName, entries ->
            // Search last package separator
            int lastPackageSeparatorIndex = typeName.lastIndexOf('/') + 1
            int lastTypeNameSeparatorIndex = typeName.lastIndexOf('$') + 1
            int lastIndex = Math.max(lastPackageSeparatorIndex, lastTypeNameSeparatorIndex)
            return p.matcher(typeName.substring(lastIndex)).matches()
        }
    }

    @CompileStatic
    static Map<String, Collection<Container.Entry>> matchWithChar(char c, Map<String, Collection<Container.Entry>> index) {
        if ((c == '*') || (c == '?')) {
            return Collections.emptyMap()
        } else {
            return index.findAll { String key, entries -> !key.isEmpty() && (key.charAt(0) == c) }
        }
    }

    @CompileStatic
    static Map<String, Collection<Container.Entry>> matchWithString(String pattern, Map<String, Collection<Container.Entry>> index) {
        def p = createPattern(pattern)
        return index.findAll { String key, entries -> p.matcher(key).matches() }
    }

    @CompileStatic
    static Map<String, Collection<Container.Entry>> matchStringWithChar(char c, Map<String, Collection<Container.Entry>> index) {
        if ((c == '*') || (c == '?')) {
            return Collections.emptyMap()
        } else {
            def p = Pattern.compile(String.valueOf(c))
            return index.findAll { String key, entries -> p.matcher(key).find() }
        }
    }

    @CompileStatic
    static Map<String, Collection<Container.Entry>> matchStringWithString(String pattern, Map<String, Collection<Container.Entry>> index) {
        def p = createPattern(pattern)
        return index.findAll { String key, entries -> p.matcher(key).find() }
    }

    /**
     * Create a simple regular expression
     *
     * Rules:
     *  '*'        matchTypeEntries 0 ou N characters
     *  '?'        matchTypeEntries 1 character
     */
    @CompileStatic
    static Pattern createPattern(String pattern) {
        int patternLength = pattern.length()
        def sbPattern = new StringBuffer(patternLength * 2)

        for (int i = 0; i < patternLength; i++) {
            char c = pattern.charAt(i)

            if (c == '*') {
                sbPattern.append('.*')
            } else if (c == '?') {
                sbPattern.append('.')
            } else if (c == '.') {
                sbPattern.append('\\.')
            } else {
                sbPattern.append(c)
            }
        }

        sbPattern.append('.*')

        return Pattern.compile(sbPattern.toString())
    }

    void onTypeSelected(URI uri, String pattern, int flags) {
        // Open the single entry uri
        def entry = null

        for (def container : filteredContainerWrappers) {
            entry = container.getEntry(uri)
            if (entry)
                break
        }

        if (entry) {
            def sbPattern = new StringBuffer(200 + pattern.length())

            sbPattern.append('highlightPattern=')
            sbPattern.append(pattern)
            sbPattern.append('&highlightFlags=')

            if ((flags & SearchInConstantPoolsView.SEARCH_TYPE_DECLARATION) != 0)
                sbPattern.append('d')
            if ((flags & SearchInConstantPoolsView.SEARCH_TYPE_REFERENCE) != 0)
                sbPattern.append('r')
            if ((flags & SearchInConstantPoolsView.SEARCH_TYPE_TYPE) != 0)
                sbPattern.append('t')
            if ((flags & SearchInConstantPoolsView.SEARCH_TYPE_CONSTRUCTOR) != 0)
                sbPattern.append('c')
            if ((flags & SearchInConstantPoolsView.SEARCH_TYPE_METHOD) != 0)
                sbPattern.append('m')
            if ((flags & SearchInConstantPoolsView.SEARCH_TYPE_FIELD) != 0)
                sbPattern.append('f')
            if ((flags & SearchInConstantPoolsView.SEARCH_TYPE_STRING) != 0)
                sbPattern.append('s')

            // TODO In a future release, add 'highlightScope' to display search results in correct type and inner-type
            // def type = TypeFactoryService.instance.get(entry)?.make(api, entry, null)
            // if (type) {
            //     sbPattern.append('&highlightScope=')
            //     sbPattern.append(type.name)
            //
            //     def query = sbPattern.toString()
            //     def outerPath = UriUtil.getOuterPath(collectionOfIndexes, entry, type)
            //
            //     openClosure(new URI(entry.uri.scheme, entry.uri.host, outerPath, query, null))
            // } else {
                def query = sbPattern.toString()

                openClosure(new URI(entry.uri.scheme, entry.uri.host, entry.uri.path, query, null))
            // }
        }
    }

    // --- IndexesChangeListener --- //
    void indexesChanged(Collection<Indexes> collectionOfIndexes) {
        if (searchInConstantPoolsView.isVisible()) {
            // Update the list of containers
            this.collectionOfIndexes = collectionOfIndexes
            // And refresh
            updateTree(searchInConstantPoolsView.pattern, searchInConstantPoolsView.flags)
        }
    }
}
