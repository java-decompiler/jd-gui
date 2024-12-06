/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.regex;

public class PatternMaker {
    public static String make (String first, String... next) {
        StringBuilder sb = new StringBuilder("(?s).*");

        sb.append(replace(first));

        for (String s : next) {
            sb.append("[^\\n\\r]*").append(replace(s));
        }

        sb.append(".*");

        return sb.toString();
    }

    public static String make(String s) {
        return "(?s).*" + replace(s) + ".*";
    }

    protected static String replace(String s) {
        return s
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace(".", "\\.")
                .replace("?", "\\?")
                .replace("+", "\\+")
                .replace("*", "\\*")
                .replace("|", "\\|")
                .replace("^", "\\^")
                .replaceAll("\\s*\\{\\s*", "[^\\\\n\\\\r]*\\\\{[^\\\\n\\\\r]*")
                .replaceAll("\\s*\\}\\s*", "[^\\\\n\\\\r]*\\\\}[^\\\\n\\\\r]*")
                .replace(",", "[^\\n\\r]*,");
    }
}
