package org.jd.gui.util.matcher;

import junit.framework.TestCase;
import org.junit.Assert;

public class DescriptorMatcherTest extends TestCase {
    public void testMatchFieldDescriptors() {
        Assert.assertTrue(DescriptorMatcher.matchFieldDescriptors("?", "?"));

        Assert.assertTrue(DescriptorMatcher.matchFieldDescriptors("I", "I"));
        Assert.assertTrue(DescriptorMatcher.matchFieldDescriptors("?", "I"));
        Assert.assertTrue(DescriptorMatcher.matchFieldDescriptors("I", "?"));

        Assert.assertTrue(DescriptorMatcher.matchFieldDescriptors("Ltest/Test;", "Ltest/Test;"));
        Assert.assertTrue(DescriptorMatcher.matchFieldDescriptors("?", "Ltest/Test;"));
        Assert.assertTrue(DescriptorMatcher.matchFieldDescriptors("Ltest/Test;", "?"));
        Assert.assertTrue(DescriptorMatcher.matchFieldDescriptors("L*/Test;", "Ltest/Test;"));
        Assert.assertTrue(DescriptorMatcher.matchFieldDescriptors("Ltest/Test;", "L*/Test;"));

        Assert.assertTrue(DescriptorMatcher.matchFieldDescriptors("L*/Test;", "L*/Test;"));
        Assert.assertTrue(DescriptorMatcher.matchFieldDescriptors("?", "L*/Test;"));
        Assert.assertTrue(DescriptorMatcher.matchFieldDescriptors("L*/Test;", "?"));

        Assert.assertTrue(DescriptorMatcher.matchFieldDescriptors("[Z", "[Z"));
        Assert.assertTrue(DescriptorMatcher.matchFieldDescriptors("[Z", "?"));
        Assert.assertTrue(DescriptorMatcher.matchFieldDescriptors("?", "[Z"));

        Assert.assertTrue(DescriptorMatcher.matchFieldDescriptors("Ltest/Test;", "Ltest/Test;"));
        Assert.assertTrue(DescriptorMatcher.matchFieldDescriptors("Ltest/Test;", "?"));
        Assert.assertTrue(DescriptorMatcher.matchFieldDescriptors("?", "Ltest/Test;"));

        Assert.assertTrue(DescriptorMatcher.matchFieldDescriptors("[[[Ltest/Test;", "[[[Ltest/Test;"));
        Assert.assertTrue(DescriptorMatcher.matchFieldDescriptors("[[[Ltest/Test;", "?"));
        Assert.assertTrue(DescriptorMatcher.matchFieldDescriptors("?", "[[[Ltest/Test;"));

        Assert.assertTrue(DescriptorMatcher.matchFieldDescriptors("[[[L*/Test;", "[[[L*/Test;"));
        Assert.assertTrue(DescriptorMatcher.matchFieldDescriptors("[[[L*/Test;", "?"));
        Assert.assertTrue(DescriptorMatcher.matchFieldDescriptors("?", "[[[L*/Test;"));
    }

    public void testMatchMethodDescriptors() {
        Assert.assertFalse(DescriptorMatcher.matchMethodDescriptors("I", "I"));

        Assert.assertTrue(DescriptorMatcher.matchMethodDescriptors("()I", "()I"));
        Assert.assertTrue(DescriptorMatcher.matchMethodDescriptors("(*)?", "()I"));
        Assert.assertTrue(DescriptorMatcher.matchMethodDescriptors("()I", "(*)?"));

        Assert.assertTrue(DescriptorMatcher.matchMethodDescriptors("(I)I", "(I)I"));
        Assert.assertTrue(DescriptorMatcher.matchMethodDescriptors("(*)?", "(I)I"));
        Assert.assertTrue(DescriptorMatcher.matchMethodDescriptors("(I)I", "(*)?"));

        Assert.assertTrue(DescriptorMatcher.matchMethodDescriptors("(IJ)I", "(IJ)I"));
        Assert.assertTrue(DescriptorMatcher.matchMethodDescriptors("(*)?", "(IJ)I"));
        Assert.assertTrue(DescriptorMatcher.matchMethodDescriptors("(IJ)I", "(*)?"));

        Assert.assertTrue(DescriptorMatcher.matchMethodDescriptors("(Ltest/Test;)Ltest/Test;", "(Ltest/Test;)Ltest/Test;"));
        Assert.assertTrue(DescriptorMatcher.matchMethodDescriptors("(*)?", "(Ltest/Test;)Ltest/Test;"));
        Assert.assertTrue(DescriptorMatcher.matchMethodDescriptors("(Ltest/Test;)Ltest/Test;", "(*)?"));
        Assert.assertTrue(DescriptorMatcher.matchMethodDescriptors("([[Ltest/Test;[[Ltest/Test;)Ltest/Test;", "([[L*/Test;[[L*/Test;)L*/Test;"));
        Assert.assertTrue(DescriptorMatcher.matchMethodDescriptors("([[L*/Test;[[L*/Test;)L*/Test;", "([[Ltest/Test;[[Ltest/Test;)Ltest/Test;"));

        Assert.assertTrue(DescriptorMatcher.matchMethodDescriptors("(Ltest/Test;Ltest/Test;)Ltest/Test;", "(Ltest/Test;Ltest/Test;)Ltest/Test;"));
        Assert.assertTrue(DescriptorMatcher.matchMethodDescriptors("(*)?", "(Ltest/Test;Ltest/Test;)Ltest/Test;"));
        Assert.assertTrue(DescriptorMatcher.matchMethodDescriptors("(Ltest/Test;Ltest/Test;)Ltest/Test;", "(*)?"));

        Assert.assertTrue(DescriptorMatcher.matchMethodDescriptors("([[Ltest/Test;[[Ltest/Test;)Ltest/Test;", "([[Ltest/Test;[[Ltest/Test;)Ltest/Test;"));
        Assert.assertTrue(DescriptorMatcher.matchMethodDescriptors("(*)?", "([[Ltest/Test;[[Ltest/Test;)Ltest/Test;"));
        Assert.assertTrue(DescriptorMatcher.matchMethodDescriptors("([[Ltest/Test;[[Ltest/Test;)Ltest/Test;", "(*)?"));
        Assert.assertTrue(DescriptorMatcher.matchMethodDescriptors("([[L*/Test;[[L*/Test;)L*/Test;", "([[Ltest/Test;[[Ltest/Test;)Ltest/Test;"));
        Assert.assertTrue(DescriptorMatcher.matchMethodDescriptors("([[Ltest/Test;[[Ltest/Test;)Ltest/Test;", "([[L*/Test;[[L*/Test;)L*/Test;"));

        Assert.assertTrue(DescriptorMatcher.matchMethodDescriptors("(L*/Test;)L*/Test;", "(L*/Test;)L*/Test;"));
        Assert.assertTrue(DescriptorMatcher.matchMethodDescriptors("(*)?", "(L*/Test;)L*/Test;"));
        Assert.assertTrue(DescriptorMatcher.matchMethodDescriptors("(L*/Test;)L*/Test;", "(*)?"));

        Assert.assertTrue(DescriptorMatcher.matchMethodDescriptors("(L*/Test;L*/Test;)L*/Test;", "(L*/Test;L*/Test;)L*/Test;"));
        Assert.assertTrue(DescriptorMatcher.matchMethodDescriptors("(*)?", "(L*/Test;L*/Test;)L*/Test;"));
        Assert.assertTrue(DescriptorMatcher.matchMethodDescriptors("(Ltest/Test;Ltest/Test;)Ltest/Test;", "(*)?"));

        Assert.assertTrue(DescriptorMatcher.matchMethodDescriptors("([[L*/Test;[[L*/Test;)L*/Test;", "([[L*/Test;[[L*/Test;)L*/Test;"));
        Assert.assertTrue(DescriptorMatcher.matchMethodDescriptors("(*)?", "([[L*/Test;[[L*/Test;)L*/Test;"));
        Assert.assertTrue(DescriptorMatcher.matchMethodDescriptors("([[L*/Test;[[L*/Test;)L*/Test;", "(*)?"));
    }
}
