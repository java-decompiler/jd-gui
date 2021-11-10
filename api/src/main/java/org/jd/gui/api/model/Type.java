/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.api.model;

import javax.swing.*;
import java.util.Collection;

public interface Type {
    int FLAG_PUBLIC = 1;
    int FLAG_PRIVATE = 2;
    int FLAG_PROTECTED = 4;
    int FLAG_STATIC = 8;
    int FLAG_FINAL = 16;
    int FLAG_VARARGS = 128;
    int FLAG_INTERFACE = 512;
    int FLAG_ABSTRACT = 1024;
    int FLAG_ANNOTATION = 8192;
    int FLAG_ENUM = 16384;

    int getFlags();

    String getName();

    String getSuperName();

    String getOuterName();

    String getDisplayTypeName();

    String getDisplayInnerTypeName();

    String getDisplayPackageName();

    Icon getIcon();

    Collection<Type> getInnerTypes();

    Collection<Field> getFields();

    Collection<Method> getMethods();

    interface Field {
        int getFlags();

        String getName();

        String getDescriptor();

        String getDisplayName();

        Icon getIcon();
    }

    interface Method {
        int getFlags();

        String getName();

        String getDescriptor();

        String getDisplayName();

        Icon getIcon();
    }
}
