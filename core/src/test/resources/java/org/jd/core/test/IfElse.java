/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.test;

public class IfElse {

    public void if_() {
        System.out.println("start");

        if (this == null) {
            System.out.println("what? 'this' is null?");
        }

        System.out.println("end");
    }

    public void ifCallBoolean() {
        System.out.println("start");

        if ("abc".isEmpty() && "abc".isEmpty()) {
            System.out.println("what? 'abc' is empty?");
        }

        System.out.println("end");
    }

    public void ifElse() {
        System.out.println("start");

        if (this == null) {
            System.out.println("what? 'this' is null?");
        } else {
            System.out.println("whew!");
        }

        System.out.println("end");
    }

    public void ifElseIfElse() {
        System.out.println("start");

        if (this == null) {
            System.out.println("what? 'this' is null?");
        } else if (this == null) {
            System.out.println("how this message can be written?");
        } else {
            System.out.println("whew!");
        }

        System.out.println("end");
    }

    public void ifIf(int i) {
        System.out.println("start");

        if (i == 0) {

            if (i == 1) {
                System.out.println("0");
            }
        }

        System.out.println("end");
    }

    public void methodCallInIfCondition(int i) {
        System.out.println("start");

        if (i == System.currentTimeMillis()) {
            System.out.println("==");
        } else if (i != System.currentTimeMillis()) {
            System.out.println("!=");
        } else if (i > System.currentTimeMillis()) {
            System.out.println(">");
        } else if (i < System.currentTimeMillis()) {
            System.out.println("<");
        } else if (i >= System.currentTimeMillis()) {
            System.out.println(">=");
        } else if (i <= System.currentTimeMillis()) {
            System.out.println("<=");
        }

        System.out.println("end");
    }

    public void ifORCondition(int i) {
        System.out.println("start");

        if (i==4 || i==5 || i==6) {
            System.out.println("a");
        }

        System.out.println("end");
    }

    public void ifANDCondition(int i) {
        System.out.println("start");

        if (i==4 && i==5 && i==6) {
            System.out.println("a");
        }

        System.out.println("end");
    }

    public void ifElseORCondition(int i) {
        System.out.println("start");

        if (i==4 || i==5 || i==6) {
            System.out.println("a");
        } else {
            System.out.println("b");
        }

        System.out.println("end");
    }

    public void ifElseANDCondition(int i) {
        System.out.println("start");

        if (i==4 && i==5 && i==6) {
            System.out.println("a");
        } else {
            System.out.println("b");
        }

        System.out.println("end");
    }

    public void ANDAndORConditions(int i) {
        System.out.println("start");

        if ((i == 3) || (i == 5) || (i == 6))
            System.out.println("a");
        else if ((i != 4) && (i > 7) && (i > 8))
            System.out.println("b");
        else
            System.out.println("c");

        System.out.println("end");
    }

    public void ifElse6ANDAnd2ORCondition(int i) {
        System.out.println("start");

        if ((i==1 && i==2 && i==3) || (i==4 && i==5 && i==6) || (i==7 && i==8 && i==9)) {
            System.out.println("a");
        } else {
            System.out.println("b");
        }

        System.out.println("end");
    }

    public void ifElse6ORAnd2ANDCondition(int i) {
        System.out.println("start");

        if ((i==1 || i==2 || i==3) && (i==4 || i==5 || i==6) && (i==7 || i==8 || i==9)) {
            System.out.println("a");
        } else {
            System.out.println("b");
        }

        System.out.println("end");
    }

    public void ifElseORAndANDConditions(int i) {
        System.out.println("start");

        if ((i==1 || (i==5 && i==6 && i==7) || i==8 || (i==9 && i==10 && i==11)) && (i==4 || (i%200) > 50) && (i>3 || i>4)) {
            System.out.println("a");
        } else {
            System.out.println("b");
        }

        System.out.println("end");
    }

    public void ifElseANDAndORConditions(int i) {
        System.out.println("start");

        if ((i==1 && (i==5 || i==6 || i==7) && i==8 && (i==9 || i==10 || i==11)) || (i==4 && (i%200) > 50) || (i>3 && i>4)) {
            System.out.println("a");
        } else {
            System.out.println("b");
        }

        System.out.println("end");
    }

    public void ifThrow() {
        System.out.println("start");

        if (this == null)
            throw null;

        System.out.println("end");
    }
}
