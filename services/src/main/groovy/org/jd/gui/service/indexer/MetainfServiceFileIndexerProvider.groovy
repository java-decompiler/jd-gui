/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.service.indexer

import groovy.transform.CompileStatic
import org.jd.gui.api.API
import org.jd.gui.api.model.Container
import org.jd.gui.api.model.Indexes

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
            def trim = line.trim()

            if (trim && (trim.charAt(0) != '#')) {
                def internalTypeName = trim.replace('.', '/')

                index.get(internalTypeName).add(entry)
            }
        }
    }
}
