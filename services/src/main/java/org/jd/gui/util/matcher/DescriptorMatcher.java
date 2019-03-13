/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.util.matcher;

/*
 * Descriptor format : @see jd.gui.api.feature.UriOpenable
 */
public class DescriptorMatcher {

    public static boolean matchFieldDescriptors(String d1, String d2) {
        return matchDescriptors(new CharBuffer(d1), new CharBuffer(d2));
    }

    protected static boolean matchDescriptors(CharBuffer cb1, CharBuffer cb2) {
        if (cb1.read() == '?') {
            if (cb2.read() == '?') {
                return true;
            } else {
                cb2.unread();
                return cb2.skipType();
            }
        } else {
            cb1.unread();

            if (cb2.read() == '?') {
                return cb1.skipType();
            } else {
                cb2.unread();
                return cb1.compareTypeWith(cb2);
            }
        }
    }

    public static boolean matchMethodDescriptors(String d1, String d2) {
        CharBuffer cb1 = new CharBuffer(d1);
        CharBuffer cb2 = new CharBuffer(d2);

        if ((cb1.read() != '(') || (cb2.read() != '('))
            return false;

        if (cb1.read() == '*') {
            return true;
        }
        if (cb2.read() == '*') {
            return true;
        }

        cb1.unread();
        cb2.unread();

        // Check parameter descriptors
        while (cb2.get() != ')') {
            if (!matchDescriptors(cb1, cb2))
                return false;
        }

        if ((cb1.read() != ')') || (cb2.read() != ')'))
            return false;

        // Check return descriptor
        return matchDescriptors(cb1, cb2);
    }

    protected static class CharBuffer {
        protected char[] buffer;
        protected int length;
        protected int offset;

        public CharBuffer(String s) {
            this.buffer = s.toCharArray();
            this.length = buffer.length;
            this.offset = 0;
        }

        public char read() {
            if (offset < length)
                return buffer[offset++];
            else
                return (char)0;
        }

        public boolean unread() {
            if (offset > 0) {
                offset--;
                return true;
            } else {
                return false;
            }
        }

        public char get() {
            if (offset < length)
                return buffer[offset];
            else
                return (char)0;
        }

        public boolean skipType() {
            if (offset < length) {
                char c = buffer[offset++];

                while ((c == '[') && (offset < length)) {
                    c = buffer[offset++];
                }

                if (c == 'L') {
                    while (offset < length) {
                        if (buffer[offset++] == ';')
                            return true;
                    }
                } else if (c != '[') {
                    return true;
                }
            }
            return false;
        }

        public boolean compareTypeWith(CharBuffer other) {
            if (offset >= length)
                return false;

            char c = buffer[offset++];

            if (c != other.read())
                return false;

            if (c == 'L') {
                if ((offset >= length) || (other.offset >= other.length))
                    return false;

                char[] otherBuffer = other.buffer;

                if ((buffer[offset] == '*') || (otherBuffer[other.offset] == '*')) {
                    int start = offset;
                    int otherStart = other.offset;

                    // Search ';'
                    if (!searchEndOfType() || !other.searchEndOfType())
                        return false;

                    int current = offset - 1;
                    int otherCurrent = other.offset - 1;

                    // Backward comparison
                    while ((start < current) && (otherStart < otherCurrent)) {
                        c = buffer[--current];
                        if (c == '*')
                            return true;

                        char otherC = otherBuffer[--otherCurrent];
                        if (otherC == '*')
                            return true;
                        if (c != otherC)
                            return false;
                    }
                } else {
                    // Forward comparison
                    while (offset < length) {
                        c = buffer[offset++];
                        if (c != other.read())
                            return false;
                        if (c == ';')
                            return true;
                    }
                    return false;
                }
            }

            return true;
        }

        protected boolean searchEndOfType() {
            while (offset < length) {
                if (buffer[offset++] == ';')
                    return true;
            }
            return false;
        }

        public String toString() {
            return new String(buffer, offset, length-offset);
        }
    }
}
