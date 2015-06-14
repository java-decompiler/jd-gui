/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.treenode

import groovy.transform.CompileStatic
import jd.gui.api.API
import jd.gui.api.feature.UriGettable
import jd.gui.api.model.Container
import jd.gui.view.component.ClassFilePage
import jd.gui.view.data.TreeNodeBean

import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode

class ClassFileTreeNodeFactoryProvider extends AbstractTypeFileTreeNodeFactoryProvider {
    static final ImageIcon CLASS_FILE_ICON = new ImageIcon(ClassFileTreeNodeFactoryProvider.class.classLoader.getResource('images/classf_obj.png'))

    static final Factory FACTORY = new Factory();

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
                FACTORY
            )
    }

    static class Factory implements AbstractTypeFileTreeNodeFactoryProvider.PageAndTipFactory {
        public <T extends JComponent & UriGettable> T makePage(API a, Container.Entry e) {
            return new ClassFilePage(a, e)
        }

        public String makeTip(API api, Container.Entry entry) {
            def file = new File(entry.container.root.uri)
            def tip = "<html>Location: $file.path"

            entry.inputStream.withStream { is ->
                is.skip(4) // Skip magic number
                int minorVersion = readUnsignedShort(is)
                int majorVersion = readUnsignedShort(is)

                if (majorVersion >= 49) {
                    tip += "<br>Java compiler version: ${majorVersion - (49-5)} ($majorVersion.$minorVersion)"
                } else if (majorVersion >= 45) {
                    tip += "<br>Java compiler version: 1.${majorVersion - (45-1)} ($majorVersion.$minorVersion)"
                }
            }

            tip += "</html>"

            return tip
        }

        /**
         * @see java.io.DataInputStream#readUnsignedShort()
         */
        @CompileStatic
        int readUnsignedShort(InputStream is) throws IOException {
            int ch1 = is.read()
            int ch2 = is.read()
            if ((ch1 | ch2) < 0)
                throw new EOFException()
            return (ch1 << 8) + (ch2 << 0)
        }
    }
}
