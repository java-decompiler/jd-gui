/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.indexer;

import org.jd.gui.api.model.Container;
import org.jd.gui.api.model.Indexes;
import org.jd.gui.spi.Indexer;
import org.jd.gui.util.exception.ExceptionUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

public abstract class AbstractIndexerProvider implements Indexer {
    protected List<String> externalSelectors;
    protected Pattern externalPathPattern;

    /**
     * Initialize "selectors" and "pathPattern" with optional external properties file
     */
    public AbstractIndexerProvider() {
        Properties properties = new Properties();
        Class clazz = this.getClass();

        try (InputStream is = clazz.getClassLoader().getResourceAsStream(clazz.getName().replace('.', '/') + ".properties")) {
            if (is != null) {
                properties.load(is);
            }
        } catch (IOException e) {
            assert ExceptionUtil.printStackTrace(e);
        }

        init(properties);
    }

    protected void init(Properties properties) {
        String selectors = properties.getProperty("selectors");

        if (selectors != null) {
            externalSelectors = Arrays.asList(selectors.split(","));
        }

        String pathRegExp = properties.getProperty("pathRegExp");

        if (pathRegExp != null) {
            externalPathPattern = Pattern.compile(pathRegExp);
        }
    }

    protected String[] appendSelectors(String selector) {
        if (externalSelectors == null) {
            return new String[] { selector };
        } else {
            int size = externalSelectors.size();
            String[] array = new String[size+1];
            externalSelectors.toArray(array);
            array[size] = selector;
            return array;
        }
    }

    protected String[] appendSelectors(String... selectors) {
        if (externalSelectors == null) {
            return selectors;
        } else {
            int size = externalSelectors.size();
            String[] array = new String[size+selectors.length];
            externalSelectors.toArray(array);
            System.arraycopy(selectors, 0, array, size, selectors.length);
            return array;
        }
    }

    @Override public Pattern getPathPattern() { return externalPathPattern; }

    @SuppressWarnings("unchecked")
    protected static void addToIndexes(Indexes indexes, String indexName, Set<String> set, Container.Entry entry) {
        if (set.size() > 0) {
            Map<String, Collection> index = indexes.getIndex(indexName);

            for (String key : set) {
                index.get(key).add(entry);
            }
        }
    }
}
