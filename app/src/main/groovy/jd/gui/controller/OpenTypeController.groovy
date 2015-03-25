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
import jd.gui.util.UriUtil
import jd.gui.view.OpenTypeView

import java.awt.Cursor
import java.awt.Point
import java.util.regex.Pattern

class OpenTypeController implements IndexesChangeListener {
    static final int CACHE_MAX_ENTRIES = 5*20

    API api
    OpenTypeView openTypeView
    SelectLocationController selectLocationController
    Collection<Indexes> collectionOfIndexes
    Closure openClosure
    long indexesHashCode = 0L
    Map<String, Map<String, Collection<Container.Entry>>> cache

    OpenTypeController(SwingBuilder swing, Configuration configuration, API api) {
        this.api = api
        // Create UI
        openTypeView = new OpenTypeView(
            swing, configuration, api,
            { newPattern -> updateList(newPattern) },                               // onPatternChangedClosure
            { leftBottom, entries, tn -> onTypeSelected(leftBottom, entries, tn) }, // onTypeSelectedClosure
        )
        selectLocationController = new SelectLocationController(swing, configuration, api)
        // Create result cache
        cache = new LinkedHashMap<String, Collection<Container.Entry>>(CACHE_MAX_ENTRIES*3/2, 0.7f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, Collection<Container.Entry>> eldest) {
                return size() > CACHE_MAX_ENTRIES
            }
        }
    }

    void show(Collection<Indexes> collectionOfIndexes, Closure openClosure) {
        // Init attributes
        this.collectionOfIndexes = collectionOfIndexes
        this.openClosure = openClosure
        // Refresh view
        long hashCode = collectionOfIndexes.hashCode()
        if (hashCode != indexesHashCode) {
            // List of indexes has changed -> Refresh result list
            updateList(openTypeView.pattern)
            indexesHashCode = hashCode
        }
        // Show
        openTypeView.show()
    }

    protected void updateList(String pattern) {
        int patternLength = pattern.length()
        Map<String, Collection<Container.Entry>> result

        if (patternLength == 0) {
            result = Collections.emptyMap()
        } else {
            def regExpPattern = createRegExpPattern(pattern)

            result = new HashMap<String, Collection<Container.Entry>>().withDefault { new HashSet<Container.Entry>() }

            for (def indexes : collectionOfIndexes) {
                String key = String.valueOf(indexes.hashCode()) + '***' + pattern
                def matchingEntries = cache.get(key)

                if (matchingEntries) {
                    // Merge 'result' and 'matchingEntries'
                    for (def mapEntry : matchingEntries.entrySet()) {
                        result.get(mapEntry.key).addAll(mapEntry.value)
                    }
                } else {
                    // Waiting the end of indexation...
                    openTypeView.swing.openTypeDialog.rootPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR))
                    def index = indexes.getIndex('typeDeclarations')
                    openTypeView.swing.openTypeDialog.rootPane.setCursor(Cursor.getDefaultCursor())

                    if (index) {
                        matchingEntries = new HashMap<String, Collection<Container.Entry>>().withDefault { new HashSet<Container.Entry>() }

                        // Filter
                        if (patternLength == 1) {
                            match(pattern.charAt(0), index, matchingEntries)
                        } else {
                            def lastKey = key.substring(0, patternLength-1)
                            def lastResult = cache.get(lastKey)
                            if (lastResult) {
                                match(regExpPattern, lastResult, matchingEntries)
                            } else {
                                match(regExpPattern, index, matchingEntries)
                            }
                        }

                        // Store 'matchingEntries'
                        cache.put(key, matchingEntries)
                        // Merge 'result' and 'matchingEntries'
                        for (def mapEntry : matchingEntries.entrySet()) {
                            result.get(mapEntry.key).addAll(mapEntry.value)
                        }
                    }
                }
            }
        }

        // Display
        openTypeView.updateList(result)
    }

    @CompileStatic
    protected static void match(char c, Map<String, Collection<Container.Entry>> index, Map<String, Collection<Container.Entry>> result) {
        // Filter
        if (Character.isLowerCase(c)) {
            char upperCase = Character.toUpperCase(c)

            for (def mapEntry : index.entrySet()) {
                def typeName = mapEntry.key
                def entries = mapEntry.value
                // Search last package separator
                int lastPackageSeparatorIndex = typeName.lastIndexOf('/') + 1
                int lastTypeNameSeparatorIndex = typeName.lastIndexOf('$') + 1
                int lastIndex = Math.max(lastPackageSeparatorIndex, lastTypeNameSeparatorIndex)

                if (lastIndex < typeName.length()) {
                    char first = typeName.charAt(lastIndex)

                    if ((first == c) || (first == upperCase)) {
                        result[typeName].addAll(entries)
                    }
                }
            }
        } else {
            for (def mapEntry : index.entrySet()) {
                def typeName = mapEntry.key
                def entries = mapEntry.value
                // Search last package separator
                int lastPackageSeparatorIndex = typeName.lastIndexOf('/') + 1
                int lastTypeNameSeparatorIndex = typeName.lastIndexOf('$') + 1
                int lastIndex = Math.max(lastPackageSeparatorIndex, lastTypeNameSeparatorIndex)

                if ((lastIndex < typeName.length()) && (typeName.charAt(lastIndex) == c)) {
                    result[typeName].addAll(entries)
                }
            }
        }
    }

    /**
     * Create a regular expression to match package, type and inner type name.
     *
     * Rules:
     *  '*'        matches 0 ou N characters
     *  '?'        matches 1 character
     *  lower case matches insensitive case
     *  upper case matches upper case
     */
    @CompileStatic
    protected static Pattern createRegExpPattern(String pattern) {
        // Create regular expression
        int patternLength = pattern.length()
        def sbPattern = new StringBuffer(patternLength * 4)

        for (int i=0; i<patternLength; i++) {
            char c = pattern.charAt(i)

            if (Character.isUpperCase(c)) {
                if (i > 1) {
                    sbPattern.append('.*')
                }
                sbPattern.append(c)
            } else if (Character.isLowerCase(c)) {
                sbPattern.append('[').append(c).append(Character.toUpperCase(c)).append(']')
            } else if (c == '*') {
                sbPattern.append('.*')
            } else if (c == '?') {
                sbPattern.append('.')
            } else {
                sbPattern.append(c)
            }
        }

        sbPattern.append('.*')

        return Pattern.compile(sbPattern.toString())
    }

    @CompileStatic
    protected static void match(Pattern regExpPattern, Map<String, Collection<Container.Entry>> index, Map<String, Collection<Container.Entry>> result) {
        for (def mapEntry : index.entrySet()) {
            def typeName = mapEntry.key
            def entries = mapEntry.value
            // Search last package separator
            int lastPackageSeparatorIndex = typeName.lastIndexOf('/') + 1
            int lastTypeNameSeparatorIndex = typeName.lastIndexOf('$') + 1
            int lastIndex = Math.max(lastPackageSeparatorIndex, lastTypeNameSeparatorIndex)

            if (regExpPattern.matcher(typeName.substring(lastIndex)).matches()) {
                result[typeName].addAll(entries)
            }
        }
    }

    protected void onTypeSelected(Point leftBottom, Collection<Container.Entry> entries, String typeName) {
        if (entries.size() == 1) {
            // Open the single entry uri
            openClosure(UriUtil.createURI(api, collectionOfIndexes, entries.iterator().next(), null, typeName))
        } else {
            // Multiple entries -> Open a "Select location" popup
            selectLocationController.show(
                new Point(leftBottom.x+(16+2) as int, leftBottom.y+2 as int),
                entries,
                { entry -> openClosure(UriUtil.createURI(api, collectionOfIndexes, entry, null, typeName)) },   // entry selected closure
                { openTypeView.focus() })                                                                       // popup close closure
        }
    }

    // --- IndexesChangeListener --- //
    void indexesChanged(Collection<Indexes> collectionOfIndexes) {
        if (openTypeView.isVisible()) {
            // Update the list of containers
            this.collectionOfIndexes = collectionOfIndexes
            // And refresh
            updateList(openTypeView.pattern)
        }
    }
}
