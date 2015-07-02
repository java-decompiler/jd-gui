/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.service.treenode

import org.jd.gui.api.API
import org.jd.gui.api.feature.ContainerEntryGettable
import org.jd.gui.api.feature.UriGettable
import org.jd.gui.api.model.Container
import org.jd.gui.util.JarContainerEntryUtil
import org.jd.gui.view.data.TreeNodeBean

import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode

class JarFileTreeNodeFactoryProvider extends ZipFileTreeNodeFactoryProvider {
    static final ImageIcon JAR_FILE_ICON = new ImageIcon(JarFileTreeNodeFactoryProvider.class.classLoader.getResource('org/jd/gui/images/jar_obj.png'))
    static final ImageIcon EJB_FILE_ICON = new ImageIcon(JarFileTreeNodeFactoryProvider.class.classLoader.getResource('org/jd/gui/images/ejbmodule_obj.gif'))

    /**
     * @return local + optional external selectors
     */
    String[] getSelectors() { ['*:file:*.jar'] + externalSelectors }

    public <T extends DefaultMutableTreeNode & ContainerEntryGettable & UriGettable> T make(API api, Container.Entry entry) {
        int lastSlashIndex = entry.path.lastIndexOf('/')
        def name = entry.path.substring(lastSlashIndex+1)
        def icon = isAEjbModule(entry) ? EJB_FILE_ICON : JAR_FILE_ICON
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
