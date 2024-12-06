/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.test;

public class Synchronized
{
    public int methodSynchronized(StringBuilder paramStringBuilder)
    {
        synchronized (paramStringBuilder)
        {
            inSynchronized();
        }
        return 2;
    }

    public int methodSynchronized2(StringBuilder paramStringBuilder)
    {
        synchronized (paramStringBuilder)
        {
            inSynchronized();
            return 2;
        }
    }

    public void methodSynchronized3(StringBuilder paramStringBuilder)
    {
        synchronized (paramStringBuilder)
        {
            inSynchronized();
            return;
        }
    }

    public void methodSynchronized4(StringBuilder paramStringBuilder)
    {
        synchronized (paramStringBuilder)
        {
            if (paramStringBuilder == null)
                throw new RuntimeException();
        }
    }

    public int methodSynchronized5(StringBuilder paramStringBuilder)
    {
        before();

        synchronized (paramStringBuilder)
        {
            inSynchronized();
        }

        after();
        return 2;
    }

    public int methodSynchronized6(StringBuilder paramStringBuilder)
    {
        before();

        synchronized (paramStringBuilder)
        {
            inSynchronized();
            return 2;
        }
    }

    public void methodSynchronized7(StringBuilder paramStringBuilder)
    {
        before();

        synchronized (paramStringBuilder)
        {
            inSynchronized();
            throw new RuntimeException();
        }
    }

    public void methodSynchronized8(StringBuilder paramStringBuilder)
    {
        before();

        synchronized (paramStringBuilder)
        {
            if (paramStringBuilder == null)
                throw new RuntimeException();
        }

        after();
    }

    public boolean contentEquals1(String s)
    {
        synchronized(s)
        {
            return subContentEquals(s);
        }
    }

    public boolean contentEquals2(String s)
    {
        synchronized(s)
        {
            s += "z";
            return subContentEquals(s);
        }
    }

    public boolean contentEquals3(String s)
    {
        synchronized(s)
        {
            s += "z";
        }
        return subContentEquals(s);
    }

    public void methodIfIfSynchronized()
    {
        if (this == null)
        {
            if (this != null)
            {
                System.out.println("1\\2");

                synchronized (this)
                {
                    System.out.println("2");
                }
            }
            else
            {
                System.out.println("3");
            }
        }
    }

    public boolean subContentEquals(String s)
    {
        return (s == null);
    }

    private void before()
    {
    }

    private void inSynchronized()
    {
    }

    private void after()
    {
    }
}
