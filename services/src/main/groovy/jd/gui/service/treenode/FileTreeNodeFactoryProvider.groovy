/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.treenode

import jd.gui.api.API
import jd.gui.api.feature.ContainerEntryGettable
import jd.gui.api.feature.UriGettable
import jd.gui.api.model.Container
import jd.gui.view.data.TreeNodeBean

import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode

class FileTreeNodeFactoryProvider extends AbstractTreeNodeFactoryProvider {
	static final ImageIcon ICON = new ImageIcon(FileTreeNodeFactoryProvider.class.classLoader.getResource('images/file_plain_obj.png'))

    /**
     * @return local + optional external selectors
     */
    String[] getSelectors() { ['*:file:*'] + externalSelectors }

    public <T extends DefaultMutableTreeNode & ContainerEntryGettable & UriGettable> T make(API api, Container.Entry entry) {
        int lastSlashIndex = entry.path.lastIndexOf('/')
        def name = entry.path.substring(lastSlashIndex+1)
		return new TreeNode(entry, new TreeNodeBean(label:name, icon:ICON))
	}

    static class TreeNode extends DefaultMutableTreeNode implements ContainerEntryGettable, UriGettable {
        Container.Entry entry

        TreeNode(Container.Entry entry, Object userObject) {
            super(userObject)
            this.entry = entry
        }

        // --- ContainerEntryGettable --- //
        Container.Entry getEntry() { entry }

        // --- UriGettable --- //
        URI getUri() { entry.uri }
    }
}
