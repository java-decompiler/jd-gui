/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.fragment;

public class EndMovableBlockFragment extends FlexibleFragment {
    public EndMovableBlockFragment() {
        super(0, 0, 0, 0, "End movable block");
    }

    @Override
    public String toString() {
        return "{end-movable-block}";
    }

    @Override
    public void accept(FragmentVisitor visitor) {
        visitor.visit(this);
    }
}
