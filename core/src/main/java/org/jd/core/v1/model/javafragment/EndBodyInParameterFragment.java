/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javafragment;

public class EndBodyInParameterFragment extends EndBodyFragment implements JavaFragment {
    public EndBodyInParameterFragment(int minimalLineCount, int lineCount, int maximalLineCount, int weight, String label, StartBodyFragment startBodyFragment) {
        super(minimalLineCount, lineCount, maximalLineCount, weight, label, startBodyFragment);
    }

    @Override
    public boolean incLineCount(boolean force) {
        if (lineCount < maximalLineCount) {
            lineCount++;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean decLineCount(boolean force) {
        if (lineCount > minimalLineCount) {
            lineCount--;
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
