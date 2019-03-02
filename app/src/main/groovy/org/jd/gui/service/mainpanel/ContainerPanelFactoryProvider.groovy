/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.service.mainpanel

import groovy.transform.CompileStatic
import org.jd.gui.api.API
import org.jd.gui.api.feature.ContentIndexable
import org.jd.gui.api.feature.SourcesSavable
import org.jd.gui.api.feature.SourcesSavable.Controller
import org.jd.gui.api.feature.SourcesSavable.Listener
import org.jd.gui.api.feature.UriGettable
import org.jd.gui.api.model.Container
import org.jd.gui.api.model.Indexes
import org.jd.gui.spi.PanelFactory
import org.jd.gui.spi.SourceSaver
import org.jd.gui.spi.TreeNodeFactory
import org.jd.gui.view.component.panel.TreeTabbedPanel

import javax.swing.JComponent
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path

class ContainerPanelFactoryProvider implements PanelFactory {

	@Override String[] getTypes() { ['default'] }

    @Override
    public <T extends JComponent & UriGettable> T make(API api, Container container) {
        return new ContainerPanel(api, container)
	}

    class ContainerPanel extends TreeTabbedPanel implements ContentIndexable, SourcesSavable {
        Container.Entry entry

        ContainerPanel(API api, Container container) {
            super(api, container.root.uri)

            this.entry = container.root.parent

            def root = new DefaultMutableTreeNode()

            for (def entry : container.root.children) {
                TreeNodeFactory factory = api.getTreeNodeFactory(entry)
                if (factory) {
                    root.add(factory.make(api, entry))
                }
            }

            tree.model = new DefaultTreeModel(root)
        }

        // --- ContentIndexable --- //
        @Override
        @CompileStatic
        Indexes index(API api) {
            // Classic map
            def map = new HashMap<String, Map<String, ArrayList>>()
            // Map populating value automatically
            def mapWithDefault = new HashMap<String, Map<String, ArrayList>>().withDefault { key ->
                def subMap = new HashMap<String, ArrayList>()

                map.put(key, subMap)

                return subMap.withDefault { subKey ->
                    def array = new ArrayList()
                    subMap.put(subKey, array)
                    return array
                }
            }
            // Index populating value automatically
            def indexesWithDefault = new Indexes() {
                @Override void waitIndexers() {}
                @Override Map<String, Collection> getIndex(String name) { mapWithDefault.get(name) }
            }

            api.getIndexer(entry)?.index(api, entry, indexesWithDefault)

            // To prevent memory leaks, return an index without the 'populate' behaviour
            return new Indexes() {
                @Override void waitIndexers() {}
                @Override Map<String, Collection> getIndex(String name) { map.get(name) }
            }
        }

        // --- SourcesSavable --- //
        @Override
        String getSourceFileName() {
            def path = api.getSourceSaver(entry)?.getSourcePath(entry)
            int index = path.lastIndexOf('/')
            return path.substring(index+1)
        }

        @Override int getFileCount() { api.getSourceSaver(entry)?.getFileCount(api, entry) }

        @Override
        void save(API api, Controller controller, Listener listener, Path path) {
            def parentPath = path.parent

            if (parentPath && !Files.exists(parentPath)) {
                Files.createDirectories(parentPath)
            }

            def uri = path.toUri()
            def archiveUri = new URI('jar:' + uri.scheme, uri.host, uri.path + '!/', null)
            def archiveFs = FileSystems.newFileSystem(archiveUri, [create: 'true'])
            def archiveRootPath = archiveFs.getPath('/')

            api.getSourceSaver(entry)?.saveContent(
                    api,
                    new SourceSaver.Controller() {
                        boolean isCancelled() { controller.isCancelled() }
                    },
                    new SourceSaver.Listener() {
                        void pathSaved(Path p) { listener.pathSaved(p) }
                    },
                archiveRootPath, archiveRootPath, entry
            )

            archiveFs.close()
        }
    }
}
