/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use, 
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.treenode

import javax.swing.ImageIcon
import java.util.regex.Pattern

class MetainfDirectoryTreeNodeFactoryProvider extends DirectoryTreeNodeFactoryProvider {
    static final ImageIcon ICON = new ImageIcon(MetainfDirectoryTreeNodeFactoryProvider.class.classLoader.getResource('org/jd/gui/images/inf_obj.png'))

    /**
     * @return local + optional external selectors
     */
    String[] getSelectors() { ['jar:dir:*', 'war:dir:*', 'ear:dir:*'] + externalSelectors }

    /**
     * @return external or local path pattern
     */
    Pattern getPathPattern() { externalPathPattern ?: ~/(WEB-INF|(WEB-INF\/classes\/)?META-IN(F|F\/.*))/ }

    ImageIcon getIcon() { ICON }
    ImageIcon getOpenIcon() { null }
}
