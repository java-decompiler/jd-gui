/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javafragment;

import org.jd.core.v1.model.fragment.EndFlexibleBlockFragment;

public class EndStatementsBlockFragment extends EndFlexibleBlockFragment implements JavaFragment {
    protected final StartStatementsBlockFragment.Group group;

    public EndStatementsBlockFragment(int minimalLineCount, int lineCount, int maximalLineCount, int weight, String label, StartStatementsBlockFragment.Group group) {
        super(minimalLineCount, lineCount, maximalLineCount, weight, label);
        this.group = group;
        group.add(this);
    }

    public StartStatementsBlockFragment.Group getGroup() {
        return group;
    }

    @Override
    public void accept(JavaFragmentVisitor visitor) {
        visitor.visit(this);
    }
}
