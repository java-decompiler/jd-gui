/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.service.sourcesaver

import groovy.transform.CompileStatic
import org.jd.gui.api.model.Container
import org.jd.gui.spi.SourceSaver

@CompileStatic
@Singleton(lazy = true)
class SourceSaverService {
	protected List<SourceSaver> providers = ServiceLoader.load(SourceSaver).toList()

    protected Map<String, SourceSavers> mapProviders = populate(providers)

    protected Map<String, SourceSavers> populate(List<SourceSaver> providers) {
        Map<String, SourceSavers> mapProviders = [:]

        def mapProvidersWithDefault = mapProviders.withDefault { new SourceSavers() }

        for (def provider : providers) {
            for (String selector : provider.selectors) {
                mapProvidersWithDefault[selector].add(provider)
            }
        }

        return mapProviders
    }

    SourceSaver get(Container.Entry entry) {
        SourceSaver saver = get(entry.container.type, entry)
        return saver ?: get('*', entry)
    }

    SourceSaver get(String containerType, Container.Entry entry) {
        String path = entry.path
        String type = entry.isDirectory() ? 'dir' : 'file'
        String prefix = containerType + ':' + type
        SourceSaver saver = mapProviders.get(prefix + ':' + path)?.match(path)

        if (!saver) {
            int lastSlashIndex = path.lastIndexOf('/')
            String name = path.substring(lastSlashIndex+1)

            saver = mapProviders.get(prefix + ':*/' + name)?.match(path)

            if (!saver) {
                int index = name.lastIndexOf('.')
                if (index != -1) {
                    String extension = name.substring(index + 1)
                    saver = mapProviders.get(prefix + ':*.' + extension)?.match(path)
                }
                if (!saver) {
                    saver = mapProviders.get(prefix + ':*')?.match(path)
                }
            }
        }

        return saver
    }

    static class SourceSavers {
        ArrayList<SourceSaver> savers = []
        SourceSaver defaultSaver

        void add(SourceSaver saver) {
            if (saver.pathPattern) {
                savers << saver
            } else {
                defaultSaver = saver
            }
        }

        SourceSaver match(String path) {
            for (def saver : savers) {
                if (path ==~ saver.pathPattern) {
                    return saver
                }
            }
            return defaultSaver
        }
    }
}
