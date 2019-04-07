/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.util.io;

import org.jd.gui.util.exception.ExceptionUtil;

import java.io.*;

public class TextReader {

    public static String getText(File file) {
        try {
            return getText(new FileInputStream(file));
        } catch (IOException e) {
            assert ExceptionUtil.printStackTrace(e);
            return "";
        }
    }

    public static String getText(InputStream is) {
        StringBuilder sb = new StringBuilder();
        char[] charBuffer = new char[8192];
        int nbCharRead;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            while ((nbCharRead = reader.read(charBuffer)) != -1) {
                // appends buffer
                sb.append(charBuffer, 0, nbCharRead);
            }
        } catch (IOException e) {
            assert ExceptionUtil.printStackTrace(e);
        }

        return sb.toString();
    }
}
