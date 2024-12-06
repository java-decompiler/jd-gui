/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.classfile.attribute;

public class ModuleInfo {
    protected String name;
    protected int flags;
    protected String version;

    public ModuleInfo(String name, int flags, String version) {
        this.name = name;
        this.flags = flags;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public int getFlags() {
        return flags;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("ModuleInfo{name=").append(name);
        sb.append(", flags=").append(flags);

        if (version != null) {
            sb.append(", version=").append(version);
        }

        return sb.append("}").toString();
    }
}
