/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.util.index;

import org.jd.gui.api.model.Container;
import org.jd.gui.api.model.Indexes;
import org.jd.gui.util.exception.ExceptionUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class IndexesUtil {
    public static boolean containsInternalTypeName(Collection<Future<Indexes>> collectionOfFutureIndexes, String internalTypeName) {
        return contains(collectionOfFutureIndexes, "typeDeclarations", internalTypeName);
    }

    @SuppressWarnings("unchecked")
    public static List<Container.Entry> findInternalTypeName(Collection<Future<Indexes>> collectionOfFutureIndexes, String internalTypeName) {
        return find(collectionOfFutureIndexes, "typeDeclarations", internalTypeName);
    }

    public static boolean contains(Collection<Future<Indexes>> collectionOfFutureIndexes, String indexName, String key) {
        try {
            for (Future<Indexes> futureIndexes : collectionOfFutureIndexes) {
                if (futureIndexes.isDone()) {
                    Map<String, Collection> index = futureIndexes.get().getIndex(indexName);
                    if ((index != null) && (index.get(key) != null)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            assert ExceptionUtil.printStackTrace(e);
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    public static List<Container.Entry> find(Collection<Future<Indexes>> collectionOfFutureIndexes, String indexName, String key) {
        ArrayList<Container.Entry> entries = new ArrayList<>();

        try {
            for (Future<Indexes> futureIndexes : collectionOfFutureIndexes) {
                if (futureIndexes.isDone()) {
                    Map<String, Collection> index = futureIndexes.get().getIndex(indexName);
                    if (index != null) {
                        Collection<Container.Entry> collection = index.get(key);
                        if (collection != null) {
                            entries.addAll(collection);
                        }
                    }
                }
            }
        } catch (Exception e) {
            assert ExceptionUtil.printStackTrace(e);
        }

        return entries;
    }
}
