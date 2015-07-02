/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.service.treenode

import javax.swing.*

class WebinfClassesDirectoryTreeNodeFactoryProvider extends DirectoryTreeNodeFactoryProvider {
    static final ImageIcon ICON = new ImageIcon(WebinfClassesDirectoryTreeNodeFactoryProvider.class.classLoader.getResource('org/jd/gui/images/packagefolder_obj.png'))

    /**
     * @return local + optional external selectors
     */
    String[] getSelectors() { ['war:dir:WEB-INF/classes'] + externalSelectors }

    ImageIcon getIcon() { ICON }
    ImageIcon getOpenIcon() { null }
}
