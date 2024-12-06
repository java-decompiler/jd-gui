/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.type;

import org.jd.core.v1.util.Base;

public interface BaseType extends TypeVisitable, Base<Type> {
    default boolean isGenericType() { return false; }
    default boolean isInnerObjectType() { return false; }
    default boolean isObjectType() { return false; }
    default boolean isPrimitiveType() { return false; }
    default boolean isTypes() { return false; }

    default ObjectType getOuterType() { return ObjectType.TYPE_UNDEFINED_OBJECT; }

    default String getInternalName() { return ""; }
}
