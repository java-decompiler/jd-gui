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

class FileTreeNodeFactoryProvider implements TreeNodeFactory {
	static final ImageIcon ICON = new ImageIcon(FileTreeNodeFactoryProvider.class.classLoader.getResource('images/file_plain_obj.png'))

    String[] getSelectors() { ['*:file:*'] }

    Pattern getPathPattern() { null }

    public <T extends DefaultMutableTreeNode & UriGettable> T make(API api, Container.Entry entry) {
        int lastSlashIndex = entry.path.lastIndexOf('/')
        def name = entry.path.substring(lastSlashIndex+1)
		return new TreeNode(entry, new TreeNodeBean(label:name, icon:ICON))
	}

    static class TreeNode extends DefaultMutableTreeNode implements UriGettable {
        Container.Entry entry

        TreeNode(Container.Entry entry, Object userObject) {
            super(userObject)
            this.entry = entry
        }

        // --- UriGettable --- //
        URI getUri() { entry.uri }
    }
}
