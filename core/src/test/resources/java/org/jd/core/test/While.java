/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.test;

public class While {

    public void simpleWhile(int i) {
        System.out.println("start");

        while (i-- > 0) {
            System.out.println("loop");
        }

        System.out.println("end");
    }

    public void whileTry(int i, Object o) {
        while (i<10) {
            System.out.println("a");
            try
            {
                System.out.println("b");
            }
            catch (RuntimeException e)
            {
                System.out.println("c");
            }
        }
    }

    public void whileTryThrow(int i0) throws Exception {
        int $i1 = 1;

        if ($i1 == 2)
            throw new Exception("2");

        while (i0 > 20)
        {
            try
            {
                $i1 = i0;
                i0 = i0 -1;
                System.out.println($i1);

                if ($i1 == 3)
                    throw new Exception("3");
            }
            catch (RuntimeException $r3)
            {
                System.out.println("RuntimeException caught: " + $r3);
            }
        }

        return;
    }

    public void loop3() {
        int i = 0;

        while (i<5)
        {
            System.out.println(i);
            ++i;
        }
    }

    public void whileThrow() {
        System.out.println("a");
        int i = 0;

        while (i < 10)
        {
            System.out.println("b");
            i++;

            if (i == 3)
                throw new RuntimeException();
        }

        System.out.println("c");
    }

    public void whileReturn() {
        System.out.println("a");
        int i = 0;

        while (i < 10)
        {
            System.out.println("b");
            i++;

            if (i == 3)
                return;
        }

        System.out.println("c");
    }

    public void whileIfContinue() {
        System.out.println("a");
        int i = 0;

        while (i > 0)
        {
            System.out.println("b");
            i++;
            if (i > 5)
                continue;
            System.out.println("bb");
        }
        System.out.println("c");
    }

    public void whileIfBreak() {
        System.out.println("a");
        int i = 0;

        while (i > 0)
        {
            System.out.println("b");
            i++;
            if (i > 8)
                break;
            System.out.println("bb");
        }
        System.out.println("c");
    }

    public void whileIfContinueBreak() {
        System.out.println("a");
        int i = 0;

        while (i > 0)
        {
            System.out.println("b");
            i++;
            if (i > 5)
                continue;
            if (i > 8)
                break;
            if (i > 10)
                break;
            System.out.println("bb");
        }
        System.out.println("c");
    }

    public void whileTrue() {
        System.out.println("a");
        int i = 0;

        while (true)
        {
            System.out.println("b");
            i++;
            if (i > 5)
                continue;
            if (i > 8)
                break;
            if (i > 10)
                break;
            System.out.println("bb");
        }

        System.out.println("c");
    }

    public void whileTrueIf() {
        int i=0;

        System.out.println("a");

        while (true)
        {
            System.out.println("b");
            if (i == 4)
            {
                for (int j=0; j<10; j++)
                    System.out.println("c");
                break;
            }
            else if (i == 5)
            {
                System.out.println("d");
                continue;
            }
            else if (i == 6)
            {
                System.out.println("e");
                break;
            }
            System.out.println("f");
            i++;
        }

        System.out.println("g");

        switch (i)
        {
        case 1:
            System.out.println("h");
        case 2:
            System.out.println("i");
            break;
        case 3:
            System.out.println("j");
        }

        System.out.println("k");
    }

    private void whileWhile() {
        int a = 2;
        int b = 2;

        while (a>0) {
            while (b>0) {
                a--;
                b--;
            }
        }
    }

    private static void whileTestPreInc() {
        int i = 0;

        while (++i < 10)
        {
            System.out.println("1");
        }
    }

    private static void whilePreInc() {
        int i = 0;

        while (i < 10)
        {
            System.out.println("2");
            ++i;
        }
    }

    private static void whileTestPostInc() {
        int i = 0;

        while (i++ < 10)
        {
            System.out.println("1");
        }
    }

    private static void whilePostInc() {
        int i = 0;

        while (i < 10)
        {
            System.out.println("2");
            i++;
        }
    }

    public void whileANDCondition(int i) {
        System.out.println("start");

        while ((i==4) && (i==5) && (i==6)) {
            System.out.println("a");
        }

        System.out.println("end");
    }

    public void whileORAndANDConditions(int i) {
        System.out.println("start");

        while ((i==1 || (i==5 && i==6 && i==7) || i==8 || (i==9 && i==10 && i==11)) && (i==4 || (i%200) > 50) && (i>3 || i>4)) {
            System.out.println("a");
        }

        System.out.println("end");
    }

    public void whileAndANDConditions(int i)
    {
        System.out.println("start");

        while ((i==1 && (i==5 || i==6 || i==7) && i==8 && (i==9 || i==10 || i==11)) || (i==4 && (i%200) > 50) || (i>3 && i>4)) {
            System.out.println("a");
        }

        System.out.println("end");
    }

    public static int whileContinueBreak(int[] array) {
        int i = 0;
        int length = array.length;
        int counter = 0;

        while (i < length) {
            int item = array[i];

            if (item % 2 == 1) {
                counter++;
                continue;
            }

            i++;
            break;
        }

        i++;

        return counter;
    }

    public static void twoWiles() {
        int i = 0;

        while (i < 5) {
            i++;
        }

        while (i < 10) {
            i++;
        }

        i++;
    }

    public static void whileSwitch() {
        System.out.println("start");

        int i = 0;

        while (i++ < 10) {
            System.out.println("a");

            switch (i) {
                case 1:
                    System.out.println('1');
                    break;
                case 2:
                    System.out.println('2');
                    break;
                case 3:
                    System.out.println('3');
                    break;
            }
        }

        System.out.println("end");
    }

    public static void whileSwitchDefault() {
        System.out.println("start");

        int i = 0;

        while (i++ < 10) {
            System.out.println("a");

            switch (i) {
                case 1:
                    System.out.println('1');
                    break;
                case 2:
                    System.out.println('2');
                    break;
                case 3:
                    System.out.println('3');
                    break;
                default:
                    System.out.println("default");
                    break;
            }
        }

        System.out.println("end");
    }

    public static void whileTryFinally(int i) {
        System.out.println("start");

        while (i < 5) {
            try {
                System.out.println("a");
            } finally {
                i++;
            }
        }

        System.out.println("end");
    }

    public static void tryWhileFinally(int i) {
        System.out.println("start");

        try {
            while (i < 5) {
                System.out.println("a");
                i++;
            }
        } finally {
            System.out.println("b");
        }

        System.out.println("end");
    }

    public static void infiniteWhileTryFinally(int i) {
        System.out.println("start");

        while (true) {
            try {
                System.out.println("a");
            } finally {
                i++;
            }
        }
    }

    public static void tryInfiniteWhileFinally(int i) {
        System.out.println("start");

        try {
            while (true) {
                System.out.println("a");
            }
        } finally {
            System.out.println("b");
        }
    }
}
