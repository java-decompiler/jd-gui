/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.treenode;

import org.jd.gui.api.API;
import org.jd.gui.api.feature.ContainerEntryGettable;
import org.jd.gui.api.feature.UriGettable;
import org.jd.gui.api.model.Container;
import org.jd.gui.util.exception.ExceptionUtil;
import org.jd.gui.view.component.DynamicPage;
import org.jd.gui.view.data.TreeNodeBean;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

public class ClassFileTreeNodeFactoryProvider extends AbstractTypeFileTreeNodeFactoryProvider {
    protected static final ImageIcon CLASS_FILE_ICON = new ImageIcon(ClassFileTreeNodeFactoryProvider.class.getClassLoader().getResource("org/jd/gui/images/classf_obj.png"));
    protected static final Factory FACTORY = new Factory();

    static {
        // Early class loading
        try {
            Class.forName(DynamicPage.class.getName());
        } catch (Exception e) {
            assert ExceptionUtil.printStackTrace(e);
        }
    }

    @Override public String[] getSelectors() { return appendSelectors("*:file:*.class"); }

    @Override
    public Pattern getPathPattern() {
        if (externalPathPattern == null) {
            return Pattern.compile("^((?!module-info\\.class).)*$");
        } else {
            return externalPathPattern;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends DefaultMutableTreeNode & ContainerEntryGettable & UriGettable> T make(API api, Container.Entry entry) {
        int lastSlashIndex = entry.getPath().lastIndexOf('/');
        String label = entry.getPath().substring(lastSlashIndex+1);
        return (T)new FileTreeNode(entry, new TreeNodeBean(label, CLASS_FILE_ICON), FACTORY);
    }

    protected static class Factory implements AbstractTypeFileTreeNodeFactoryProvider.PageAndTipFactory {
        @Override
        @SuppressWarnings("unchecked")
        public <T extends JComponent & UriGettable> T makePage(API a, Container.Entry e) {
            return (T)new DynamicPage(a, e);
        }

        @Override
        public String makeTip(API api, Container.Entry entry) {
            String location = new File(entry.getUri()).getPath();
            StringBuilder tip = new StringBuilder("<html>Location: ");

            tip.append(location);
            tip.append("<br>Java compiler version: ");

            try (InputStream is = entry.getInputStream()) {
                is.skip(4); // Skip magic number
                int minorVersion = readUnsignedShort(is);
                int majorVersion = readUnsignedShort(is);

                if (majorVersion >= 49) {
                    tip.append(majorVersion - (49-5));
                } else if (majorVersion >= 45) {
                    tip.append("1.");
                    tip.append(majorVersion - (45-1));
                }
                tip.append(" (");
                tip.append(majorVersion);
                tip.append('.');
                tip.append(minorVersion);
                tip.append(')');
            } catch (IOException e) {
                assert ExceptionUtil.printStackTrace(e);
            }

            tip.append("</html>");

            return tip.toString();
        }

        /**
         * @see java.io.DataInputStream#readUnsignedShort()
         */
        protected int readUnsignedShort(InputStream is) throws IOException {
            int ch1 = is.read();
            int ch2 = is.read();
            if ((ch1 | ch2) < 0)
                throw new EOFException();
            return (ch1 << 8) + (ch2 << 0);
        }
    }
}
