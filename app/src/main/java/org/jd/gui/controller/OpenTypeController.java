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
import org.jd.gui.util.exception.ExceptionUtil;
import org.jd.gui.util.net.UriUtil;
import org.jd.gui.view.OpenTypeView;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class OpenTypeController implements IndexesChangeListener {
    protected static final int CACHE_MAX_ENTRIES = 5*20;

    protected API api;
    protected ScheduledExecutorService executor;
    protected Collection<Future<Indexes>> collectionOfFutureIndexes;
    protected Consumer<URI> openCallback;

    protected JFrame mainFrame;
    protected OpenTypeView openTypeView;
    protected SelectLocationController selectLocationController;

    protected long indexesHashCode = 0L;
    protected Map<String, Map<String, Collection>> cache;

    public OpenTypeController(API api, ScheduledExecutorService executor, JFrame mainFrame) {
        this.api = api;
        this.executor = executor;
        this.mainFrame = mainFrame;
        // Create UI
        openTypeView = new OpenTypeView(api, mainFrame, this::updateList, this::onTypeSelected);
        selectLocationController = new SelectLocationController(api, mainFrame);
        // Create result cache
        cache = new LinkedHashMap<String, Map<String, Collection>>(CACHE_MAX_ENTRIES*3/2, 0.7f, true) {
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
            // List of indexes has changed -> Refresh result list
            updateList(openTypeView.getPattern());
            indexesHashCode = hashCode;
        }
        // Show
        openTypeView.show();
    }

    @SuppressWarnings("unchecked")
    protected void updateList(String pattern) {
        int patternLength = pattern.length();

        if (patternLength == 0) {
            // Display
            openTypeView.updateList(Collections.emptyMap());
        } else {
            executor.execute(() -> {
                // Waiting the end of indexation...
                openTypeView.showWaitCursor();

                Pattern regExpPattern = createRegExpPattern(pattern);
                Map<String, Collection<Container.Entry>> result = new HashMap<>();

                try {
                    for (Future<Indexes> futureIndexes : collectionOfFutureIndexes) {
                        if (futureIndexes.isDone()) {
                            Indexes indexes = futureIndexes.get();
                            String key = String.valueOf(indexes.hashCode()) + "***" + pattern;
                            Map<String, Collection> matchingEntries = cache.get(key);

                            if (matchingEntries != null) {
                                // Merge 'result' and 'matchingEntries'
                                for (Map.Entry<String, Collection> mapEntry : matchingEntries.entrySet()) {
                                    Collection<Container.Entry> collection = result.get(mapEntry.getKey());
                                    if (collection == null) {
                                        result.put(mapEntry.getKey(), collection = new HashSet<>());
                                    }
                                    collection.addAll(mapEntry.getValue());
                                }
                            } else {
                                // Waiting the end of indexation...
                                Map<String, Collection> index = indexes.getIndex("typeDeclarations");

                                if ((index != null) && !index.isEmpty()) {
                                    matchingEntries = new HashMap<>();

                                    // Filter
                                    if (patternLength == 1) {
                                        match(pattern.charAt(0), index, matchingEntries);
                                    } else {
                                        String lastKey = key.substring(0, patternLength - 1);
                                        Map<String, Collection> lastResult = cache.get(lastKey);

                                        if (lastResult != null) {
                                            match(regExpPattern, lastResult, matchingEntries);
                                        } else {
                                            match(regExpPattern, index, matchingEntries);
                                        }
                                    }

                                    // Store 'matchingEntries'
                                    cache.put(key, matchingEntries);

                                    // Merge 'result' and 'matchingEntries'
                                    for (Map.Entry<String, Collection> mapEntry : matchingEntries.entrySet()) {
                                        Collection<Container.Entry> collection = result.get(mapEntry.getKey());
                                        if (collection == null) {
                                            result.put(mapEntry.getKey(), collection = new HashSet<>());
                                        }
                                        collection.addAll(mapEntry.getValue());
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    assert ExceptionUtil.printStackTrace(e);
                }

                SwingUtilities.invokeLater(() -> {
                    openTypeView.hideWaitCursor();
                    // Display
                    openTypeView.updateList(result);
                });
            });
        }
    }

    @SuppressWarnings("unchecked")
    protected static void match(char c, Map<String, Collection> index, Map<String, Collection> result) {
        // Filter
        if (Character.isLowerCase(c)) {
            char upperCase = Character.toUpperCase(c);

            for (Map.Entry<String, Collection> mapEntry : index.entrySet()) {
                String typeName = mapEntry.getKey();
                Collection<Container.Entry> entries = mapEntry.getValue();
                // Search last package separator
                int lastPackageSeparatorIndex = typeName.lastIndexOf('/') + 1;
                int lastTypeNameSeparatorIndex = typeName.lastIndexOf('$') + 1;
                int lastIndex = Math.max(lastPackageSeparatorIndex, lastTypeNameSeparatorIndex);

                if (lastIndex < typeName.length()) {
                    char first = typeName.charAt(lastIndex);

                    if ((first == c) || (first == upperCase)) {
                        add(result, typeName, entries);
                    }
                }
            }
        } else {
            for (Map.Entry<String, Collection> mapEntry : index.entrySet()) {
                String typeName = mapEntry.getKey();
                Collection<Container.Entry> entries = mapEntry.getValue();
                // Search last package separator
                int lastPackageSeparatorIndex = typeName.lastIndexOf('/') + 1;
                int lastTypeNameSeparatorIndex = typeName.lastIndexOf('$') + 1;
                int lastIndex = Math.max(lastPackageSeparatorIndex, lastTypeNameSeparatorIndex);

                if ((lastIndex < typeName.length()) && (typeName.charAt(lastIndex) == c)) {
                    add(result, typeName, entries);
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
    protected static Pattern createRegExpPattern(String pattern) {
        // Create regular expression
        int patternLength = pattern.length();
        StringBuilder sbPattern = new StringBuilder(patternLength * 4);

        for (int i=0; i<patternLength; i++) {
            char c = pattern.charAt(i);

            if (Character.isUpperCase(c)) {
                if (i > 1) {
                    sbPattern.append(".*");
                }
                sbPattern.append(c);
            } else if (Character.isLowerCase(c)) {
                sbPattern.append('[').append(c).append(Character.toUpperCase(c)).append(']');
            } else if (c == '*') {
                sbPattern.append(".*");
            } else if (c == '?') {
                sbPattern.append(".");
            } else {
                sbPattern.append(c);
            }
        }

        sbPattern.append(".*");

        return Pattern.compile(sbPattern.toString());
    }

    @SuppressWarnings("unchecked")
    protected static void match(Pattern regExpPattern, Map<String, Collection> index, Map<String, Collection> result) {
        for (Map.Entry<String, Collection> mapEntry : index.entrySet()) {
            String typeName = mapEntry.getKey();
            Collection<Container.Entry> entries = mapEntry.getValue();
            // Search last package separator
            int lastPackageSeparatorIndex = typeName.lastIndexOf('/') + 1;
            int lastTypeNameSeparatorIndex = typeName.lastIndexOf('$') + 1;
            int lastIndex = Math.max(lastPackageSeparatorIndex, lastTypeNameSeparatorIndex);

            if (regExpPattern.matcher(typeName.substring(lastIndex)).matches()) {
                add(result, typeName, entries);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected static void add(Map<String, Collection> map, String key, Collection value) {
        Collection<Container.Entry> collection = map.get(key);

        if (collection == null) {
            map.put(key, collection = new HashSet<>());
        }

        collection.addAll(value);
    }

    protected void onTypeSelected(Point leftBottom, Collection<Container.Entry> entries, String typeName) {
        if (entries.size() == 1) {
            // Open the single entry uri
            openCallback.accept(UriUtil.createURI(api, collectionOfFutureIndexes, entries.iterator().next(), null, typeName));
        } else {
            // Multiple entries -> Open a "Select location" popup
            selectLocationController.show(
                new Point(leftBottom.x+(16+2), leftBottom.y+2),
                entries,
                (entry) -> openCallback.accept(UriUtil.createURI(api, collectionOfFutureIndexes, entry, null, typeName)), // entry selected callback
                () -> openTypeView.focus());                                                                              // popup close callback
        }
    }

    // --- IndexesChangeListener --- //
    public void indexesChanged(Collection<Future<Indexes>> collectionOfFutureIndexes) {
        if (openTypeView.isVisible()) {
            // Update the list of containers
            this.collectionOfFutureIndexes = collectionOfFutureIndexes;
            // And refresh
            updateList(openTypeView.getPattern());
        }
    }
}
