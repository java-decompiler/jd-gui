/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.service.sourcesaver

import groovy.transform.CompileStatic
import org.jd.gui.api.API
import org.jd.gui.api.model.Container
import org.jd.gui.spi.SourceSaver

import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class ZipFileSourceSaverProvider extends DirectorySourceSaverProvider {

    /**
     * @return local + optional external selectors
     */
    @Override String[] getSelectors() { ['*:file:*.zip', '*:file:*.jar', '*:file:*.war', '*:file:*.ear'] + externalSelectors }

    @Override
    @CompileStatic
    public void save(API api, SourceSaver.Controller controller, SourceSaver.Listener listener, Path rootPath, Container.Entry entry) {
        def sourcePath = getSourcePath(entry)
        def path = rootPath.resolve(sourcePath)
        def parentPath = path.parent

        if (parentPath && !Files.exists(parentPath)) {
            Files.createDirectories(parentPath)
        }

        def tmpFile = File.createTempFile('jd-gui.', '.tmp.zip')
        tmpFile.delete()

        def tmpFileUri = tmpFile.toURI()
        def tmpArchiveUri = new URI('jar:' + tmpFileUri.scheme, tmpFileUri.host, tmpFileUri.path + '!/', null)
        def tmpArchiveFs = FileSystems.newFileSystem(tmpArchiveUri, [create: 'true']);
        def tmpArchiveRootPath = tmpArchiveFs.getPath('/')

        saveContent(api, controller, listener, tmpArchiveRootPath, tmpArchiveRootPath, entry)

        tmpArchiveFs.close()

        def tmpPath = Paths.get(tmpFile.absolutePath)

        Files.move(tmpPath, path)
    }
}
