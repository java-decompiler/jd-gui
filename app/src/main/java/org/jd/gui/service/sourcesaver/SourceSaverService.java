/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.sourcesaver;

import org.jd.gui.api.model.Container;
import org.jd.gui.service.extension.ExtensionService;
import org.jd.gui.spi.SourceSaver;

import java.util.Collection;
import java.util.HashMap;

public class SourceSaverService {
    protected static final SourceSaverService SOURCE_SAVER_SERVICE = new SourceSaverService();

    public static SourceSaverService getInstance() { return SOURCE_SAVER_SERVICE; }

    protected HashMap<String, SourceSavers> mapProviders = new HashMap<>();

    protected SourceSaverService() {
        Collection<SourceSaver> providers = ExtensionService.getInstance().load(SourceSaver.class);

        for (SourceSaver provider : providers) {
            for (String selector : provider.getSelectors()) {
                SourceSavers savers = mapProviders.get(selector);

                if (savers == null) {
                    mapProviders.put(selector, savers=new SourceSavers());
                }

                savers.add(provider);
            }
        }
    }

    public SourceSaver get(Container.Entry entry) {
        SourceSaver saver = get(entry.getContainer().getType(), entry);
        return (saver != null) ? saver : get("*", entry);
    }

    protected SourceSaver get(String containerType, Container.Entry entry) {
        String path = entry.getPath();
        String type = entry.isDirectory() ? "dir" : "file";
        String prefix = containerType + ':' + type;
        SourceSaver saver = null;
        SourceSavers savers = mapProviders.get(prefix + ':' + path);

        if (savers != null) {
            saver = savers.match(path);
        }

        if (saver == null) {
            int lastSlashIndex = path.lastIndexOf('/');
            String name = path.substring(lastSlashIndex+1);

            savers = mapProviders.get(prefix + ":*/" + path);
            if (savers != null) {
                saver = savers.match(path);
            }

            if (saver == null) {
                int index = name.lastIndexOf('.');

                if (index != -1) {
                    String extension = name.substring(index + 1);

                    savers = mapProviders.get(prefix + ":*." + extension);
                    if (savers != null) {
                        saver = savers.match(path);
                    }
                }

                if (saver == null) {
                    savers = mapProviders.get(prefix + ":*");
                    if (savers != null) {
                        saver = savers.match(path);
                    }
                }
            }
        }

        return saver;
    }

    protected static class SourceSavers {
        protected HashMap<String, SourceSaver> savers = new HashMap<>();
        protected SourceSaver defaultSaver;

        void add(SourceSaver saver) {
            if (saver.getPathPattern() != null) {
                savers.put(saver.getPathPattern().pattern(), saver);
            } else {
                defaultSaver = saver;
            }
        }

        SourceSaver match(String path) {
            for (SourceSaver saver : savers.values()) {
                if (saver.getPathPattern().matcher(path).matches()) {
                    return saver;
                }
            }
            return defaultSaver;
        }
    }
}
