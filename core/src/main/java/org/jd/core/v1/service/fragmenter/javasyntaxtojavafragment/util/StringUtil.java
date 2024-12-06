/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.fragmenter.javasyntaxtojavafragment.util;


public class StringUtil {

    public static String escapeString(String s) {
        int length = s.length();

        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);

            if ((c == '\\') || (c == '"') || (c < ' ')) {
                StringBuilder sb = new StringBuilder(length * 2);

                sb.append(s.substring(0, i));

                for (; i < length; i++) {
                    c = s.charAt(i);

                    switch (c) {
                        case '\\':
                            sb.append("\\\\");
                            break;
                        case '\b':
                            sb.append("\\b");
                            break;
                        case '\f':
                            sb.append("\\f");
                            break;
                        case '\n':
                            sb.append("\\n");
                            break;
                        case '\r':
                            sb.append("\\r");
                            break;
                        case '\t':
                            sb.append("\\t");
                            break;
                        case '"':
                            sb.append("\\\"");
                            break;
                        default:
                            if (c < ' ') {
                                sb.append("\\0");
                                sb.append((char)('0' + ((int)c >> 3)));
                                sb.append((char)('0' + ((int)c & 7)));
                            } else {
                                sb.append(c);
                            }
                    }
                }

                return sb.toString();
            }
        }

        return s;
    }
}
