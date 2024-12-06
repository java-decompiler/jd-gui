/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.test;

public class InfiniteLayout {

    public class InnerClass {
        protected int innerField1 = 0;

        public void innerMethod(int param1) {
            int localVariable1 = param1;
        }

        public class InnerInnerClass {}
    }

    public static class StaticInnerClass {
        protected int innerField1 = 0;
    }
}
