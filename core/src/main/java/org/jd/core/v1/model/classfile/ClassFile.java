/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.classfile;


import org.jd.core.v1.model.classfile.attribute.Attribute;

import java.util.List;
import java.util.Map;

import static org.jd.core.v1.model.classfile.Constants.*;

public class ClassFile {
    protected int majorVersion;
    protected int minorVersion;
    protected int accessFlags;
    protected String internalTypeName;
    protected String superTypeName;
    protected String[] interfaceTypeNames;
    protected Field[] fields;
    protected Method[] methods;
    protected Map<String, Attribute> attributes;

    protected ClassFile outerClassFile;
    protected List<ClassFile> innerClassFiles;

    public ClassFile(int majorVersion, int minorVersion, int accessFlags, String internalTypeName, String superTypeName, String[] interfaceTypeNames, Field[] fields, Method[] methods, Map<String, Attribute> attributes) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.accessFlags = accessFlags;
        this.internalTypeName = internalTypeName;
        this.superTypeName = superTypeName;
        this.interfaceTypeNames = interfaceTypeNames;
        this.fields = fields;
        this.methods = methods;
        this.attributes = attributes;
    }

    public int getMinorVersion() { return minorVersion; }
    public int getMajorVersion() { return majorVersion; }

    public int getAccessFlags() {
        return accessFlags;
    }
    public void setAccessFlags(int accessFlags) {
        this.accessFlags = accessFlags;
    }

    public boolean isEnum()       { return (accessFlags & ACC_ENUM) != 0; }
    public boolean isAnnotation() { return (accessFlags & ACC_ANNOTATION) != 0; }
    public boolean isInterface()  { return (accessFlags & ACC_INTERFACE) != 0; }
    public boolean isModule()     { return (accessFlags & ACC_MODULE) != 0; }
    public boolean isStatic()     { return (accessFlags & ACC_STATIC) != 0; }

    public String getInternalTypeName() {
        return internalTypeName;
    }

    public String getSuperTypeName() {
        return superTypeName;
    }

    public String[] getInterfaceTypeNames() {
        return interfaceTypeNames;
    }

    public Field[] getFields() {
        return fields;
    }

    public Method[] getMethods() {
        return methods;
    }

    @SuppressWarnings("unchecked")
    public <T extends Attribute> T getAttribute(String name) {
        return (attributes == null) ? null : (T)attributes.get(name);
    }

    public ClassFile getOuterClassFile() {
        return outerClassFile;
    }

    public void setOuterClassFile(ClassFile outerClassFile) {
        this.outerClassFile = outerClassFile;
    }

    public List<ClassFile> getInnerClassFiles() {
        return innerClassFiles;
    }

    public void setInnerClassFiles(List<ClassFile> innerClassFiles) {
        this.innerClassFiles = innerClassFiles;
    }

    @Override
    public String toString() {
        return "ClassFile{" + internalTypeName + "}";
    }
}
