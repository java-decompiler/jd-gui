/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.test;

public class TryCatchFinally
{
    public void methodTryCatch() {
        before();

        try {
            inTry();
            if (this == null)
                inTry();
        } catch (RuntimeException runtimeexception) {
            runtimeexception.printStackTrace();
            inCatch1();
        }

        after();
    }

    public void methodTryCatchCatch() {
        before();

        try {
            inTry();
        } catch (RuntimeException runtimeexception) {
            inCatch1();
            runtimeexception.printStackTrace();
        } catch (Exception exception) {
            inCatch2();
        }

        after();
    }

    public void methodTryCatchCatchOneExitInTry() {
        before();

        try {
            inTry();
        } catch (RuntimeException runtimeexception) {
            inCatch1();
            throw new RuntimeException();
        } catch (Exception exception) {
            inCatch2();
            return;
        }

        after();
    }

    public void methodTryCatchCatchOneExitInFirstCatch() {
        before();

        try {
            inTry();
            return;
        } catch (RuntimeException runtimeexception) {
            inCatch1();
        } catch (Exception exception) {
            inCatch2();
            return;
        }

        after();
    }

    public void methodTryCatchCatchOneExitInLastCatch() {
        before();

        try {
            inTry();
            return;
        } catch (RuntimeException runtimeexception) {
            inCatch1();
            return;
        } catch (Exception exception) {
            inCatch2();
        }

        after();
    }

    public void methodTrySwitchFinally() throws Exception {
        System.out.println("start");

        try {
            System.out.println("in try");

            switch ((int)(System.currentTimeMillis() & 0xF)) {
                case 0:
                    System.out.println("0");
                    break;
                default:
                    System.out.println("default");
                    break;
            }
        } finally {
            System.out.println("in finally");
        }

        System.out.println("end");
    }

    public void methodTryFinally1() {
        before();

        try {
            inTry();
        } finally {
            inFinally();
        }

        after();
    }

    public void methodTryFinally2() {
        before();

        try {
            inTry();
            return;
        } finally {
            inFinally();
        }
    }

    public void methodTryFinally3() {
        before();

        try {
            inTry();
            throw new RuntimeException();
        } finally {
            inFinally();
        }
    }

    public long methodTryCatchFinallyReturn() {
        before();

        try {
            inTry();

            return
                    System.currentTimeMillis();
        } catch (RuntimeException e) {
            inCatch1();

            return
                    System.currentTimeMillis();
        } finally {
            inFinally();
        }
    }

    public long methodTryFinallyReturn() {
        before();

        try {
            inTry();

            return System.currentTimeMillis();
        } finally {
            inFinally();
        }
    }

    public void methodTryFinally4() {
        before();

        try {
            inTry();
            if (this == null)
                return;
        } finally {
            inFinally();
            System.out.println("ee");
        }

        after();
    }

    public void methodEmptyTryCatch() {
        before();

        try {
            System.out.println("try");
        } catch (RuntimeException e) {
            // Empty
        }

        after();
    }

    public void methodEmptyTryFinally() {
        before();

        try {
            System.out.println("try");
        } finally {
            // Empty
        }

        after();
    }

    public void methodTryCatchFinally1() {
        before();

        try {
            inTry();
        } catch (RuntimeException runtimeexception) {
            inCatch1();
        } finally {
            inFinally();
        }

        after();
    }

    public void methodTryCatchFinally2() {
        before();

        try {
            inTry();
        } catch (RuntimeException runtimeexception) {
            inCatch1();
            if (this == null)
                return;
            inCatch2();
        } finally {
            inFinally();
            System.out.println("ee");
        }

        after();
    }

    public int methodTryCatchFinally3() {
        before();

        try {
            inTry();
        } catch (RuntimeException e) {
            inCatch1();
        } catch (Exception e) {
            inCatch2();
            return 1;
        } finally {
            inFinally();
            new RuntimeException();
        }

        after();
        return 2;
    }

    public int methodTryCatchFinally4() {
        before();

        try {
            inTry();
            throw new Exception();
        } catch (Exception e) {
            inCatch2();
        } finally {
            inFinally();
        }

        after();
        return 2;
    }

    public void methodTryCatchFinally5() throws Exception {
        System.out.println("start");

        try {
            System.out.println("in try");

            if (this == null)
                return;
            if (this == null)
                throw new RuntimeException();

            // throw new Exception(); //
            return;
        } catch (RuntimeException e) {
            System.out.println("in catch");

            if (this == null)
                return;
            if (this == null)
                throw new RuntimeException();

            System.out.println("in catch");
        } finally {
            System.out.println("in finally");
        }

        System.out.println("end");
    }

    public int methodTryCatchFinallyInTryCatchFinally() throws Exception {
        System.out.println("start");

        int a = 1;
        int b = 1;

        try {
            System.out.println("in try");

            try {
                System.out.println("in inner try");

                if (this == null) {
                    System.out.println("before throw in inner try");
                    throw new RuntimeException();
                }

                return b;
            } catch (RuntimeException e) {
                System.out.println("in catch in inner try");

                if (this == null)
                    throw new RuntimeException();

                System.out.println("in catch in inner try");
            } finally {
                for (int i=0; i<10; i++)
                    System.out.println("in finally in inner try");
            }

            System.out.println("in try");

            try {
                System.out.println("in inner try");

                if (this == null) {
                    System.out.println("before throw in inner try");
                    throw new RuntimeException();
                }

                return b;
            } catch (RuntimeException e) {
                System.out.println("in catch in inner try");

                if (this == null)
                    throw new RuntimeException();

                System.out.println("in catch in inner try");
            } finally {
                for (int i=0; i<10; i++)
                    System.out.println("in finally in inner try");
            }

            System.out.println("in try");
        } finally {
            for (int i=0; i<10; i++)
                System.out.println("in finally");
        }

        System.out.println("end");

        return a + b;
    }

    public void methodTryCatchCatchFinally() {
        before();

        try {
            inTry();
        } catch (RuntimeException e) {
            inCatch1();
        } catch (Exception e) {
            inCatch2();
        } finally {
            inFinally();
        }

        after();
    }

    public void methodTryCatchCatchCatchFinally() {
        before();

        try {
            inTry();
        } catch (ClassCastException classcastexception) {
            inCatch1();
        } catch (RuntimeException runtimeexception) {
            inCatch2();
        } catch (Exception exception) {
            inCatch3();
        } finally {
            inFinally();
        }
        after();
    }

    public void methodTryTryReturnFinallyFinally() {
        before();

        try {
            try {
                inTryA();
                return;
            } finally {
                inFinallyA();
            }
        } finally {
            inFinally();
        }
    }

    public void methodTryTryReturnFinallyCatchFinally() {
        before();

        try {
            try {
                inTry();
                return;
            } finally {
                inFinally();
            }
        } catch (ClassCastException classcastexception) {
            try {
                inTryA();
                return;
            } finally {
                inFinallyA();
            }
        } catch (RuntimeException runtimeexception) {
        } catch (Exception exception) {
            try {
                inTryC();
                return;
            } finally {
                inFinallyC();
            }
        } finally {
            inFinally();
        }
    }

    public void methodTryTryFinallyFinallyTryFinally() {
        before();

        try {
            try {
                inTry();
            } finally {
                inFinally();
            }
        } finally {
            try {
                inTryC();
            } finally {
                inFinallyC();
            }
        }

        after();
    }

    public void methodTryTryFinallyFinallyTryFinallyReturn() {
        before();

        try {
            try {
                inTry();
                if (this == null)
                    return;
            } finally {
                inFinally();
                if (this == null)
                    return;
            }
        } finally {
            try {
                inTryC();
                if (this == null)
                    return;
            } finally {
                inFinallyC();
                if (this == null)
                    return;
            }
        }

        after();
    }

    public void methodIfIfTryCatch() {
        if (this == null) {
            if (this != null) {
                System.out.println("if if");

                try {
                    System.out.println("if if try");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new RuntimeException(ex.getMessage());
                }
            } else {
                System.out.println("if else");
            }
        }
    }

    public void methodIfIfTryFinally() {
        if (this == null) {
            if (this != null) {
                System.out.println("if if");

                try {
                    System.out.println("if if try");
                } finally {
                    System.out.println("if if finally");
                }
            } else {
                System.out.println("if else");
            }
        }
    }

    public Object testBug155(Object obj) {
        try {
            if (obj != null)
                return obj;
        } catch (Exception e) {
        }

        return obj;
    }

    public void testBugLdapUtil(int i) {
        System.out.println("a");

        if ((i < 0) && (i > 4)) {
            try {
                if (i == 0) {
                    System.out.println("b");
                } else {
                    System.out.println("c");
                }
            } catch (RuntimeException e) {
                System.out.println("d");
            }
        }

        if ((i < 2) && (i < 6)) {
            System.out.println("e");
        }
    }

    public int methodTryCatch2() {
        before();

        try {
            inTry();
            return 1;
        } catch (RuntimeException e) {
            e.printStackTrace();
            inCatch1();
            return 2;
        }
    }

    public int methodTryCatch3() {
        before();

        try {
            inTry();
            return 1;
        } catch (RuntimeException e) {
            e.printStackTrace();
            inCatch1();
        }

        return 2;
    }

    public int methodTryCatch4() {
        before();

        try {
            inTry();
            throw new RuntimeException("t");
        } catch (RuntimeException e) {
            e.printStackTrace();
            inCatch1();
            return 2;
        }
    }

    public int methodTryCatch5() {
        before();

        try {
            return 1;
        } catch (RuntimeException e) {
            inCatch1();
        }

        after();
        return 2;
    }

    public int methodTryCatchCatch2() {
        before();

        try {
            inTry();
            return 1;
        } catch (RuntimeException e) {
            inCatch1();
            return 2;
        } catch (Exception e) {
            inCatch2();
            return 3;
        }
    }

    public void methodTryCatchCatch3() {
        before();

        try {
            inTry();
            throw new RuntimeException("t");
        } catch (RuntimeException e) {
            inCatch1();
            throw new RuntimeException("1");
        } catch (Exception e) {
            inCatch2();
            throw new RuntimeException("2");
        }
    }

    public int methodTryCatchCatch4() {
        before();

        try {
            inTry();
            throw new RuntimeException("t");
        } catch (RuntimeException e) {
            inCatch1();
            throw new RuntimeException("1");
        } catch (Exception e) {
            inCatch2();
            return 3;
        }
    }

    public int methodTryCatchCatch5() {
        before();

        try {
            inTry();
        } catch (RuntimeException e) {
            return 1;
        } catch (Exception e) {
            inCatch2();
        }

        after();
        return 2;
    }

    public int methodTryCatchCatch6() {
        before();

        try {
            inTry();
        } catch (RuntimeException e) {
            inCatch1();
        } catch (Exception e) {
            return 1;
        }

        after();
        return 2;
    }

    public void methodTryFinally() {
        before();

        try {
            inTry();
        } finally {
            inFinally();
        }

        after();
    }

    public void methodEmptyTryCatch2() {
        before();

        try {
        } catch (RuntimeException e) {
            new RuntimeException();
        }

        after();
    }

    public void methodTryCatchFinally() {
        before();

        try {
            inTry();
        } catch (RuntimeException e) {
            inCatch1();
        } finally {
            inFinally();
        }

        after();
    }

    public int methodTryCatchCatchFinally3() {
        before();

        int a = 1;
        int b = 1;
        int c = 1;
        int d = 1;
        int f = 1;

        try {
            inTry();
        } catch (RuntimeException e) {
            return 1;
        } catch (Exception e) {
            return 2;
        } finally {
            inFinally();
        }

        after();
        return 3;
    }

    public int methodTryCatchCatchCatchFinally3() {
        before();

        try {
            inTry();
        } catch (ClassCastException e) {
            return 1;
        } catch (RuntimeException e) {
            return 2;
        } catch (Exception e) {
            inCatch3();
        } finally {
            inFinally();
        }

        after();
        return 3;
    }

    public int methodTryCatchCatchCatchFinally4() {
        before();

        try {
            inTry();
        } catch (ClassCastException e) {
            inCatch1();
        } catch (RuntimeException e) {
            return 1;
        } catch (Exception e) {
            return 2;
        } finally {
            inFinally();
        }

        after();
        return 3;
    }

    private Object methodTryCatchTryCatchThrow() throws Exception {
        try {
            return null;
        } catch (Exception e) {
            if (this != null) {
                try {
                    return null;
                } catch (Exception e2) {
                }
            }
            throw e;
        }
    }

    public void complexMethodTryCatchCatchFinally() {
        before();

        try {
            try {
                inTry();
            } catch (RuntimeException runtimeexception) {
                inCatch1();
            } catch (Exception exception) {
                inCatch2();
            } finally {
                inFinally();
            }
        } catch (RuntimeException runtimeexception1) {
            try {
                inTryA();
            } catch (RuntimeException runtimeexception2) {
                inCatch1A();
            } catch (Exception exception3) {
                inCatch2A();
            } finally {
                inFinallyA();
            }
        } catch (Exception exception1) {
            try {
                inTryB();
            } catch (RuntimeException runtimeexception3) {
                inCatch1B();
            } catch (Exception exception4) {
                inCatch2B();
            } finally {
                inFinallyB();
            }
        } finally {
            try {
                inTryC();
            } catch (RuntimeException runtimeexception4) {
                inCatch1C();
            } catch (Exception exception8) {
                inCatch2C();
            } finally {
                inFinallyC();
            }
        }

        after();
    }

    public void complexMethodTryFinally() throws Exception {
        try {
            System.out.println("1");

            try {
                System.out.println("2");
            } catch (Exception e) {
                try {
                    System.out.println("3");
                } catch (Exception e1) {
                    throw new Exception("Rebuild failed: " + e.getMessage() + "; Original message: " + e1.getMessage());
                }
            }

            try {
                System.out.println("4");
            } catch (RuntimeException e) {
                System.out.println("5");
            }

            System.out.println("6");

            try {
                System.out.println("7");
            } catch (Exception e) {
            }
        } finally {
            try {
                System.out.println("8");
            } catch (Exception e) {
                throw e;
            }
        }
        return;
    }

    public void complexMethodTryCatchCatchFinally3() throws Exception {
        try {
            System.out.println("1");

            try {
                System.out.println("2");
            } catch (Exception e) {
                System.out.println("3");
            }
        } finally {
            System.out.println("8");
        }

        return;
    }

    public boolean subContentEquals(String s)
    {
        return (s == null);
    }

    private void before() {}

    private void inTry() {}
    private void inCatch1() {}
    private void inCatch2() {}
    private void inCatch3() {}
    private void inFinally() {}

    private void inTryA() {}
    private void inCatch1A() {}
    private void inCatch2A() {}
    private void inFinallyA() {}

    private void inTryB() {}
    private void inCatch1B() {}
    private void inCatch2B() {}
    private void inFinallyB() {}

    private void inTryC() {}
    private void inCatch1C() {}
    private void inCatch2C() {}
    private void inFinallyC() {}

    private void after() {}
}
