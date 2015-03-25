/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.treenode

import java.util.regex.Pattern

class MetainfServiceFileTreeNodeFactoryProvider extends TextFileTreeNodeFactoryProvider {
    Pattern pattern = ~/META-INF\/services\/[^\/]+/

    String[] getTypes() { ['*:file:*'] }

    Pattern getPathPattern() { pattern }
}
