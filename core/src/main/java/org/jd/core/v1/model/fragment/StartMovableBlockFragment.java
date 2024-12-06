/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.fragment;

public class StartMovableBlockFragment extends FlexibleFragment {
    protected int type;

    public StartMovableBlockFragment(int type) {
        super(0, 0, 0, 0, "Start movable block");
        this.type = type;
    }

    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return "{start-movable-block}";
    }

    @Override
    public void accept(FragmentVisitor visitor) {
        visitor.visit(this);
    }
}
