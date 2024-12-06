/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.classfile;

import org.jd.core.v1.model.classfile.attribute.Attribute;

import java.util.Map;

public class Field {
    protected int accessFlags;
    protected String name;
    protected String descriptor;
    protected Map<String, Attribute> attributes;

    public Field(int accessFlags, String name, String descriptor, Map<String, Attribute> attributes) {
        this.accessFlags = accessFlags;
        this.name = name;
        this.descriptor = descriptor;
        this.attributes = attributes;
    }

    public int getAccessFlags() {
        return accessFlags;
    }

    public String getName() {
        return name;
    }

    public String getDescriptor() {
        return descriptor;
    }

    @SuppressWarnings("unchecked")
    public <T extends Attribute> T getAttribute(String name) {
        return (attributes == null) ? null : (T)attributes.get(name);
    }

    @Override
    public String toString() {
        return "Field{" + name + " " + descriptor + "}";
    }
}
