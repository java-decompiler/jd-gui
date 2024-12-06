/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.reference;

import org.jd.core.v1.util.DefaultList;

import java.util.Collection;

public class ElementValues extends DefaultList<ElementValue> implements BaseElementValue {
    public ElementValues() {
    }

    public ElementValues(int capacity) {
        super(capacity);
    }

    public ElementValues(Collection<ElementValue> collection) {
        super(collection);
        assert (collection != null) && (collection.size() > 1) : "Uses 'ElementValue' or sub class instead";
    }

    @Override
    public void accept(ReferenceVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "ElementValues{" + super.toString() + "}";
    }
}
