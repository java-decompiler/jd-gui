/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.model.cfg;

import java.util.HashSet;
import java.util.Iterator;

public class Loop {
    protected BasicBlock start;
    protected HashSet<BasicBlock> members;
    protected BasicBlock end;

    public Loop(BasicBlock start, HashSet<BasicBlock> members, BasicBlock end) {
        this.start = start;
        this.members = members;
        this.end = end;
    }

    public BasicBlock getStart() {
        return start;
    }

    public void setStart(BasicBlock start) {
        this.start = start;
    }

    public HashSet<BasicBlock> getMembers() {
        return members;
    }

    public BasicBlock getEnd() {
        return end;
    }

    public void setEnd(BasicBlock end) {
        this.end = end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Loop loop = (Loop) o;

        if (!start.equals(loop.start)) return false;
        if (!members.equals(loop.members)) return false;
        return !(end != null ? !end.equals(loop.end) : loop.end != null);

    }

    @Override
    public int hashCode() {
        int result = 258190310 + start.hashCode();
        result = 31 * result + members.hashCode();
        result = 31 * result + (end != null ? end.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        String str = "Loop{start=" + start.getIndex() + ", members=[";

        if ((members != null) && (!members.isEmpty())) {
            Iterator<BasicBlock> iterator = members.iterator();
            str += iterator.next().getIndex();

            while (iterator.hasNext())
                str += ", " + iterator.next().getIndex();
        }

        return str + "], end=" + (end ==null ? "" : end.getIndex()) + "}";
    }
}
