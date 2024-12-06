/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.fragment;

public interface FragmentVisitor {
    void visit(FlexibleFragment fragment);
    void visit(EndFlexibleBlockFragment fragment);
    void visit(EndMovableBlockFragment fragment);
    void visit(SpacerBetweenMovableBlocksFragment fragment);
    void visit(StartFlexibleBlockFragment fragment);
    void visit(StartMovableBlockFragment fragment);
    void visit(FixedFragment fragment);
}
