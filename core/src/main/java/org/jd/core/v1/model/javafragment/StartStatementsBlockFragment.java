/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javafragment;

import org.jd.core.v1.model.fragment.FlexibleFragment;
import org.jd.core.v1.model.fragment.StartFlexibleBlockFragment;
import org.jd.core.v1.util.DefaultList;

public class StartStatementsBlockFragment extends StartFlexibleBlockFragment implements JavaFragment {
    protected Group group;

    public StartStatementsBlockFragment(int minimalLineCount, int lineCount, int maximalLineCount, int weight, String label) {
        super(minimalLineCount, lineCount, maximalLineCount, weight, label);
        this.group = new Group(this);
    }

    public StartStatementsBlockFragment(int minimalLineCount, int lineCount, int maximalLineCount, int weight, String label, Group group) {
        super(minimalLineCount, lineCount, maximalLineCount, weight, label);
        this.group = group;
        group.add(this);
    }

    public Group getGroup() {
        return group;
    }

    @Override
    public void accept(JavaFragmentVisitor visitor) {
        visitor.visit(this);
    }

    public static class Group {
        protected DefaultList<FlexibleFragment> fragments = new DefaultList<>();
        protected int minimalLineCount = Integer.MAX_VALUE;

        Group(FlexibleFragment fragment) {
            this.fragments.add(fragment);
        }

        void add(FlexibleFragment fragment) {
            fragments.add(fragment);
        }

        public int getMinimalLineCount() {
            if (minimalLineCount == Integer.MAX_VALUE) {
                for (FlexibleFragment fragment : fragments) {
                    if (minimalLineCount > fragment.getLineCount()) {
                        minimalLineCount = fragment.getLineCount();
                    }
                }
            }
            return minimalLineCount;
        }
    }
}
