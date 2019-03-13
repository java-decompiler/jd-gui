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

class ZipFileIndexerProvider extends AbstractIndexerProvider {

    /**
     * @return local + optional external selectors
     */
    String[] getSelectors() { ['*:file:*.zip', '*:file:*.jar', '*:file:*.war', '*:file:*.ear'] + externalSelectors }

    @CompileStatic
    void index(API api, Container.Entry entry, Indexes indexes) {
        for (def e : entry.children) {
            if (e.isDirectory()) {
                index(api, e, indexes)
            } else {
                api.getIndexer(e)?.index(api, e, indexes)
            }
        }
    }
}
