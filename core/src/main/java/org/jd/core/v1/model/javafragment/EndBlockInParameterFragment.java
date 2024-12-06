/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javafragment;

public class EndBlockInParameterFragment extends EndBlockFragment implements JavaFragment {
    public EndBlockInParameterFragment(int minimalLineCount, int lineCount, int maximalLineCount, int weight, String label, StartBlockFragment start) {
        super(minimalLineCount, lineCount, maximalLineCount, weight, label, start);
    }

    @Override
    public void accept(JavaFragmentVisitor visitor) {
        visitor.visit(this);
    }
}
