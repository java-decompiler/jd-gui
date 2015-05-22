/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.treenode

import jd.gui.api.API
import jd.gui.api.feature.UriGettable
import jd.gui.api.model.Container
import jd.gui.spi.TreeNodeFactory
import jd.gui.util.JarContainerEntryUtil
import jd.gui.view.data.TreeNodeBean

import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import java.util.regex.Pattern

class PackageTreeNodeFactoryProvider extends DirectoryTreeNodeFactoryProvider {
    static final ImageIcon icon = new ImageIcon(PackageTreeNodeFactoryProvider.class.classLoader.getResource('images/package_obj.png'))

    String[] getSelectors() { ['jar:dir:*'] }

    Pattern getPathPattern() { null }

    public <T extends DefaultMutableTreeNode & UriGettable> T make(API api, Container.Entry entry) {
        int lastSlashIndex = entry.path.lastIndexOf('/')
        Container.Entry[] entries = entry.children

        // Aggregate directory names
        while (entries.length == 1) {
            Container.Entry child = entries[0]
            if ((child.isDirectory() == false) || (api.getTreeNodeFactory(child) != this)) break
            entry = child
            entries = entry.children
        }

        def label = entry.path.substring(lastSlashIndex+1).replace('/', '.')
        def node = new TreeNode(entry, new TreeNodeBean(label:label, icon:getIcon(), openIcon:getOpenIcon()))

        if (entries.length > 0) {
            // Add dummy node
            node.add(new DefaultMutableTreeNode())
        }

        return node
    }

    ImageIcon getIcon() { icon }
    ImageIcon getOpenIcon() { null }

    static class TreeNode extends DirectoryTreeNodeFactoryProvider.TreeNode {
        TreeNode(Container.Entry entry, Object userObject) {
            super(entry, userObject)
        }

        Collection<Container.Entry> getChildren() {
            return JarContainerEntryUtil.removeInnerTypeEntries(entry.children)
        }
    }
}
