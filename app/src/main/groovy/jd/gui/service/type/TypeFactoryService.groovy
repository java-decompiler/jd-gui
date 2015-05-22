/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.type

import groovy.transform.CompileStatic
import jd.gui.api.model.Container
import jd.gui.api.model.Type
import jd.gui.spi.TypeFactory

@Singleton(lazy = true)
class TypeFactoryService {
	protected List<TypeFactory> providers = ServiceLoader.load(TypeFactory).toList()

    protected Map<String, TypeFactories> mapProviders = populate(providers)

    protected Map<String, TypeFactories> populate(List<TypeFactory> providers) {
        Map<String, TypeFactories> mapProviders = [:]

        def mapProvidersWithDefault = mapProviders.withDefault { new TypeFactories() }

        for (def provider : providers) {
            for (String selector : provider.selectors) {
                mapProvidersWithDefault[selector].add(provider)
            }
        }

        // Early interface loading
        Type.class
        Type.Field.class
        Type.Method.class

        return mapProviders
    }

    @CompileStatic
    TypeFactory get(Container.Entry entry) {
        TypeFactory factory = get(entry.container.type, entry)
        return factory ?: get('*', entry)
    }

    @CompileStatic
    TypeFactory get(String containerType, Container.Entry entry) {
        String path = entry.path
        String type = entry.isDirectory() ? 'dir' : 'file'
        String prefix = containerType + ':' + type + ':'
        TypeFactory factory = mapProviders.get(prefix + path)?.match(path)

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

    static class TypeFactories {
        ArrayList<TypeFactory> factories = []
        TypeFactory defaultFactory

        void add(TypeFactory factory) {
            if (factory.pathPattern) {
                factories << factory
            } else {
                defaultFactory = factory
            }
        }

        TypeFactory match(String path) {
            for (def factory : factories) {
                if (path ==~ factory.pathPattern) {
                    return factory
                }
            }
            return defaultFactory
        }
    }
}
