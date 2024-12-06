/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javafragment;

import org.jd.core.v1.model.fragment.StartFlexibleBlockFragment;

public class StartSingleStatementBlockFragment extends StartFlexibleBlockFragment implements JavaFragment {
    protected EndSingleStatementBlockFragment end;

    public StartSingleStatementBlockFragment(int minimalLineCount, int lineCount, int maximalLineCount, int weight, String label) {
        super(minimalLineCount, lineCount, maximalLineCount, weight, label);
    }

    public void setLineCount(int lineCount) {
        this.lineCount = lineCount;
    }

    public EndSingleStatementBlockFragment getEndSingleStatementBlockFragment() {
        return end;
    }

    public void setEndSingleStatementBlockFragment(EndSingleStatementBlockFragment end) {
        this.end = end;
    }

    @Override
    public boolean incLineCount(boolean force) {
        if (lineCount < maximalLineCount) {
            lineCount++;

            if (!force) {
                // Update end body fragment
                if (end.getLineCount() == 0) {
                    end.setLineCount(1);
                }
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean decLineCount(boolean force) {
        if (lineCount > minimalLineCount) {
            lineCount--;

            if (!force) {
                // Update end body fragment
                if (lineCount == 1) {
                    end.setLineCount(1);
                }
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void accept(JavaFragmentVisitor visitor) {
        visitor.visit(this);
    }
}
