/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.treenode

import javax.swing.*

class WebinfLibDirectoryTreeNodeFactoryProvider extends DirectoryTreeNodeFactoryProvider {
    static final ImageIcon ICON = new ImageIcon(WebinfLibDirectoryTreeNodeFactoryProvider.class.classLoader.getResource('images/archivefolder_obj.png'))

    String[] getSelectors() { ['war:dir:WEB-INF/lib'] }

    ImageIcon getIcon() { ICON }
    ImageIcon getOpenIcon() { null }
}
