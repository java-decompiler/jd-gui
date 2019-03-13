/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.indexer

import groovy.transform.CompileStatic
import org.jd.gui.api.model.Container
import org.jd.gui.service.extension.ExtensionService
import org.jd.gui.spi.Indexer

@CompileStatic
@Singleton(lazy = true)
class IndexerService {
    protected Map<String, Indexers> mapProviders = populate()

    protected Map<String, Indexers> populate() {
        Collection<Indexer> providers = ExtensionService.instance.load(Indexer)
        Map<String, Indexers> mapProviders = [:]

        def mapProvidersWithDefault = mapProviders.withDefault { new Indexers() }

        for (def provider : providers) {
            for (String selector : provider.selectors) {
                mapProvidersWithDefault[selector].add(provider)
            }
        }

        return mapProviders
    }

    Indexer get(Container.Entry entry) {
        Indexer indexer = get(entry.container.type, entry)
        return indexer ?: get('*', entry)
    }

    Indexer get(String containerType, Container.Entry entry) {
        String path = entry.path
        String type = entry.isDirectory() ? 'dir' : 'file'
        String prefix = containerType + ':' + type
        Indexer indexer = mapProviders.get(prefix + ':' + path)?.match(path)

        if (!indexer) {
            int lastSlashIndex = path.lastIndexOf('/')
            String name = path.substring(lastSlashIndex+1)

            indexer = mapProviders.get(prefix + ':*/' + name)?.match(path)

            if (!indexer) {
                int index = name.lastIndexOf('.')
                if (index != -1) {
                    String extension = name.substring(index + 1)
                    indexer = mapProviders.get(prefix + ':*.' + extension)?.match(path)
                }
                if (!indexer) {
                    indexer = mapProviders.get(prefix + ':*')?.match(path)
                }
            }
        }

        return indexer
    }

    static class Indexers {
        HashMap<String, Indexer> indexers = [:]
        Indexer defaultIndexer

        void add(Indexer indexer) {
            if (indexer.pathPattern) {
                indexers.put(indexer.pathPattern.pattern(), indexer)
            } else {
                defaultIndexer = indexer
            }
        }

        Indexer match(String path) {
            for (def indexer : indexers.values()) {
                if (path ==~ indexer.pathPattern) {
                    return indexer
                }
            }
            return defaultIndexer
        }
    }
}
