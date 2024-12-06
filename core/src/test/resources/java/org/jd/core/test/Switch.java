/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.test;

public class Switch {

    public void simpleSwitch(int i) {
        System.out.println("start");

        switch (i) {
            case 0:
                System.out.println("0");
                break;
            case 1:
                System.out.println("1");
                break;
            case 3:
                System.out.println("3");
                break;
        }

        System.out.println("end");
    }

    public void switchFirstBreakMissing(int i) {
        System.out.println("start");

        switch (i) {
            case 0:
                System.out.println("0");
            case 1:
                System.out.println("1");
                break;
            case 3:
                System.out.println("3");
                break;
        }

        System.out.println("end");
    }

    public void switchSecondBreakMissing(int i) {
        System.out.println("start");

        switch (i) {
            case 0:
                System.out.println("0");
                break;
            case 1:
                System.out.println("1");
            default:
                System.out.println("3");
                break;
        }

        System.out.println("end");
    }

    public void switchDefault(int i) {
        System.out.println("start");

        switch (i) {
            case 0:
                System.out.println("0");
            case 1:
                System.out.println("1");
                break;
            case 3:
                System.out.println("3");
                break;
            default:
                System.out.println("?");
                break;
        }

        System.out.println("end");
    }

    public void lookupSwitchDefault(int i) {
        System.out.println("start");

        switch (i) {
            case 0:
                System.out.println("0");
            case 1:
                System.out.println("1");
                break;
            case 30:
                System.out.println("30");
                break;
            default:
                System.out.println("?");
                break;
        }

        System.out.println("end");
    }

    public void switchOneExitInFirstCase(int i) {
        System.out.println("start");

        switch (i) {
            case 0:
                System.out.println("0");
                break;
            case 1:
                System.out.println("1");
                throw new RuntimeException("boom");
            default:
                System.out.println("3");
                return;
        }

        System.out.println("end");
    }

    public void switchOneExitInSecondCase(int i) {
        System.out.println("start");

        switch (i) {
            case 0:
                System.out.println("0");
                return;
            case 1:
                System.out.println("1");
                break;
            default:
                System.out.println("3");
                return;
        }

        System.out.println("end");
    }

    public void switchOneExitInLastCase(int i) {
        System.out.println("start");

        switch (i) {
            case 0:
                System.out.println("0");
                return;
            case 1:
                System.out.println("1");
                return;
            default:
                System.out.println("3");
                break;
        }

        System.out.println("end");
    }

    public void testSwitch() {
        System.out.println("start");

        switch ((int) (System.currentTimeMillis() & 0xF)) {
            case 0:
                System.out.println("0");
            case 1:
                System.out.println("0 or 1");
                break;
            case 2:
            default:
                System.out.println("2 or >= 9");
                break;
            case 3:
            case 4:
                System.out.println("3 or 4");
                break;
            case 5:
                System.out.println("5");
                break;
            case 6:
                System.out.println("6");
                break;
            case 7:
                System.out.println("7");
                break;
            case 8:
                System.out.println("8");
                break;
        }

        System.out.println("end");
    }

    public void complexSwitch(int i) {
        System.out.println("start");

        switch (i % 4) {
            case 1:
                System.out.println("1");
                switch (i * 2) {
                    case 2:
                        System.out.println("1.2");
                        break;
                    case 10:
                        System.out.println("1.10");
                    default:
                        System.out.println("1.?");
                }
                break;
            case 2:
                switch (i * 3) {
                    case 6:
                        System.out.println("2.6");
                    case 18:
                        System.out.println("2.18");
                    default:
                        System.out.println("2.?");
                }
                System.out.println("2");
            default:
                switch (i * 7) {
                    case 0:
                        System.out.println("?.0");
                        break;
                    case 7:
                        System.out.println("?.7");
                    default:
                        System.out.println("?.?");
                }
                System.out.println("?");
        }

        System.out.println("end");
    }

    public void emptySwitch(int i) {
        System.out.println("start");

        switch (i) {}

        System.out.println("end");
    }

    public void switchOnLastPosition(int i) {
        System.out.println("start");

        switch (i) {
            case 0:
                System.out.println("0");
                break;
            case 1:
                System.out.println("1");
                break;
            case 3:
                System.out.println("3");
                break;
        }
    }

    public void switchBreakDefault(int i) {
        System.out.println("start");

        switch (i) {
            case 0:
                System.out.println("0");
                break;
            case 1:
                break;
            default:
                System.out.println("default");
                break;
        }

        System.out.println("end");
    }

    public void switchBreakBreakDefault(int i) {
        System.out.println("start");

        switch (i) {
            case 0:
                System.out.println("0");
                break;
            case 1:
            case 2:
            case 3:
                break;
            case 4:
            case 5:
            default:
                System.out.println("default");
                break;
        }

        System.out.println("end");
    }

    public void switchFirstIfBreakMissing(int i) {
        System.out.println("start");

        switch (i) {
            case 0:
                System.out.println("0");
                if (i != 1)
                    break;
            case 1:
                System.out.println("1");
                break;
            case 3:
                System.out.println("3");
                break;
        }

        System.out.println("end");
    }
}
