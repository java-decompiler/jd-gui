/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.fileloader

import jd.gui.api.API
import jd.gui.api.feature.UriGettable
import jd.gui.api.model.Container
import jd.gui.spi.FileLoader

import javax.swing.*
import java.nio.file.Path

abstract class AbstractFileLoaderProvider implements FileLoader {
    protected <T extends JComponent & UriGettable> T load(API api, File file, Path rootPath) {
        // Dummy parent container
        def parentContainer = new Container() {
            String getType() { 'generic' }
            Container.Entry getRoot() { null }
        }
        // Dummy parent entry
        def uri = file.toURI()
        def parentEntry = new Container.Entry() {
            Collection<Container.Entry> children = Collections.emptyList()

            Container getContainer() { parentContainer }
            Container.Entry getParent() { null }
            URI getUri() { uri }
            String getPath() { uri.path }
            boolean isDirectory() { file.isDirectory() }
            long length() { 0 }
            InputStream getInputStream() { null }
            Collection<Container.Entry> getChildren() { children }
        }
        def container = api.getContainerFactory(rootPath)?.make(api, parentEntry, rootPath)

        if (container) {
            parentEntry.children = container.root.children

            def mainPanel = api.getMainPanelFactory(container)?.make(api, container)
            if (mainPanel) {
                api.addPanel(file.name, null, 'Location: ' + file.absolutePath, mainPanel)
                return mainPanel
            }
        }

        return null
    }
}
