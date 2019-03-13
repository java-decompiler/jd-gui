/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.indexer

import groovy.transform.CompileStatic
import org.jd.gui.api.API
import org.jd.gui.api.model.Container
import org.jd.gui.api.model.Indexes

class DirectoryIndexerProvider extends AbstractIndexerProvider {

    /**
     * @return local + optional external selectors
     */
    String[] getSelectors() { ['*:dir:*'] + externalSelectors }

    @CompileStatic
    void index(API api, Container.Entry entry, Indexes indexes) {
        int depth = 15

        try {
            depth = Integer.valueOf(api.preferences.get('DirectoryIndexerPreferences.maximumDepth'))
        } catch (NumberFormatException ignore) {
        }

        index(api, entry, indexes, depth)
    }

    @CompileStatic
    void index(API api, Container.Entry entry, Indexes indexes, int depth) {
        if (depth-- > 0) {
            for (def e : entry.children) {
                if (e.isDirectory()) {
                    index(api, e, indexes, depth)
                } else {
                    api.getIndexer(e)?.index(api, e, indexes)
                }
            }
        }
    }
}
