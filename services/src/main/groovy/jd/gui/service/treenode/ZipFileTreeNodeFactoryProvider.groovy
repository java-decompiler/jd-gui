/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.treenode

import jd.gui.api.API
import jd.gui.api.feature.UriGettable
import jd.gui.api.model.Container
import jd.gui.spi.TreeNodeFactory
import jd.gui.view.data.TreeNodeBean

import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import java.util.regex.Pattern

class ZipFileTreeNodeFactoryProvider extends DirectoryTreeNodeFactoryProvider {
	static final ImageIcon ICON = new ImageIcon(ZipFileTreeNodeFactoryProvider.class.classLoader.getResource('images/zip_obj.png'))

    String[] getSelectors() { ['*:file:*.zip'] }
    Pattern getPathPattern() { null }

    public <T extends DefaultMutableTreeNode & UriGettable> T make(API api, Container.Entry entry) {
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
