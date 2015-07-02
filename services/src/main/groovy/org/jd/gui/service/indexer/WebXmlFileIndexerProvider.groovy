/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.service.indexer

import org.jd.gui.api.API
import org.jd.gui.api.model.Container
import org.jd.gui.api.model.Indexes
import org.jd.gui.util.xml.AbstractXmlPathFinder

class WebXmlFileIndexerProvider extends XmlBasedFileIndexerProvider {

    /**
     * @return local + optional external selectors
     */
    String[] getSelectors() { ['*:file:WEB-INF/web.xml'] + externalSelectors }

    void index(API api, Container.Entry entry, Indexes indexes) {
        super.index(api, entry, indexes)

        new WebXmlPathFinder(entry, indexes).find(entry.inputStream.text)
    }

    static class WebXmlPathFinder extends AbstractXmlPathFinder {
        Container.Entry entry
        Map<String, Collection> index

        WebXmlPathFinder(Container.Entry entry, Indexes indexes) {
            super([
                'web-app/filter/filter-class',
                'web-app/listener/listener-class',
                'web-app/servlet/servlet-class'
            ])
            this.entry = entry
            this.index = indexes.getIndex('typeReferences');
        }

        void handle(String path, String text, int position) {
            index.get(text.replace('.', '/')).add(entry);
        }
    }
}
