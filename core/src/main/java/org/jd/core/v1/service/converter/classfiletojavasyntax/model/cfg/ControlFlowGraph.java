/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.model.cfg;

import org.jd.core.v1.model.classfile.Method;
import org.jd.core.v1.util.DefaultList;

import java.util.HashSet;

public class ControlFlowGraph {
    protected Method method;
    protected DefaultList<BasicBlock> list = new DefaultList<BasicBlock>() {
        public BasicBlock remove(int index) {
            throw new RuntimeException("Unexpected call");
        }
    };
    protected int[] offsetToLineNumbers = null;

    public ControlFlowGraph(Method method) {
        this.method = method;
    }

    public Method getMethod() {
        return method;
    }

    public DefaultList<BasicBlock> getBasicBlocks() {
        return list;
    }

    public BasicBlock getStart() {
        return list.get(0);
    }

    public BasicBlock newBasicBlock(BasicBlock original) {
        BasicBlock basicBlock = new BasicBlock(this, list.size(), original);
        list.add(basicBlock);
        return basicBlock;
    }

    public BasicBlock newBasicBlock(int fromOffset, int toOffset) {
        return newBasicBlock(0, fromOffset, toOffset);
    }

    public BasicBlock newBasicBlock(int type, int fromOffset, int toOffset) {
        BasicBlock basicBlock = new BasicBlock(this, list.size(), type, fromOffset, toOffset, true);
        list.add(basicBlock);
        return basicBlock;
    }

    public BasicBlock newBasicBlock(int type, int fromOffset, int toOffset, boolean inverseCondition) {
        BasicBlock basicBlock = new BasicBlock(this, list.size(), type, fromOffset, toOffset, inverseCondition);
        list.add(basicBlock);
        return basicBlock;
    }

    public BasicBlock newBasicBlock(int type, int fromOffset, int toOffset, HashSet<BasicBlock> predecessors) {
        BasicBlock basicBlock = new BasicBlock(this, list.size(), type, fromOffset, toOffset, true, predecessors);
        list.add(basicBlock);
        return basicBlock;
    }

    public void setOffsetToLineNumbers(int[] offsetToLineNumbers) {
        this.offsetToLineNumbers = offsetToLineNumbers;
    }

    public int getLineNumber(int offset) {
        return (offsetToLineNumbers == null) ? 0 : offsetToLineNumbers[offset];
    }
}
