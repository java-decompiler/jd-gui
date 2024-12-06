/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.test;

import java.io.Serializable;

public class Array implements Serializable {

    public void declarations() {
        int[] i1 = new int[1];
        int[][] i2 = new int[1][];
        int[][][] i3 = new int[1][][];
        int[][][] i4 = new int[1][2][];
        int[][][][] i5 = new int[1][2][][];

        String[] s1 = new String[1];
        String[][] s2 = new String[1][];
        String[][][] s3 = new String[1][][];
        String[][][] s4 = new String[1][2][];
        String[][][][] s5 = new String[1][2][][];
    }

    public void init() {
        byte[] b1 = { 1, 2 };
        byte[][] b2 = { { 1, 2 } };
        byte[][][][] b3 = { { { { 3, 4 } } } };

        int[][] ia = new int[][] { {

                0, 1,

                2}, {
                4} };

        int[] ia1 = new int[] {
            100, 101, 102, 103, 104, 105, 106, 107, 108, 109,
            110, 111, 112, 113, 114, 115, 116, 117, 118, 119,
            120, 121, 122 };

        int size = ia.length;

        System.out.println(ia[1][0]);

        int[][] ia2 = new int[3][];

        testException1(new Exception[] { new Exception("1") });

        testException1(new Exception[] { });

        testException2(new Exception[][] {
                { new Exception("1") },
                { new Exception("2"), new Exception("3") }
        });

        testException3(new Exception[][][] {
                { { new Exception("1") } },
                { { new Exception("2"), new Exception("3") } },
                new Exception[0][]
        });

        testException3(new Exception[][][] {
                { },
                { { new Exception("1") } },
                { { new Exception("2"), new Exception("3") } },
                { { } },
                { }
        });

        testInt1(new int[] { 1 });

        testInt2(new int[][] { { 1 }, { 2 } });

        testInt3(new int[][][] {
                { // 0
                        { // 0,0
                                0, // 0,0,0
                                1  // 0,0,1
                        }
                },
                { // 1
                        { // 1,0
                                100 // 1,0,0
                        }
                }
        });
    }

    private void testException1(Exception[] es) {}

    private void testException2(Exception[][] es) {}

    private void testException3(Exception[][][] es) {}

    private void testInt1(int[] es) {}

    private void testInt2(int[][] es) {}

    private void testInt3(int[][][] es) {}
}
