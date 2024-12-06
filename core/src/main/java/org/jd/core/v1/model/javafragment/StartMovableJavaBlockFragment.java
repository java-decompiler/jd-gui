/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javafragment;

import org.jd.core.v1.model.fragment.StartMovableBlockFragment;

public class StartMovableJavaBlockFragment extends StartMovableBlockFragment implements JavaFragment {
    public static final StartMovableJavaBlockFragment START_MOVABLE_TYPE_BLOCK = new StartMovableJavaBlockFragment(1);
    public static final StartMovableJavaBlockFragment START_MOVABLE_FIELD_BLOCK = new StartMovableJavaBlockFragment(2);
    public static final StartMovableJavaBlockFragment START_MOVABLE_METHOD_BLOCK = new StartMovableJavaBlockFragment(3);

    protected StartMovableJavaBlockFragment(int type) {
        super(type);
    }

    @Override
    public void accept(JavaFragmentVisitor visitor) {
        visitor.visit(this);
    }
}
