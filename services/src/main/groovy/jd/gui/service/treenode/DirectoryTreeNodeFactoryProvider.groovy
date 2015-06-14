/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.treenode

import jd.gui.api.API
import jd.gui.api.feature.TreeNodeExpandable
import jd.gui.api.feature.UriGettable
import jd.gui.api.model.Container
import jd.gui.api.model.Container.Entry
import jd.gui.spi.TreeNodeFactory
import jd.gui.view.data.TreeNodeBean

import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode

class DirectoryTreeNodeFactoryProvider extends AbstractTreeNodeFactoryProvider {
	static final ImageIcon ICON = new ImageIcon(DirectoryTreeNodeFactoryProvider.class.classLoader.getResource('images/folder.gif'))
	static final ImageIcon OPEN_ICON = new ImageIcon(DirectoryTreeNodeFactoryProvider.class.classLoader.getResource('images/folder_open.png'))

    /**
     * @return local + optional external selectors
     */
    String[] getSelectors() { ['*:dir:*'] + externalSelectors }

    public <T extends DefaultMutableTreeNode & UriGettable> T make(API api, Container.Entry entry) {
        int lastSlashIndex = entry.path.lastIndexOf('/')
        Collection<Container.Entry> entries = entry.children

        // Aggregate directory names
        while (entries.size() == 1) {
            Entry child = entries[0]
            if ((child.isDirectory() == false) || (api.getTreeNodeFactory(child) != this) || (entry.container != child.container)) break
            entry = child
            entries = entry.children
        }

        def label = entry.path.substring(lastSlashIndex+1)
        def node = new TreeNode(entry, new TreeNodeBean(label:label, icon:getIcon(), openIcon:getOpenIcon()))

        if (entries.size() > 0) {
            // Add dummy node
            node.add(new DefaultMutableTreeNode())
        }

		return node
	}

    ImageIcon getIcon() { ICON }
    ImageIcon getOpenIcon() { OPEN_ICON }

    static class TreeNode extends DefaultMutableTreeNode implements UriGettable, TreeNodeExpandable {
        Container.Entry entry
        boolean initialized

        TreeNode(Container.Entry entry, Object userObject) {
            super(userObject)
            this.entry = entry
            this.initialized = false
        }

        // --- UriGettable --- //
        URI getUri() { entry.uri }

        // --- TreeNodeExpandable --- //
        void populateTreeNode(API api) {
            if (!initialized) {
                removeAllChildren()

                Collection<Container.Entry> entries = getChildren()

                while (entries.size() == 1) {
                    Entry child = entries[0]
                    if ((child.isDirectory() == false) || (api.getTreeNodeFactory(child) != this)) break
                    entries = child.children
                }

                for (Entry entry : entries) {
                    TreeNodeFactory factory = api.getTreeNodeFactory(entry)
                    if (factory) {
                        add(factory.make(api, entry))
                    }
                }

                initialized = true
            }
        }

        Collection<Container.Entry> getChildren() { entry.children }
    }
}
