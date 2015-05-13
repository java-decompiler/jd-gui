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

class TextFileIndexerProvider implements Indexer {
    String[] getTypes() { [
        '*:file:*.txt', '*:file:*.html', '*:file:*.xhtml', '*:file:*.js', '*:file:*.jsp', '*:file:*.jspf',
        '*:file:*.xml', '*:file:*.xsl', '*:file:*.xslt', '*:file:*.xsd', '*:file:*.properties', '*:file:*.sql'] }

    Pattern getPathPattern() { null }

    @CompileStatic
    void index(API api, Container.Entry entry, Indexes indexes) {
        indexes.getIndex('strings').get(entry.inputStream.text).add(entry)
    }
}
