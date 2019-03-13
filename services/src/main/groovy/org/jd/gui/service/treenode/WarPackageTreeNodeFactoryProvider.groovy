/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.treenode

import java.util.regex.Pattern

class WarPackageTreeNodeFactoryProvider extends PackageTreeNodeFactoryProvider {

    /**
     * @return local + optional external selectors
     */
    String[] getSelectors() { ['war:dir:*'] + externalSelectors }

    /**
     * @return external or local path pattern
     */
    Pattern getPathPattern() { externalPathPattern ?: ~/WEB-INF\/classes\/.*/ }
}
