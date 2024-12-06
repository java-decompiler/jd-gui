/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.classfile.attribute;

public class CodeException {
    protected int index;
    protected int startPc;
    protected int endPc;
    protected int handlerPc;
    protected int catchType;

    public CodeException(int index, int startPc, int endPc, int handlerPc, int catchType) {
        this.index = index;
        this.startPc = startPc;
        this.endPc = endPc;
        this.handlerPc = handlerPc;
        this.catchType = catchType;
    }

    public int getStartPc() {
        return startPc;
    }

    public int getEndPc() {
        return endPc;
    }

    public int getHandlerPc() {
        return handlerPc;
    }

    public int getCatchType() {
        return catchType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CodeException that = (CodeException) o;

        if (startPc != that.startPc) return false;
        return endPc == that.endPc;
    }

    @Override
    public int hashCode() {
        return 969815374 + 31 * startPc + endPc;
    }

    @Override
    public String toString() {
        return "CodeException{index=" + index + ", startPc=" + startPc + ", endPc=" + endPc + ", handlerPc=" + handlerPc + ", catchType=" + catchType + "}";
    }
}
