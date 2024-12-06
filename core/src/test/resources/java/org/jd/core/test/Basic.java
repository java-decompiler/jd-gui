/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.test;

import java.io.Serializable;

public class Basic implements Serializable {
    protected static final long serialVersionUID = 9506606333927794L;

    protected static long long12 = 12L;
    protected static long long34 = 34L;

    protected short short56 = 56;
    protected int   int78 = 78;
    protected int   index =
            getClass()
                    .getName()
                    .indexOf('B');

    public void printHelloWorld() {
        System.out.println("hello");
        System.out.println("world");
    }

    public void declarations(int i, long long12) {
        String str1 = "3 == " + (i+1) + " ?";
        String str2 = str1.valueOf("abc \b \f \n \r \t \" \007 def");

        char c1 = 'a';
        char c2 = '€';
        char c3 = '\'';
        char c4 = c3 = c2 = c1 = Character.toUpperCase('x');

        Class class1;
        Class class2;
        Class class3;

        class1 = class2 = class3 = String.class;

        Class class4;
        Class class5;
        Class class6;
        String[] args1;
        String[] args2;

        class4 = class5 = doSomething(class6 = String.class, args1 = args2 = new String[] { "do", "something" });

        int j = 1, k[] = { 1 }, l[][] = {{ 1 }, { 2 }, {}};
        String stringNull = null;

        System.out.println("static long12 = " + Basic.long12);
    }

    public String[] createStringArray(String s) {
        return new String[] { s, s + '?' };
    }

    protected static Class doSomething(Class clazz, String[] args) {
        return clazz;
    }

    public void instanceOf() {
        System.out.println("start");

        if (this instanceof Object) {
            System.out.println("nice !");
        }

        System.out.println("end");
    }

    public void operator(int i) {
        System.out.println("start");

        int k = 50 / (25 + (i = 789));

        k = i += 100;

        int78 = i = int78 += 456;
        int78 = 50 / (25 + (int78 = 789));

        i = ++int78;
        i = int78++;
        i *= 10;

        int78 = ++i;
        int78 = i++;
        int78 *= 10;

        long34 = ++long12;
        long34 = long12++;
        long34 *= 10;

        i = (int)long12 + int78;

        i = k ^ 0xFF;
        i |= 0x07;

        System.out.println("end");
    }

    public int scope(int i) {
        int result;

        System.out.println("start");

        if (i > 10) {
            if (i % 2 == 0) {
                result = 1;
            } else {
                int k = i;
                result = k + 2;
            }
        } else {
            result = short56;
        }

        if (i % 2 == 0) {
            return result;
        }

        long long12 = 123L;
        int  int78 = getInt78(new Object[] { this }, (short)5);

        i = (int)(Basic.long12 + long12) + (this.int78 + int78);

        System.out.println("end");

        return 0;
    }

    public String readLine() {
        StringBuffer result = new StringBuffer();
        for (;;) {
            int intRead = read();
            if (intRead == -1) {
                return result.length() == 0 ? null : result.toString();
            }
            char c = (char)intRead;
            if (c == '\n') break;
            result.append(c);
        }
        return result.toString();
    }

    public static native int read();

    protected static double pi = Math.PI;
    protected static double e = Math.E;
    protected static int int_max = Integer.MAX_VALUE;
    protected static long long_min = Long.MIN_VALUE;

    public long returnLong() {
        return System.currentTimeMillis();
    }

    public double returnDouble() {
        return Double.MAX_VALUE;
    }

    private String doSomethingWithString(String str) {
        if (str == null) {
            str = "null";
        } else {
            if (str.length() == 0) {
                return "empty";
            }
            return str + str;
        }

        return str;
    }

    public Basic getLast(Object[] objects) {
        return (objects == null) || (objects.length == 0) ? null :
            (Basic)objects[objects.length-1];
    }

    public int getInt78(Object[] objects, short index) {
        return ((Basic)objects[index]).int78;
    }

    protected final static Integer INTEGER_255 = new Integer(255);
}
