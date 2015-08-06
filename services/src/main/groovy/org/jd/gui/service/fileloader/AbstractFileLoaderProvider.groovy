/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.service.fileloader

import org.jd.gui.api.API
import org.jd.gui.api.feature.UriGettable
import org.jd.gui.api.model.Container
import org.jd.gui.api.model.TreeNodeData
import org.jd.gui.spi.FileLoader

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
        def path = uri.path

        if (path.endsWith('/'))
            path = path.substring(0, path.length()-1)

        def parentEntry = new Container.Entry() {
            Collection<Container.Entry> children = Collections.emptyList()

            Container getContainer() { parentContainer }
            Container.Entry getParent() { null }
            URI getUri() { uri }
            String getPath() { path }
            boolean isDirectory() { file.isDirectory() }
            long length() { file.length() }
            InputStream getInputStream() { file.newInputStream() }
            Collection<Container.Entry> getChildren() { children }
        }
        def container = api.getContainerFactory(rootPath)?.make(api, parentEntry, rootPath)

        if (container) {
            parentEntry.children = container.root.children

            def mainPanel = api.getMainPanelFactory(container)?.make(api, container)
            if (mainPanel) {
                def data = api.getTreeNodeFactory(parentEntry)?.make(api, parentEntry).userObject
                def icon = (data instanceof TreeNodeData) ? data.icon : null

                api.addPanel(file.name, icon, 'Location: ' + file.absolutePath, mainPanel)
                return mainPanel
            }
        }

        return null
    }
}
