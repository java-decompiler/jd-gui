/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.test;

public class DoWhile {

    public void doWhileTry(int i, Object o) {
        do {
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
        while (i<10);
    }

	public void doWhile() {
		System.out.println("start");

		do
			System.out.println("'this' is null!!");
		while (this == null);

		System.out.println("end");
	}

	public void doWhile2() {
		System.out.println("a");
		int i = 0;

		do
		{
			System.out.println("b");
			i++;
		}
		while (i < 10);

		System.out.println("c");
	}

	public void doWhileIf() {
		int i=0;

		System.out.println("a");

		do
		{
			System.out.println("b");
			if (i == 4)
				System.out.println("c");
			System.out.println("d");
			i++;
		}
		while (i<10);

		System.out.println("e");
	}

	public int doWhileWhile(int i0, int i1) {
	    do
	    {
	        while (i0 < 10)
	        {
            	i0 = i0 + 1;
	        }

	        i1 = i1 + -1;
	    }
	    while (i1 > 0);

	    return i0;
	}

	private static void emptyDoWhile() {
		int i = 10;

		do;
		while (i < 10);
	}

	private static void doWhileTestPreInc() {
		float i = 10;

		do
		{
			System.out.println("2");
		}
		while (--i > 0);
	}

	private static void doWhileTestPostInc() {
		float i = 10;

		do
		{
			System.out.println("2");
		}
		while (i-- > 0);
	}

	public void doWhileORAndANDConditions(int i)
	{
		System.out.println("start");

		do {
			System.out.println("a");
		} while ((i==1 || (i==5 && i==6 && i==7) || i==8 || (i==9 && i==10 && i==11)) && (i==4 || (i%200) > 50) && (i>3 || i>4));

		System.out.println("end");
	}

	public void doWhileAndANDConditions(int i)
	{
		System.out.println("start");

		do {
			System.out.println("a");
		} while ((i==1 && (i==5 || i==6 || i==7) && i==8 && (i==9 || i==10 || i==11)) || (i==4 && (i%200) > 50) || (i>3 && i>4));

		System.out.println("end");
	}

	public static void doWhileTryFinally(int i) {
		System.out.println("start");

		do {
			try {
				System.out.println("a");
			} finally {
				i++;
			}
		} while (i < 5);

		System.out.println("end");
	}

	public static void tryDoWhileFinally(int i) {
		System.out.println("start");

		try {
			do {
				System.out.println("a");
				i++;
			} while (i < 5);
		} finally {
			System.out.println("b");
		}

		System.out.println("end");
	}
}
