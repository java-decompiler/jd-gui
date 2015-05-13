/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.sourcesaver

import jd.gui.api.API
import jd.gui.api.model.Container
import jd.gui.spi.SourceSaver
import jd.gui.util.JarContainerEntryUtil

import java.nio.file.Path

class PackageSourceSaverProvider extends DirectorySourceSaverProvider {
    String[] getTypes() { ['jar:dir:*', 'war:dir:*'] }

    void save(API api, SourceSaver.Controller controller, SourceSaver.Listener listener, Path path, Container.Entry entry) {
        save(api, controller, listener, path, JarContainerEntryUtil.removeInnerTypeEntries(entry.children))
    }
}
