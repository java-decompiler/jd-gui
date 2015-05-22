/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.sourcesaver

import groovy.transform.CompileStatic
import jd.gui.api.API
import jd.gui.api.model.Container
import jd.gui.spi.SourceSaver

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.regex.Pattern

class FileSourceSaverProvider implements SourceSaver {
    String[] getSelectors() { ['*:file:*'] }

    Pattern getPathPattern() { null }

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
