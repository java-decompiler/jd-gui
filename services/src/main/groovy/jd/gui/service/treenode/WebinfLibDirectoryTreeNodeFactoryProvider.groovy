/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.treenode

import javax.swing.*

class WebinfLibDirectoryTreeNodeFactoryProvider extends DirectoryTreeNodeFactoryProvider {
    static final ImageIcon icon = new ImageIcon(WebinfLibDirectoryTreeNodeFactoryProvider.class.classLoader.getResource('images/archivefolder_obj.png'))

    String[] getTypes() { ['war:dir:WEB-INF/lib'] }

    ImageIcon getIcon() { icon }
    ImageIcon getOpenIcon() { null }
}
