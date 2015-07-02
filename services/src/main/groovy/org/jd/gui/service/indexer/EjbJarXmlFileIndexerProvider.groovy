/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.service.indexer

import org.jd.gui.api.API
import org.jd.gui.api.model.Container
import org.jd.gui.api.model.Indexes
import org.jd.gui.util.xml.AbstractXmlPathFinder

class EjbJarXmlFileIndexerProvider extends XmlBasedFileIndexerProvider {

    /**
     * @return local + optional external selectors
     */
    String[] getSelectors() { ['*:file:META-INF/ejb-jar.xml'] + externalSelectors }

    void index(API api, Container.Entry entry, Indexes indexes) {
        super.index(api, entry, indexes)

        new EjbJarXmlPathFinder(entry, indexes).find(entry.inputStream.text)
    }

    static class EjbJarXmlPathFinder extends AbstractXmlPathFinder {
        Container.Entry entry
        Map<String, Collection> index

        EjbJarXmlPathFinder(Container.Entry entry, Indexes indexes) {
            super([
                'ejb-jar/assembly-descriptor/application-exception/exception-class',
                'ejb-jar/assembly-descriptor/interceptor-binding/interceptor-class',

                'ejb-jar/enterprise-beans/entity/home',
                'ejb-jar/enterprise-beans/entity/remote',
                'ejb-jar/enterprise-beans/entity/ejb-class',
                'ejb-jar/enterprise-beans/entity/prim-key-class',

                'ejb-jar/enterprise-beans/message-driven/ejb-class',
                'ejb-jar/enterprise-beans/message-driven/messaging-type',
                'ejb-jar/enterprise-beans/message-driven/resource-ref/injection-target/injection-target-class',
                'ejb-jar/enterprise-beans/message-driven/resource-env-ref/injection-target/injection-target-class',

                'ejb-jar/enterprise-beans/session/home',
                'ejb-jar/enterprise-beans/session/local',
                'ejb-jar/enterprise-beans/session/remote',
                'ejb-jar/enterprise-beans/session/business-local',
                'ejb-jar/enterprise-beans/session/business-remote',
                'ejb-jar/enterprise-beans/session/service-endpoint',
                'ejb-jar/enterprise-beans/session/ejb-class',
                'ejb-jar/enterprise-beans/session/ejb-ref/home',
                'ejb-jar/enterprise-beans/session/ejb-ref/remote',

                'ejb-jar/interceptors/interceptor/around-invoke/class',
                'ejb-jar/interceptors/interceptor/ejb-ref/home',
                'ejb-jar/interceptors/interceptor/ejb-ref/remote',
                'ejb-jar/interceptors/interceptor/interceptor-class'
            ])
            this.entry = entry
            this.index = indexes.getIndex('typeReferences');
        }

        void handle(String path, String text, int position) {
            index.get(text.replace('.', '/')).add(entry);
        }
    }
}
