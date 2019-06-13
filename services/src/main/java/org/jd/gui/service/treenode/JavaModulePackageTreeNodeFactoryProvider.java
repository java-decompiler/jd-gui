/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.treenode;

import java.util.regex.Pattern;

public class JavaModulePackageTreeNodeFactoryProvider extends PackageTreeNodeFactoryProvider {

    @Override public String[] getSelectors() { return appendSelectors("jmod:dir:*"); }

    @Override
    public Pattern getPathPattern() {
        if (externalPathPattern == null) {
            return Pattern.compile("classes\\/(?!META-INF)..*");
        } else {
            return externalPathPattern;
        }
    }
}
