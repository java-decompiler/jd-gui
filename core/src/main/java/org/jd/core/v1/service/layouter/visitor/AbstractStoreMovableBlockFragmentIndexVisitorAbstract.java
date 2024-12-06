/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.layouter.visitor;

public abstract class AbstractStoreMovableBlockFragmentIndexVisitorAbstract extends AbstractSearchMovableBlockFragmentVisitor {
    protected int[] indexes = new int[10];
    protected int size;
    protected boolean enabled;

    public void reset() {
        this.size = 0;
        this.depth = 1;
        this.index = 0;
        this.enabled = true;
    }

    public int getIndex(int i) {
        return indexes[i];
    }

    public int getSize() {
        return size;
    }

    public boolean isEnabled() {
        return enabled;
    }

    protected void storeIndex() {
        if (size == indexes.length) {
            // Enlarge list...
            int[] tmp = new int[size * 2];
            System.arraycopy(indexes, 0, tmp, 0, size);
            indexes = tmp;
        }

        indexes[size++] = index;
    }
}
