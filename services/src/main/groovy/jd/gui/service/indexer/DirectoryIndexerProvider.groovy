/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.indexer

import groovy.transform.CompileStatic
import jd.gui.api.API
import jd.gui.api.model.Container
import jd.gui.api.model.Indexes

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
