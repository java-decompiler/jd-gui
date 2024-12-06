/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1;

import junit.framework.TestCase;
import org.jd.core.v1.model.classfile.attribute.CodeException;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.ControlFlowGraphMaker;
import org.junit.Test;

import java.util.Arrays;

public class CodeExceptionComparatorTest extends TestCase {
    @Test
    public void test() throws Exception {
        // CodeException(int index, int startPc, int endPc, int handlerPc, int catchType)
        CodeException ce0 = new CodeException(0, 10, 80, 50, 1);
        CodeException ce1 = new CodeException(1, 20, 30, 35, 2);
        CodeException ce2 = new CodeException(2, 20, 25, 35, 3);
        CodeException ce3 = new CodeException(3, 25, 30, 40, 0);
        CodeException ce4 = new CodeException(4, 10, 30, 60, 0);

        CodeException[] codeExceptions = { ce0, ce1, ce2, ce3, ce4 };

        ControlFlowGraphMaker.CodeExceptionComparator comparator = new ControlFlowGraphMaker.CodeExceptionComparator();

        Arrays.sort(codeExceptions, comparator);

        assertTrue(codeExceptions[0] == ce4);
        assertTrue(codeExceptions[1] == ce0);
        assertTrue(codeExceptions[2] == ce2);
        assertTrue(codeExceptions[3] == ce1);
    }
}