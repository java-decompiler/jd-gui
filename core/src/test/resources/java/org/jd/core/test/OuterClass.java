/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.test;

import java.util.Comparator;

public class OuterClass {
    protected int outerField1 = 0;
    protected String[] outerField2 = { "0" };
    protected String[][] outerField3 = { { "0" }, { "1", "2" } };

    protected Thread thread;
    protected Thread.State state;

    public void method(int param1, String[] param2) {
        final int localVariable1 = param1;
        final String[] localVariable2 = param2;

        InnerClass innerClass = new InnerClass(param1, param2);
        innerClass.innerMethod(localVariable1, localVariable2);

        StaticInnerClass staticInnerClass = new StaticInnerClass(param1, param2);
        staticInnerClass.innerMethod(localVariable1, localVariable2);

        InnerClass anonymousClass = new InnerClass(param1, param2) {
            public void innerMethod(int param1, String... param2) {
                innerField1 = param1;
                innerField2 = param2;

                outerField1 = param1;
                outerField2 = param2;

                innerField1 = localVariable1;
                innerField2 = localVariable2;
            }
        };
        anonymousClass.innerMethod(localVariable1, localVariable2);

        StaticInnerClass staticAnonymousClass = new StaticInnerClass(param1, param2) {
            public void innerMethod(int param1, String... param2) {
                innerField1 = param1;
                innerField2 = param2;

                outerField1 = param1;
                outerField2 = param2;

                innerField1 = localVariable1;
                innerField2 = localVariable2;
            }
        };
        staticAnonymousClass.innerMethod(localVariable1, localVariable2);

        InnerEnum.A.innerMethod(localVariable1, localVariable2);

        class LocalClass {
            protected int innerField1 = 0;
            protected String[] innerField2 = { "0" };

            public LocalClass(int param1) {
                int variable1 = param1;
            }

            public LocalClass(int param1, String... param2) {
                int variable1 = param1;
                String[] variable2 = param2;

                innerField1 = param1;
                innerField2 = param2;

                outerField1 = param1;
                outerField2 = param2;

                innerField1 = localVariable1;
                innerField2 = localVariable2;
            }

            public void localMethod(int param1, String... param2) {
                int variable1 = param1;
                String[] variable2 = param2;

                innerField1 = param1;
                innerField2 = param2;

                outerField1 = param1;
                outerField2 = param2;

                innerField1 = localVariable1;
                innerField2 = localVariable2;
            }
        };

        LocalClass localClass = new LocalClass(param1, param2);
        localClass.localMethod(localVariable1, localVariable2);
    }

    public class InnerClass {
        protected int innerField1 = 0;
        protected String[] innerField2 = { "0" };

        public InnerClass(int param1, String... param2) {
            int localVariable1 = param1;
            String[] localVariable2 = param2;

            innerField1 = param1;
            innerField2 = param2;

            outerField1 = param1;
            outerField2 = param2;
        }

        public InnerClass(String s, int param1, String... param2) {
            this(param1, param2);
            System.out.println(s);
        }

        public void innerMethod(int param1, String... param2) {
            int localVariable1 = param1;
            String[] localVariable2 = param2;

            outerField1 = param1;
            outerField2 = param2;

            method(0, null);
        }

        public class InnerInnerClass {}
    }

    public static class StaticInnerClass {
        protected int innerField1 = 0;
        protected String[] innerField2 = { "0" };

        public StaticInnerClass(int param1, String... param2) {
            int localVariable1 = param1;
            String[] localVariable2 = param2;

            innerField1 = param1;
            innerField2 = param2;
        }

        public StaticInnerClass(String s, int param1, String... param2) {
            this(param1, param2);
            System.out.println(s);
        }

        public void innerMethod(int param1, String... param2) {
            int localVariable1 = param1;
            String[] localVariable2 = param2;

            innerField1 = param1;
            innerField2 = param2;
        }
    }

    public enum InnerEnum {
        A, B, C;

        public void innerMethod(int param1, String... param2) {
            int localVariable1 = param1;
            String[] localVariable2 = param2;
        }
    }

    public static class NumberComparator implements Comparator<Number> {
        @Override
        public int compare(Number o1, Number o2) {
            return o2.intValue() - o1.intValue();
        }
    }

    public static class SafeNumberComparator extends NumberComparator {
        @Override
        public int compare(Number o1, Number o2) {
            if (o1 == null) {
                return (o2 == null) ? 0 : 1;
            }
            if (o2 == null) {
                return -1;
            }
            return super.compare(o1, o2);
        }
    }
}
