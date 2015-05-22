/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.treenode

import jd.gui.api.API
import jd.gui.api.feature.UriGettable
import jd.gui.api.model.Container
import jd.gui.view.data.TreeNodeBean

import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode

class EarFileTreeNodeFactoryProvider extends ZipFileTreeNodeFactoryProvider {
    static final ImageIcon icon = new ImageIcon(JarFileTreeNodeFactoryProvider.class.classLoader.getResource('images/ear_obj.gif'))

    String[] getSelectors() { ['*:file:*.ear'] }

    public <T extends DefaultMutableTreeNode & UriGettable> T make(API api, Container.Entry entry) {
        int lastSlashIndex = entry.path.lastIndexOf('/')
        def name = entry.path.substring(lastSlashIndex+1)
        def node = new TreeNode(entry, 'ear', new TreeNodeBean(label:name, icon:icon))
        // Add dummy node
        node.add(new DefaultMutableTreeNode())
        return node
    }
}
