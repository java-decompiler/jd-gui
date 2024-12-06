/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.test;

import java.io.Serializable;
import java.util.Enumeration;

public abstract class Layout extends Number implements Serializable, Comparable, Runnable {
    protected String str = "str";
    protected int[][] array = {
            {0, 1},
            {2, 3, 4}
    };

    protected enum Priority {
        LOW(0), HIGH(1);

        private final int level;

        Priority(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    };

    public static void main(String[] args) {
        int b = 0;
        int c = 1;

        if (args == null) {
            return;
        }

        int i = call(
                "aaaa",
                b,
                new java.util.Enumeration() {
                    public boolean hasMoreElements() {
                        return false;
                    }

                    public Object nextElement() {
                        return null;
                    }
                },
                c);

        System.out.println(i);

        if (i == 2) {
            System.out.println('2');
        } else {
            System.out.println('?');
        }

        switch (i) {
            case 0:
                System.out.println('0');
                break;
            case 1:
            case 2:
                System.out.println("1 or 2");
                break;
            default:
                System.out.println('?');
                break;
        }

        System.out.println(i);

        try {
            i = 42 / i;
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(i);

        do {
            i++;
        } while (i < 10);

        System.out.println(i);

        while (i > 5) {
            i--;
        }

        System.out.println(i);
    }

    @Override
    @Deprecated
    public int intValue() {
        return 0;
    }

    public static native int call(String s, int b, Enumeration e, int c);
}
