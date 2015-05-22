/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.treenode

import javax.swing.*
import java.util.regex.Pattern

class WebinfClassesDirectoryTreeNodeFactoryProvider extends DirectoryTreeNodeFactoryProvider {
    static final ImageIcon icon = new ImageIcon(WebinfClassesDirectoryTreeNodeFactoryProvider.class.classLoader.getResource('images/packagefolder_obj.png'))

    String[] getSelectors() { ['war:dir:WEB-INF/classes'] }

    ImageIcon getIcon() { icon }
    ImageIcon getOpenIcon() { null }
}
