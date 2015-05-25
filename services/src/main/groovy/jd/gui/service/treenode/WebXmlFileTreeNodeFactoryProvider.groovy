/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.treenode

import jd.gui.api.API
import jd.gui.api.feature.PageCreator
import jd.gui.api.feature.UriGettable
import jd.gui.api.model.Container
import jd.gui.view.component.WebXmlFilePage
import jd.gui.view.data.TreeNodeBean

import javax.swing.ImageIcon
import javax.swing.JComponent
import javax.swing.tree.DefaultMutableTreeNode


class WebXmlFileTreeNodeFactoryProvider extends FileTreeNodeFactoryProvider {
    static final ImageIcon ICON = new ImageIcon(ManifestFileTreeNodeFactoryProvider.class.classLoader.getResource('images/xml_obj.gif'))

    String[] getSelectors() { ['war:file:WEB-INF/web.xml'] }

    public <T extends DefaultMutableTreeNode & UriGettable> T make(API api, Container.Entry entry) {
        return new TreeNode(entry, new TreeNodeBean(label:'web.xml', icon:ICON, tip:"Location: $entry.uri.path"))
    }

    static class TreeNode extends FileTreeNodeFactoryProvider.TreeNode implements PageCreator {
        TreeNode(Container.Entry entry, Object userObject) {
            super(entry, userObject)
        }
        // --- PageCreator --- //
        public <T extends JComponent & UriGettable> T createPage(API api) {
            return new WebXmlFilePage(api, entry)
        }
    }
}
