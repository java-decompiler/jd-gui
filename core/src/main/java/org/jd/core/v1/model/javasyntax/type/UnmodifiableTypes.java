/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.type;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class UnmodifiableTypes extends Types {
    public UnmodifiableTypes() {}

    public UnmodifiableTypes(int capacity) {
        super(capacity);
    }

    public UnmodifiableTypes(Collection<Type> collection) {
        super(collection);
    }

    @SuppressWarnings("unchecked")
    public UnmodifiableTypes(Type type, Type... types) {
        super(type, types);
        assert (types != null) && (types.length > 0) : "Uses 'Type' implementation instead";
    }

    @Override
    public Type removeFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Type removeLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Type remove(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void removeRange(int i, int i1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<Type> listIterator(int i) {
        return new UnmodifiableTypesListIterator(super.listIterator(i));
    }

    @Override
    public ListIterator<Type> listIterator() {
        return new UnmodifiableTypesListIterator(super.listIterator());
    }

    @Override
    public Type set(int i, Type type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeIf(Predicate<? super Type> predicate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void replaceAll(UnaryOperator<Type> unaryOperator) {
        throw new UnsupportedOperationException();
    }


    // --- ListIterator --- //
    private class UnmodifiableTypesListIterator implements ListIterator<Type> {
        protected ListIterator<Type> listIterator;

        public UnmodifiableTypesListIterator(ListIterator<Type> listIterator) {
            this.listIterator = listIterator;
        }

        @Override public int nextIndex() { return listIterator.nextIndex(); }
        @Override public int previousIndex() { return listIterator.previousIndex(); }
        @Override public boolean hasNext() { return listIterator.hasNext(); }
        @Override public boolean hasPrevious() { return listIterator.hasPrevious(); }
        @Override public Type next() { return listIterator.next(); }
        @Override public Type previous() { return listIterator.previous(); }

        @Override public void set(Type element) { throw new UnsupportedOperationException(); }
        @Override public void add(Type element) { throw new UnsupportedOperationException(); }
        @Override public void remove() { throw new UnsupportedOperationException(); }
    }
}
