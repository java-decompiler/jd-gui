/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use, 
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.treenode;

import javax.swing.*;

public class ClassesDirectoryTreeNodeFactoryProvider extends DirectoryTreeNodeFactoryProvider {
    protected static final ImageIcon ICON = new ImageIcon(ClassesDirectoryTreeNodeFactoryProvider.class.getClassLoader().getResource("org/jd/gui/images/packagefolder_obj.png"));

    @Override public String[] getSelectors() {
        return appendSelectors(
                "jar:dir:META-INF/versions",
                "jar:dir:META-INF/versions/5",
                "jar:dir:META-INF/versions/6",
                "jar:dir:META-INF/versions/7",
                "jar:dir:META-INF/versions/8",
                "jar:dir:META-INF/versions/9",
                "jar:dir:META-INF/versions/10",
                "jar:dir:META-INF/versions/11",
                "jar:dir:META-INF/versions/12",
                "jar:dir:META-INF/versions/13",
                "jar:dir:META-INF/versions/14",
                "war:dir:WEB-INF/classes",
                "jmod:dir:classes");
    }

    @Override public ImageIcon getIcon() { return ICON; }
    @Override public ImageIcon getOpenIcon() { return null; }
}
