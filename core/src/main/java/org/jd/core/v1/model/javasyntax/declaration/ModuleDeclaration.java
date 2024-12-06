/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.declaration;

import java.util.List;

public class ModuleDeclaration extends TypeDeclaration {
    protected String            version;
    protected List<ModuleInfo>  requires;
    protected List<PackageInfo> exports;
    protected List<PackageInfo> opens;
    protected List<String>      uses;
    protected List<ServiceInfo> provides;

    public ModuleDeclaration(int flags, String internalName, String name, String version, List<ModuleInfo> requires, List<PackageInfo> exports, List<PackageInfo> opens, List<String> uses, List<ServiceInfo> provides) {
        super(null, flags, internalName, name, null);
        this.version = version;
        this.requires = requires;
        this.exports = exports;
        this.opens = opens;
        this.uses = uses;
        this.provides = provides;
    }

    public String getVersion() { return version; }
    public List<ModuleInfo> getRequires() { return requires; }
    public List<PackageInfo> getExports() { return exports; }
    public List<PackageInfo> getOpens() { return opens; }
    public List<String> getUses() { return uses; }
    public List<ServiceInfo> getProvides() { return provides; }

    @Override
    public void accept(DeclarationVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "ModuleDeclaration{" + internalTypeName + "}";
    }

    public static class ModuleInfo {
        protected String name;
        protected int flags;
        protected String version;

        public ModuleInfo(String name, int flags, String version) {
            this.name = name;
            this.flags = flags;
            this.version = version;
        }

        public String getName() { return name; }
        public int getFlags() { return flags; }
        public String getVersion() { return version; }

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

    public static class PackageInfo {
        protected String       internalName;
        protected int flags;
        protected List<String> moduleInfoNames;

        public PackageInfo(String internalName, int flags, List<String> moduleInfoNames) {
            this.internalName = internalName;
            this.flags = flags;
            this.moduleInfoNames = moduleInfoNames;
        }

        public String getInternalName() { return internalName; }
        public int getFlags() { return flags; }
        public List<String> getModuleInfoNames() { return moduleInfoNames; }

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

    public static class ServiceInfo {
        protected String       interfaceTypeName;
        protected List<String> implementationTypeNames;

        public ServiceInfo(String interfaceTypeName, List<String> implementationTypeNames) {
            this.interfaceTypeName = interfaceTypeName;
            this.implementationTypeNames = implementationTypeNames;
        }

        public String getInterfaceTypeName() { return interfaceTypeName; }
        public List<String> getImplementationTypeNames() { return implementationTypeNames; }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append("ServiceInfo{interfaceTypeName=").append(interfaceTypeName);

            if (implementationTypeNames != null) {
                sb.append(", implementationTypeNames=").append(implementationTypeNames);
            }

            return sb.append("}").toString();
        }
    }
}
