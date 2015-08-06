/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.service.treenode

import groovy.transform.CompileStatic
import org.jd.gui.api.model.Container
import org.jd.gui.service.extension.ExtensionService
import org.jd.gui.spi.TreeNodeFactory

@CompileStatic
@Singleton(lazy = true)
class TreeNodeFactoryService {
	protected Map<String, TreeNodeFactories> mapProviders = populate()

    protected Map<String, TreeNodeFactories> populate() {
        Collection<TreeNodeFactory> providers = ExtensionService.instance.load(TreeNodeFactory)
        Map<String, TreeNodeFactories> mapProviders = [:]

        def mapProvidersWithDefault = mapProviders.withDefault { new TreeNodeFactories() }

        for (def provider : providers) {
            for (String selector : provider.selectors) {
                mapProvidersWithDefault[selector].add(provider)
            }
        }

        return mapProviders
    }

    @CompileStatic
    TreeNodeFactory get(Container.Entry entry) {
        TreeNodeFactory factory = get(entry.container.type, entry)
        return factory ?: get('*', entry)
    }

    @CompileStatic
    TreeNodeFactory get(String containerType, Container.Entry entry) {
        String path = entry.path
        String type = entry.isDirectory() ? 'dir' : 'file'
        String prefix = containerType + ':' + type + ':'
        TreeNodeFactory factory = mapProviders.get(prefix + path)?.match(path)

        if (!factory) {
            int lastSlashIndex = path.lastIndexOf('/')
            String name = path.substring(lastSlashIndex+1)

            factory = mapProviders.get(prefix + '*/' + name)?.match(path)
            if (!factory) {
                int index = name.lastIndexOf('.')
                if (index != -1) {
                    String extension = name.substring(index + 1)
                    factory = mapProviders.get(prefix + '*.' + extension)?.match(path)
                }
                if (!factory) {
                    factory = mapProviders.get(prefix + '*')?.match(path)
                }
            }
        }

        return factory
    }

    static class TreeNodeFactories {
        HashMap<String, TreeNodeFactory> factories = [:]
        TreeNodeFactory defaultFactory

        void add(TreeNodeFactory factory) {
            if (factory.pathPattern) {
                factories.put(factory.pathPattern.pattern(), factory)
            } else {
                defaultFactory = factory
            }
        }

        TreeNodeFactory match(String path) {
            for (def factory : factories.values()) {
                if (path ==~ factory.pathPattern) {
                    return factory
                }
            }
            return defaultFactory
        }
    }
}
