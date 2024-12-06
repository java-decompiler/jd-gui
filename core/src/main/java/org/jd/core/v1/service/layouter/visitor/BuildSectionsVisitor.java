/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.layouter.visitor;

import org.jd.core.v1.model.fragment.*;
import org.jd.core.v1.service.layouter.model.Section;
import org.jd.core.v1.util.DefaultList;

public class BuildSectionsVisitor implements FragmentVisitor {
    protected DefaultList<Section> sections = new DefaultList<>();
    protected DefaultList<FlexibleFragment> flexibleFragments = new DefaultList<>();
    protected Section previousSection = null;

    @Override public void visit(FlexibleFragment fragment) { flexibleFragments.add(fragment); }
    @Override public void visit(EndFlexibleBlockFragment fragment) { flexibleFragments.add(fragment); }
    @Override public void visit(EndMovableBlockFragment fragment) { flexibleFragments.add(fragment); }
    @Override public void visit(SpacerBetweenMovableBlocksFragment fragment) { flexibleFragments.add(fragment); }
    @Override public void visit(StartFlexibleBlockFragment fragment) { flexibleFragments.add(fragment); }
    @Override public void visit(StartMovableBlockFragment fragment) { flexibleFragments.add(fragment); }

    @Override
    public void visit(FixedFragment fragment) {
        sections.add(previousSection = new Section(flexibleFragments, fragment, previousSection));
        flexibleFragments = new DefaultList<>();
    }

    public DefaultList<Section> getSections() {
        sections.add(new Section(flexibleFragments, null, previousSection));
        return sections;
    }
}