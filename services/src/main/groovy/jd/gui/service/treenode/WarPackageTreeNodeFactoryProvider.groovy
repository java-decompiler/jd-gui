/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.treenode

import java.util.regex.Pattern

class WarPackageTreeNodeFactoryProvider extends PackageTreeNodeFactoryProvider {
    Pattern pattern = ~/WEB-INF\/classes\/.*/

    String[] getTypes() { ['war:dir:*'] }

    Pattern getPathPattern() { pattern }
}
