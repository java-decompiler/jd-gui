/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.indexer

import groovy.transform.CompileStatic
import jd.gui.api.API
import jd.gui.api.model.Container
import jd.gui.api.model.Indexes

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
