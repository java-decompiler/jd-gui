/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.util.matcher

class DescriptorMatcherTest extends GroovyTestCase {
    void testMatchFieldDescriptors() {
        assertTrue DescriptorMatcher.matchFieldDescriptors("?", "?")

        assertTrue DescriptorMatcher.matchFieldDescriptors("I", "I")
        assertTrue DescriptorMatcher.matchFieldDescriptors("?", "I")
        assertTrue DescriptorMatcher.matchFieldDescriptors("I", "?")

        assertTrue DescriptorMatcher.matchFieldDescriptors("Ltest/Test;", "Ltest/Test;")
        assertTrue DescriptorMatcher.matchFieldDescriptors("?", "Ltest/Test;")
        assertTrue DescriptorMatcher.matchFieldDescriptors("Ltest/Test;", "?")
        assertTrue DescriptorMatcher.matchFieldDescriptors("L*/Test;", "Ltest/Test;")
        assertTrue DescriptorMatcher.matchFieldDescriptors("Ltest/Test;", "L*/Test;")

        assertTrue DescriptorMatcher.matchFieldDescriptors("L*/Test;", "L*/Test;")
        assertTrue DescriptorMatcher.matchFieldDescriptors("?", "L*/Test;")
        assertTrue DescriptorMatcher.matchFieldDescriptors("L*/Test;", "?")

        assertTrue DescriptorMatcher.matchFieldDescriptors("[Z", "[Z")
        assertTrue DescriptorMatcher.matchFieldDescriptors("[Z", "?")
        assertTrue DescriptorMatcher.matchFieldDescriptors("?", "[Z")

        assertTrue DescriptorMatcher.matchFieldDescriptors("Ltest/Test;", "Ltest/Test;")
        assertTrue DescriptorMatcher.matchFieldDescriptors("Ltest/Test;", "?")
        assertTrue DescriptorMatcher.matchFieldDescriptors("?", "Ltest/Test;")

        assertTrue DescriptorMatcher.matchFieldDescriptors("[[[Ltest/Test;", "[[[Ltest/Test;")
        assertTrue DescriptorMatcher.matchFieldDescriptors("[[[Ltest/Test;", "?")
        assertTrue DescriptorMatcher.matchFieldDescriptors("?", "[[[Ltest/Test;")

        assertTrue DescriptorMatcher.matchFieldDescriptors("[[[L*/Test;", "[[[L*/Test;")
        assertTrue DescriptorMatcher.matchFieldDescriptors("[[[L*/Test;", "?")
        assertTrue DescriptorMatcher.matchFieldDescriptors("?", "[[[L*/Test;")
    }

    void testMatchMethodDescriptors() {
        assertFalse DescriptorMatcher.matchMethodDescriptors("I", "I")

        assertTrue DescriptorMatcher.matchMethodDescriptors("()I", "()I")
        assertTrue DescriptorMatcher.matchMethodDescriptors("(*)?", "()I")
        assertTrue DescriptorMatcher.matchMethodDescriptors("()I", "(*)?")

        assertTrue DescriptorMatcher.matchMethodDescriptors("(I)I", "(I)I")
        assertTrue DescriptorMatcher.matchMethodDescriptors("(*)?", "(I)I")
        assertTrue DescriptorMatcher.matchMethodDescriptors("(I)I", "(*)?")

        assertTrue DescriptorMatcher.matchMethodDescriptors("(IJ)I", "(IJ)I")
        assertTrue DescriptorMatcher.matchMethodDescriptors("(*)?", "(IJ)I")
        assertTrue DescriptorMatcher.matchMethodDescriptors("(IJ)I", "(*)?")

        assertTrue DescriptorMatcher.matchMethodDescriptors("(Ltest/Test;)Ltest/Test;", "(Ltest/Test;)Ltest/Test;")
        assertTrue DescriptorMatcher.matchMethodDescriptors("(*)?", "(Ltest/Test;)Ltest/Test;")
        assertTrue DescriptorMatcher.matchMethodDescriptors("(Ltest/Test;)Ltest/Test;", "(*)?")
        assertTrue DescriptorMatcher.matchMethodDescriptors("([[Ltest/Test;[[Ltest/Test;)Ltest/Test;", "([[L*/Test;[[L*/Test;)L*/Test;")
        assertTrue DescriptorMatcher.matchMethodDescriptors("([[L*/Test;[[L*/Test;)L*/Test;", "([[Ltest/Test;[[Ltest/Test;)Ltest/Test;")

        assertTrue DescriptorMatcher.matchMethodDescriptors("(Ltest/Test;Ltest/Test;)Ltest/Test;", "(Ltest/Test;Ltest/Test;)Ltest/Test;")
        assertTrue DescriptorMatcher.matchMethodDescriptors("(*)?", "(Ltest/Test;Ltest/Test;)Ltest/Test;")
        assertTrue DescriptorMatcher.matchMethodDescriptors("(Ltest/Test;Ltest/Test;)Ltest/Test;", "(*)?")

        assertTrue DescriptorMatcher.matchMethodDescriptors("([[Ltest/Test;[[Ltest/Test;)Ltest/Test;", "([[Ltest/Test;[[Ltest/Test;)Ltest/Test;")
        assertTrue DescriptorMatcher.matchMethodDescriptors("(*)?", "([[Ltest/Test;[[Ltest/Test;)Ltest/Test;")
        assertTrue DescriptorMatcher.matchMethodDescriptors("([[Ltest/Test;[[Ltest/Test;)Ltest/Test;", "(*)?")
        assertTrue DescriptorMatcher.matchMethodDescriptors("([[L*/Test;[[L*/Test;)L*/Test;", "([[Ltest/Test;[[Ltest/Test;)Ltest/Test;")
        assertTrue DescriptorMatcher.matchMethodDescriptors("([[Ltest/Test;[[Ltest/Test;)Ltest/Test;", "([[L*/Test;[[L*/Test;)L*/Test;")

        assertTrue DescriptorMatcher.matchMethodDescriptors("(L*/Test;)L*/Test;", "(L*/Test;)L*/Test;")
        assertTrue DescriptorMatcher.matchMethodDescriptors("(*)?", "(L*/Test;)L*/Test;")
        assertTrue DescriptorMatcher.matchMethodDescriptors("(L*/Test;)L*/Test;", "(*)?")

        assertTrue DescriptorMatcher.matchMethodDescriptors("(L*/Test;L*/Test;)L*/Test;", "(L*/Test;L*/Test;)L*/Test;")
        assertTrue DescriptorMatcher.matchMethodDescriptors("(*)?", "(L*/Test;L*/Test;)L*/Test;")
        assertTrue DescriptorMatcher.matchMethodDescriptors("(Ltest/Test;Ltest/Test;)Ltest/Test;", "(*)?")

        assertTrue DescriptorMatcher.matchMethodDescriptors("([[L*/Test;[[L*/Test;)L*/Test;", "([[L*/Test;[[L*/Test;)L*/Test;")
        assertTrue DescriptorMatcher.matchMethodDescriptors("(*)?", "([[L*/Test;[[L*/Test;)L*/Test;")
        assertTrue DescriptorMatcher.matchMethodDescriptors("([[L*/Test;[[L*/Test;)L*/Test;", "(*)?")
    }
}
