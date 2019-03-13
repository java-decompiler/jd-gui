/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.type

import groovy.transform.CompileStatic
import org.jd.gui.api.model.Container
import org.jd.gui.api.model.Type
import org.jd.gui.service.extension.ExtensionService
import org.jd.gui.spi.TypeFactory

@Singleton(lazy = true)
class TypeFactoryService {
	protected Map<String, TypeFactories> mapProviders = populate()

    protected Map<String, TypeFactories> populate() {
        Collection<TypeFactory> providers = ExtensionService.instance.load(TypeFactory)
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
        HashMap<String, TypeFactory> factories = [:]
        TypeFactory defaultFactory

        void add(TypeFactory factory) {
            if (factory.pathPattern) {
                factories.put(factory.pathPattern.pattern(), factory)
            } else {
                defaultFactory = factory
            }
        }

        TypeFactory match(String path) {
            for (def factory : factories.values()) {
                if (path ==~ factory.pathPattern) {
                    return factory
                }
            }
            return defaultFactory
        }
    }
}
