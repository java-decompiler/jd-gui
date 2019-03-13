/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use, 
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.treenode

import javax.swing.*

class WebinfLibDirectoryTreeNodeFactoryProvider extends DirectoryTreeNodeFactoryProvider {
    static final ImageIcon ICON = new ImageIcon(WebinfLibDirectoryTreeNodeFactoryProvider.class.classLoader.getResource('org/jd/gui/images/archivefolder_obj.png'))

    /**
     * @return local + optional external selectors
     */
    String[] getSelectors() { ['war:dir:WEB-INF/lib'] + externalSelectors }

    ImageIcon getIcon() { ICON }
    ImageIcon getOpenIcon() { null }
}
