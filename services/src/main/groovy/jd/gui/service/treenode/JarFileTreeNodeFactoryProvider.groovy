/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.treenode

import jd.gui.api.API
import jd.gui.api.feature.UriGettable
import jd.gui.api.model.Container
import jd.gui.util.JarContainerEntryUtil
import jd.gui.view.data.TreeNodeBean

import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode

class JarFileTreeNodeFactoryProvider extends ZipFileTreeNodeFactoryProvider {
	static final ImageIcon icon = new ImageIcon(JarFileTreeNodeFactoryProvider.class.classLoader.getResource('images/jar_obj.png'))

    String[] getTypes() { ['*:file:*.jar'] }

    public <T extends DefaultMutableTreeNode & UriGettable> T make(API api, Container.Entry entry) {
        int lastSlashIndex = entry.path.lastIndexOf('/')
        def name = entry.path.substring(lastSlashIndex+1)
        def node = new TreeNode(entry, 'jar', new TreeNodeBean(label:name, icon:icon))
        // Add dummy node
        node.add(new DefaultMutableTreeNode())
        return node
	}

    static class TreeNode extends ZipFileTreeNodeFactoryProvider.TreeNode {
        TreeNode(Container.Entry entry, String containerType, Object userObject) {
            super(entry, containerType, userObject)
        }

        Collection<Container.Entry> getChildren() { JarContainerEntryUtil.removeInnerTypeEntries(entry.children) }
    }
}
