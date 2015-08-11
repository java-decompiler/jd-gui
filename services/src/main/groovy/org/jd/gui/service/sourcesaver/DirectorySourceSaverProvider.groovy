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

class DirectorySourceSaverProvider extends AbstractSourceSaverProvider {

    /**
     * @return local + optional external selectors
     */
    @Override String[] getSelectors() { ['*:dir:*'] + externalSelectors }

    @Override String getSourcePath(Container.Entry entry) { entry.path + '.src.zip' }

    @Override int getFileCount(API api, Container.Entry entry) { getFileCount(api, entry.children) }

    @CompileStatic
    protected int getFileCount(API api, Collection<Container.Entry> entries) {
        int count = 0

        for (def e : entries) {
            count += api.getSourceSaver(e)?.getFileCount(api, e)
        }

        return count
    }

    @Override
    @CompileStatic
    public void save(API api, SourceSaver.Controller controller, SourceSaver.Listener listener, Path rootPath, Container.Entry entry) {
        Path path = rootPath.resolve(entry.getPath())

        Files.createDirectories(path)

        saveContent(api, controller, listener, rootPath, path, entry);
    }

    @Override
    @CompileStatic
    public void saveContent(API api, SourceSaver.Controller controller, SourceSaver.Listener listener, Path rootPath, Path path, Container.Entry entry) {
        for (def e : getChildren(entry)) {
            if (controller.isCancelled()) {
                break
            }

            api.getSourceSaver(e)?.save(api, controller, listener, rootPath, e)
        }
    }

    protected Collection<Container.Entry> getChildren(Container.Entry entry) { entry.children }
}
