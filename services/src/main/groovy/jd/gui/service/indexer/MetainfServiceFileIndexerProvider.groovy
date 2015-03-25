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

class MetainfServiceFileIndexerProvider implements Indexer {
    Pattern pattern = ~/META-INF\/services\/[^\/]+/

    String[] getTypes() { ['*:file:*'] }

    Pattern getPathPattern() { pattern }

    @CompileStatic
    void index(API api, Container.Entry entry, Indexes indexes) {
        def index = indexes.getIndex('typeReferences')

        entry.inputStream.text.eachLine { String line ->
            index.get(line).add(entry)
        }
    }
}
