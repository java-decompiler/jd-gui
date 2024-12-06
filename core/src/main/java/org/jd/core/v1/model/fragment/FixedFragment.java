/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.fragment;

public abstract class FixedFragment implements Fragment {
    protected final int firstLineNumber;
    protected final int lastLineNumber;

    public FixedFragment(int firstLineNumber, int lastLineNumber) {
        this.firstLineNumber = firstLineNumber;
        this.lastLineNumber = lastLineNumber;
    }

    public int getFirstLineNumber() {
        return firstLineNumber;
    }

    public int getLastLineNumber() {
        return lastLineNumber;
    }

    @Override
    public String toString() {
        return "{first-line-number=" + firstLineNumber + ", last-line-number=" + lastLineNumber + "}";
    }

    @Override
    public void accept(FragmentVisitor visitor) {
        visitor.visit(this);
    }
}
