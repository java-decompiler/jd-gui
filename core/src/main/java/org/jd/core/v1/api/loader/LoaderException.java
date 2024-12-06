/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.api.loader;


public class LoaderException extends Exception {
    private static final long serialVersionUID = 9506606333927794L;
    public LoaderException() {}

    public LoaderException(String msg) { super(msg); }

    public LoaderException(Throwable cause) { super(cause); }
}
