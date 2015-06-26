/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.treenode

import jd.gui.api.API
import jd.gui.api.feature.ContainerEntryGettable
import jd.gui.api.feature.PageCreator
import jd.gui.api.feature.UriGettable
import jd.gui.api.model.Container
import jd.gui.view.component.OneTypeReferenceByLinePage
import jd.gui.view.data.TreeNodeBean

import javax.swing.ImageIcon
import javax.swing.JComponent
import javax.swing.tree.DefaultMutableTreeNode
import java.util.regex.Pattern

class MetainfServiceFileTreeNodeFactoryProvider extends FileTreeNodeFactoryProvider {
    static final ImageIcon ICON = new ImageIcon(TextFileTreeNodeFactoryProvider.class.classLoader.getResource('images/ascii_obj.png'))

    /**
     * @return local + optional external selectors
     */
    String[] getSelectors() { ['*:file:*'] + externalSelectors }

    /**
     * @return external or local path pattern
     */
    Pattern getPathPattern() { externalPathPattern ?: ~/META-INF\/services\/[^\/]+/ }

    public <T extends DefaultMutableTreeNode & ContainerEntryGettable & UriGettable> T make(API api, Container.Entry entry) {
        int lastSlashIndex = entry.path.lastIndexOf('/')
        def name = entry.path.substring(lastSlashIndex+1)
        return new TreeNode(entry, new TreeNodeBean(label:name, icon:ICON, tip:"Location: $entry.uri.path"))
    }

    static class TreeNode extends FileTreeNodeFactoryProvider.TreeNode implements PageCreator {
        TreeNode(Container.Entry entry, Object userObject) {
            super(entry, userObject)
        }
        // --- PageCreator --- //
        public <T extends JComponent & UriGettable> T createPage(API api) {
            return new OneTypeReferenceByLinePage(api, entry)
        }
    }
}
