/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.treenode

import org.jd.gui.api.API
import org.jd.gui.api.feature.ContainerEntryGettable
import org.jd.gui.api.feature.PageCreator
import org.jd.gui.api.feature.UriGettable
import org.jd.gui.api.model.Container
import org.jd.gui.view.component.EjbJarXmlFilePage
import org.jd.gui.view.data.TreeNodeBean

import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode

class EjbJarXmlFileTreeNodeFactoryProvider extends FileTreeNodeFactoryProvider {
    static final ImageIcon ICON = new ImageIcon(ManifestFileTreeNodeFactoryProvider.class.classLoader.getResource('org/jd/gui/images/xml_obj.gif'))

    /**
     * @return local + optional external selectors
     */
    String[] getSelectors() { ['jar:file:META-INF/ejb-jar.xml'] + externalSelectors }

    public <T extends DefaultMutableTreeNode & ContainerEntryGettable & UriGettable> T make(API api, Container.Entry entry) {
        return new TreeNode(entry, new TreeNodeBean(label:'ejb-jar.xml', icon:ICON, tip:"Location: $entry.uri.path"))
    }

    static class TreeNode extends FileTreeNodeFactoryProvider.TreeNode implements PageCreator {
        TreeNode(Container.Entry entry, Object userObject) {
            super(entry, userObject)
        }
        // --- PageCreator --- //
        public <T extends JComponent & UriGettable> T createPage(API api) {
            return new EjbJarXmlFilePage(api, entry)
        }
    }
}
