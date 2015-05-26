/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.sourcesaver

import groovy.transform.CompileStatic
import jd.gui.api.API
import jd.gui.api.model.Container
import jd.gui.spi.SourceSaver

import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class ZipFileSourceSaverProvider extends DirectorySourceSaverProvider {

    /**
     * @return local + optional external selectors
     */
    String[] getSelectors() { ['*:file:*.zip', '*:file:*.jar', '*:file:*.war', '*:file:*.ear'] + externalSelectors }

    String getSourcePath(Container.Entry entry) { entry.path + '.src.zip' }

    @CompileStatic
    void save(API api, SourceSaver.Controller controller, SourceSaver.Listener listener, Path path, Container.Entry entry) {
        def tmpFile = File.createTempFile('jd-gui.', '.tmp.zip')
        tmpFile.delete()

        def env = new HashMap<String, String>()
        env.put('create', 'true')

        def tmpURI = URI.create('jar:' + tmpFile.toURI() + '!/')
        def tmpFs = FileSystems.newFileSystem(tmpURI, env);

        super.save(api, controller, listener, tmpFs.getPath('/'), entry)
        tmpFs.close()

        def tmpPath = Paths.get(tmpFile.absolutePath)
        def srcZipParentPath = path.parent

        if (srcZipParentPath && !Files.exists(srcZipParentPath)) {
            Files.createDirectories(srcZipParentPath)
        }

        Files.move(tmpPath, path)
    }
}
