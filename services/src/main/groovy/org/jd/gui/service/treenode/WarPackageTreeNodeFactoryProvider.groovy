/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
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
