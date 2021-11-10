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
import org.jd.gui.view.data.TreeNodeBean;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;
import java.net.URI;

public class FileTreeNodeFactoryProvider extends AbstractTreeNodeFactoryProvider {
    protected static final ImageIcon ICON = new ImageIcon(FileTreeNodeFactoryProvider.class.getClassLoader().getResource("org/jd/gui/images/file_plain_obj.png"));

    @Override public String[] getSelectors() { return appendSelectors("*:file:*"); }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends DefaultMutableTreeNode & ContainerEntryGettable & UriGettable> T make(API api, Container.Entry entry) {
        int lastSlashIndex = entry.getPath().lastIndexOf('/');
        String label = entry.getPath().substring(lastSlashIndex+1);
        String location = new File(entry.getUri()).getPath();
        return (T)new TreeNode(entry, new TreeNodeBean(label, "Location: " + location, ICON));
    }

    protected static class TreeNode extends DefaultMutableTreeNode implements ContainerEntryGettable, UriGettable {
        protected Container.Entry entry;

        public TreeNode(Container.Entry entry, Object userObject) {
            super(userObject);
            this.entry = entry;
        }

        // --- ContainerEntryGettable --- //
        @Override public Container.Entry getEntry() { return entry; }

        // --- UriGettable --- //
        @Override public URI getUri() { return entry.getUri(); }
    }
}
