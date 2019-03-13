/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.treenode

import org.jd.gui.api.API
import org.jd.gui.api.feature.ContainerEntryGettable
import org.jd.gui.api.feature.UriGettable
import org.jd.gui.api.model.Container
import org.jd.gui.spi.TreeNodeFactory
import org.jd.gui.view.data.TreeNodeBean

import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode

class ZipFileTreeNodeFactoryProvider extends DirectoryTreeNodeFactoryProvider {
	static final ImageIcon ICON = new ImageIcon(ZipFileTreeNodeFactoryProvider.class.classLoader.getResource('org/jd/gui/images/zip_obj.png'))

    /**
     * @return local + optional external selectors
     */
    String[] getSelectors() { ['*:file:*.zip'] + externalSelectors }

    public <T extends DefaultMutableTreeNode & ContainerEntryGettable & UriGettable> T make(API api, Container.Entry entry) {
        int lastSlashIndex = entry.path.lastIndexOf('/')
        def name = entry.path.substring(lastSlashIndex+1)
        def node = new TreeNode(entry, 'generic', new TreeNodeBean(label:name, icon:ICON))
        // Add dummy node
        node.add(new DefaultMutableTreeNode())
        return node
	}

    static class TreeNode extends DirectoryTreeNodeFactoryProvider.TreeNode {
        String ct

        TreeNode(Container.Entry entry, String containerType, Object userObject) {
            super(entry, userObject)
            ct = containerType
        }

        // --- TreeNodeExpandable --- //
        void populateTreeNode(API api) {
            if (!initialized) {
                removeAllChildren()

                for (def e : getChildren()) {
                    TreeNodeFactory factory = api.getTreeNodeFactory(e)
                    if (factory) {
                        add(factory.make(api, e))
                    }
                }

                initialized = true
            }
        }
    }
}
