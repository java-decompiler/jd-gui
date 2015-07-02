/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
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
