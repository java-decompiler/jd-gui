/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.util.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class NewlineOutputStream extends FilterOutputStream {
    private static byte[] lineSeparator;

    public NewlineOutputStream(OutputStream os) {
        super(os);

        if (lineSeparator == null) {
            String s = System.getProperty("line.separator");

            if ((s == null) || (s.length() <= 0))
                s = "\n";

            lineSeparator = s.getBytes(Charset.forName("UTF-8"));
        }
    }

    public void write(int b) throws IOException {
        if (b == '\n') {
            out.write(lineSeparator);
        } else {
            out.write(b);
        }
    }

    public void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }

    public void write(byte b[], int off, int len) throws IOException {
        int i;

        for (i=off; i<len; i++) {
            if (b[i] == '\n') {
                out.write(b, off, i-off);
                out.write(lineSeparator);
                off = i+1;
            }
        }

        out.write(b, off, i-off);
    }
}
