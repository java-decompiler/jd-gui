/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.test;

import org.jd.core.test.annotation.Name;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;

public class AnonymousClass {

    protected long time = System.currentTimeMillis();
    
    public void test(Enumeration e, String s) {
        System.out.println("start");

        Object obj = new Object() {
            public String toString() {
                return "toString() return " + super.toString() + " at " + time;
            }
        };
        
        System.out.println(obj);

        System.out.println("end");
    }
    
    LinkedList<String> list;
    
    public void anonymousImplInterface(final String s1, @Name("s2") final String s2, String s3, final int i1) {
        System.out.println("start");
        
        final long l1 = System.currentTimeMillis();
        
        Enumeration e = new Enumeration() {
            Iterator<String> i = list.iterator(); 
            
            public boolean hasMoreElements() {
                time = System.currentTimeMillis();
                return i.hasNext() && (s1 == s2) && (i1 > l1); 
            }
            
            public Object nextElement() {
              return i.next();
            }
         };

        test(e,
         "test");

        System.out.println("end");
    }
    
    public void anonymousImplClass(final String s1, final String s2, String s3) {
        System.out.println("start");
        
        final int i = s1.length();
        
        System.out.println("2" + (new StringWrapper(123456L) {
            
            public String toString(String a, String b) {
                time = System.currentTimeMillis();
                if ((s1 == s2) && (i == 5))
                    return s1;
                else
                    return s2;
            }
            
        }) + "3");

        System.out.println("end");
    }

    public void twoAnonymousClasses() {
        System.out.println("start");

        final Object abc = "abc";
        final Object def = "def";

        Serializable serializable = new Serializable() {
            public boolean equals(Object obj) {

                final Object ghi = "ghi";
                final Object jkl = "jkl";

                Serializable serializable = new Serializable() {
                    public boolean equals(Object obj) {

                        Object ghi = "overwrite ghi";
                        Object jkl = "overwrite jkl";

                        return abc.equals(obj) || def.equals(obj) || ghi.equals(obj) || jkl.equals(obj);
                    }
                };

                return abc.equals(obj) || def.equals(obj);
            }
        };

        System.out.println("end");
    }

    public static class StringWrapper {
        protected long l;

        public StringWrapper(long l) {
            this.l = l & 128L;
        }
    }
}
