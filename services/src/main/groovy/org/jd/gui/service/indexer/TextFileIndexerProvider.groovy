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

class TextFileIndexerProvider extends AbstractIndexerProvider {

    /**
     * @return local + optional external selectors
     */
    String[] getSelectors() {
        ['*:file:*.txt', '*:file:*.html', '*:file:*.xhtml', '*:file:*.js', '*:file:*.jsp', '*:file:*.jspf',
         '*:file:*.xml', '*:file:*.xsl', '*:file:*.xslt', '*:file:*.xsd', '*:file:*.properties', '*:file:*.sql',
         '*:file:*.yaml', '*:file:*.yml', '*:file:*.json'] + externalSelectors }

    @CompileStatic
    void index(API api, Container.Entry entry, Indexes indexes) {
        indexes.getIndex('strings').get(entry.inputStream.text).add(entry)
    }
}
