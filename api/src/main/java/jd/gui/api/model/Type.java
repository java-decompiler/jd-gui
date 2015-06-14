/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.api.model;

import javax.swing.*;
import java.util.Collection;

public interface Type {
    public final static int FLAG_PUBLIC = 1;
    public final static int FLAG_PRIVATE = 2;
    public final static int FLAG_PROTECTED = 4;
    public final static int FLAG_STATIC = 8;
    public final static int FLAG_FINAL = 16;
    public final static int FLAG_VARARGS = 128;
    public final static int FLAG_INTERFACE = 512;
    public final static int FLAG_ABSTRACT = 1024;
    public final static int FLAG_ANNOTATION = 8192;
    public final static int FLAG_ENUM = 16384;

    public int getFlags();

    public String getName();

    public String getSuperName();

    public String getOuterName();

    public String getDisplayTypeName();

    public String getDisplayInnerTypeName();

    public String getDisplayPackageName();

    public Icon getIcon();

    public Collection<Type> getInnerTypes();

    public Collection<Field> getFields();

    public Collection<Method> getMethods();

    public interface Field {
        public int getFlags();

        public String getName();

        public String getDescriptor();

        public Icon getIcon();
    }

    public interface Method {
        public int getFlags();

        public String getName();

        public String getDescriptor();

        public Icon getIcon();
    }
}
