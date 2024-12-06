/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.layouter.model;

import org.jd.core.v1.model.fragment.FixedFragment;
import org.jd.core.v1.model.fragment.FlexibleFragment;
import org.jd.core.v1.service.layouter.util.VisitorsHolder;
import org.jd.core.v1.service.layouter.visitor.AbstractSearchMovableBlockFragmentVisitor;
import org.jd.core.v1.service.layouter.visitor.AbstractStoreMovableBlockFragmentIndexVisitorAbstract;
import org.jd.core.v1.util.DefaultList;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class Section {
    protected final DefaultList<FlexibleFragment> flexibleFragments;
    protected final FixedFragment fixedFragment;
    protected final Section previousSection;
    protected       Section nextSection;
    protected final int targetLineCount;
    protected       int rate = 0;

    // Uses by "layout" method
    protected int lastLineCount = -1;
    protected int delta;

    public Section(DefaultList<FlexibleFragment> flexibleFragments, FixedFragment fixedFragment, Section previousSection) {
        this.flexibleFragments = flexibleFragments;
        this.fixedFragment = fixedFragment;
        this.previousSection = previousSection;

        assert (flexibleFragments != null) && (flexibleFragments.size() > 0) : "Section must contain flexible fragments";

        int previousLineNumber;

        if (previousSection == null) {
            previousLineNumber = 1;
        } else {
            previousSection.nextSection = this;
            previousLineNumber = previousSection.fixedFragment.getLastLineNumber();
        }

        if (fixedFragment == null) {
            this.targetLineCount = 0;
        } else {
            this.targetLineCount = fixedFragment.getFirstLineNumber() - previousLineNumber;
        }
    }

    public DefaultList<FlexibleFragment> getFlexibleFragments() { return flexibleFragments; }
    public FixedFragment getFixedFragment() { return fixedFragment; }
    public Section getPreviousSection() { return previousSection; }
    public Section getNextSection() { return nextSection; }
    public int getRate() { return rate; }

    public void updateRate() {
        rate = 0;

        for (FlexibleFragment flexibleFragment : flexibleFragments) {
            if (flexibleFragment.getInitialLineCount() > flexibleFragment.getLineCount()) {
                rate += flexibleFragment.getInitialLineCount() - flexibleFragment.getLineCount();
            }
        }
    }

    /**
     * @return true if a fragment has changed
     */
    public boolean layout(boolean force) {
        // Skip layout of last section
        if (fixedFragment != null) {
            // Compute line count
            int currentLineCount = 0;

            for (FlexibleFragment flexibleFragment : flexibleFragments) {
                currentLineCount += flexibleFragment.getLineCount();
            }

            // Do not re-layout if nothing has changed
            if (force || (lastLineCount != currentLineCount)) {
                lastLineCount = currentLineCount;

                if (targetLineCount != currentLineCount) {
                    AutoGrowthList filteredFlexibleFragments = new AutoGrowthList();
                    DefaultList<FlexibleFragment> constrainedFlexibleFragments = new DefaultList<>(flexibleFragments.size());

                    if (targetLineCount > currentLineCount) {
                        // Expands fragments
                        int oldDelta = delta = targetLineCount - currentLineCount;

                        for (FlexibleFragment flexibleFragment : flexibleFragments) {
                            if (flexibleFragment.getLineCount() < flexibleFragment.getMaximalLineCount()) {
                                // Keep only expandable fragments
                                filteredFlexibleFragments.get(flexibleFragment.getWeight()).add(flexibleFragment);
                            }
                        }

                        // First, expand compacted fragments
                        for (DefaultList<FlexibleFragment> flexibleFragments : filteredFlexibleFragments) {
                            constrainedFlexibleFragments.clear();

                            for (FlexibleFragment flexibleFragment : flexibleFragments) {
                                if (flexibleFragment.getLineCount() < flexibleFragment.getInitialLineCount()) {
                                    // Store compacted flexibleFragments
                                    constrainedFlexibleFragments.add(flexibleFragment);
                                }
                            }

                            expand(constrainedFlexibleFragments, force);
                            if (delta == 0) break;
                        }

                        // Next, expand all
                        if (delta > 0) {
                            for (DefaultList<FlexibleFragment> flexibleFragments : filteredFlexibleFragments) {
                                expand(flexibleFragments, force);
                                if (delta == 0) break;
                            }
                        }

                        // Something changed ?
                        return oldDelta != delta;
                    } else {
                        // Compacts fragments
                        int oldDelta = delta = currentLineCount - targetLineCount;

                        for (FlexibleFragment flexibleFragment : flexibleFragments) {
                            if (flexibleFragment.getMinimalLineCount() < flexibleFragment.getLineCount()) {
                                // Keep only compactable fragments
                                filteredFlexibleFragments.get(flexibleFragment.getWeight()).add(flexibleFragment);
                            }
                        }

                        // First, compact expanded fragments
                        for (DefaultList<FlexibleFragment> flexibleFragments : filteredFlexibleFragments) {
                            constrainedFlexibleFragments.clear();

                            for (FlexibleFragment flexibleFragment : flexibleFragments) {
                                if (flexibleFragment.getLineCount() > flexibleFragment.getInitialLineCount()) {
                                    // Store expanded flexibleFragments
                                    constrainedFlexibleFragments.add(flexibleFragment);
                                }
                            }

                            compact(constrainedFlexibleFragments, force);
                            if (delta == 0) break;
                        }

                        // Next, compact all
                        if (delta > 0) {
                            for (DefaultList<FlexibleFragment> flexibleFragments : filteredFlexibleFragments) {
                                compact(flexibleFragments, force);
                                if (delta == 0) break;
                            }
                        }

                        // Something changed ?
                        return oldDelta != delta;
                    }
                }
            }
        }

        return false;
    }

    protected void expand(DefaultList<FlexibleFragment> flexibleFragments, boolean force) {
        int oldDelta = Integer.MAX_VALUE;

        while ((delta > 0) && (delta < oldDelta)) {
            oldDelta = delta;

            for (FlexibleFragment flexibleFragment : flexibleFragments) {
                if (flexibleFragment.incLineCount(force)) {
                    if (--delta == 0) {
                        break;
                    }
                }
            }
        }
    }

    protected void compact(DefaultList<FlexibleFragment> flexibleFragments, boolean force) {
        int oldDelta = Integer.MAX_VALUE;

        while ((delta > 0) && (delta < oldDelta)) {
            oldDelta = delta;

            for (FlexibleFragment flexibleFragment : flexibleFragments) {
                if (flexibleFragment.decLineCount(force)) {
                    if (--delta == 0) {
                        break;
                    }
                }
            }
        }
    }

    public boolean releaseConstraints(VisitorsHolder holder) {
        int flexibleCount = flexibleFragments.size();
        AbstractStoreMovableBlockFragmentIndexVisitorAbstract backwardSearchStartIndexesVisitor = holder.getBackwardSearchStartIndexesVisitor();
        AbstractStoreMovableBlockFragmentIndexVisitorAbstract forwardSearchEndIndexesVisitor = holder.getForwardSearchEndIndexesVisitor();
        AbstractSearchMovableBlockFragmentVisitor forwardSearchVisitor = holder.getForwardSearchVisitor();
        AbstractSearchMovableBlockFragmentVisitor backwardSearchVisitor = holder.getBackwardSearchVisitor();
        ListIterator<FlexibleFragment> iterator = flexibleFragments.listIterator(flexibleCount);

        backwardSearchStartIndexesVisitor.reset();
        forwardSearchEndIndexesVisitor.reset();

        while (iterator.hasPrevious() && backwardSearchStartIndexesVisitor.isEnabled()) {
            iterator.previous().accept(backwardSearchStartIndexesVisitor);
        }

        for (FlexibleFragment flexibleFragment : flexibleFragments) {
            flexibleFragment.accept(forwardSearchEndIndexesVisitor);
            if (! forwardSearchEndIndexesVisitor.isEnabled())
                break;
        }

        int size = backwardSearchStartIndexesVisitor.getSize();
        Section nextSection = searchNextSection(forwardSearchVisitor);

        if ((size > 1) && (nextSection != null)) {
            int index1 = flexibleCount - 1 - backwardSearchStartIndexesVisitor.getIndex(size/2);
            int index2 = flexibleCount - 1 - backwardSearchStartIndexesVisitor.getIndex(0);
            int nextIndex = forwardSearchVisitor.getIndex();

            size = forwardSearchEndIndexesVisitor.getSize();

            if (size > 1) {
                int index3 = forwardSearchEndIndexesVisitor.getIndex(0) + 1;
                int index4 = forwardSearchEndIndexesVisitor.getIndex(size/2) + 1;
                Section previousSection = searchPreviousSection(backwardSearchVisitor);

                if (nextSection.getRate() > previousSection.getRate()) {
                    int index = previousSection.getFlexibleFragments().size() - backwardSearchVisitor.getIndex();
                    previousSection.addFragmentsAtEnd(holder, index, extract(index3, index4));
                } else {
                    nextSection.addFragmentsAtBeginning(holder, nextIndex, extract(index1, index2));
                }
            } else {
                nextSection.addFragmentsAtBeginning(holder, nextIndex, extract(index1, index2));
            }

            return true;
        } else {
            size = forwardSearchEndIndexesVisitor.getSize();

            if (size > 1) {
                int index3 = forwardSearchEndIndexesVisitor.getIndex(0) + 1;
                int index4 = forwardSearchEndIndexesVisitor.getIndex(size/2) + 1;
                Section previousSection = searchPreviousSection(backwardSearchVisitor);

                if ((size > 1) && (previousSection != null)) {
                    int index = previousSection.getFlexibleFragments().size() - backwardSearchVisitor.getIndex();
                    previousSection.addFragmentsAtEnd(holder, index, extract(index3, index4));
                    return true;
                }
            }
        }

        return false;
    }

    protected Section searchNextSection(AbstractSearchMovableBlockFragmentVisitor visitor) {
        Section section = getNextSection();

        visitor.reset();

        while (section != null) {
            visitor.resetIndex();

            for (FlexibleFragment flexibleFragment : section.getFlexibleFragments()) {
                flexibleFragment.accept(visitor);
                if (visitor.getDepth() == 0)
                    return section;
            }

            section = section.getNextSection();
        }

        return null;
    }

    protected Section searchPreviousSection(AbstractSearchMovableBlockFragmentVisitor visitor) {
        Section section = getPreviousSection();

        visitor.reset();

        while (section != null) {
            DefaultList<FlexibleFragment> flexibleFragments = section.getFlexibleFragments();
            ListIterator<FlexibleFragment> iterator = flexibleFragments.listIterator(flexibleFragments.size());

            visitor.resetIndex();

            while (iterator.hasPrevious()) {
                iterator.previous().accept(visitor);
                if (visitor.getDepth() == 0)
                    return section;
            }

            section = section.getPreviousSection();
        }

        return null;
    }

    protected void addFragmentsAtBeginning(VisitorsHolder holder, int index, List<FlexibleFragment> flexibleFragments) {
        AbstractSearchMovableBlockFragmentVisitor visitor = holder.getForwardSearchVisitor();
        ListIterator<FlexibleFragment> iterator = flexibleFragments.listIterator(flexibleFragments.size());

        // Extract separators
        visitor.reset();

        while (iterator.hasPrevious()) {
            iterator.previous().accept(visitor);
            if (visitor.getDepth() == 0)
                break;
        }

        assert (visitor.getIndex() < flexibleFragments.size()) && (visitor.getIndex() > 1);

        int index1 = flexibleFragments.size() + 1 - visitor.getIndex();

        // Insert other fragments
        this.flexibleFragments.addAll(index, flexibleFragments.subList(0, index1));
        // Insert separator at beginning

        this.flexibleFragments.addAll(index, flexibleFragments.subList(index1, flexibleFragments.size()));

        resetLineCount();
    }

    protected void addFragmentsAtEnd(VisitorsHolder holder, int index, List<FlexibleFragment> flexibleFragments) {
        AbstractSearchMovableBlockFragmentVisitor visitor = holder.getForwardSearchVisitor();

        // Extract separators
        visitor.reset();

        for (FlexibleFragment flexibleFragment : flexibleFragments) {
            flexibleFragment.accept(visitor);
            if (visitor.getDepth() == 2)
                break;
        }

        assert (visitor.getIndex() < flexibleFragments.size()) && (visitor.getIndex() > 1);

        int index1 = visitor.getIndex() - 1;

        // Insert other fragments
        this.flexibleFragments.addAll(index, flexibleFragments.subList(0, index1));
        // Insert separator at end
        this.flexibleFragments.addAll(index, flexibleFragments.subList(index1, flexibleFragments.size()));

        resetLineCount();
    }

    protected List<FlexibleFragment> extract(int index1, int index2) {
        resetLineCount();

        List<FlexibleFragment> subList = flexibleFragments.subList(index1, index2);
        List<FlexibleFragment> fragmentsToMove = new DefaultList<>(subList);

        subList.clear();

        return fragmentsToMove;
    }

    protected void resetLineCount() {
        for (FlexibleFragment flexibleFragment : flexibleFragments) {
            flexibleFragment.resetLineCount();
        }
    }

    @Override
    public String toString() {
        return "Section{flexibleFragments.size=" + flexibleFragments.size() + ", fixedFragment.firstLineNumber=" + (fixedFragment ==null ? "undefined" : fixedFragment.getFirstLineNumber()) + ", rate=" + rate + "}";
    }

    @SuppressWarnings("unchecked")
    protected class AutoGrowthList implements Iterable<DefaultList<FlexibleFragment>>, Iterator<DefaultList<FlexibleFragment>> {
        protected DefaultList<FlexibleFragment>[] elements = new DefaultList[21];
        protected int iteratorIndex;

        public void set(int index, DefaultList<FlexibleFragment> element) {
            ensureCapacity(index);
            elements[index] = element;
        }

        public DefaultList<FlexibleFragment> get(int index) {
            ensureCapacity(index);

            DefaultList<FlexibleFragment> element = elements[index];

            if (element == null) {
                elements[index] = element = new DefaultList<>(flexibleFragments.size());
            }

            return element;
        }

        public void reverse() {
            for (int index1=0, index2=elements.length-1; index1<index2; index1++, index2--) {
                DefaultList<FlexibleFragment> tmp = elements[index1];
                elements[index1] = elements[index2];
                elements[index2] = tmp;
            }
        }

        protected void ensureCapacity(int minCapacity) {
            if (elements.length <= minCapacity) {
                DefaultList<FlexibleFragment>[] tmp = new DefaultList[minCapacity + 10];
                System.arraycopy(elements, 0, tmp, 0, elements.length);
                elements = tmp;
            }
        }

        @Override
        public Iterator<DefaultList<FlexibleFragment>> iterator() {
            int length = elements.length;

            iteratorIndex = 0;

            while ((iteratorIndex < length) && (elements[iteratorIndex] == null)) {
                iteratorIndex++;
            }

            return this;
        }

        @Override
        public boolean hasNext() {
            return iteratorIndex < elements.length;
        }

        @Override
        public DefaultList<FlexibleFragment> next() {
            DefaultList<FlexibleFragment> element = elements[iteratorIndex++];
            int length = elements.length;

            while ((iteratorIndex < length) && (elements[iteratorIndex] == null)) {
                iteratorIndex++;
            }

            return element;
        }

        @Override
        public void remove() {}
    }
}
