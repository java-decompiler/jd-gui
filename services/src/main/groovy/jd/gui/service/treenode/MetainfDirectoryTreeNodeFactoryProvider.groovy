/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.treenode

import javax.swing.ImageIcon
import java.util.regex.Pattern

class MetainfDirectoryTreeNodeFactoryProvider extends DirectoryTreeNodeFactoryProvider {
    static final ImageIcon ICON = new ImageIcon(MetainfDirectoryTreeNodeFactoryProvider.class.classLoader.getResource('images/inf_obj.png'))

    Pattern pattern = ~/(WEB-INF|(WEB-INF\/classes\/)?META-IN(F|F\/.*))/

    String[] getSelectors() { ['jar:dir:*', 'war:dir:*', 'ear:dir:*'] }

    Pattern getPathPattern() { pattern }

    ImageIcon getIcon() { ICON }
    ImageIcon getOpenIcon() { null }
}
