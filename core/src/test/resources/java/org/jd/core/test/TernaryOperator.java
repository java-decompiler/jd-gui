/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.test;

public class TernaryOperator {
    protected String str = "str";

    public void ternaryOperator(String s) {
        System.out.println("start");

        str =
            (s == null) ?
                "1" :
                "2";

        System.out.println("end");
    }

    public void ternaryOperatorsInTernaryOperator(String s) {
        System.out.println("start");

        s = (s==null) ? ((s==null) ? "1" : "2") : ((s==null) ? "3" : "4");

        System.out.println("end");
    }

    public boolean ternaryOperatorsInReturn(String s) {
        System.out.println("start");

        long time = System.currentTimeMillis();

        return (s == s) && (time >= time);
    }

    public void ternaryOperatorInIf1(String s1, String s2) {
        System.out.println("start");

        if ((s1 == null) ? (s2 == null) : s1.equals(s2))
            System.out.println("a");

        System.out.println("end");
    }

    public void ternaryOperatorInIfElse0(String s1, String s2) {
        System.out.println("start");

        if ((s1 != null) && (s1.length() > 0))
            System.out.println("a");
        else
            System.out.println("b");

        System.out.println("end");
    }

    public void ternaryOperatorInIfElse1(String s1, String s2) {
        System.out.println("start");

        if ((s1 == null) ? (s2 == null) : s1.equals(s2))
            System.out.println("a");
        else
            System.out.println("b");

        System.out.println("end");
    }

    public void ternaryOperatorInIfElse2(String s1, String s2) {
        System.out.println("start");

        if ((s1 == null) ? false : (s1.length() > 0))
            System.out.println("a");
        else
            System.out.println("b");

        System.out.println("end");
    }

    public void ternaryOperatorInIfElse3(String s1, String s2) {
        System.out.println("start");

        if ((s1 != null) ? (s1.length() > 0) : false)
            System.out.println("a");
        else
            System.out.println("b");

        System.out.println("end");
    }

    public void ternaryOperatorInIfElse4(String s1, String s2) {
        System.out.println("start");

        if ((s1 == null) ? s1.equals(s2) : (s2 == null))
            System.out.println("a");
        else
            System.out.println("b");

        System.out.println("end");
    }

    public void ternaryOperatorInIfElse5(String s1, String s2) {
        System.out.println("start");

        if ((s1 == null) ? (s2 == null) : (s1 + s2 == null))
            System.out.println("a");
        else
            System.out.println("b");

        System.out.println("end");
    }

    public void ternaryOperatorInIfElse6(String s1, String s2) {
        System.out.println("start");

        if ((s1 == null) ? ((s2 == null) ? (s1 != null) : (s2 != null)) : ((s1 + s2 == null) ? (s1 != null) : (s2 != null)))
            System.out.println("a");
        else
            System.out.println("b");

        System.out.println("end");
    }

    public void ternaryOperatorInIfElseFalse(String s1, String s2) {
        System.out.println("start");

        if ((s1 == null) ? false : false)
            System.out.println("a");
        else
            System.out.println("b");

        System.out.println("end");
    }

    public void ternaryOperatorInIfElseANDCondition(String s1, String s2) {
        System.out.println("start");

        if ((s1 == s2) && ((s1 == null) ? (s2 == null) : s1.equals(s2)) && (s1 == s2))
            System.out.println("a");
        else
            System.out.println("b");

        System.out.println("end");
    }

    public void ternaryOperatorInIfElseORCondition(String s1, String s2) {
        System.out.println("start");

        if ((s1 == s2) || ((s1 == null) ? (s2 == null) : s1.equals(s2)) || (s1 == s2))
            System.out.println("a");
        else
            System.out.println("b");

        System.out.println("end");
    }

    public String castTernaryOperator() {
        return Short.toString((short)((this == null) ? 1 : 2));
    }
}
