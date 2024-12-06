/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.fragment;

public abstract class AbstractNopFlexibleFragmentVisitor implements FragmentVisitor {
    @Override public void visit(EndFlexibleBlockFragment fragment) {}
    @Override public void visit(EndMovableBlockFragment fragment) {}
    @Override public void visit(StartFlexibleBlockFragment fragment) {}
    @Override public void visit(StartMovableBlockFragment fragment) {}
    @Override public void visit(FixedFragment fragment) {}
}
