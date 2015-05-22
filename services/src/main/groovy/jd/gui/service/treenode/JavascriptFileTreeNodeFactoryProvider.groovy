/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.treenode

import jd.gui.api.API
import jd.gui.api.feature.UriGettable
import jd.gui.api.model.Container
import jd.gui.view.data.TreeNodeBean
import org.fife.ui.rsyntaxtextarea.SyntaxConstants

import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode

class JavascriptFileTreeNodeFactoryProvider extends TextFileTreeNodeFactoryProvider {
    static final ImageIcon icon = new ImageIcon(JavascriptFileTreeNodeFactoryProvider.class.classLoader.getResource('images/js_obj.png'))

    String[] getSelectors() { ['*:file:*.js'] }

    public <T extends DefaultMutableTreeNode & UriGettable> T make(API api, Container.Entry entry) {
        int lastSlashIndex = entry.path.lastIndexOf('/')
        def name = entry.path.substring(lastSlashIndex+1)
        return new TreeNode(entry, new TreeNodeBean(label:name, icon:icon, tip:"Location: $entry.uri.path"))
    }

    static class TreeNode extends TextFileTreeNodeFactoryProvider.TreeNode {
        TreeNode(Container.Entry entry, Object userObject) {
            super(entry, userObject)
        }

        public <T extends JComponent & UriGettable> T createPage(API api) {
            return new TextFileTreeNodeFactoryProvider.Page(entry) {
                String getSyntaxStyle() {
                    SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT
                }
            }
        }
    }
}