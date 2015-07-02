/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.service.sourcesaver

import groovy.transform.CompileStatic
import org.jd.gui.api.API
import org.jd.gui.api.model.Container
import org.jd.gui.spi.SourceSaver

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

class FileSourceSaverProvider extends AbstractSourceSaverProvider {

    /**
     * @return local + optional external selectors
     */
    String[] getSelectors() { ['*:file:*'] + externalSelectors }

    String getSourcePath(Container.Entry entry) { entry.path }

    int getFileCount(API api, Container.Entry entry) { 1 }

    @CompileStatic
    void save(API api, SourceSaver.Controller controller, SourceSaver.Listener listener, Path path, Container.Entry entry) {
        listener.pathSaved(path)

        entry.inputStream.withStream { InputStream is ->
            Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING)
        }
    }
}
