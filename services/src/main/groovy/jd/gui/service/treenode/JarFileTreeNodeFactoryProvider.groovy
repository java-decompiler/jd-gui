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
    static final ImageIcon jarFileIcon = new ImageIcon(JarFileTreeNodeFactoryProvider.class.classLoader.getResource('images/jar_obj.png'))
    static final ImageIcon ejbFileIcon = new ImageIcon(JarFileTreeNodeFactoryProvider.class.classLoader.getResource('images/ejbmodule_obj.gif'))

    String[] getSelectors() { ['*:file:*.jar'] }

    public <T extends DefaultMutableTreeNode & UriGettable> T make(API api, Container.Entry entry) {
        int lastSlashIndex = entry.path.lastIndexOf('/')
        def name = entry.path.substring(lastSlashIndex+1)
        def icon = isAEjbModule(entry) ? ejbFileIcon : jarFileIcon
        def node = new TreeNode(entry, 'jar', new TreeNodeBean(label:name, icon:icon))
        // Add dummy node
        node.add(new DefaultMutableTreeNode())
        return node
	}

    static boolean isAEjbModule(Container.Entry entry) {
        return entry.children?.find { it.path.equals('META-INF') }?.children?.find { it.path.equals('META-INF/ejb-jar.xml') }
    }

    static class TreeNode extends ZipFileTreeNodeFactoryProvider.TreeNode {
        TreeNode(Container.Entry entry, String containerType, Object userObject) {
            super(entry, containerType, userObject)
        }

        Collection<Container.Entry> getChildren() { JarContainerEntryUtil.removeInnerTypeEntries(entry.children) }
    }
}
