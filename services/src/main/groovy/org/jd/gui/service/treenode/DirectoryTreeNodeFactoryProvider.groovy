/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.treenode

import org.jd.gui.api.API
import org.jd.gui.api.feature.ContainerEntryGettable
import org.jd.gui.api.feature.TreeNodeExpandable
import org.jd.gui.api.feature.UriGettable
import org.jd.gui.api.model.Container
import org.jd.gui.api.model.Container.Entry
import org.jd.gui.spi.TreeNodeFactory
import org.jd.gui.view.data.TreeNodeBean

import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode

class DirectoryTreeNodeFactoryProvider extends AbstractTreeNodeFactoryProvider {
	static final ImageIcon ICON = new ImageIcon(DirectoryTreeNodeFactoryProvider.class.classLoader.getResource('org/jd/gui/images/folder.gif'))
	static final ImageIcon OPEN_ICON = new ImageIcon(DirectoryTreeNodeFactoryProvider.class.classLoader.getResource('org/jd/gui/images/folder_open.png'))

    /**
     * @return local + optional external selectors
     */
    String[] getSelectors() { ['*:dir:*'] + externalSelectors }

    public <T extends DefaultMutableTreeNode & ContainerEntryGettable & UriGettable> T make(API api, Container.Entry entry) {
        int lastSlashIndex = entry.path.lastIndexOf('/')
        Collection<Container.Entry> entries = entry.children

        // Aggregate directory names
        while (entries.size() == 1) {
            Entry child = entries[0]
            if (!child.isDirectory() || api.getTreeNodeFactory(child) != this || entry.container != child.container) break
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

    static class TreeNode extends DefaultMutableTreeNode implements ContainerEntryGettable, UriGettable, TreeNodeExpandable {
        Container.Entry entry
        boolean initialized

        TreeNode(Container.Entry entry, Object userObject) {
            super(userObject)
            this.entry = entry
            this.initialized = false
        }

        // --- ContainerEntryGettable --- //
        Container.Entry getEntry() { entry }

        // --- UriGettable --- //
        URI getUri() { entry.uri }

        // --- TreeNodeExpandable --- //
        void populateTreeNode(API api) {
            if (!initialized) {
                removeAllChildren()

                Collection<Container.Entry> entries = getChildren()

                while (entries.size() == 1) {
                    Entry child = entries[0]
                    if (!child.isDirectory() || api.getTreeNodeFactory(child) != this) {
                        break
                    }
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
