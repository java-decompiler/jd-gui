/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class For {

    public void simpleFor() {
        System.out.println("start");

        for (int i=0; i<10; i++) {
            System.out.println("loop");
        }

        System.out.println("end");
    }

    public void declarationAndFor() {
        System.out.println("start");

        int i;

        for (i=0; i<5; ++i) {
            System.out.println("loop");
        }

        System.out.println("end");
    }

    public void initAndForTestUpdate() {
        System.out.println("start");

        int i=0;

        for (; i<10; i++) {
            System.out.println("loop");
        }

        System.out.println("end");
    }

    public void forTestUpdate(int i) {
        System.out.println("start");

        for (; i<10; i++) {
            System.out.println("loop");
        }

        System.out.println("end");
    }

    public void forInitUpdate() {
        System.out.println("start");

        for (int i=0;; i++) {
            System.out.println("loop");
        }
    }

    public void forUpdate(int i) {
        System.out.println("start");

        for (;; i++) {
            System.out.println("loop");
        }
    }

    public void forInitTest() {
        System.out.println("start");

        for (int i=0; i<10;) {
            System.out.println("loop");
        }
    }

    public void forTest(int i) {
        System.out.println("start");

        for (; i<10;) {
            System.out.println("loop");
        }
    }

    public void forInit() {
        System.out.println("start");

        for (int i=0;;) {
            System.out.println("loop");
        }
    }

    public void forInfiniteLoop() {
        System.out.println("start");

        for (;;) {
            System.out.println("loop");
        }
    }

    public void forMultipleVariables1() {
        System.out.println("start");

        for (int i=0, j=i, size=10; i<size; i++, j+=i) {
            System.out.println("loop");
        }

        System.out.println("end");
    }

    public void forMultipleVariables2() {
        System.out.println("start");

        for (int i=0, j=i, size=10;
             i<size;
             i++, j+=i) {
            System.out.println("loop");
        }

        System.out.println("end");
    }

    public void forMultipleVariables3() {
        System.out.println("start");

        for (int i=0,
             j=i,
             size=10;
             i<size;
             i++,
             j+=i) {
            System.out.println("loop");
        }

        System.out.println("end");
    }

    public void forMultipleVariables4() {
        System.out.println("start");

        for (int i=0,

             j=i,

             size=10;

             i<size;

             i++,

             j+=i) {
            System.out.println("loop");
        }

        System.out.println("end");
    }

    public void emptyFor() {
        System.out.println("start");

        for (int i=0; i<10; i++);

        System.out.println("end");
    }

    public void emptyForTestUpdate(int i) {
        System.out.println("start");

        for (; i<10; i++);

        System.out.println("end");
    }

    public void emptyForInitUpdate() {
        System.out.println("start");

        for (int i=0;; i++);
    }

    public void emptyForUpdate(int i) {
        System.out.println("start");

        for (;; i++);
    }

    public void emptyForInitTest1() {
        System.out.println("start");

        for (int i=0; i<10;);
    }

    public void emptyForInitTest2() {
        System.out.println("start");

        for (int[] i={0}; i.length<10;);
    }

    public void emptyForInitTest3() {
        System.out.println("start");

        for (int i=0, j=i, k=i; i<10;);
    }

    public void emptyForInitTest4() {
        System.out.println("start");

        for (int[] i={0}, j=i, k=j; i.length<10;);
    }

    public void emptyForInitTest5() {
        System.out.println("start");

        for (int i=0, j[]={1}; i<10;);
    }

    public void emptyForTest(int i) {
        System.out.println("start");

        for (; i<10;);
    }

    public void emptyForInit() {
        System.out.println("start");

        for (int i=0;;);
    }

    public void emptyForInfiniteLoop() {
        System.out.println("start");

        for (;;);
    }

    public void emptyForMultipleVariables() {
        System.out.println("start");

        for (int i=0, j=i, size=10; i<size; i++, j+=i);

        System.out.println("end");
    }

    public void testInfiniteLoop2() {
        System.out.println("start");

        for (;;) {
            System.out.println("infinite loop");
            if (this == null)
                System.out.println("infinite loop");
            System.out.println("infinite loop");
        }
    }

    public void testForEach(List<String> list) {
        System.out.println("start");

        for (String s : list)
            System.out.println(s);

        System.out.println("end");
    }

    public void forTry(int i, Object o) {
        System.out.println("start");

        for (i=0; i<10; i++) {
            System.out.println("a");
            try {
                System.out.println("b");
            } catch (RuntimeException e) {
                System.out.println("c");
            }
        }

        System.out.println("end");
    }

    public void forTryReturn(int i, Object o) {
        System.out.println("start");

        for (i=0; i<10; i++) {
            System.out.println("a");
            try {
                System.out.println("b");
                return;
            } catch (RuntimeException e) {
                System.out.println("c");
            }
        }

        System.out.println("end");
    }

    public Object forFor() {
        System.out.println("start");

        for (int i=0; i<5; ++i)
        {
            if (this == null) return null;
            System.out.println(i);
        }

        for (int i : new int[] { 4 })
        {
          if (0 == i)
          {
              System.out.println(i);
          }
        }

        System.out.println("end");

        return this;
    }

    public void forIf() {
        System.out.println("start");

        for (int i=0; i<10; i++)
        {
            System.out.println("b");
            if (i == 4)
                System.out.println("c");
            System.out.println("d");
        }

        if (this == null)
            System.out.println("e");

        System.out.println("end");
    }

    private static void forAndEmptyDoWhile() {
        System.out.println("start");

        int i;

        for (i=0; i<20; i++)
            System.out.println(i);

        do;
        while (i < 10);

        System.out.println("end");
    }

    private static void forAndEmptyDoWhileTestOr() {
        System.out.println("start");

        int i;

        for (i=0; i<10; i++)
            System.out.println(i);

        do;
        while ((i < 20) || (i < 10) || (i < 0));

        System.out.println("end");
    }

    private static void forAndEmptyDoWhileTestAnd() {
        System.out.println("start");

        int i;

        for (i=0; i<10; i++)
            System.out.println(i);

        do;
        while ((i < 20) && (i < 10) && (i < 0));

        System.out.println("end");
    }

    public static void forEachArray(String[] array) {
        System.out.println("start");

        for(String s : array) {
            System.out.println(s);
        }

        for(String s : array) {
            System.out.println(s);
        }

        System.out.println("end");
    }

    public static void forEachList(List<String> list) {
        System.out.println("start");

        for(String s : list) {
            System.out.println(s);
        }

        for(String s : list) {
            System.out.println(s);
        }

        System.out.println("end");
    }

    public void notAForEach() {
        Iterator<Class> iterator = Arrays.<Class>asList(this.getClass().getInterfaces()).iterator();
        while (iterator.hasNext()) {
            Class clazz = iterator.next();
            System.out.println(clazz);
        }

        System.out.println(iterator);
    }

    public static void forUnderscore(String[] __) {
        for (int ___ = 0; ___ < __.length; ___++)
            System.out.println(__[___]);
    }

    private byte[] forTryReturn() throws Exception
    {
        for(int i=0; i<3; i++) {
            try {
                byte[] data = null;
                return data;
            } catch (Exception e) {
                Thread.sleep(300);
            }
        }
        throw new Exception("comm error");
    }

    protected boolean ifForIfReturn(int[] array) {
        boolean flag = false;

        if (flag == false) {
            for (int i : array) {
                if (flag != false)
                    break;
            }

            if (flag == false) {
                flag = true;
            }
        }

        return flag;
    }

    public static void forIfContinue() {
        System.out.println("start");

        for (int i=0; i<100; i++) {
            System.out.println("a");
            if (i == 0) {
                System.out.println("b");
                if (i == 1) {
                    continue;
                }
                System.out.println("c");
            }
            System.out.println("d");
        }

        System.out.println("end");
    }

    public void forIfIfContinue() {
        for (int i=0; i<100; i++) {
            if (i == 1) {
                if (i != 2) {
                    continue;
                }
            }
            i += 42;
        }
    }

    public void forIfIfContinue2() {
        for (int i=0; i<100; i++) {
            if (i == 1) {
                if (i != 2) {
                    i = 3;
                    continue;
                }
                i = 4;
            }
            i += 42;
        }
    }

    public void forIterator(Map map) {
        for (Iterator<Map.Entry> it = map.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = it.next();
            if (entry.getValue() instanceof String) {
                it.remove();
            }
        }
    }

    public void forBreak(Object[] array) {
        System.out.println("start");

        for (int i=0; i<array.length; i++) {
            Object o = array[i];

            if (o == null) {
                System.out.println("array[" + i + "] = null");
                if (i > 0) {
                    array[i] = "null";
                    continue;
                }
            }

            System.out.println("array[" + i + "] = " + o);
            break;
        }

        System.out.println("end");
    }
}
