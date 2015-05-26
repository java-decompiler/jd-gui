/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.indexer

import groovy.transform.CompileStatic
import jd.gui.api.API
import jd.gui.api.model.Container
import jd.gui.api.model.Indexes

import java.util.regex.Pattern

class MetainfServiceFileIndexerProvider extends AbstractIndexerProvider {

    /**
     * @return local + optional external selectors
     */
    String[] getSelectors() { ['*:file:*'] + externalSelectors }

    /**
     * @return external or local path pattern
     */
    Pattern getPathPattern() { externalPathPattern ?: ~/META-INF\/services\/[^\/]+/ }

    @CompileStatic
    void index(API api, Container.Entry entry, Indexes indexes) {
        def index = indexes.getIndex('typeReferences')

        entry.inputStream.text.eachLine { String line ->
            index.get(line).add(entry)
        }
    }
}
