/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.util;

public class DefaultStack<E> {
    protected E[] elements;
    protected int head;

    @SuppressWarnings("unchecked")
    public DefaultStack() {
        elements = (E[])new Object[16];
        head = 0;
    }

    public DefaultStack(DefaultStack<E> other) {
        elements = other.elements.clone();
        head = other.head;
    }

    public void clear() {
        head = 0;
    }

    public int size() {
        return head;
    }

    public boolean isEmpty() {
        return head == 0;
    }

    @SuppressWarnings("unchecked")
    public void copy(DefaultStack other) {
        if (elements.length < other.head) {
            elements = (E[])new Object[other.head];
        }

        System.arraycopy(other.elements, 0, elements, 0, other.head);
        head = other.head;
    }

    @SuppressWarnings("unchecked")
    public void push(E expression) {
        if (head == elements.length) {
            E[] tmp = (E[])new Object[elements.length * 2];
            System.arraycopy(elements, 0, tmp, 0, elements.length);
            elements = tmp;
        }

        elements[head++] = expression;
    }

    public E pop() {
        E e = elements[--head];
        elements[head] = null;
        return e;

        //return elements[--head];
    }

    public E peek() {
        return elements[head-1];
    }

    public void replace(E old, E nevv) {
        int i = head - 1;

        while ((i >=0) && (elements[i] == old)) {
            elements[i--] = nevv;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Stack{head=");
        sb.append(head);
        sb.append(", elements=[");

        if (head > 0) {
            sb.append(elements[0]);
            for (int i = 1; i < head; i++) {
                sb.append(", ");
                sb.append(elements[i]);
            }
        }

        sb.append("]}");

        return sb.toString();
    }
}
