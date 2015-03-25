/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.indexer

import groovy.transform.CompileStatic
import jd.gui.api.model.Container
import jd.gui.spi.Indexer

@CompileStatic
@Singleton(lazy = true)
class IndexerService {
	protected List<Indexer> providers = ServiceLoader.load(Indexer).toList()

    protected Map<String, Indexers> mapProviders = populate(providers)

    protected Map<String, Indexers> populate(List<Indexer> providers) {
        Map<String, Indexers> mapProviders = [:]

        def mapProvidersWithDefault = mapProviders.withDefault { new Indexers() }

        for (def provider : providers) {
            for (String type : provider.types) {
                mapProvidersWithDefault[type].add(provider)
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
        ArrayList<Indexer> indexers = []
        Indexer defaultIndexer

        void add(Indexer indexer) {
            if (indexer.pathPattern) {
                indexers << indexer
            } else {
                defaultIndexer = indexer
            }
        }

        Indexer match(String path) {
            for (def indexer : indexers) {
                if (path ==~ indexer.pathPattern) {
                    return indexer
                }
            }
            return defaultIndexer
        }
    }
}
