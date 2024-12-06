/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.layouter.util;

import org.jd.core.v1.model.fragment.EndMovableBlockFragment;
import org.jd.core.v1.model.fragment.StartMovableBlockFragment;
import org.jd.core.v1.service.layouter.visitor.AbstractSearchMovableBlockFragmentVisitor;
import org.jd.core.v1.service.layouter.visitor.AbstractStoreMovableBlockFragmentIndexVisitorAbstract;

public class VisitorsHolder {
    protected AbstractSearchMovableBlockFragmentVisitor visitor7;
    protected AbstractSearchMovableBlockFragmentVisitor visitor8;
    protected AbstractStoreMovableBlockFragmentIndexVisitorAbstract visitor9;
    protected AbstractStoreMovableBlockFragmentIndexVisitorAbstract visitor10;

    public AbstractSearchMovableBlockFragmentVisitor getForwardSearchVisitor() {
        if (visitor7 == null) {
            visitor7 = new AbstractSearchMovableBlockFragmentVisitor() {
                @Override
                public void visit(EndMovableBlockFragment fragment) {
                    depth--;
                    index++;
                }

                @Override
                public void visit(StartMovableBlockFragment fragment) {
                    depth++;
                    index++;
                }
            };
        }
        return visitor7;
    }

    public AbstractSearchMovableBlockFragmentVisitor getBackwardSearchVisitor() {
        if (visitor8 == null) {
            visitor8 = new AbstractSearchMovableBlockFragmentVisitor() {
                @Override
                public void visit(EndMovableBlockFragment fragment) {
                    depth++;
                    index++;
                }

                @Override
                public void visit(StartMovableBlockFragment fragment) {
                    depth--;
                    index++;
                }
            };
        }
        return visitor8;
    }

    public AbstractStoreMovableBlockFragmentIndexVisitorAbstract getBackwardSearchStartIndexesVisitor() {
        if (visitor9 == null) {
            visitor9 = new AbstractStoreMovableBlockFragmentIndexVisitorAbstract() {
                @Override
                public void visit(EndMovableBlockFragment fragment) {
                    if (enabled) {
                        depth++;
                        index++;
                    }
                }

                @Override
                public void visit(StartMovableBlockFragment fragment) {
                    if (enabled) {
                        if (depth == 0) {
                            enabled = false;
                        } else {
                            depth--;
                            if (depth == 0) {
                                storeIndex();
                            }
                            index++;
                        }
                    }
                }
            };
        }
        return visitor9;
    }

    public AbstractStoreMovableBlockFragmentIndexVisitorAbstract getForwardSearchEndIndexesVisitor() {
        if (visitor10 == null) {
            visitor10 = new AbstractStoreMovableBlockFragmentIndexVisitorAbstract() {
                @Override
                public void visit(EndMovableBlockFragment fragment) {
                    if (enabled) {
                        if (depth == 0) {
                            enabled = false;
                        } else {
                            depth--;
                            if (depth == 0) {
                                storeIndex();
                            }
                            index++;
                        }
                    }
                }

                @Override
                public void visit(StartMovableBlockFragment fragment) {
                    if (enabled) {
                        depth++;
                        index++;
                    }
                }
            };
        }
        return visitor10;
    }
}
