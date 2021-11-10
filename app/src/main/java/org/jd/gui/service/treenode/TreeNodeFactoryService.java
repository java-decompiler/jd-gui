/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.treenode;

import org.jd.gui.api.model.Container;
import org.jd.gui.service.extension.ExtensionService;
import org.jd.gui.spi.TreeNodeFactory;

import java.util.Collection;
import java.util.HashMap;

public class TreeNodeFactoryService {
    protected static final TreeNodeFactoryService TREE_NODE_FACTORY_SERVICE = new TreeNodeFactoryService();

    public static TreeNodeFactoryService getInstance() { return TREE_NODE_FACTORY_SERVICE; }

    protected HashMap<String, TreeNodeFactories> mapProviders = new HashMap<>();

    protected TreeNodeFactoryService() {
        Collection<TreeNodeFactory> providers = ExtensionService.getInstance().load(TreeNodeFactory.class);

        for (TreeNodeFactory provider : providers) {
            for (String selector : provider.getSelectors()) {
                TreeNodeFactories factories = mapProviders.get(selector);

                if (factories == null) {
                    mapProviders.put(selector, factories=new TreeNodeFactories());
                }

                factories.add(provider);
            }
        }
    }

    public TreeNodeFactory get(Container.Entry entry) {
        TreeNodeFactory factory = get(entry.getContainer().getType(), entry);
        return (factory != null) ? factory : get("*", entry);
    }

    protected TreeNodeFactory get(String containerType, Container.Entry entry) {
        String path = entry.getPath();
        String type = entry.isDirectory() ? "dir" : "file";
        String prefix = containerType + ':' + type + ':';
        TreeNodeFactory factory = null;
        TreeNodeFactories factories = mapProviders.get(prefix + path);

        if (factories != null) {
            factory = factories.match(path);
        }

        if (factory == null) {
            int lastSlashIndex = path.lastIndexOf('/');
            String name = path.substring(lastSlashIndex+1);

            factories = mapProviders.get(prefix + "*/" + name);
            if (factories != null) {
                factory = factories.match(path);
            }

            if (factory == null) {
                int index = name.lastIndexOf('.');

                if (index != -1) {
                    String extension = name.substring(index + 1);

                    factories = mapProviders.get(prefix + "*." + extension);
                    if (factories != null) {
                        factory = factories.match(path);
                    }
                }

                if (factory == null) {
                    factories = mapProviders.get(prefix + "*");
                    if (factories != null) {
                        factory = factories.match(path);
                    }
                }
            }
        }

        return factory;
    }

    protected static class TreeNodeFactories {
        protected HashMap<String, TreeNodeFactory> factories = new HashMap<>();
        protected TreeNodeFactory defaultFactory;

        public void add(TreeNodeFactory factory) {
            if (factory.getPathPattern() != null) {
                factories.put(factory.getPathPattern().pattern(), factory);
            } else {
                defaultFactory = factory;
            }
        }

        public TreeNodeFactory match(String path) {
            for (TreeNodeFactory factory : factories.values()) {
                if (factory.getPathPattern().matcher(path).matches()) {
                    return factory;
                }
            }
            return defaultFactory;
        }
    }
}
