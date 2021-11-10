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

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;

public class JavaModuleFileTreeNodeFactoryProvider extends ZipFileTreeNodeFactoryProvider {
    @Override public String[] getSelectors() { return appendSelectors("*:file:*.jmod"); }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends DefaultMutableTreeNode & ContainerEntryGettable & UriGettable> T make(API api, Container.Entry entry) {
        int lastSlashIndex = entry.getPath().lastIndexOf("/");
        String label = entry.getPath().substring(lastSlashIndex+1);
        String location = new File(entry.getUri()).getPath();
        T node = (T)new TreeNode(entry, new TreeNodeBean(label, "Location: " + location, ICON));
        // Add dummy node
        node.add(new DefaultMutableTreeNode());
        return node;
    }
}
