/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.type;

import org.jd.gui.api.model.Container;
import org.jd.gui.service.extension.ExtensionService;
import org.jd.gui.spi.TypeFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TypeFactoryService {
    protected static final TypeFactoryService TYPE_FACTORY_SERVICE = new TypeFactoryService();

    protected Map<String, TypeFactories> mapProviders;

    public static TypeFactoryService getInstance() {
        return TYPE_FACTORY_SERVICE;
    }

    protected TypeFactoryService() {
        Collection<TypeFactory> providers = ExtensionService.getInstance().load(TypeFactory.class);

        mapProviders = new HashMap<>();

        for (TypeFactory provider : providers) {
            for (String selector : provider.getSelectors()) {
                TypeFactories typeFactories = mapProviders.get(selector);

                if (typeFactories == null) {
                    mapProviders.put(selector, typeFactories=new TypeFactories());
                }

                typeFactories.add(provider);
            }
        }
    }

    public TypeFactory get(Container.Entry entry) {
        TypeFactory typeFactory = get(entry.getContainer().getType(), entry);
        return (typeFactory != null) ? typeFactory : get("*", entry);
    }

    public TypeFactory get(String containerType, Container.Entry entry) {
        String path = entry.getPath();
        String type = entry.isDirectory() ? "dir" : "file";
        String prefix = containerType + ':' + type + ':';
        TypeFactories typeFactories = mapProviders.get(prefix + path);
        TypeFactory factory = null;

        if (typeFactories != null) {
            factory = typeFactories.match(path);
        }

        if (factory == null) {
            int lastSlashIndex = path.lastIndexOf('/');
            String name = path.substring(lastSlashIndex+1);

            typeFactories = mapProviders.get(prefix + "*/" + name);

            if (typeFactories != null) {
                factory = typeFactories.match(path);
            }

            if (factory == null) {
                int index = name.lastIndexOf('.');
                if (index != -1) {
                    String extension = name.substring(index + 1);

                    typeFactories = mapProviders.get(prefix + "*." + extension);

                    if (typeFactories != null) {
                        factory = typeFactories.match(path);
                    }
                }
                if (factory == null) {
                    typeFactories = mapProviders.get(prefix + '*');

                    if (typeFactories != null) {
                        factory = typeFactories.match(path);
                    }
                }
            }
        }

        return factory;
    }

    protected static class TypeFactories {
        protected HashMap<String, TypeFactory> factories = new HashMap<>();
        protected TypeFactory defaultFactory;

        public void add(TypeFactory factory) {
            Pattern pathPattern = factory.getPathPattern();

            if (pathPattern != null) {
                factories.put(pathPattern.pattern(), factory);
            } else {
                defaultFactory = factory;
            }
        }

        public TypeFactory match(String path) {
            for (TypeFactory factory : factories.values()) {
                Matcher matcher = factory.getPathPattern().matcher(path);

                if (matcher.matches()) {
                    return factory;
                }
            }
            return defaultFactory;
        }
    }
}
