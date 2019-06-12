/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use, 
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.treenode;

import javax.swing.*;
import java.util.regex.Pattern;

public class MetainfDirectoryTreeNodeFactoryProvider extends DirectoryTreeNodeFactoryProvider {
    protected static final ImageIcon ICON = new ImageIcon(MetainfDirectoryTreeNodeFactoryProvider.class.getClassLoader().getResource("org/jd/gui/images/inf_obj.png"));

    @Override public String[] getSelectors() {
        return appendSelectors(
                "jar:dir:META-INF",
                "war:dir:WEB-INF",
                "war:dir:WEB-INF/classes/META-INF",
                "ear:dir:META-INF",
                "jmod:dir:classes/META-INF");
    }

    @Override public ImageIcon getIcon() { return ICON; }
    @Override public ImageIcon getOpenIcon() { return null; }
}
