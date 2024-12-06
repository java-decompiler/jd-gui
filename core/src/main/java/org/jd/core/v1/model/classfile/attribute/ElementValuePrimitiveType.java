/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.classfile.attribute;

import org.jd.core.v1.model.classfile.constant.ConstantValue;

public class ElementValuePrimitiveType implements ElementValue {
	/*
	 * type = {'B', 'D', 'F', 'I', 'J', 'S', 'Z', 'C', 's'}
	 */	
	protected int type;
    protected ConstantValue constValue;

    public ElementValuePrimitiveType(int type, ConstantValue constValue) {
        this.type = type;
        this.constValue = constValue;
    }

    public int getType() {
        return type;
    }

    @SuppressWarnings("unchecked")
    public <T extends ConstantValue> T getConstValue() {
        return (T)constValue;
    }

    @Override
    public void accept(ElementValueVisitor visitor) {
        visitor.visit(this);
    }
}
