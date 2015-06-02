/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.treenode

import jd.gui.api.API
import jd.gui.api.feature.UriGettable
import jd.gui.api.model.Container
import jd.gui.view.component.ClassFilePage
import jd.gui.view.data.TreeNodeBean

import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode

class ClassFileTreeNodeFactoryProvider extends AbstractTypeFileTreeNodeFactoryProvider {
    static final ImageIcon CLASS_FILE_ICON = new ImageIcon(ClassFileTreeNodeFactoryProvider.class.classLoader.getResource('images/classf_obj.png'))

    static {
        // Early class loading
        try {
            new ClassFilePage(null, null)
        } catch (Exception ignore) {}
    }

    /**
     * @return local + optional external selectors
     */
    String[] getSelectors() { ['*:file:*.class'] + externalSelectors }

    public <T extends DefaultMutableTreeNode & UriGettable> T make(API api, Container.Entry entry) {
        int lastSlashIndex = entry.path.lastIndexOf('/')
        def name = entry.path.substring(lastSlashIndex+1)

        return new FileTreeNode(
                entry,
                new TreeNodeBean(label:name, icon:CLASS_FILE_ICON),
                new AbstractTypeFileTreeNodeFactoryProvider.PageFactory() {
                    public <T extends JComponent & UriGettable> T make(API a, Container.Entry e) { new ClassFilePage(a, e) }
                }
            )
    }
}
