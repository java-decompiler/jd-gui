/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.controller;

import org.jd.gui.api.API;
import org.jd.gui.api.feature.IndexesChangeListener;
import org.jd.gui.api.model.Container;
import org.jd.gui.api.model.Indexes;
import org.jd.gui.api.model.Type;
import org.jd.gui.model.container.DelegatingFilterContainer;
import org.jd.gui.service.type.TypeFactoryService;
import org.jd.gui.spi.TypeFactory;
import org.jd.gui.util.exception.ExceptionUtil;
import org.jd.gui.util.function.TriConsumer;
import org.jd.gui.view.SearchInConstantPoolsView;

import javax.swing.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class SearchInConstantPoolsController implements IndexesChangeListener {
    protected static final int CACHE_MAX_ENTRIES = 5*20*9;

    protected API api;
    protected ScheduledExecutorService executor;

    protected JFrame mainFrame;
    protected SearchInConstantPoolsView searchInConstantPoolsView;
    protected Map<String, Map<String, Collection>> cache;
    protected Set<DelegatingFilterContainer> delegatingFilterContainers = new HashSet<>();
    protected Collection<Future<Indexes>> collectionOfFutureIndexes;
    protected Consumer<URI> openCallback;
    protected long indexesHashCode = 0L;

    @SuppressWarnings("unchecked")
    public SearchInConstantPoolsController(API api, ScheduledExecutorService executor, JFrame mainFrame) {
        this.api = api;
        this.executor = executor;
        this.mainFrame = mainFrame;
        // Create UI
        this.searchInConstantPoolsView = new SearchInConstantPoolsView(
            api, mainFrame,
            new BiConsumer<String, Integer>() {
                @Override public void accept(String pattern, Integer flags) { updateTree(pattern, flags); }
            },
            new TriConsumer<URI, String, Integer>() {
                @Override public void accept(URI uri, String pattern, Integer flags) { onTypeSelected(uri, pattern, flags); }
            }
        );
        // Create result cache
        this.cache = new LinkedHashMap<String, Map<String, Collection>>(CACHE_MAX_ENTRIES*3/2, 0.7f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Map<String, Collection>> eldest) {
                return size() > CACHE_MAX_ENTRIES;
            }
        };
    }

    public void show(Collection<Future<Indexes>> collectionOfFutureIndexes, Consumer<URI> openCallback) {
        // Init attributes
        this.collectionOfFutureIndexes = collectionOfFutureIndexes;
        this.openCallback = openCallback;
        // Refresh view
        long hashCode = collectionOfFutureIndexes.hashCode();
        if (hashCode != indexesHashCode) {
            // List of indexes has changed
            updateTree(searchInConstantPoolsView.getPattern(), searchInConstantPoolsView.getFlags());
            indexesHashCode = hashCode;
        }
        // Show
        searchInConstantPoolsView.show();
    }

    @SuppressWarnings("unchecked")
    protected void updateTree(String pattern, int flags) {
        delegatingFilterContainers.clear();

        executor.execute(() -> {
            // Waiting the end of indexation...
            searchInConstantPoolsView.showWaitCursor();

            int matchingTypeCount = 0;
            int patternLength = pattern.length();

            if (patternLength > 0) {
                try {
                    for (Future<Indexes> futureIndexes : collectionOfFutureIndexes) {
                        if (futureIndexes.isDone()) {
                            Indexes indexes = futureIndexes.get();
                            HashSet<Container.Entry> matchingEntries = new HashSet<>();
                            // Find matched entries
                            filter(indexes, pattern, flags, matchingEntries);

                            if (!matchingEntries.isEmpty()) {
                                // Search root container with first matching entry
                                Container.Entry parentEntry = matchingEntries.iterator().next();
                                Container container = null;

                                while (parentEntry.getContainer().getRoot() != null) {
                                    container = parentEntry.getContainer();
                                    parentEntry = container.getRoot().getParent();
                                }

                                // TODO In a future release, display matching strings, types, inner-types, fields and methods, not only matching files
                                matchingEntries = getOuterEntries(matchingEntries);

                                matchingTypeCount += matchingEntries.size();

                                // Create a filtered container
                                delegatingFilterContainers.add(new DelegatingFilterContainer(container, matchingEntries));
                            }
                        }
                    }
                } catch (Exception e) {
                    assert ExceptionUtil.printStackTrace(e);
                }
            }

            final int count = matchingTypeCount;

            searchInConstantPoolsView.hideWaitCursor();
            searchInConstantPoolsView.updateTree(delegatingFilterContainers, count);
        });
    }

    protected HashSet<Container.Entry> getOuterEntries(Set<Container.Entry> matchingEntries) {
        HashMap<Container.Entry, Container.Entry> innerTypeEntryToOuterTypeEntry = new HashMap<>();
        HashSet<Container.Entry> matchingOuterEntriesSet = new HashSet<>();

        for (Container.Entry entry : matchingEntries) {
            TypeFactory typeFactory = TypeFactoryService.getInstance().get(entry);

            if (typeFactory != null) {
                Type type = typeFactory.make(api, entry, null);

                if ((type != null) && (type.getOuterName() != null)) {
                    Container.Entry outerTypeEntry = innerTypeEntryToOuterTypeEntry.get(entry);

                    if (outerTypeEntry == null) {
                        HashMap<String, Container.Entry> typeNameToEntry = new HashMap<>();
                        HashMap<String, String> innerTypeNameToOuterTypeName = new HashMap<>();

                        // Populate "typeNameToEntry" and "innerTypeNameToOuterTypeName"
                        for (Container.Entry e : entry.getParent().getChildren()) {
                            typeFactory = TypeFactoryService.getInstance().get(e);

                            if (typeFactory != null) {
                                type = typeFactory.make(api, e, null);

                                if (type != null) {
                                    typeNameToEntry.put(type.getName(), e);
                                    if (type.getOuterName() != null) {
                                        innerTypeNameToOuterTypeName.put(type.getName(), type.getOuterName());
                                    }
                                }
                            }
                        }

                        // Search outer type entries and populate "innerTypeEntryToOuterTypeEntry"
                        for (Map.Entry<String, String> e : innerTypeNameToOuterTypeName.entrySet()) {
                            Container.Entry innerTypeEntry = typeNameToEntry.get(e.getKey());

                            if (innerTypeEntry != null) {
                                String outerTypeName = e.getValue();

                                for (;;) {
                                    String typeName = innerTypeNameToOuterTypeName.get(outerTypeName);
                                    if (typeName != null) {
                                        outerTypeName = typeName;
                                    } else {
                                        break;
                                    }
                                }

                                outerTypeEntry = typeNameToEntry.get(outerTypeName);

                                if (outerTypeEntry != null) {
                                    innerTypeEntryToOuterTypeEntry.put(innerTypeEntry, outerTypeEntry);
                                }
                            }
                        }

                        // Get outer type entry
                        outerTypeEntry = innerTypeEntryToOuterTypeEntry.get(entry);

                        if (outerTypeEntry == null) {
                            outerTypeEntry = entry;
                        }
                    }

                    matchingOuterEntriesSet.add(outerTypeEntry);
                } else{
                    matchingOuterEntriesSet.add(entry);
                }
            }
        }

        return matchingOuterEntriesSet;
    }

    protected void filter(Indexes indexes, String pattern, int flags, Set<Container.Entry> matchingEntries) {
        boolean declarations = ((flags & SearchInConstantPoolsView.SEARCH_DECLARATION) != 0);
        boolean references = ((flags & SearchInConstantPoolsView.SEARCH_REFERENCE) != 0);

        if ((flags & SearchInConstantPoolsView.SEARCH_TYPE) != 0) {
            if (declarations)
                match(indexes, "typeDeclarations", pattern,
                      SearchInConstantPoolsController::matchTypeEntriesWithChar,
                      SearchInConstantPoolsController::matchTypeEntriesWithString, matchingEntries);
            if (references)
                match(indexes, "typeReferences", pattern,
                      SearchInConstantPoolsController::matchTypeEntriesWithChar,
                      SearchInConstantPoolsController::matchTypeEntriesWithString, matchingEntries);
        }

        if ((flags & SearchInConstantPoolsView.SEARCH_CONSTRUCTOR) != 0) {
            if (declarations)
                match(indexes, "constructorDeclarations", pattern,
                      SearchInConstantPoolsController::matchTypeEntriesWithChar,
                      SearchInConstantPoolsController::matchTypeEntriesWithString, matchingEntries);
            if (references)
                match(indexes, "constructorReferences", pattern,
                      SearchInConstantPoolsController::matchTypeEntriesWithChar,
                      SearchInConstantPoolsController::matchTypeEntriesWithString, matchingEntries);
        }

        if ((flags & SearchInConstantPoolsView.SEARCH_METHOD) != 0) {
            if (declarations)
                match(indexes, "methodDeclarations", pattern,
                      SearchInConstantPoolsController::matchWithChar,
                      SearchInConstantPoolsController::matchWithString, matchingEntries);
            if (references)
                match(indexes, "methodReferences", pattern,
                      SearchInConstantPoolsController::matchWithChar,
                      SearchInConstantPoolsController::matchWithString, matchingEntries);
        }

        if ((flags & SearchInConstantPoolsView.SEARCH_FIELD) != 0) {
            if (declarations)
                match(indexes, "fieldDeclarations", pattern,
                      SearchInConstantPoolsController::matchWithChar,
                      SearchInConstantPoolsController::matchWithString, matchingEntries);
            if (references)
                match(indexes, "fieldReferences", pattern,
                      SearchInConstantPoolsController::matchWithChar,
                      SearchInConstantPoolsController::matchWithString, matchingEntries);
        }

        if ((flags & SearchInConstantPoolsView.SEARCH_STRING) != 0) {
            if (declarations || references)
                match(indexes, "strings", pattern,
                      SearchInConstantPoolsController::matchWithChar,
                      SearchInConstantPoolsController::matchWithString, matchingEntries);
        }

        if ((flags & SearchInConstantPoolsView.SEARCH_MODULE) != 0) {
            if (declarations)
                match(indexes, "javaModuleDeclarations", pattern,
                        SearchInConstantPoolsController::matchWithChar,
                        SearchInConstantPoolsController::matchWithString, matchingEntries);
            if (references)
                match(indexes, "javaModuleReferences", pattern,
                        SearchInConstantPoolsController::matchWithChar,
                        SearchInConstantPoolsController::matchWithString, matchingEntries);
        }
    }

    @SuppressWarnings("unchecked")
    protected void match(Indexes indexes, String indexName, String pattern,
                         BiFunction<Character, Map<String, Collection>, Map<String, Collection>> matchWithCharFunction,
                         BiFunction<String, Map<String, Collection>, Map<String, Collection>> matchWithStringFunction,
                         Set<Container.Entry> matchingEntries) {
        int patternLength = pattern.length();

        if (patternLength > 0) {
            String key = String.valueOf(indexes.hashCode()) + "***" + indexName + "***" + pattern;
            Map<String, Collection> matchedEntries = cache.get(key);

            if (matchedEntries == null) {
                Map<String, Collection> index = indexes.getIndex(indexName);

                if (index != null) {
                    if (patternLength == 1) {
                        matchedEntries = matchWithCharFunction.apply(pattern.charAt(0), index);
                    } else {
                        String lastKey = key.substring(0, key.length() - 1);
                        Map<String, Collection> lastMatchedTypes = cache.get(lastKey);
                        if (lastMatchedTypes != null) {
                            matchedEntries = matchWithStringFunction.apply(pattern, lastMatchedTypes);
                        } else {
                            matchedEntries = matchWithStringFunction.apply(pattern, index);
                        }
                    }
                }

                // Cache matchingEntries
                cache.put(key, matchedEntries);
            }

            if (matchedEntries != null) {
                for (Collection<Container.Entry> entries : matchedEntries.values()) {
                    matchingEntries.addAll(entries);
                }
            }
        }
    }

    protected static Map<String, Collection> matchTypeEntriesWithChar(char c, Map<String, Collection> index) {
        if ((c == '*') || (c == '?')) {
            return index;
        } else {
            Map<String, Collection> map = new HashMap<>();

            for (String typeName : index.keySet()) {
                // Search last package separator
                int lastPackageSeparatorIndex = typeName.lastIndexOf('/') + 1;
                int lastTypeNameSeparatorIndex = typeName.lastIndexOf('$') + 1;
                int lastIndex = Math.max(lastPackageSeparatorIndex, lastTypeNameSeparatorIndex);

                if ((lastIndex < typeName.length()) && (typeName.charAt(lastIndex) == c)) {
                    map.put(typeName, index.get(typeName));
                }
            }

            return map;
        }
    }

    protected static Map<String, Collection> matchTypeEntriesWithString(String pattern, Map<String, Collection> index) {
        Pattern p = createPattern(pattern);
        Map<String, Collection> map = new HashMap<>();

        for (String typeName : index.keySet()) {
            // Search last package separator
            int lastPackageSeparatorIndex = typeName.lastIndexOf('/') + 1;
            int lastTypeNameSeparatorIndex = typeName.lastIndexOf('$') + 1;
            int lastIndex = Math.max(lastPackageSeparatorIndex, lastTypeNameSeparatorIndex);

            if (p.matcher(typeName.substring(lastIndex)).matches()) {
                map.put(typeName, index.get(typeName));
            }
        }

        return map;
    }

    protected static Map<String, Collection> matchWithChar(char c, Map<String, Collection> index) {
        if ((c == '*') || (c == '?')) {
            return index;
        } else {
            Map<String, Collection> map = new HashMap<>();

            for (String key : index.keySet()) {
                if (!key.isEmpty() && (key.charAt(0) == c)) {
                    map.put(key, index.get(key));
                }
            }

            return map;
        }
    }

    protected static Map<String, Collection> matchWithString(String pattern, Map<String, Collection> index) {
        Pattern p = createPattern(pattern);
        Map<String, Collection> map = new HashMap<>();

        for (String key : index.keySet()) {
            if (p.matcher(key).matches()) {
                map.put(key, index.get(key));
            }
        }

        return map;
    }

    /**
     * Create a simple regular expression
     *
     * Rules:
     *  '*'        matchTypeEntries 0 ou N characters
     *  '?'        matchTypeEntries 1 character
     */
    protected static Pattern createPattern(String pattern) {
        int patternLength = pattern.length();
        StringBuilder sbPattern = new StringBuilder(patternLength * 2);

        for (int i = 0; i < patternLength; i++) {
            char c = pattern.charAt(i);

            if (c == '*') {
                sbPattern.append(".*");
            } else if (c == '?') {
                sbPattern.append('.');
            } else if (c == '.') {
                sbPattern.append("\\.");
            } else {
                sbPattern.append(c);
            }
        }

        sbPattern.append(".*");

        return Pattern.compile(sbPattern.toString());
    }

    protected void onTypeSelected(URI uri, String pattern, int flags) {
        // Open the single entry uri
        Container.Entry entry = null;

        for (DelegatingFilterContainer container : delegatingFilterContainers) {
            entry = container.getEntry(uri);
            if (entry != null)
                break;
        }

        if (entry != null) {
            StringBuilder sbPattern = new StringBuilder(200 + pattern.length());

            sbPattern.append("highlightPattern=");
            sbPattern.append(pattern);
            sbPattern.append("&highlightFlags=");

            if ((flags & SearchInConstantPoolsView.SEARCH_DECLARATION) != 0)
                sbPattern.append('d');
            if ((flags & SearchInConstantPoolsView.SEARCH_REFERENCE) != 0)
                sbPattern.append('r');
            if ((flags & SearchInConstantPoolsView.SEARCH_TYPE) != 0)
                sbPattern.append('t');
            if ((flags & SearchInConstantPoolsView.SEARCH_CONSTRUCTOR) != 0)
                sbPattern.append('c');
            if ((flags & SearchInConstantPoolsView.SEARCH_METHOD) != 0)
                sbPattern.append('m');
            if ((flags & SearchInConstantPoolsView.SEARCH_FIELD) != 0)
                sbPattern.append('f');
            if ((flags & SearchInConstantPoolsView.SEARCH_STRING) != 0)
                sbPattern.append('s');
            if ((flags & SearchInConstantPoolsView.SEARCH_MODULE) != 0)
                sbPattern.append('M');

            // TODO In a future release, add 'highlightScope' to display search results in correct type and inner-type
            // def type = TypeFactoryService.instance.get(entry)?.make(api, entry, null)
            // if (type) {
            //     sbPattern.append('&highlightScope=')
            //     sbPattern.append(type.name)
            //
            //     def query = sbPattern.toString()
            //     def outerPath = UriUtil.getOuterPath(collectionOfFutureIndexes, entry, type)
            //
            //     openClosure(new URI(entry.uri.scheme, entry.uri.host, outerPath, query, null))
            // } else {
                String query = sbPattern.toString();
                URI u = entry.getUri();

                try {
                    openCallback.accept(new URI(u.getScheme(), u.getHost(), u.getPath(), query, null));
                } catch (URISyntaxException e) {
                    assert ExceptionUtil.printStackTrace(e);
                }
            // }
        }
    }

    // --- IndexesChangeListener --- //
    public void indexesChanged(Collection<Future<Indexes>> collectionOfFutureIndexes) {
        if (searchInConstantPoolsView.isVisible()) {
            // Update the list of containers
            this.collectionOfFutureIndexes = collectionOfFutureIndexes;
            // And refresh
            updateTree(searchInConstantPoolsView.getPattern(), searchInConstantPoolsView.getFlags());
        }
    }
}
