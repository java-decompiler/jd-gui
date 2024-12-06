/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javafragment;

import org.jd.core.v1.model.fragment.StartFlexibleBlockFragment;

public class StartBlockFragment extends StartFlexibleBlockFragment implements JavaFragment {
    protected EndBlockFragment end;

    public StartBlockFragment(int minimalLineCount, int lineCount, int maximalLineCount, int weight, String label) {
        super(minimalLineCount, lineCount, maximalLineCount, weight, label);
    }

    public EndBlockFragment getEndArrayInitializerBlockFragment() {
        return end;
    }

    public void setEndArrayInitializerBlockFragment(EndBlockFragment end) {
        this.end = end;
    }

    public void setLineCount(int lineCount) {
        this.lineCount = lineCount;
    }

    @Override
    public boolean incLineCount(boolean force) {
        if (lineCount < maximalLineCount) {
            lineCount++;

            if (!force) {
                // Update end body fragment
                if ((lineCount == 1) && (end.getLineCount() == 0)) {
                    end.setLineCount(lineCount);
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
                    end.setLineCount(lineCount);
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
