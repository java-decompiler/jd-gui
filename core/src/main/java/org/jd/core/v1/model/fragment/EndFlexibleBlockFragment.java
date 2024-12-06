/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.fragment;

public abstract class EndFlexibleBlockFragment extends StartFlexibleBlockFragment {
    protected EndFlexibleBlockFragment(int minimalLineCount, int lineCount, int maximalLineCount, int weight, String label) {
        super(minimalLineCount, lineCount, maximalLineCount, weight, label);
    }

    @Override
    public void accept(FragmentVisitor visitor) {
        visitor.visit(this);
    }
}
