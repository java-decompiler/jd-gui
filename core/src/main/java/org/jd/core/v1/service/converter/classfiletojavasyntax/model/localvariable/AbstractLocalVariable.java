/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.model.localvariable;

import org.jd.core.v1.model.javasyntax.type.BaseType;
import org.jd.core.v1.model.javasyntax.type.Type;
import org.jd.core.v1.util.DefaultList;

import java.util.HashSet;
import java.util.Map;

public abstract class AbstractLocalVariable {
    protected Frame frame;
    protected AbstractLocalVariable next;
    protected boolean declared;
    protected int index;
    protected int fromOffset;
    protected int toOffset;
    protected String name;
    protected DefaultList<LocalVariableReference> references = new DefaultList<>();
    protected HashSet<AbstractLocalVariable> variablesOnRight = null;
    protected HashSet<AbstractLocalVariable> variablesOnLeft = null;

    public AbstractLocalVariable(int index, int offset, String name) {
        this(index, offset, name, (offset == 0));
    }

    public AbstractLocalVariable(int index, int offset, String name, boolean declared) {
        this.declared = declared;
        this.index = index;
        this.fromOffset = offset;
        this.toOffset = offset;
        this.name = name;
    }

    public Frame getFrame() { return frame; }
    public void setFrame(Frame frame) { this.frame = frame; }

    public AbstractLocalVariable getNext() { return next; }
    public void setNext(AbstractLocalVariable next) { this.next = next; }

    public boolean isDeclared() { return declared; }
    public void setDeclared(boolean declared) { this.declared = declared; }

    public int getIndex() { return index; }

    public int getFromOffset() { return fromOffset; }

    public void setFromOffset(int fromOffset) {
        assert fromOffset <= toOffset;
        this.fromOffset = fromOffset;
    }

    public int getToOffset() { return toOffset; }

    public void setToOffset(int offset) {
        if (this.fromOffset > offset)
            this.fromOffset = offset;
        if (this.toOffset < offset)
            this.toOffset = offset;
    }

    public void setToOffset(int offset, boolean force) {
        this.toOffset = offset;
    }

    public abstract Type getType();

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public abstract int getDimension();

    public abstract void accept(LocalVariableVisitor visitor);

    public DefaultList<LocalVariableReference> getReferences() { return references; }
    public void addReference(LocalVariableReference reference) { references.add(reference); }

    /**
     * Determines if the local variable represented by this object is either the same as, or is a super type variable
     * of, the local variable represented by the specified parameter.
     */
    public abstract boolean isAssignableFrom(Map<String, BaseType> typeBounds, Type type);
    public abstract void typeOnRight(Map<String, BaseType> typeBounds, Type type);
    public abstract void typeOnLeft(Map<String, BaseType> typeBounds, Type type);

    public abstract boolean isAssignableFrom(Map<String, BaseType> typeBounds, AbstractLocalVariable variable);
    public abstract void variableOnRight(Map<String, BaseType> typeBounds, AbstractLocalVariable variable);
    public abstract void variableOnLeft(Map<String, BaseType> typeBounds, AbstractLocalVariable variable);

    protected void fireChangeEvent(Map<String, BaseType> typeBounds) {
        if (variablesOnLeft != null) {
            for (AbstractLocalVariable v : variablesOnLeft) {
                v.variableOnRight(typeBounds, this);
            }
        }
        if (variablesOnRight != null) {
            for (AbstractLocalVariable v : variablesOnRight) {
                v.variableOnLeft(typeBounds, this);
            }
        }
    }

    protected void addVariableOnLeft(AbstractLocalVariable variable) {
        if (variablesOnLeft == null) {
            variablesOnLeft = new HashSet<>();
            variablesOnLeft.add(variable);
            variable.addVariableOnRight(this);
        } else if (!variablesOnLeft.contains(variable)) {
            variablesOnLeft.add(variable);
            variable.addVariableOnRight(this);
        }
    }

    protected void addVariableOnRight(AbstractLocalVariable variable) {
        if (variablesOnRight == null) {
            variablesOnRight = new HashSet<>();
            variablesOnRight.add(variable);
            variable.addVariableOnLeft(this);
        } else if (!variablesOnRight.contains(variable)) {
            variablesOnRight.add(variable);
            variable.addVariableOnLeft(this);
        }
    }

    public boolean isPrimitiveLocalVariable() { return false; }
}
