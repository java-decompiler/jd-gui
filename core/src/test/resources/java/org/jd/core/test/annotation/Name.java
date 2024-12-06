/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.test.annotation;

public @interface Name {
    String salutation() default ""; // Salutation
    String value();                 // First name
    String last() default "";       // Last name
}
