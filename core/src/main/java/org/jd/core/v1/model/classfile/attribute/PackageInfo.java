/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.classfile.attribute;

public class PackageInfo {
    protected String   internalName;
    protected int flags;
    protected String[] moduleInfoNames;

    public PackageInfo(String internalName, int flags, String[] moduleInfoNames) {
        this.internalName = internalName;
        this.flags = flags;
        this.moduleInfoNames = moduleInfoNames;
    }

    public String getInternalName() {
        return internalName;
    }

    public int getFlags() {
        return flags;
    }

    public String[] getModuleInfoNames() {
        return moduleInfoNames;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("PackageInfo{internalName=").append(internalName);
        sb.append(", flags=").append(flags);

        if (moduleInfoNames != null) {
            sb.append(", moduleInfoNames=").append(moduleInfoNames);
        }

        return sb.append("}").toString();
    }
}
