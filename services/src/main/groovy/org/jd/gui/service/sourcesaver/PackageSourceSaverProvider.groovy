/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.service.sourcesaver

import org.jd.gui.api.API
import org.jd.gui.api.model.Container
import org.jd.gui.spi.SourceSaver
import org.jd.gui.util.JarContainerEntryUtil

import java.nio.file.Path

class PackageSourceSaverProvider extends DirectorySourceSaverProvider {
    /**
     * @return local + optional external selectors
     */
    String[] getSelectors() { ['jar:dir:*', 'war:dir:*', 'ear:dir:*'] + externalSelectors }

    void save(API api, SourceSaver.Controller controller, SourceSaver.Listener listener, Path path, Container.Entry entry) {
        save(api, controller, listener, path, JarContainerEntryUtil.removeInnerTypeEntries(entry.children))
    }
}
