/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.layouter.visitor;

import org.jd.core.v1.model.fragment.*;
import org.jd.core.v1.util.DefaultList;


public class UpdateSpacerBetweenMovableBlocksVisitor implements FragmentVisitor {
    protected DefaultList<StartMovableBlockFragment> blocks = new DefaultList<>();
    protected DefaultList<SpacerBetweenMovableBlocksFragment> spacers = new DefaultList<>();

    protected int lastStartMovableBlockFragmentType;
    protected SpacerBetweenMovableBlocksFragment lastSpacer;
    protected int depth;

    public void reset() {
        lastStartMovableBlockFragmentType = 0;
        lastSpacer = null;
        depth = 0;
    }

    @Override
    public void visit(StartMovableBlockFragment fragment) {
        if (lastSpacer != null) {
            // type=2 ==> Field
            if ((lastStartMovableBlockFragmentType == 2) && (fragment.getType() == 2)) {
                // 1 new line between 2 field declarations
                lastSpacer.setInitialLineCount(1);
            } else {
                // otherwise, 2 new lines
                lastSpacer.setInitialLineCount(2);
            }
        }

        if (depth != 0) {
            blocks.add(fragment);
            spacers.add(lastSpacer);
            lastSpacer = null;
        }

        lastStartMovableBlockFragmentType = fragment.getType();
        depth = 1;
    }

    @Override
    public void visit(EndMovableBlockFragment fragment) {
        if (depth != 1) {
            lastSpacer = spacers.remove(spacers.size()-1);
            lastStartMovableBlockFragmentType = blocks.removeLast().getType();
        }

        depth = 0;
    }

    @Override
    public void visit(SpacerBetweenMovableBlocksFragment fragment) {
        lastSpacer = fragment;
    }

    @Override public void visit(FlexibleFragment fragment) {}
    @Override public void visit(EndFlexibleBlockFragment fragment) {}
    @Override public void visit(StartFlexibleBlockFragment fragment) {}
    @Override public void visit(FixedFragment fragment) {}
}
