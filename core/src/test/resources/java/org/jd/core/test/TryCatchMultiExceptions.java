/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.test;

public class TryCatchMultiExceptions {

    public void methodTryMultiCatchCatchCatchFinally() {
        before();

        try {
            inTry();
        } catch (ClassCastException | ArithmeticException | NullPointerException exception) {
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

    private void before() {}

    private void inTry() {}
    private void inCatch1() {}
    private void inCatch2() {}
    private void inCatch3() {}
    private void inFinally() {}

    private void after() {}
}
