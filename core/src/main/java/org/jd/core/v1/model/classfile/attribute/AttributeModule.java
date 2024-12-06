/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.classfile.attribute;

// Example: https://github.com/netroby/jdk9-dev/blob/master/jdk/src/java.management/share/classes/module-info.java
public class AttributeModule implements Attribute {
    protected String name;
    protected int flags;
    protected String version;

    protected ModuleInfo[]  requires;
    protected PackageInfo[] exports;
    protected PackageInfo[] opens;
    protected String[]      uses;
    protected ServiceInfo[] provides;

    public AttributeModule(String name, int flags, String version, ModuleInfo[] requires, PackageInfo[] exports, PackageInfo[] opens, String[] uses, ServiceInfo[] provides) {
        this.name = name;
        this.flags = flags;
        this.version = version;
        this.requires = requires;
        this.exports = exports;
        this.opens = opens;
        this.uses = uses;
        this.provides = provides;
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

    public ModuleInfo[] getRequires() {
        return requires;
    }

    public PackageInfo[] getExports() {
        return exports;
    }

    public PackageInfo[] getOpens() {
        return opens;
    }

    public String[] getUses() {
        return uses;
    }

    public ServiceInfo[] getProvides() {
        return provides;
    }
}
