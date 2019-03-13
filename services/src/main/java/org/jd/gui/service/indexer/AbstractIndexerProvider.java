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
    AbstractIndexerProvider() {
        Properties properties = new Properties();
        Class clazz = this.getClass();

        try (InputStream is = clazz.getClassLoader().getResourceAsStream(clazz.getName().replace('.', '/') + ".properties")) {
            if (is != null) {
                properties.load(is);
            }
        } catch (IOException ignore) {
        }

        init(properties);
    }

    protected void init(Properties properties) {
        String selectors = properties.getProperty("selectors");
        externalSelectors = (selectors == null) ? null : Arrays.asList(selectors.split(","));

        String pathRegExp = properties.getProperty("pathRegExp");
        externalPathPattern = (pathRegExp == null) ? null : Pattern.compile(pathRegExp);
    }

    protected List<String> getExternalSelectors() { return externalSelectors; }
    protected Pattern getExternalPathPattern() { return externalPathPattern; }

    public String[] getSelectors() {
        return (externalSelectors==null) ? null : externalSelectors.toArray(new String[externalSelectors.size()]);
    }
    public Pattern getPathPattern() { return externalPathPattern; }

    @SuppressWarnings("unchecked")
    protected static void addToIndex(Indexes indexes, String indexName, Set<String> set, Container.Entry entry) {
        if (set.size() > 0) {
            Map<String, Collection> index = indexes.getIndex(indexName);

            for (String key : set) {
                index.get(key).add(entry);
            }
        }
    }
}
