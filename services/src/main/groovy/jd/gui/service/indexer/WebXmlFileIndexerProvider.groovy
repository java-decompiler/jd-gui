/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.indexer

import jd.gui.api.API
import jd.gui.api.model.Container
import jd.gui.api.model.Indexes
import jd.gui.util.xml.AbstractXmlPathFinder

class WebXmlFileIndexerProvider extends XmlFileIndexerProvider {
    String[] getSelectors() { ['*:file:WEB-INF/web.xml'] }

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
