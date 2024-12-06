/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.classfile.attribute;

public class ServiceInfo {
    protected String   interfaceTypeName;
    protected String[] implementationTypeNames;

    public ServiceInfo(String interfaceTypeName, String[] implementationTypeNames) {
        this.interfaceTypeName = interfaceTypeName;
        this.implementationTypeNames = implementationTypeNames;
    }

    public String getInterfaceTypeName() {
        return interfaceTypeName;
    }

    public String[] getImplementationTypeNames() {
        return implementationTypeNames;
    }

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
