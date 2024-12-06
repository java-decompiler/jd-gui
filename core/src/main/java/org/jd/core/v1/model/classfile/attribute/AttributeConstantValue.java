/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.classfile.attribute;

import org.jd.core.v1.model.classfile.constant.ConstantValue;

public class AttributeConstantValue implements Attribute {
    protected ConstantValue constantValue;

    public AttributeConstantValue(ConstantValue constantValue) {
        this.constantValue = constantValue;
    }

    public ConstantValue getConstantValue() {
        return constantValue;
    }
}
