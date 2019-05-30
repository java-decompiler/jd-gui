/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.treenode;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.Gutter;
import org.jd.gui.api.API;
import org.jd.gui.api.feature.ContainerEntryGettable;
import org.jd.gui.api.feature.PageCreator;
import org.jd.gui.api.feature.UriGettable;
import org.jd.gui.api.model.Container;
import org.jd.gui.util.exception.ExceptionUtil;
import org.jd.gui.util.io.TextReader;
import org.jd.gui.view.component.TextPage;
import org.jd.gui.view.data.TreeNodeBean;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;
import java.io.IOException;
import java.net.URI;

public class TextFileTreeNodeFactoryProvider extends FileTreeNodeFactoryProvider {
    protected static final ImageIcon ICON = new ImageIcon(TextFileTreeNodeFactoryProvider.class.getClassLoader().getResource("org/jd/gui/images/ascii_obj.png"));

    static {
        // Early class loading
        new Gutter(new RSyntaxTextArea());
        try {
            Theme.load(TextFileTreeNodeFactoryProvider.class.getClassLoader().getResourceAsStream("rsyntaxtextarea/themes/eclipse.xml"));
        } catch (IOException e) {
            assert ExceptionUtil.printStackTrace(e);
        }
    }

    @Override public String[] getSelectors() {
        return appendSelectors("*:file:*.txt", "*:file:*.md", "*:file:*.SF", "*:file:*.policy", "*:file:*.yaml", "*:file:*.yml", "*:file:*/COPYRIGHT", "*:file:*/LICENSE");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends DefaultMutableTreeNode & ContainerEntryGettable & UriGettable> T make(API api, Container.Entry entry) {
        int lastSlashIndex = entry.getPath().lastIndexOf("/");
        String label = entry.getPath().substring(lastSlashIndex+1);
        String location = new File(entry.getUri()).getPath();
        return (T)new TreeNode(entry, new TreeNodeBean(label, "Location: " + location, ICON));
    }

    protected static class TreeNode extends FileTreeNodeFactoryProvider.TreeNode implements PageCreator {
        public TreeNode(Container.Entry entry, Object userObject) { super(entry, userObject); }

        // --- PageCreator --- //
        @Override
        @SuppressWarnings("unchecked")
        public <T extends JComponent & UriGettable> T createPage(API api) {
            return (T)new Page(entry);
        }
    }

    protected static class Page extends TextPage implements UriGettable {
        protected Container.Entry entry;

        public Page(Container.Entry entry) {
            this.entry = entry;
            setText(TextReader.getText(entry.getInputStream()));
        }

        // --- UriGettable --- //
        @Override public URI getUri() { return entry.getUri(); }

        // --- ContentSavable --- //
        public String getFileName() {
            String path = entry.getPath();
            int index = path.lastIndexOf("/");
            return path.substring(index+1);
        }
    }
}