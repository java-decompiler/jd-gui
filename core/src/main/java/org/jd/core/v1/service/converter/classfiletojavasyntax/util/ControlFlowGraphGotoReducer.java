/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.util;

import org.jd.core.v1.service.converter.classfiletojavasyntax.model.cfg.BasicBlock;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.cfg.ControlFlowGraph;

import java.util.Set;

import static org.jd.core.v1.service.converter.classfiletojavasyntax.model.cfg.BasicBlock.*;

public class ControlFlowGraphGotoReducer {

    public static void reduce(ControlFlowGraph cfg) {
        for (BasicBlock basicBlock : cfg.getBasicBlocks()) {
            if (basicBlock.getType() == TYPE_GOTO) {
                BasicBlock successor = basicBlock.getNext();

                if (basicBlock == successor) {
                    basicBlock.getPredecessors().remove(basicBlock);
                    basicBlock.setType(TYPE_INFINITE_GOTO);
                } else {
                    Set<BasicBlock> successorPredecessors = successor.getPredecessors();
                    successorPredecessors.remove(basicBlock);

                    for (BasicBlock predecessor : basicBlock.getPredecessors()) {
                        predecessor.replace(basicBlock, successor);
                        successorPredecessors.add(predecessor);
                    }

                    basicBlock.setType(TYPE_DELETED);
                }
            }
        }
    }
}
