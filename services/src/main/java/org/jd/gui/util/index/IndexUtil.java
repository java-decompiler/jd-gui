/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.util.index;

import org.jd.gui.api.model.Container;
import org.jd.gui.api.model.Indexes;

import java.util.*;

public class IndexUtil {
    public static boolean containsInternalTypeName(Collection<Indexes> collectionOfIndexes, String internalTypeName) {
        for (Indexes indexes : collectionOfIndexes) {
            Map<String, Collection> index = indexes.getIndex("typeDeclarations");
            if ((index != null) && (index.get(internalTypeName) != null)) {
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    public static List<Container.Entry> grepInternalTypeName(Collection<Indexes> collectionOfIndexes, String internalTypeName) {
        ArrayList<Container.Entry> entries = new ArrayList<>();

        for (Indexes indexes : collectionOfIndexes) {
            Map<String, Collection> index = indexes.getIndex("typeDeclarations");
            if (index != null) {
                Collection<Container.Entry> collection = index.get(internalTypeName);
                if (collection != null) {
                    entries.addAll(collection);
                }
            }
        }

        return entries;
    }
}
