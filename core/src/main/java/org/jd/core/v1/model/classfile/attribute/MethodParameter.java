/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.classfile.attribute;

public class MethodParameter {
    protected String name;
    protected int    access;

    public MethodParameter(String name, int access) {
        this.name = name;
        this.access = access;
    }

    public String getName() {
        return name;
    }

    public int getAccess() {
        return access;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Parameter{name=").append(name);
        sb.append(", access=").append(access);

        return sb.append("}").toString();
    }
}
