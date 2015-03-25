/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.util

import jd.gui.api.API
import jd.gui.api.model.Container
import jd.gui.api.model.Indexes
import jd.gui.api.model.Type
import jd.gui.service.type.TypeFactoryService

class UriUtil {
    /*
     * Convert inner entry URI to outer entry uri with a fragment.
     * Example:
     *  file://codebase/a/b/c/D$E.class => file://codebase/a/b/c/D.class#typeDeclaration=D$E
     */
    static URI createURI(API api, Collection<Indexes> collectionOfIndexes, Container.Entry entry, String query, String fragment) {
        def type = TypeFactoryService.instance.get(entry)?.make(api, entry, null)
        def uri = entry.uri
        def path = type?.outerName ? getOuterPath(collectionOfIndexes, entry, type) : uri.path
        return new URI(uri.scheme, uri.host, path, query, fragment)
    }

    protected static String getOuterPath(Collection<Indexes> collectionOfIndexes, Container.Entry entry, Type type) {
        def outerName = type.outerName

        if (outerName) {
            for (def indexes : collectionOfIndexes) {
                def outerEntries = indexes.getIndex('typeDeclarations').get(outerName)
                if (outerEntries) {
                    for (def outerEntry : outerEntries) {
                        if (outerEntry.container == entry.container) {
                            return outerEntry.uri.path
                        }
                    }
                }
            }
        }

        return entry.uri.path
    }
}
