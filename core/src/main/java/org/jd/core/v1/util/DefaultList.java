/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

@SuppressWarnings("unchecked")
public class DefaultList<E> extends ArrayList<E> {
    protected static final EmptyList EMPTY_LIST = new EmptyList();

    public DefaultList() {}

    public DefaultList(int capacity) {
        super(capacity);
    }

    public DefaultList(Collection<E> collection) {
        super(collection);
    }

    public DefaultList(E element, E... elements) {
        ensureCapacity(elements.length + 1);

        add(element);

        for (E e : elements) {
            add(e);
        }
    }

    public DefaultList(E[] elements) {
        if ((elements != null) && (elements.length > 0)) {
            ensureCapacity(elements.length);

            for (E e : elements) {
                add(e);
            }
        }
    }

    public E getFirst() {
        return (E)get(0);
    }

    public E getLast() {
        return (E)get(size()-1);
    }

    public E removeFirst() {
        return (E)remove(0);
    }

    public E removeLast() {
        return (E)remove(size()-1);
    }

    public boolean isList() {
        return true;
    }

    public DefaultList<E> getList() {
        return this;
    }

    public static <T> DefaultList<T> emptyList() {
        return EMPTY_LIST;
    }

    protected static class EmptyList<E> extends DefaultList<E> implements Iterator<E> {
        public EmptyList() { super(0); }

        public E set(int index, E e) {
            throw new UnsupportedOperationException();
        }
        public void add(int index, E e) {
            throw new UnsupportedOperationException();
        }
        public E remove(int index) {
            throw new UnsupportedOperationException();
        }
        public Iterator iterator() { return this; }

        public boolean hasNext() { return false; }
        public E next() { throw new NoSuchElementException(); }
        public void remove() { throw new UnsupportedOperationException(); }
    }
}
