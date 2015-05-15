/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.indexer

import groovy.transform.CompileStatic
import jd.gui.api.API
import jd.gui.api.model.Container
import jd.gui.api.model.Indexes
import jd.gui.spi.Indexer

import java.util.regex.Pattern

class ZipFileIndexerProvider implements Indexer {
    String[] getTypes() { ['*:file:*.zip', '*:file:*.jar', '*:file:*.war', '*:file:*.ear'] }

    Pattern getPathPattern() { null }

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
