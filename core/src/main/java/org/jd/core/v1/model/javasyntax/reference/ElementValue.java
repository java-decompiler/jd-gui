/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.reference;

public interface ElementValue extends BaseElementValue {
    public static final byte EV_UNKNOWN= 0;
    public static final byte EV_PRIMITIVE_TYPE= 1;
    public static final byte EV_ENUM_CONST_VALUE= 2;
    public static final byte EV_CLASS_INFO= 3;
    public static final byte EV_ANNOTATION_VALUE= 4;
    public static final byte EV_ARRAY_VALUE= 5;
}
