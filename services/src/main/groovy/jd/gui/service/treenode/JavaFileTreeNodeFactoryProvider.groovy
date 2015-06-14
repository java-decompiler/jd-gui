/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.treenode

import jd.gui.api.API
import jd.gui.api.feature.UriGettable
import jd.gui.api.model.Container
import jd.gui.view.component.JavaFilePage
import jd.gui.view.data.TreeNodeBean

import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode

class JavaFileTreeNodeFactoryProvider extends AbstractTypeFileTreeNodeFactoryProvider {
    static final ImageIcon JAVA_FILE_ICON = new ImageIcon(JavaFileTreeNodeFactoryProvider.class.classLoader.getResource('images/jcu_obj.png'))

    static final Factory FACTORY = new Factory();

    /**
     * @return local + optional external selectors
     */
    String[] getSelectors() { ['*:file:*.java'] + externalSelectors }

    public <T extends DefaultMutableTreeNode & UriGettable> T make(API api, Container.Entry entry) {
        int lastSlashIndex = entry.path.lastIndexOf('/')
        def name = entry.path.substring(lastSlashIndex+1)

        return new FileTreeNode(
                entry,
                new TreeNodeBean(label:name, icon:JAVA_FILE_ICON),
                FACTORY
            )
    }

    static class Factory implements AbstractTypeFileTreeNodeFactoryProvider.PageAndTipFactory {
        public <T extends JComponent & UriGettable> T makePage(API a, Container.Entry e) {
            return new JavaFilePage(a, e)
        }

        public String makeTip(API api, Container.Entry entry) {
            def file = new File(entry.container.root.uri)
            return "<html>Location: $file.path</html>"
        }
    }
}
