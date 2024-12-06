/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.classfile.attribute;

public class AttributeModulePackages implements Attribute {
    protected String[] packageNames;

    public AttributeModulePackages(String[] packageNames) {
        this.packageNames = packageNames;
    }

    public String[] getPackageNames() {
        return packageNames;
    }
}
