/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1;

import junit.framework.TestCase;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.loader.ClassPathLoader;
import org.jd.core.v1.loader.ZipLoader;
import org.jd.core.v1.model.classfile.Method;
import org.jd.core.v1.model.javasyntax.CompilationUnit;
import org.jd.core.v1.model.javasyntax.declaration.*;
import org.jd.core.v1.model.message.Message;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.cfg.BasicBlock;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.cfg.ControlFlowGraph;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.cfg.Loop;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.declaration.*;
import org.jd.core.v1.service.converter.classfiletojavasyntax.processor.ConvertClassFileProcessor;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.*;
import org.jd.core.v1.service.deserializer.classfile.DeserializeClassFileProcessor;
import org.jd.core.v1.cfg.ControlFlowGraphPlantUMLWriter;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;

import static org.jd.core.v1.service.converter.classfiletojavasyntax.model.cfg.BasicBlock.*;

public class ControlFlowGraphTest extends TestCase {
    protected DeserializeClassFileProcessor deserializer = new DeserializeClassFileProcessor();
    protected ConvertClassFileProcessor converter = new ConvertClassFileProcessor();
    protected ClassPathLoader loader = new ClassPathLoader();
    protected TypeMaker typeMaker = new TypeMaker(loader);

    // --- Basic test ----------------------------------------------------------------------------------------------- //
    @Test
    public void testJdk170BasicDoSomethingWithString() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/Basic", "doSomethingWithString"));
    }


    // --- Test 'if' and 'if-else' ---------------------------------------------------------------------------------- //
    @Test
    public void testJdk170If() throws Exception {
        checkIfReduction(checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/IfElse", "if_")));
    }

    @Test
    public void testJdk170IfIf() throws Exception {
        ControlFlowGraph cfg = checkIfReduction(checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/IfElse", "ifIf")));

        BasicBlock ifBB = cfg.getStart().getNext().getNext();

        assertEquals(ifBB.getType(), TYPE_IF);
        assertEquals(ifBB.getSub1().getType(), TYPE_IF);
    }

    @Test
    public void testJdk170MethodCallInIfCondition() throws Exception {
        checkIfElseReduction(checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/IfElse", "methodCallInIfCondition")));
    }

    @Test
    public void testJdk170IlElse() throws Exception {
        ControlFlowGraph cfg = checkIfElseReduction(checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/IfElse", "ifElse")));
        BasicBlock ifElseBB = cfg.getStart().getNext().getNext();

        assertEquals(ifElseBB.getType(), TYPE_IF_ELSE);
        assertEquals(ifElseBB.getCondition().getType(), TYPE_CONDITION);
        assertTrue(ifElseBB.getCondition().mustInverseCondition());
    }

    @Test
    public void testJdk170IlElseIfElse() throws Exception {
        ControlFlowGraph cfg = checkIfElseReduction(checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/IfElse", "ifElseIfElse")));
        BasicBlock ifElseBB = cfg.getStart().getNext().getNext();

        assertEquals(ifElseBB.getSub2().getType(), TYPE_IF_ELSE);
    }

    @Test
    public void testJdk170IfORCondition() throws Exception {
        ControlFlowGraph cfg = checkIfReduction(checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/IfElse", "ifORCondition")));
        BasicBlock ifBB = cfg.getStart().getNext().getNext();

        assertEquals(ifBB.getCondition().getType(), TYPE_CONDITION_OR);
        assertEquals(ifBB.getCondition().getSub1().getType(), TYPE_CONDITION);
        assertFalse(ifBB.getCondition().getSub1().mustInverseCondition());
        assertEquals(ifBB.getCondition().getSub2().getType(), TYPE_CONDITION_OR);
        assertEquals(ifBB.getCondition().getSub2().getSub1().getType(), TYPE_CONDITION);
        assertFalse(ifBB.getCondition().getSub2().getSub1().mustInverseCondition());
        assertEquals(ifBB.getCondition().getSub2().getSub2().getType(), TYPE_CONDITION);
        assertTrue(ifBB.getCondition().getSub2().getSub2().mustInverseCondition());
    }

    @Test
    public void testJdk170IfANDCondition() throws Exception {
        ControlFlowGraph cfg = checkIfReduction(checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/IfElse", "ifANDCondition")));
        BasicBlock ifBB = cfg.getStart().getNext().getNext();

        assertEquals(ifBB.getCondition().getType(), TYPE_CONDITION_AND);
        assertEquals(ifBB.getCondition().getSub1().getType(), TYPE_CONDITION);
        assertTrue(ifBB.getCondition().getSub1().mustInverseCondition());
        assertEquals(ifBB.getCondition().getSub2().getType(), TYPE_CONDITION_AND);
        assertEquals(ifBB.getCondition().getSub2().getSub1().getType(), TYPE_CONDITION);
        assertTrue(ifBB.getCondition().getSub2().getSub1().mustInverseCondition());
        assertEquals(ifBB.getCondition().getSub2().getSub2().getType(), TYPE_CONDITION);
        assertTrue(ifBB.getCondition().getSub2().getSub2().mustInverseCondition());
    }

    @Test
    public void testJdk170IfElseORCondition() throws Exception {
        ControlFlowGraph cfg = checkIfElseReduction(checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/IfElse", "ifElseORCondition")));
        BasicBlock ifElseBB = cfg.getStart().getNext().getNext();

        assertEquals(ifElseBB.getCondition().getType(), TYPE_CONDITION_OR);
        assertEquals(ifElseBB.getCondition().getSub1().getType(), TYPE_CONDITION);
        assertFalse(ifElseBB.getCondition().getSub1().mustInverseCondition());
        assertEquals(ifElseBB.getCondition().getSub2().getType(), TYPE_CONDITION_OR);
    }

    @Test
    public void testJdk170IfElseANDCondition() throws Exception {
        ControlFlowGraph cfg = checkIfElseReduction(checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/IfElse", "ifElseANDCondition")));
        BasicBlock ifElseBB = cfg.getStart().getNext().getNext();

        assertEquals(ifElseBB.getCondition().getType(), TYPE_CONDITION_AND);
        assertEquals(ifElseBB.getCondition().getSub1().getType(), TYPE_CONDITION);
        assertTrue(ifElseBB.getCondition().getSub1().mustInverseCondition());
        assertEquals(ifElseBB.getCondition().getSub2().getType(), TYPE_CONDITION_AND);
    }

    @Test
    public void testJdk170IfElse6ANDAnd2ORCondition() throws Exception {
        ControlFlowGraph cfg = checkIfElseReduction(checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/IfElse", "ifElse6ANDAnd2ORCondition")));
        BasicBlock ifElseBB = cfg.getStart().getNext().getNext();

        assertEquals(ifElseBB.getCondition().getType(), TYPE_CONDITION_OR);
        assertEquals(ifElseBB.getCondition().getSub1().getType(), TYPE_CONDITION_AND);
        assertEquals(ifElseBB.getCondition().getSub2().getType(), TYPE_CONDITION_OR);
    }

    @Test
    public void testJdk170IfElse6ORAnd2ANDCondition() throws Exception {
        ControlFlowGraph cfg = checkIfElseReduction(checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/IfElse", "ifElse6ORAnd2ANDCondition")));
        BasicBlock ifElseBB = cfg.getStart().getNext().getNext();

        assertEquals(ifElseBB.getCondition().getType(), TYPE_CONDITION_AND);
        assertEquals(ifElseBB.getCondition().getSub1().getType(), TYPE_CONDITION_OR);
        assertEquals(ifElseBB.getCondition().getSub2().getType(), TYPE_CONDITION_AND);
    }

    @Test
    public void testJdk170IfElseORAndANDConditions() throws Exception {
        ControlFlowGraph cfg = checkIfElseReduction(checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/IfElse", "ifElseORAndANDConditions")));
        BasicBlock ifElseBB = cfg.getStart().getNext().getNext();

        assertEquals(ifElseBB.getType(), TYPE_IF_ELSE);
        assertEquals(ifElseBB.getCondition().getType(), TYPE_CONDITION_AND);
        assertEquals(ifElseBB.getCondition().getSub1().getType(), TYPE_CONDITION_OR);
        assertEquals(ifElseBB.getCondition().getSub2().getType(), TYPE_CONDITION_AND);
    }

    @Test
    public void testIfElseANDAndORConditions() throws Exception {
        ControlFlowGraph cfg = checkIfElseReduction(checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/IfElse", "ifElseANDAndORConditions")));
        BasicBlock ifElseBB = cfg.getStart().getNext().getNext();

        assertEquals(ifElseBB.getType(), TYPE_IF_ELSE);
        assertEquals(ifElseBB.getCondition().getType(), TYPE_CONDITION_OR);
        assertEquals(ifElseBB.getCondition().getSub1().getType(), TYPE_CONDITION_AND);
        assertEquals(ifElseBB.getCondition().getSub2().getType(), TYPE_CONDITION_OR);
    }

    protected static ControlFlowGraph checkIfReduction(ControlFlowGraph cfg) throws Exception {
        BasicBlock ifBB = checkIfCommonReduction(cfg);

        assertEquals(ifBB.getType(), TYPE_IF);

        return cfg;
    }

    protected static ControlFlowGraph checkIfElseReduction(ControlFlowGraph cfg) throws Exception {
        BasicBlock ifElseBB = checkIfCommonReduction(cfg);

        assertEquals(ifElseBB.getType(), TYPE_IF_ELSE);
        assertNotNull(ifElseBB.getSub2());
        assertEquals(ifElseBB.getSub2().getNext(), END);

        return cfg;
    }

    protected static BasicBlock checkIfCommonReduction(ControlFlowGraph cfg) throws Exception {
        BasicBlock startBB = cfg.getStart();

        assertNotNull(startBB);
        assertEquals(startBB.getType(), TYPE_START);

        assertNotNull(startBB.getNext());

        BasicBlock ifBB = startBB.getNext().getNext();

        assertNotNull(ifBB.getCondition());
        assertNotNull(ifBB.getSub1());
        assertEquals(ifBB.getSub1().getNext(), END);
        assertNotNull(ifBB.getNext());

        return ifBB;
    }


    // --- Test outer & inner classes ------------------------------------------------------------------------------- //
    @Test
    public void testJdk170OuterClass() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/OuterClass", "<init>"));
    }


    // --- Test ternary operator ------------------------------------------------------------------------------------ //
    @Test
    public void testJdk170TernaryOperatorsInTernaryOperator() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TernaryOperator", "ternaryOperatorsInTernaryOperator"));
    }

    @Test
    public void testJdk118TernaryOperatorsInReturn() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.1.8.zip"), "org/jd/core/test/TernaryOperator", "ternaryOperatorsInReturn"));
    }

    @Test
    public void testJdk170TernaryOperatorsInReturn() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TernaryOperator", "ternaryOperatorsInReturn"));
    }

    @Test
    public void testJdk118TernaryOperatorInIf1() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.1.8.zip"), "org/jd/core/test/TernaryOperator", "ternaryOperatorInIf1"));

        BasicBlock ifElseBB = cfg.getStart().getNext().getNext();

        assertEquals(ifElseBB.getType(), TYPE_IF);
        assertEquals(ifElseBB.getNext().getType(), TYPE_STATEMENTS);
        assertEquals(ifElseBB.getNext().getNext().getType(), TYPE_RETURN);

        BasicBlock conditionTernaryOperatorBB = ifElseBB.getCondition();

        assertEquals(conditionTernaryOperatorBB.getType(), TYPE_CONDITION_TERNARY_OPERATOR);
        assertEquals(conditionTernaryOperatorBB.getCondition().getType(), TYPE_CONDITION);
        assertTrue(conditionTernaryOperatorBB.getCondition().mustInverseCondition());
        assertEquals(conditionTernaryOperatorBB.getSub1().getType(), TYPE_GOTO_IN_TERNARY_OPERATOR);
        assertEquals(conditionTernaryOperatorBB.getSub2().getType(), TYPE_STATEMENTS);
    }

    @Test
    public void testJdk170TernaryOperatorInIf1() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TernaryOperator", "ternaryOperatorInIf1"));
    }

    @Test
    public void testJdk118TernaryOperatorInIfElse1() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.1.8.zip"), "org/jd/core/test/TernaryOperator", "ternaryOperatorInIfElse1"));
    }

    @Test
    public void testJdk170TernaryOperatorInIfElse1() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TernaryOperator", "ternaryOperatorInIfElse1"));
    }

    @Test
    public void testJdk118TernaryOperatorInIfElse2() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.1.8.zip"), "org/jd/core/test/TernaryOperator", "ternaryOperatorInIfElse2"));
    }

    @Test
    public void testJdk170TernaryOperatorInIfElse2() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TernaryOperator", "ternaryOperatorInIfElse2"));
    }

    @Test
    public void testJdk118TernaryOperatorInIfElse3() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.1.8.zip"), "org/jd/core/test/TernaryOperator", "ternaryOperatorInIfElse3"));

        BasicBlock ifElseBB = cfg.getStart().getNext().getNext();

        assertEquals(ifElseBB.getType(), TYPE_IF_ELSE);
        assertEquals(ifElseBB.getNext().getType(), TYPE_STATEMENTS);
        assertEquals(ifElseBB.getNext().getNext().getType(), TYPE_RETURN);

        BasicBlock conditionAndBB = ifElseBB.getCondition();

        assertEquals(conditionAndBB.getType(), TYPE_CONDITION_AND);
        assertEquals(conditionAndBB.getSub1().getType(), TYPE_CONDITION);
        assertTrue(conditionAndBB.getSub1().mustInverseCondition());
        assertEquals(conditionAndBB.getSub2().getType(), TYPE_CONDITION);
        assertFalse(conditionAndBB.getSub2().mustInverseCondition());
    }

    @Test
    public void testJdk170TernaryOperatorInIfElse3() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TernaryOperator", "ternaryOperatorInIfElse3"));
    }

    @Test
    public void testJdk118TernaryOperatorInIfElse4() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.1.8.zip"), "org/jd/core/test/TernaryOperator", "ternaryOperatorInIfElse4"));
    }

    @Test
    public void testJdk118TernaryOperatorInIfElse5() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.1.8.zip"), "org/jd/core/test/TernaryOperator", "ternaryOperatorInIfElse5"));
    }

    @Test
    public void testJdk118TernaryOperatorInIfElse6() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.1.8.zip"), "org/jd/core/test/TernaryOperator", "ternaryOperatorInIfElse6"));
    }

    @Test
    public void testJdk118TernaryOperatorInIfElseFalse() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.1.8.zip"), "org/jd/core/test/TernaryOperator", "ternaryOperatorInIfElseFalse"));
    }

    @Test
    public void testJdk170TernaryOperatorInIfElseFalse() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TernaryOperator", "ternaryOperatorInIfElseFalse"));
    }

    @Test
    public void testJdk118TernaryOperatorInIfElseANDCondition() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.1.8.zip"), "org/jd/core/test/TernaryOperator", "ternaryOperatorInIfElseANDCondition"));
    }

    @Test
    public void testJdk170TernaryOperatorInIfElseANDCondition() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TernaryOperator", "ternaryOperatorInIfElseANDCondition"));
    }

    @Test
    public void testJdk118TernaryOperatorInIfElseORCondition() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.1.8.zip"), "org/jd/core/test/TernaryOperator", "ternaryOperatorInIfElseORCondition"));

        BasicBlock ifElseBB = cfg.getStart().getNext().getNext();

        assertEquals(ifElseBB.getType(), TYPE_IF_ELSE);
        assertEquals(ifElseBB.getNext().getType(), TYPE_STATEMENTS);
        assertEquals(ifElseBB.getNext().getNext().getType(), TYPE_RETURN);

        BasicBlock conditionOrBB = ifElseBB.getCondition();

        assertEquals(conditionOrBB.getType(), TYPE_CONDITION_OR);
        assertEquals(conditionOrBB.getSub2().getType(), TYPE_CONDITION);
        assertTrue(conditionOrBB.getSub2().mustInverseCondition());

        BasicBlock conditionOrBB2 = conditionOrBB.getSub1();

        assertEquals(conditionOrBB2.getType(), TYPE_CONDITION_OR);
        assertEquals(conditionOrBB2.getSub1().getType(), TYPE_CONDITION);
        assertFalse(conditionOrBB2.getSub1().mustInverseCondition());

        BasicBlock conditionTernaryOperatorBB = conditionOrBB2.getSub2();

        assertEquals(conditionTernaryOperatorBB.getType(), TYPE_CONDITION_TERNARY_OPERATOR);
        assertEquals(conditionTernaryOperatorBB.getCondition().getType(), TYPE_CONDITION);
        assertTrue(conditionTernaryOperatorBB.getCondition().mustInverseCondition());
        assertEquals(conditionTernaryOperatorBB.getSub1().getType(), TYPE_GOTO_IN_TERNARY_OPERATOR);
        assertEquals(conditionTernaryOperatorBB.getSub2().getType(), TYPE_STATEMENTS);
    }

    @Test
    public void testJdk170TernaryOperatorInIfElseORCondition() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TernaryOperator", "ternaryOperatorInIfElseORCondition"));

        BasicBlock ifElseBB = cfg.getStart().getNext().getNext();

        assertEquals(ifElseBB.getType(), TYPE_IF_ELSE);
        assertEquals(ifElseBB.getNext().getType(), TYPE_STATEMENTS);
        assertEquals(ifElseBB.getNext().getNext().getType(), TYPE_RETURN);

        BasicBlock conditionOrBB = ifElseBB.getCondition();

        assertEquals(conditionOrBB.getType(), TYPE_CONDITION_OR);
        assertEquals(conditionOrBB.getSub2().getType(), TYPE_CONDITION);
        assertTrue(conditionOrBB.getSub2().mustInverseCondition());

        BasicBlock conditionOrBB2 = conditionOrBB.getSub1();

        assertEquals(conditionOrBB2.getType(), TYPE_CONDITION_OR);
        assertEquals(conditionOrBB2.getSub1().getType(), TYPE_CONDITION);
        assertFalse(conditionOrBB2.getSub1().mustInverseCondition());

        BasicBlock conditionTernaryOperatorBB = conditionOrBB2.getSub2();

        assertEquals(conditionTernaryOperatorBB.getType(), TYPE_CONDITION_TERNARY_OPERATOR);
        assertEquals(conditionTernaryOperatorBB.getCondition().getType(), TYPE_CONDITION);
        assertTrue(conditionTernaryOperatorBB.getCondition().mustInverseCondition());
        assertEquals(conditionTernaryOperatorBB.getSub1().getType(), TYPE_CONDITION);
        assertTrue(conditionTernaryOperatorBB.getSub1().mustInverseCondition());
        assertEquals(conditionTernaryOperatorBB.getSub2().getType(), TYPE_CONDITION);
        assertFalse(conditionTernaryOperatorBB.getSub2().mustInverseCondition());
    }


    // --- Test 'switch' -------------------------------------------------------------------------------------------- //
    @Test
    public void testJdk170SimpleSwitch() throws Exception {
        checkSwitchReduction(checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/Switch", "simpleSwitch")));
    }

    @Test
    public void testJdk170SwitchFirstBreakMissing() throws Exception {
        BasicBlock switchBB = checkSwitchReduction(checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/Switch", "switchFirstBreakMissing")));
        SwitchCase sc0 = switchBB.getSwitchCases().get(0);

        assertFalse(sc0.isDefaultCase());
        assertEquals(sc0.getValue(), 0);
        assertEquals(sc0.getBasicBlock().getNext(), END);

        SwitchCase sc1 = switchBB.getSwitchCases().get(1);

        assertFalse(sc1.isDefaultCase());
        assertEquals(sc1.getValue(), 1);
        assertEquals(sc0.getBasicBlock().getNext(), SWITCH_BREAK);
    }

    @Test
    public void testJdk170SwitchSecondBreakMissing() throws Exception {
        BasicBlock switchBB = checkSwitchReduction(checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/Switch", "switchSecondBreakMissing")));
        SwitchCase sc0 = switchBB.getSwitchCases().get(0);

        assertTrue(sc0.isDefaultCase());

        SwitchCase sc1 = switchBB.getSwitchCases().get(1);

        assertFalse(sc1.isDefaultCase());
        assertEquals(sc1.getValue(), 0);
        assertEquals(sc1.getBasicBlock().getType(), TYPE_STATEMENTS);
        assertEquals(sc1.getBasicBlock().getNext().getType(), TYPE_SWITCH_BREAK);

        SwitchCase sc2 = switchBB.getSwitchCases().get(2);

        assertFalse(sc2.isDefaultCase());
        assertEquals(sc2.getValue(), 1);
        assertEquals(sc2.getBasicBlock().getType(), TYPE_STATEMENTS);
        assertEquals(sc2.getBasicBlock().getNext().getType(), TYPE_END);
    }

    @Test
    public void testJdk170SwitchDefault() throws Exception {
        BasicBlock switchBB = checkSwitchReduction(checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/Switch", "switchDefault")));
        SwitchCase scDefault = switchBB.getSwitchCases().get(0);

        assertTrue(scDefault.isDefaultCase());

        SwitchCase sc1 = switchBB.getSwitchCases().get(1);

        assertFalse(sc1.isDefaultCase());
        assertEquals(sc1.getValue(), 0);
        assertEquals(sc1.getBasicBlock().getType(), TYPE_STATEMENTS);
        assertEquals(sc1.getBasicBlock().getNext().getType(), TYPE_END);

        SwitchCase sc2 = switchBB.getSwitchCases().get(2);

        assertFalse(sc2.isDefaultCase());
        assertEquals(sc2.getValue(), 1);
        assertEquals(sc2.getBasicBlock().getType(), TYPE_STATEMENTS);
        assertEquals(sc2.getBasicBlock().getNext().getType(), TYPE_SWITCH_BREAK);
    }

    @Test
    public void testJdk170LookupSwitchDefault() throws Exception {
        BasicBlock switchBB = checkSwitchReduction(checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/Switch", "lookupSwitchDefault")));
        SwitchCase scDefault = switchBB.getSwitchCases().get(0);

        assertTrue(scDefault.isDefaultCase());
        assertEquals(scDefault.getBasicBlock().getType(), TYPE_STATEMENTS);
        assertEquals(scDefault.getBasicBlock().getNext().getType(), TYPE_SWITCH_BREAK);

        SwitchCase sc1 = switchBB.getSwitchCases().get(1);

        assertFalse(sc1.isDefaultCase());
        assertEquals(sc1.getValue(), 0);
        assertEquals(sc1.getBasicBlock().getType(), TYPE_STATEMENTS);
        assertEquals(sc1.getBasicBlock().getNext().getType(), TYPE_END);

        SwitchCase sc2 = switchBB.getSwitchCases().get(2);

        assertFalse(sc2.isDefaultCase());
        assertEquals(sc2.getValue(), 1);
        assertEquals(sc2.getBasicBlock().getType(), TYPE_STATEMENTS);
        assertEquals(sc2.getBasicBlock().getNext().getType(), TYPE_SWITCH_BREAK);
    }

    @Test
    public void testJdk170SwitchOneExitInFirstCase() throws Exception {
        BasicBlock switchBB = checkSwitchReduction(checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/Switch", "switchOneExitInFirstCase")));
        SwitchCase scDefault = switchBB.getSwitchCases().get(0);

        assertTrue(scDefault.isDefaultCase());
        assertEquals(scDefault.getBasicBlock().getType(), TYPE_STATEMENTS);
        assertEquals(scDefault.getBasicBlock().getNext().getType(), TYPE_RETURN);

        SwitchCase sc2 = switchBB.getSwitchCases().get(2);

        assertFalse(sc2.isDefaultCase());
        assertEquals(sc2.getValue(), 1);
        assertEquals(sc2.getBasicBlock().getType(), TYPE_THROW);
    }

    @Test
    public void testJdk170SwitchOneExitInSecondCase() throws Exception {
        BasicBlock switchBB = checkSwitchReduction(checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/Switch", "switchOneExitInSecondCase")));
        SwitchCase scDefault = switchBB.getSwitchCases().get(0);

        assertTrue(scDefault.isDefaultCase());
        assertEquals(scDefault.getBasicBlock().getType(), TYPE_STATEMENTS);
        assertEquals(scDefault.getBasicBlock().getNext().getType(), TYPE_RETURN);

        SwitchCase sc1 = switchBB.getSwitchCases().get(1);

        assertFalse(sc1.isDefaultCase());
        assertEquals(sc1.getValue(), 0);
        assertEquals(sc1.getBasicBlock().getType(), TYPE_STATEMENTS);
        assertEquals(sc1.getBasicBlock().getNext().getType(), TYPE_RETURN);
    }

    @Test
    public void testJdk170SwitchOneExitInLastCase() throws Exception {
        BasicBlock switchBB = checkSwitchReduction(checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/Switch", "switchOneExitInLastCase")));
        SwitchCase sc0 = switchBB.getSwitchCases().get(0);

        assertFalse(sc0.isDefaultCase());
        assertEquals(sc0.getValue(), 0);
        assertEquals(sc0.getBasicBlock().getType(), TYPE_STATEMENTS);
        assertEquals(sc0.getBasicBlock().getNext().getType(), TYPE_RETURN);

        SwitchCase sc1 = switchBB.getSwitchCases().get(1);

        assertFalse(sc1.isDefaultCase());
        assertEquals(sc1.getValue(), 1);
        assertEquals(sc1.getBasicBlock().getType(), TYPE_STATEMENTS);
        assertEquals(sc1.getBasicBlock().getNext().getType(), TYPE_RETURN);
    }

    @Test
    public void testJdk170ComplexSwitch() throws Exception {
        BasicBlock switchBB = checkSwitchReduction(checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/Switch", "complexSwitch")));
        SwitchCase scDefault = switchBB.getSwitchCases().get(0);

        assertTrue(scDefault.isDefaultCase());

        SwitchCase sc1 = switchBB.getSwitchCases().get(1);

        assertFalse(sc1.isDefaultCase());
        assertEquals(sc1.getValue(), 1);

        SwitchCase sc2 = switchBB.getSwitchCases().get(2);

        assertFalse(sc2.isDefaultCase());
        assertEquals(sc2.getValue(), 2);
    }

    @Test
    public void testJdk170SwitchOnLastPosition() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/Switch", "switchOnLastPosition"));
    }

    @Test
    public void testJdk170SwitchFirstIfBreakMissing() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/Switch", "switchFirstIfBreakMissing"));
    }

    @Test
    public void testJdk170SwitchString() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/AdvancedSwitch", "switchString"));
    }

    protected static BasicBlock checkSwitchReduction(ControlFlowGraph cfg) throws Exception {
        BasicBlock startBB = cfg.getStart();

        assertNotNull(startBB);

        BasicBlock switchBB = startBB.getNext();

        assertNotNull(switchBB);
        assertEquals(switchBB.getType(), TYPE_SWITCH);

        BasicBlock next = switchBB.getNext();
        assertNotNull(next);
        assertEquals(next.getType(), TYPE_STATEMENTS);

        assertNotNull(next.getNext());
        assertEquals(next.getNext().getType(), TYPE_RETURN);

        return switchBB;
    }


    // --- Test 'while' --------------------------------------------------------------------------------------------- //
    @Test
    public void testJdk170SimpleWhile() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/While", "simpleWhile"));
    }

    @Test
    public void testJdk170WhileIfContinue() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/While", "whileIfContinue"));
    }

    @Test
    public void testJdk170WhileIfBreak() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/While", "whileIfBreak"));
    }

    @Test
    public void testJdk170WhileWhile() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/While", "whileWhile"));
    }

    @Test
    public void testJdk170WhileThrow() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/While", "whileThrow"));
    }

    @Test
    public void testJdk170WhileTrue() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/While", "whileTrue"));
    }

    @Test
    public void testJdk170WhileTryFinally() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/While", "whileTryFinally"));
    }

    @Test
    public void testJdk170TryWhileFinally() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/While", "tryWhileFinally"));
    }

    @Test
    public void testJdk170InfiniteWhileTryFinally() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/While", "infiniteWhileTryFinally"));
    }

    @Test
    public void testJdk170TryInfiniteWhileFinally() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/While", "tryInfiniteWhileFinally"));
    }

    @Test
    public void testJdk170WhileTrueIf() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/While", "whileTrueIf"));

        BasicBlock mainLoopBB = cfg.getStart().getNext().getNext();

        assertEquals(mainLoopBB.getType(), TYPE_LOOP);

        BasicBlock firstIfBB = mainLoopBB.getSub1().getNext();

        assertEquals(firstIfBB.getType(), TYPE_IF);

        BasicBlock innerLoopBB = firstIfBB.getSub1().getNext();

        assertEquals(innerLoopBB.getType(), TYPE_LOOP);
        assertEquals(innerLoopBB.getNext().getType(), TYPE_LOOP_END);

        BasicBlock secondIfBB = firstIfBB.getNext();

        assertEquals(secondIfBB.getSub1().getNext().getType(), TYPE_LOOP_START);
    }

    @Test
    public void testJdk170WhileContinueBreak() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/While", "whileContinueBreak"));
    }

    @Test
    public void testJdk170TwoWiles() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/While", "twoWiles"));

        BasicBlock firstLoopBB = cfg.getStart().getNext().getNext();

        assertEquals(firstLoopBB.getType(), TYPE_LOOP);

        BasicBlock nextLoopBB = firstLoopBB.getNext();

        assertEquals(nextLoopBB.getType(), TYPE_LOOP);

        BasicBlock stmtBB = nextLoopBB.getNext();

        assertEquals(stmtBB.getType(), TYPE_STATEMENTS);

        BasicBlock returnBB = stmtBB.getNext();

        assertEquals(returnBB.getType(), TYPE_RETURN);
    }

    // --- Test 'do-while' ------------------------------------------------------------------------------------------ //
    @Test
    public void testJdk170DoWhileWhile() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/DoWhile", "doWhileWhile"));
    }

    @Test
    public void testJdk170DoWhileTestPreInc() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/DoWhile", "doWhileTestPreInc"));
    }

    @Test
    public void testJdk170DoWhileTryFinally() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/DoWhile", "doWhileTryFinally"));
    }

    @Test
    public void testJdk170TryDoWhileFinally() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/DoWhile", "tryDoWhileFinally"));
    }

    // --- Test 'for' ----------------------------------------------------------------------------------------------- //
    @Test
    public void testJdk150ForTryReturn() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.5.0.zip"), "org/jd/core/test/For", "forTryReturn"));
        BasicBlock loopBB = cfg.getStart().getNext().getNext();

        assertEquals(loopBB.getType(), TYPE_LOOP);
        assertEquals(loopBB.getNext().getType(), TYPE_STATEMENTS);
        assertEquals(loopBB.getNext().getNext().getType(), TYPE_RETURN);

        BasicBlock bb = loopBB.getSub1();

        assertEquals(bb.getType(), TYPE_IF);
        assertEquals(bb.getNext().getType(), TYPE_LOOP_END);

        bb = bb.getSub1();

        assertEquals(bb.getType(), TYPE_STATEMENTS);
        assertEquals(bb.getNext().getType(), TYPE_TRY);
        assertEquals(bb.getNext().getNext().getType(), TYPE_STATEMENTS);
        assertEquals(bb.getNext().getNext().getNext().getType(), TYPE_LOOP_START);
    }

    @Test
    public void testJdk170IfForIfReturn() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/For", "ifForIfReturn"));
    }

    @Test
    public void testJdk170ForIfContinue() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/For", "forIfContinue"));
    }

    @Test
    public void testJdk170ForIfIfContinue() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/For", "forIfIfContinue"));
    }

    @Test
    public void testJdk170ForMultipleVariables2() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/For", "forMultipleVariables2"));
    }

    @Test
    public void testJdk170ForBreak() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/For", "forBreak"));
    }


    // --- Test 'break' and 'continue' ------------------------------------------------------------------------------ //
    @Test
    public void testJdk170DoWhileContinue() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/BreakContinue", "doWhileContinue"));

        BasicBlock mainLoopBB = cfg.getStart().getNext().getNext();

        assertEquals(mainLoopBB.getType(), TYPE_LOOP);

        BasicBlock sub1 = mainLoopBB.getSub1();

        assertEquals(sub1.getType(), TYPE_STATEMENTS);
        assertEquals(sub1.getNext().getType(), TYPE_IF);
        assertEquals(sub1.getNext().getSub1().getType(), TYPE_IF);
        assertEquals(sub1.getNext().getNext().getType(), TYPE_IF);
        assertEquals(sub1.getNext().getNext().getNext().getType(), TYPE_LOOP_START);
    }

    @Test
    public void testJdk170TripleDoWhile1() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/BreakContinue", "tripleDoWhile1"));
    }

    @Test
    public void testJdk170TripleDoWhile2() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/BreakContinue", "tripleDoWhile2"));

        BasicBlock mainLoopBB = cfg.getStart().getNext().getNext();

        assertEquals(mainLoopBB.getType(), TYPE_LOOP);

        BasicBlock innerLoopBB = mainLoopBB.getSub1();

        assertEquals(innerLoopBB.getType(), TYPE_LOOP);
        assertEquals(innerLoopBB.getNext().getType(), TYPE_IF);
        assertEquals(innerLoopBB.getNext().getNext().getType(), TYPE_LOOP_START);
        assertEquals(innerLoopBB.getNext().getSub1().getType(), TYPE_LOOP_END);

        BasicBlock innerInnerLoopBB = innerLoopBB.getSub1();

        assertEquals(innerInnerLoopBB.getType(), TYPE_LOOP);
        assertEquals(innerInnerLoopBB.getNext().getType(), TYPE_IF);
        assertEquals(innerInnerLoopBB.getNext().getNext().getType(), TYPE_LOOP_START);
        assertEquals(innerInnerLoopBB.getNext().getSub1().getType(), TYPE_LOOP_END);

        BasicBlock bb = innerInnerLoopBB.getSub1();

        assertEquals(bb.getType(), TYPE_STATEMENTS);
        assertEquals(bb.getNext().getType(), TYPE_IF);
        assertEquals(bb.getNext().getNext().getType(), TYPE_IF);
        assertEquals(bb.getNext().getNext().getSub1().getType(), TYPE_JUMP);
        assertEquals(bb.getNext().getNext().getNext().getType(), TYPE_STATEMENTS);
        assertEquals(bb.getNext().getNext().getNext().getNext().getType(), TYPE_IF);
        assertEquals(bb.getNext().getNext().getNext().getNext().getNext().getType(), TYPE_LOOP_START);
        assertEquals(bb.getNext().getNext().getNext().getNext().getSub1().getType(), TYPE_LOOP_END);
    }

    @Test
    public void testJdk170DoWhileWhileIf() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/BreakContinue", "doWhileWhileIf"));
    }

    @Test
    public void testJdk170DoWhileWhileTryBreak() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/BreakContinue", "doWhileWhileTryBreak"));

        BasicBlock mainLoopBB = cfg.getStart().getNext().getNext();

        assertEquals(mainLoopBB.getType(), TYPE_LOOP);

        BasicBlock innerLoopBB = mainLoopBB.getSub1();

        assertEquals(innerLoopBB.getType(), TYPE_LOOP);
        assertEquals(innerLoopBB.getSub1().getType(), TYPE_IF);
        assertEquals(innerLoopBB.getSub1().getSub1().getType(), TYPE_TRY);
        assertEquals(innerLoopBB.getSub1().getSub1().getSub1().getType(), TYPE_IF);
        assertEquals(innerLoopBB.getSub1().getSub1().getSub1().getNext().getType(), TYPE_JUMP);
    }


    // --- Test 'try-catch-finally' --------------------------------------------------------------------------------- //
    @Test
    public void testJdk170MethodTryCatch() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryCatch"));
        BasicBlock startBB = cfg.getStart();

        assertNotNull(startBB);

        BasicBlock simpleBB = startBB.getNext();

        assertEquals(simpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock tryBB = simpleBB.getNext();

        assertNotNull(tryBB);
        assertEquals(tryBB.getType(), TYPE_TRY);
        assertEquals(tryBB.getExceptionHandlers().size(), 1);

        BasicBlock bb = tryBB.getSub1();

        assertEquals(bb.getType(), TYPE_STATEMENTS);
        assertEquals(bb.getNext().getType(), TYPE_IF);
        assertEquals(bb.getNext().getNext().getType(), TYPE_END);

        ExceptionHandler eh0 = tryBB.getExceptionHandlers().get(0);

        assertEquals(eh0.getInternalThrowableName(), "java/lang/RuntimeException");
        assertEquals(eh0.getBasicBlock().getType(), TYPE_STATEMENTS);
        assertEquals(eh0.getBasicBlock().getNext().getType(), TYPE_END);

        BasicBlock nextSimpleBB = tryBB.getNext();

        assertNotNull(nextSimpleBB);
        assertEquals(nextSimpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock returnBB = nextSimpleBB.getNext();

        assertNotNull(returnBB);
        assertEquals(returnBB.getType(), TYPE_RETURN);
    }

    @Test
    public void testEclipseJavaCompiler370MethodTrySwitchFinally() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-eclipse-java-compiler-3.7.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTrySwitchFinally"));
        BasicBlock startBB = cfg.getStart();

        assertNotNull(startBB);

        BasicBlock simpleBB = startBB.getNext();

        assertEquals(simpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock tryBB = simpleBB.getNext();

        assertNotNull(tryBB);
        assertEquals(tryBB.getType(), TYPE_TRY_ECLIPSE);
        assertEquals(tryBB.getExceptionHandlers().size(), 1);

        BasicBlock bb = tryBB.getSub1();

        assertEquals(bb.getType(), TYPE_SWITCH);
        assertEquals(bb.getNext().getType(), TYPE_END);

        ExceptionHandler eh0 = tryBB.getExceptionHandlers().get(0);

        assertNull(eh0.getInternalThrowableName());
        assertEquals(eh0.getBasicBlock().getType(), TYPE_THROW);

        BasicBlock nextSimpleBB = tryBB.getNext();

        assertNotNull(nextSimpleBB);
        assertEquals(nextSimpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock returnBB = nextSimpleBB.getNext();

        assertNotNull(returnBB);
        assertEquals(returnBB.getType(), TYPE_RETURN);
    }

    @Test
    public void testEclipseJavaCompiler3130MethodTrySwitchFinally() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-eclipse-java-compiler-3.13.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTrySwitchFinally"));
        BasicBlock startBB = cfg.getStart();

        assertNotNull(startBB);

        BasicBlock simpleBB = startBB.getNext();

        assertEquals(simpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock tryBB = simpleBB.getNext();

        assertNotNull(tryBB);
        assertEquals(tryBB.getType(), TYPE_TRY_ECLIPSE);
        assertEquals(tryBB.getExceptionHandlers().size(), 1);

        BasicBlock bb = tryBB.getSub1();

        assertEquals(bb.getType(), TYPE_SWITCH);
        assertEquals(bb.getNext().getType(), TYPE_END);

        ExceptionHandler eh0 = tryBB.getExceptionHandlers().get(0);

        assertNull(eh0.getInternalThrowableName());
        assertEquals(eh0.getBasicBlock().getType(), TYPE_THROW);

        BasicBlock nextSimpleBB = tryBB.getNext();

        assertNotNull(nextSimpleBB);
        assertEquals(nextSimpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock returnBB = nextSimpleBB.getNext();

        assertNotNull(returnBB);
        assertEquals(returnBB.getType(), TYPE_RETURN);
    }

    @Test
    public void testJdk170MethodTryCatchCatch() throws Exception {
        BasicBlock tryBB = checkTryCatchFinallyReduction(checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryCatchCatch")));

        assertEquals(tryBB.getExceptionHandlers().size(), 2);

        BasicBlock next = tryBB.getNext();

        assertEquals(next.getType(), TYPE_STATEMENTS);
        assertEquals(next.getNext().getType(), TYPE_RETURN);
    }

    @Test
    public void testEclipseJavaCompiler321MethodTryCatchCatch() throws Exception {
        BasicBlock tryBB = checkTryCatchFinallyReduction(checkCFGReduction(searchMethod(getResource("zip/data-java-eclipse-java-compiler-3.2.1.zip"), "org/jd/core/test/TryCatchFinally", "methodTryCatchCatch")));

        assertEquals(tryBB.getExceptionHandlers().size(), 2);

        BasicBlock next = tryBB.getNext();

        assertEquals(next.getType(), TYPE_STATEMENTS);
        assertEquals(next.getNext().getType(), TYPE_RETURN);
    }

    @Test
    public void testEclipseJavaCompiler321MethodTryCatchFinally1() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-eclipse-java-compiler-3.2.1.zip"), "org/jd/core/test/TryCatchFinally", "methodTryCatchFinally1"));
    }

    @Test
    public void testJdk170MethodTryCatchCatchExitInTry() throws Exception {
        BasicBlock tryBB = checkTryCatchFinallyReduction(checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryCatchCatchOneExitInTry")));

        assertEquals(tryBB.getExceptionHandlers().size(), 2);

        BasicBlock bb = tryBB.getSub1();

        assertEquals(bb.getType(), TYPE_STATEMENTS);
        assertEquals(bb.getNext().getType(), TYPE_END);

        ExceptionHandler eh0 = tryBB.getExceptionHandlers().get(0);

        assertEquals(eh0.getInternalThrowableName(), "java/lang/RuntimeException");
        assertEquals(eh0.getBasicBlock().getType(), TYPE_THROW);

        ExceptionHandler eh1 = tryBB.getExceptionHandlers().get(1);

        assertEquals(eh1.getInternalThrowableName(), "java/lang/Exception");
        assertEquals(eh1.getBasicBlock().getType(), TYPE_STATEMENTS);
        assertEquals(eh1.getBasicBlock().getNext().getType(), TYPE_RETURN);

        BasicBlock next = tryBB.getNext();

        assertEquals(next.getType(), TYPE_STATEMENTS);
        assertEquals(next.getNext().getType(), TYPE_RETURN);
    }

    @Test
    public void testJdk170MethodTryCatchCatchExitInFirstCatch() throws Exception {
        BasicBlock tryBB = checkTryCatchFinallyReduction(checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryCatchCatchOneExitInFirstCatch")));

        assertEquals(tryBB.getExceptionHandlers().size(), 2);

        BasicBlock bb = tryBB.getSub1();

        assertEquals(bb.getType(), TYPE_STATEMENTS);
        assertEquals(bb.getNext().getType(), TYPE_RETURN);

        ExceptionHandler eh0 = tryBB.getExceptionHandlers().get(0);

        assertEquals(eh0.getInternalThrowableName(), "java/lang/RuntimeException");
        assertEquals(eh0.getBasicBlock().getType(), TYPE_STATEMENTS);
        assertEquals(eh0.getBasicBlock().getNext().getType(), TYPE_END);

        ExceptionHandler eh1 = tryBB.getExceptionHandlers().get(1);

        assertEquals(eh1.getInternalThrowableName(), "java/lang/Exception");
        assertEquals(eh1.getBasicBlock().getType(), TYPE_STATEMENTS);
        assertEquals(eh1.getBasicBlock().getNext().getType(), TYPE_RETURN);

        BasicBlock next = tryBB.getNext();

        assertEquals(next.getType(), TYPE_STATEMENTS);
        assertEquals(next.getNext().getType(), TYPE_RETURN);
    }

    @Test
    public void testJdk170MethodTryCatchCatchExitInLastCatch() throws Exception {
        BasicBlock tryBB = checkTryCatchFinallyReduction(checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryCatchCatchOneExitInLastCatch")));

        assertEquals(tryBB.getExceptionHandlers().size(), 2);

        BasicBlock bb = tryBB.getSub1();

        assertEquals(bb.getType(), TYPE_STATEMENTS);
        assertEquals(bb.getNext().getType(), TYPE_RETURN);

        ExceptionHandler eh0 = tryBB.getExceptionHandlers().get(0);

        assertEquals(eh0.getInternalThrowableName(), "java/lang/RuntimeException");
        assertEquals(eh0.getBasicBlock().getType(), TYPE_STATEMENTS);
        assertEquals(eh0.getBasicBlock().getNext().getType(), TYPE_RETURN);

        ExceptionHandler eh1 = tryBB.getExceptionHandlers().get(1);

        assertEquals(eh1.getInternalThrowableName(), "java/lang/Exception");
        assertEquals(eh1.getBasicBlock().getType(), TYPE_STATEMENTS);
        assertEquals(eh1.getBasicBlock().getNext().getType(), TYPE_RETURN);

        BasicBlock next = tryBB.getNext();

        assertEquals(next.getType(), TYPE_END);
    }

    @Test
    public void testJdk170MethodTrySwitchFinally() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTrySwitchFinally"));
        BasicBlock startBB = cfg.getStart();

        assertNotNull(startBB);

        BasicBlock simpleBB = startBB.getNext();

        assertEquals(simpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock tryBB = simpleBB.getNext();

        assertNotNull(tryBB);
        assertEquals(tryBB.getType(), TYPE_TRY);
        assertEquals(tryBB.getExceptionHandlers().size(), 1);

        BasicBlock bb = tryBB.getSub1();

        assertEquals(bb.getType(), TYPE_SWITCH);
        assertEquals(bb.getNext().getType(), TYPE_STATEMENTS);
        assertEquals(bb.getNext().getNext().getType(), TYPE_END);

        ExceptionHandler eh0 = tryBB.getExceptionHandlers().get(0);

        assertNull(eh0.getInternalThrowableName());
        assertEquals(eh0.getBasicBlock().getType(), TYPE_THROW);

        BasicBlock nextSimpleBB = tryBB.getNext();

        assertNotNull(nextSimpleBB);
        assertEquals(nextSimpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock returnBB = nextSimpleBB.getNext();

        assertNotNull(returnBB);
        assertEquals(returnBB.getType(), TYPE_RETURN);
    }

    @Test
    public void testJdk131MethodTryCatchFinallyInTryCatchFinally() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.3.1.zip"), "org/jd/core/test/TryCatchFinally", "methodTryCatchFinallyInTryCatchFinally"));
    }

    @Test
    public void testJdk170MethodTryCatchFinallyInTryCatchFinally() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryCatchFinallyInTryCatchFinally"));
        BasicBlock startBB = cfg.getStart();

        assertNotNull(startBB);

        BasicBlock simpleBB = startBB.getNext();

        assertEquals(simpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock tryBB = simpleBB.getNext();

        assertNotNull(tryBB);
        assertEquals(tryBB.getType(), TYPE_TRY);
        assertNotNull(tryBB.getExceptionHandlers());
        assertEquals(tryBB.getExceptionHandlers().size(), 1);

        BasicBlock bb = tryBB.getSub1();

        assertEquals(bb.getType(), TYPE_STATEMENTS);
        assertEquals(bb.getNext().getType(), TYPE_TRY);
        assertEquals(bb.getNext().getNext().getType(), TYPE_STATEMENTS);
        assertEquals(bb.getNext().getNext().getNext().getType(), TYPE_TRY);
        assertEquals(bb.getNext().getNext().getNext().getNext().getType(), TYPE_STATEMENTS);
        assertEquals(bb.getNext().getNext().getNext().getNext().getNext().getType(), TYPE_LOOP);
        assertEquals(bb.getNext().getNext().getNext().getNext().getNext().getNext().getType(), TYPE_END);

        ExceptionHandler eh0 = tryBB.getExceptionHandlers().get(0);

        assertNull(eh0.getInternalThrowableName());
        assertEquals(eh0.getBasicBlock().getType(), TYPE_STATEMENTS);
        assertEquals(eh0.getBasicBlock().getNext().getType(), TYPE_LOOP);
        assertEquals(eh0.getBasicBlock().getNext().getNext().getType(), TYPE_THROW);

        BasicBlock returnBB = tryBB.getNext();

        assertNotNull(returnBB);
        assertEquals(returnBB.getType(), TYPE_RETURN_VALUE);
    }

    protected static BasicBlock checkTryCatchFinallyReduction(ControlFlowGraph cfg) throws Exception {
        BasicBlock startBB = cfg.getStart();

        assertNotNull(startBB);

        BasicBlock simpleBB = startBB.getNext();

        assertNotNull(simpleBB);
        assertEquals(simpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock tryBB = simpleBB.getNext();

        assertNotNull(tryBB);
        assertNotNull(tryBB.getSub1());
        assertNotNull(tryBB.getExceptionHandlers());
        assertEquals(tryBB.getExceptionHandlers().size(), 2);

        for (ExceptionHandler exceptionHandler : tryBB.getExceptionHandlers()) {
            assertNotNull(exceptionHandler.getInternalThrowableName());
            assertNotNull(exceptionHandler.getBasicBlock());
        }

        return tryBB;
    }


    // --- Test 'try-with-resources' --------------------------------------------------------------------------------- //
    @Test
    public void testJdk170Try1Resource() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TryWithResources", "try1Resource"));
    }

    @Test
    public void testJdk170TryCatch1Resource() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TryWithResources", "tryCatch1Resource"));
    }

    @Test
    public void testJdk170TryFinally1Resource() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TryWithResources", "tryFinally1Resource"));
    }

    @Test
    public void testJdk170TryCatchFinally1Resource() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TryWithResources", "tryCatchFinally1Resource"));
    }

    @Test
    public void testJdk170TryCatchFinally2Resources() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TryWithResources", "tryCatchFinally2Resources"));
    }

    @Test
    public void testJdk170TryCatchFinally4Resources() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TryWithResources", "tryCatchFinally4Resources"));
    }


    // --- methodTryFinallyReturn --- //
    @Test
    public void testJdk170MethodTryFinallyReturn() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryFinallyReturn"));
        BasicBlock startBB = cfg.getStart();

        assertNotNull(startBB);

        BasicBlock simpleBB = startBB.getNext();

        assertEquals(simpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock tryBB = simpleBB.getNext();

        assertNotNull(tryBB);
        assertEquals(tryBB.getType(), TYPE_TRY);
        assertNotNull(tryBB.getExceptionHandlers());
        assertEquals(tryBB.getExceptionHandlers().size(), 1);

        BasicBlock bb = tryBB.getSub1();

        assertEquals(bb.getType(), TYPE_RETURN_VALUE);

        ExceptionHandler eh0 = tryBB.getExceptionHandlers().get(0);

        assertNull(eh0.getInternalThrowableName());
        assertEquals(eh0.getBasicBlock().getType(), TYPE_THROW);

        BasicBlock nextBB = tryBB.getNext();

        assertNotNull(nextBB);
        assertEquals(nextBB.getType(), TYPE_END);
    }

    @Test
    public void testEclipseJavaCompiler370MethodTryFinallyReturn() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-eclipse-java-compiler-3.7.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryFinallyReturn"));
        BasicBlock startBB = cfg.getStart();

        assertNotNull(startBB);

        BasicBlock simpleBB = startBB.getNext();

        assertEquals(simpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock tryBB = simpleBB.getNext();

        assertNotNull(tryBB);
        assertEquals(tryBB.getType(), TYPE_TRY);
        assertNotNull(tryBB.getExceptionHandlers());
        assertEquals(tryBB.getExceptionHandlers().size(), 1);

        BasicBlock bb = tryBB.getSub1();

        assertEquals(bb.getType(), TYPE_STATEMENTS);
        assertEquals(bb.getNext().getType(), TYPE_RETURN_VALUE);

        ExceptionHandler eh0 = tryBB.getExceptionHandlers().get(0);

        assertNull(eh0.getInternalThrowableName());
        assertEquals(eh0.getBasicBlock().getType(), TYPE_THROW);

        BasicBlock nextBB = tryBB.getNext();

        assertNotNull(nextBB);
        assertEquals(nextBB.getType(), TYPE_END);
    }

    @Test
    public void testEclipseJavaCompiler3130MethodTryFinallyReturn() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-eclipse-java-compiler-3.13.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryFinallyReturn"));
        BasicBlock startBB = cfg.getStart();

        assertNotNull(startBB);

        BasicBlock simpleBB = startBB.getNext();

        assertEquals(simpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock tryBB = simpleBB.getNext();

        assertNotNull(tryBB);
        assertEquals(tryBB.getType(), TYPE_TRY);
        assertNotNull(tryBB.getExceptionHandlers());
        assertEquals(tryBB.getExceptionHandlers().size(), 1);

        BasicBlock bb = tryBB.getSub1();

        assertEquals(bb.getType(), TYPE_STATEMENTS);
        assertEquals(bb.getNext().getType(), TYPE_RETURN_VALUE);

        ExceptionHandler eh0 = tryBB.getExceptionHandlers().get(0);

        assertNull(eh0.getInternalThrowableName());
        assertEquals(eh0.getBasicBlock().getType(), TYPE_THROW);

        BasicBlock nextBB = tryBB.getNext();

        assertNotNull(nextBB);
        assertEquals(nextBB.getType(), TYPE_END);
    }


    // --- methodTryCatch3 --- //
    @Test
    public void testJdk170MethodTryCatch3() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryCatch3"));
        BasicBlock startBB = cfg.getStart();

        assertNotNull(startBB);

        BasicBlock simpleBB = startBB.getNext();

        assertEquals(simpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock tryBB = simpleBB.getNext();

        assertNotNull(tryBB);
        assertEquals(tryBB.getType(), TYPE_TRY);
        assertNotNull(tryBB.getExceptionHandlers());
        assertEquals(tryBB.getExceptionHandlers().size(), 1);

        BasicBlock bb = tryBB.getSub1();

        assertEquals(bb.getType(), TYPE_RETURN_VALUE);

        ExceptionHandler eh0 = tryBB.getExceptionHandlers().get(0);

        assertEquals(eh0.getInternalThrowableName(), "java/lang/RuntimeException");
        assertEquals(eh0.getBasicBlock().getType(), TYPE_RETURN_VALUE);

        BasicBlock nextBB = tryBB.getNext();

        assertNotNull(nextBB);
        assertEquals(nextBB.getType(), TYPE_END);
    }

    @Test
    public void testEclipseJavaCompiler370MethodTryCatch3() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-eclipse-java-compiler-3.7.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryCatch3"));
        BasicBlock startBB = cfg.getStart();

        assertNotNull(startBB);

        BasicBlock simpleBB = startBB.getNext();

        assertEquals(simpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock tryBB = simpleBB.getNext();

        assertNotNull(tryBB);
        assertEquals(tryBB.getType(), TYPE_TRY);
        assertNotNull(tryBB.getExceptionHandlers());
        assertEquals(tryBB.getExceptionHandlers().size(), 1);

        BasicBlock bb = tryBB.getSub1();

        assertEquals(bb.getType(), TYPE_RETURN_VALUE);

        ExceptionHandler eh0 = tryBB.getExceptionHandlers().get(0);

        assertEquals(eh0.getInternalThrowableName(), "java/lang/RuntimeException");
        assertEquals(eh0.getBasicBlock().getType(), TYPE_RETURN_VALUE);

        BasicBlock nextBB = tryBB.getNext();

        assertNotNull(nextBB);
        assertEquals(nextBB.getType(), TYPE_END);
    }

    @Test
    public void testEclipseJavaCompiler3130MethodTryCatch3() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-eclipse-java-compiler-3.13.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryCatch3"));
        BasicBlock startBB = cfg.getStart();

        assertNotNull(startBB);

        BasicBlock simpleBB = startBB.getNext();

        assertEquals(simpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock tryBB = simpleBB.getNext();

        assertNotNull(tryBB);
        assertEquals(tryBB.getType(), TYPE_TRY);
        assertNotNull(tryBB.getExceptionHandlers());
        assertEquals(tryBB.getExceptionHandlers().size(), 1);

        BasicBlock bb = tryBB.getSub1();

        assertEquals(bb.getType(), TYPE_RETURN_VALUE);

        ExceptionHandler eh0 = tryBB.getExceptionHandlers().get(0);

        assertEquals(eh0.getInternalThrowableName(), "java/lang/RuntimeException");
        assertEquals(eh0.getBasicBlock().getType(), TYPE_RETURN_VALUE);

        BasicBlock nextBB = tryBB.getNext();

        assertNotNull(nextBB);
        assertEquals(nextBB.getType(), TYPE_END);
    }


    // --- methodTryFinally1 --- //
    @Test
    public void testJdk170MethodTryFinally1() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryFinally1"));
        BasicBlock startBB = cfg.getStart();

        assertNotNull(startBB);

        BasicBlock simpleBB = startBB.getNext();

        assertEquals(simpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock tryBB = simpleBB.getNext();

        assertNotNull(tryBB);
        assertEquals(tryBB.getType(), TYPE_TRY);
        assertEquals(tryBB.getExceptionHandlers().size(), 1);

        BasicBlock bb = tryBB.getSub1();

        assertEquals(bb.getType(), TYPE_STATEMENTS);
        assertEquals(bb.getNext().getType(), TYPE_END);

        ExceptionHandler eh0 = tryBB.getExceptionHandlers().get(0);

        assertNull(eh0.getInternalThrowableName());
        assertEquals(eh0.getBasicBlock().getType(), TYPE_THROW);

        BasicBlock nextSimpleBB = tryBB.getNext();
        assertNotNull(nextSimpleBB);
        assertEquals(nextSimpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock returnBB = nextSimpleBB.getNext();

        assertNotNull(returnBB);
        assertEquals(returnBB.getType(), TYPE_RETURN);
    }


    // --- methodTryFinally3 --- //
    @Test
    public void testJdk170MethodTryFinally3() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryFinally3"));
        BasicBlock startBB = cfg.getStart();

        assertNotNull(startBB);

        BasicBlock simpleBB = startBB.getNext();

        assertEquals(simpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock tryBB = simpleBB.getNext();

        assertNotNull(tryBB);
        assertEquals(tryBB.getType(), TYPE_TRY);
        assertNotNull(tryBB.getExceptionHandlers());
        assertEquals(tryBB.getExceptionHandlers().size(), 1);

        BasicBlock bb = tryBB.getSub1();

        assertEquals(bb.getType(), TYPE_THROW);

        ExceptionHandler eh0 = tryBB.getExceptionHandlers().get(0);

        assertNull(eh0.getInternalThrowableName());
        assertEquals(eh0.getBasicBlock().getType(), TYPE_THROW);

        BasicBlock nextBB = tryBB.getNext();

        assertNotNull(nextBB);
        assertEquals(nextBB.getType(), TYPE_END);
    }

    @Test
    public void testEclipseJavaCompiler370MethodTryFinally3() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-eclipse-java-compiler-3.7.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryFinally3"));
        BasicBlock startBB = cfg.getStart();

        assertNotNull(startBB);

        BasicBlock simpleBB = startBB.getNext();

        assertEquals(simpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock tryBB = simpleBB.getNext();

        assertNotNull(tryBB);
        assertEquals(tryBB.getType(), TYPE_TRY);
        assertNotNull(tryBB.getExceptionHandlers());
        assertEquals(tryBB.getExceptionHandlers().size(), 1);

        BasicBlock bb = tryBB.getSub1();

        assertEquals(bb.getType(), TYPE_THROW);

        ExceptionHandler eh0 = tryBB.getExceptionHandlers().get(0);

        assertNull(eh0.getInternalThrowableName());
        assertEquals(eh0.getBasicBlock().getType(), TYPE_THROW);

        BasicBlock nextBB = tryBB.getNext();

        assertNotNull(nextBB);
        assertEquals(nextBB.getType(), TYPE_END);
    }

    @Test
    public void testEclipseJavaCompiler3130MethodTryFinally3() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-eclipse-java-compiler-3.13.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryFinally3"));
        BasicBlock startBB = cfg.getStart();

        assertNotNull(startBB);

        BasicBlock simpleBB = startBB.getNext();

        assertEquals(simpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock tryBB = simpleBB.getNext();

        assertNotNull(tryBB);
        assertEquals(tryBB.getType(), TYPE_TRY);
        assertNotNull(tryBB.getExceptionHandlers());
        assertEquals(tryBB.getExceptionHandlers().size(), 1);

        BasicBlock bb = tryBB.getSub1();

        assertEquals(bb.getType(), TYPE_THROW);

        ExceptionHandler eh0 = tryBB.getExceptionHandlers().get(0);

        assertNull(eh0.getInternalThrowableName());
        assertEquals(eh0.getBasicBlock().getType(), TYPE_THROW);

        BasicBlock nextBB = tryBB.getNext();

        assertNotNull(nextBB);
        assertEquals(nextBB.getType(), TYPE_END);
    }


    // --- methodTryFinally4 --- //
    @Test
    public void testJdk170MethodTryFinally4() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryFinally4"));
        BasicBlock startBB = cfg.getStart();

        assertNotNull(startBB);

        BasicBlock simpleBB = startBB.getNext();

        assertEquals(simpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock tryBB = simpleBB.getNext();

        assertNotNull(tryBB);
        assertEquals(tryBB.getType(), TYPE_TRY);
        assertNotNull(tryBB.getExceptionHandlers());
        assertEquals(tryBB.getExceptionHandlers().size(), 1);

        BasicBlock bb = tryBB.getSub1();

        assertEquals(bb.getType(), TYPE_STATEMENTS);
        assertEquals(bb.getNext().getType(), TYPE_IF);
        assertEquals(bb.getNext().getNext().getType(), TYPE_STATEMENTS);
        assertEquals(bb.getNext().getNext().getNext().getType(), TYPE_END);
        assertEquals(bb.getNext().getSub1().getType(), TYPE_STATEMENTS);
        assertEquals(bb.getNext().getSub1().getNext().getType(), TYPE_RETURN);

        ExceptionHandler eh0 = tryBB.getExceptionHandlers().get(0);

        assertNull(eh0.getInternalThrowableName());
        assertEquals(eh0.getBasicBlock().getType(), TYPE_THROW);

        BasicBlock nextBB = tryBB.getNext();

        assertNotNull(nextBB);
        assertEquals(nextBB.getType(), TYPE_STATEMENTS);
        assertEquals(nextBB.getNext().getType(), TYPE_RETURN);
    }

    @Test
    public void testEclipseJavaCompiler370MethodTryFinally4() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-eclipse-java-compiler-3.7.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryFinally4"));
        BasicBlock startBB = cfg.getStart();

        assertNotNull(startBB);

        BasicBlock simpleBB = startBB.getNext();

        assertEquals(simpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock tryBB = simpleBB.getNext();

        assertNotNull(tryBB);
        assertEquals(tryBB.getType(), TYPE_TRY_ECLIPSE);
        assertNotNull(tryBB.getExceptionHandlers());
        assertEquals(tryBB.getExceptionHandlers().size(), 1);

        BasicBlock bb = tryBB.getSub1();

        assertEquals(bb.getType(), TYPE_STATEMENTS);
        assertEquals(bb.getNext().getType(), TYPE_IF);
        assertEquals(bb.getNext().getNext().getType(), TYPE_END);
        assertEquals(bb.getNext().getSub1().getType(), TYPE_STATEMENTS);
        assertEquals(bb.getNext().getSub1().getNext().getType(), TYPE_RETURN);

        ExceptionHandler eh0 = tryBB.getExceptionHandlers().get(0);

        assertNull(eh0.getInternalThrowableName());
        assertEquals(eh0.getBasicBlock().getType(), TYPE_THROW);

        BasicBlock nextBB = tryBB.getNext();

        assertNotNull(nextBB);
        assertEquals(nextBB.getType(), TYPE_STATEMENTS);
        assertEquals(nextBB.getNext().getType(), TYPE_RETURN);
    }

    @Test
    public void testEclipseJavaCompiler3130MethodTryFinally4() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-eclipse-java-compiler-3.13.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryFinally4"));
        BasicBlock startBB = cfg.getStart();

        assertNotNull(startBB);

        BasicBlock simpleBB = startBB.getNext();

        assertEquals(simpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock tryBB = simpleBB.getNext();

        assertNotNull(tryBB);
        assertEquals(tryBB.getType(), TYPE_TRY_ECLIPSE);
        assertNotNull(tryBB.getExceptionHandlers());
        assertEquals(tryBB.getExceptionHandlers().size(), 1);

        BasicBlock bb = tryBB.getSub1();

        assertEquals(bb.getType(), TYPE_STATEMENTS);
        assertEquals(bb.getNext().getType(), TYPE_IF);
        assertEquals(bb.getNext().getNext().getType(), TYPE_END);
        assertEquals(bb.getNext().getSub1().getType(), TYPE_STATEMENTS);
        assertEquals(bb.getNext().getSub1().getNext().getType(), TYPE_RETURN);

        ExceptionHandler eh0 = tryBB.getExceptionHandlers().get(0);

        assertNull(eh0.getInternalThrowableName());
        assertEquals(eh0.getBasicBlock().getType(), TYPE_THROW);

        BasicBlock nextBB = tryBB.getNext();

        assertNotNull(nextBB);
        assertEquals(nextBB.getType(), TYPE_STATEMENTS);
        assertEquals(nextBB.getNext().getType(), TYPE_RETURN);
    }


    // --- methodTryCatchFinally2 --- //
    @Test
    public void testJdk170MethodTryCatchFinally2() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryCatchFinally2"));
    }

    @Test
    public void testEclipseJavaCompiler370MethodTryCatchFinally2() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-eclipse-java-compiler-3.7.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryCatchFinally2"));
        BasicBlock tryBB = cfg.getStart().getNext().getNext();

        assertEquals(tryBB.getType(), TYPE_TRY_ECLIPSE);
        assertEquals(tryBB.getSub1().getType(), TYPE_STATEMENTS);
        assertEquals(tryBB.getSub1().getNext().getType(), TYPE_END);
        assertEquals(tryBB.getNext().getType(), TYPE_STATEMENTS);
        assertEquals(tryBB.getNext().getNext().getType(), TYPE_STATEMENTS);
        assertEquals(tryBB.getNext().getNext().getNext().getType(), TYPE_RETURN);
    }

    @Test
    public void testEclipseJavaCompiler3130MethodTryCatchFinally2() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-eclipse-java-compiler-3.13.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryCatchFinally2"));
        BasicBlock tryBB = cfg.getStart().getNext().getNext();

        assertEquals(tryBB.getType(), TYPE_TRY_ECLIPSE);
        assertEquals(tryBB.getSub1().getType(), TYPE_STATEMENTS);
        assertEquals(tryBB.getSub1().getNext().getType(), TYPE_END);
        assertEquals(tryBB.getNext().getType(), TYPE_STATEMENTS);
        assertEquals(tryBB.getNext().getNext().getType(), TYPE_STATEMENTS);
        assertEquals(tryBB.getNext().getNext().getNext().getType(), TYPE_RETURN);
    }


    // --- methodTryCatchFinally4 --- //
    @Test
    public void testJdk170MethodTryCatchFinally4() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryCatchFinally4"));
        BasicBlock startBB = cfg.getStart();

        assertNotNull(startBB);

        BasicBlock simpleBB = startBB.getNext();

        assertEquals(simpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock tryBB = simpleBB.getNext();

        assertNotNull(tryBB);
        assertEquals(tryBB.getType(), TYPE_TRY);
        assertNotNull(tryBB.getExceptionHandlers());
        assertEquals(tryBB.getExceptionHandlers().size(), 2);

        BasicBlock bb = tryBB.getSub1();

        assertEquals(bb.getType(), TYPE_THROW);

        ExceptionHandler eh0 = tryBB.getExceptionHandlers().get(0);

        assertEquals(eh0.getInternalThrowableName(), "java/lang/Exception");
        assertEquals(eh0.getBasicBlock().getType(), TYPE_STATEMENTS);
        assertEquals(eh0.getBasicBlock().getNext().getType(), TYPE_END);

        ExceptionHandler eh1 = tryBB.getExceptionHandlers().get(1);

        assertNull(eh1.getInternalThrowableName());
        assertEquals(eh1.getBasicBlock().getType(), TYPE_THROW);

        BasicBlock returnBB = tryBB.getNext();

        assertNotNull(returnBB);
        assertEquals(returnBB.getType(), TYPE_RETURN_VALUE);
    }


    // --- methodTryCatchFinally5 --- //
    @Test
    public void testEclipseJavaCompiler321MethodTryCatchFinally5() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-eclipse-java-compiler-3.2.1.zip"), "org/jd/core/test/TryCatchFinally", "methodTryCatchFinally5"));
    }

    @Test
    public void testEclipseJavaCompiler370MethodTryCatchFinally5() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-eclipse-java-compiler-3.7.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryCatchFinally5"));
    }

    @Test
    public void testEclipseJavaCompiler3130MethodTryCatchFinally5() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-eclipse-java-compiler-3.13.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryCatchFinally5"));
    }

    @Test
    public void testJdk170MethodTryCatchFinally5() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryCatchFinally5"));
    }

    // --- methodTryTryReturnFinally*Finally --- //
    @Test
    public void testJdk170MethodTryTryReturnFinallyFinally() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryTryReturnFinallyFinally"));
    }

    @Test
    public void testEclipseJavaCompiler321MethodTryTryReturnFinallyCatchFinally() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-eclipse-java-compiler-3.2.1.zip"), "org/jd/core/test/TryCatchFinally", "methodTryTryReturnFinallyCatchFinally"));
    }

    @Test
    public void testJdk170MethodTryTryReturnFinallyCatchFinally() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryTryReturnFinallyCatchFinally"));
    }


    // --- methodTryTryFinallyFinallyTryFinallyReturn --- //
    @Test
    public void testEclipseJavaCompiler370MethodTryTryFinallyFinallyTryFinallyReturn() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-eclipse-java-compiler-3.7.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryTryFinallyFinallyTryFinallyReturn"));
    }

    @Test
    public void testEclipseJavaCompiler3130MethodTryTryFinallyFinallyTryFinallyReturn() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-eclipse-java-compiler-3.13.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryTryFinallyFinallyTryFinallyReturn"));
    }

    @Test
    public void testJdk170MethodTryTryFinallyFinallyTryFinallyReturn() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryTryFinallyFinallyTryFinallyReturn"));
        BasicBlock startBB = cfg.getStart();

        assertNotNull(startBB);

        BasicBlock simpleBB = startBB.getNext();

        assertEquals(simpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock tryBB = simpleBB.getNext();

        assertNotNull(tryBB);
        assertEquals(tryBB.getType(), TYPE_TRY);
        assertNotNull(tryBB.getExceptionHandlers());
        assertEquals(tryBB.getExceptionHandlers().size(), 1);

        BasicBlock bb = tryBB.getSub1();

        assertEquals(bb.getType(), TYPE_TRY);
        assertEquals(bb.getNext().getType(), TYPE_TRY);
        assertEquals(bb.getNext().getNext().getType(), TYPE_END);

        ExceptionHandler eh0 = tryBB.getExceptionHandlers().get(0);

        assertNull(eh0.getInternalThrowableName());
        assertEquals(eh0.getBasicBlock().getType(), TYPE_STATEMENTS);
        assertEquals(eh0.getBasicBlock().getNext().getType(), TYPE_TRY);
        assertEquals(eh0.getBasicBlock().getNext().getNext().getType(), TYPE_THROW);

        BasicBlock nextBB = tryBB.getNext();

        assertNotNull(nextBB);
        assertEquals(nextBB.getType(), TYPE_STATEMENTS);
        assertEquals(nextBB.getNext().getType(), TYPE_RETURN);
    }


    // --- complexMethodTryCatchCatchFinally --- //
    @Test
    public void testJdk170MethodTryTryFinallyFinallyTryFinally() throws Exception {
        checkComplexMethodTryCatchCatchFinally_JDK5(checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TryCatchFinally", "complexMethodTryCatchCatchFinally")));
    }

    @Test
    public void testEclipseJavaCompiler321ComplexMethodTryCatchCatchFinally() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-eclipse-java-compiler-3.2.1.zip"), "org/jd/core/test/TryCatchFinally", "complexMethodTryCatchCatchFinally"));
        BasicBlock startBB = cfg.getStart();

        assertNotNull(startBB);

        BasicBlock simpleBB = startBB.getNext();

        assertEquals(simpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock tryBB = simpleBB.getNext();

        assertNotNull(tryBB);
        assertEquals(tryBB.getType(), TYPE_TRY_ECLIPSE);
    }

    @Test
    public void testEclipseJavaCompiler370ComplexMethodTryCatchCatchFinally() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-eclipse-java-compiler-3.7.0.zip"), "org/jd/core/test/TryCatchFinally", "complexMethodTryCatchCatchFinally"));
        BasicBlock startBB = cfg.getStart();

        assertNotNull(startBB);

        BasicBlock simpleBB = startBB.getNext();

        assertEquals(simpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock tryBB = simpleBB.getNext();

        assertNotNull(tryBB);
        assertEquals(tryBB.getType(), TYPE_TRY_ECLIPSE);
    }

    @Test
    public void testEclipseJavaCompiler3130ComplexMethodTryCatchCatchFinally() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-eclipse-java-compiler-3.13.0.zip"), "org/jd/core/test/TryCatchFinally", "complexMethodTryCatchCatchFinally"));
        BasicBlock startBB = cfg.getStart();

        assertNotNull(startBB);

        BasicBlock simpleBB = startBB.getNext();

        assertEquals(simpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock tryBB = simpleBB.getNext();

        assertNotNull(tryBB);
        assertEquals(tryBB.getType(), TYPE_TRY_ECLIPSE);
    }

    @Test
    public void testHarmonyJdkR533500ComplexMethodTryCatchCatchFinally() throws Exception {
        checkComplexMethodTryCatchCatchFinally_JDK118(checkCFGReduction(searchMethod(getResource("zip/data-java-harmony-jdk-r533500.zip"), "org/jd/core/test/TryCatchFinally", "complexMethodTryCatchCatchFinally")));
    }

    @Test
    public void testIbm_J9_VmComplexMethodTryCatchCatchFinally() throws Exception {
        checkComplexMethodTryCatchCatchFinally_JDK5(checkCFGReduction(searchMethod(getResource("zip/data-java-ibm-j9_vm.zip"), "org/jd/core/test/TryCatchFinally", "complexMethodTryCatchCatchFinally")));
    }

    @Test
    public void testJdk118ComplexMethodTryCatchCatchFinally() throws Exception {
        checkComplexMethodTryCatchCatchFinally_JDK118(checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.1.8.zip"), "org/jd/core/test/TryCatchFinally", "complexMethodTryCatchCatchFinally")));
    }

    @Test
    public void testJdk131ComplexMethodTryCatchCatchFinally() throws Exception {
        checkComplexMethodTryCatchCatchFinally_JDK118(checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.3.1.zip"), "org/jd/core/test/TryCatchFinally", "complexMethodTryCatchCatchFinally")));
    }

    @Test
    public void testJdk142ComplexMethodTryCatchCatchFinally() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.4.2.zip"), "org/jd/core/test/TryCatchFinally", "complexMethodTryCatchCatchFinally"));
        BasicBlock startBB = cfg.getStart();

        assertNotNull(startBB);

        BasicBlock simpleBB = startBB.getNext();

        assertEquals(simpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock tryBB = simpleBB.getNext();

        assertNotNull(tryBB);
        assertEquals(tryBB.getType(), TYPE_TRY_JSR);
        assertNotNull(tryBB.getExceptionHandlers());
        assertEquals(tryBB.getExceptionHandlers().size(), 3);

        assertEquals(tryBB.getSub1().getType(), TYPE_TRY);
        assertEquals(tryBB.getSub1().getNext().getType(), TYPE_END);

        ExceptionHandler eh0 = tryBB.getExceptionHandlers().get(0);

        assertNotNull(eh0.getInternalThrowableName());
        assertEquals(eh0.getBasicBlock().getType(), TYPE_STATEMENTS);
        assertEquals(eh0.getBasicBlock().getNext().getType(), TYPE_TRY);
        assertEquals(eh0.getBasicBlock().getNext().getNext().getType(), TYPE_END);

        ExceptionHandler eh1 = tryBB.getExceptionHandlers().get(1);

        assertNotNull(eh1.getInternalThrowableName());
        assertEquals(eh1.getBasicBlock().getType(), TYPE_STATEMENTS);
        assertEquals(eh1.getBasicBlock().getNext().getType(), TYPE_TRY);
        assertEquals(eh1.getBasicBlock().getNext().getNext().getType(), TYPE_END);

        ExceptionHandler eh2 = tryBB.getExceptionHandlers().get(2);

        assertNull(eh2.getInternalThrowableName());
        assertEquals(eh2.getBasicBlock().getType(), TYPE_STATEMENTS);
        assertEquals(eh2.getBasicBlock().getNext().getType(), TYPE_JSR);
        assertEquals(eh2.getBasicBlock().getNext().getNext().getType(), TYPE_THROW);
        assertEquals(eh2.getBasicBlock().getNext().getBranch().getType(), TYPE_STATEMENTS);
        assertEquals(eh2.getBasicBlock().getNext().getBranch().getNext().getType(), TYPE_TRY);
        assertEquals(eh2.getBasicBlock().getNext().getBranch().getNext().getNext().getType(), TYPE_RET);

        BasicBlock nextBB = tryBB.getNext();

        assertNotNull(nextBB);
        assertEquals(nextBB.getType(), TYPE_STATEMENTS);
        assertEquals(nextBB.getNext().getType(), TYPE_RETURN);
    }

    @Test
    public void testJdk150ComplexMethodTryCatchCatchFinally() throws Exception {
        checkComplexMethodTryCatchCatchFinally_JDK5(checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.5.0.zip"), "org/jd/core/test/TryCatchFinally", "complexMethodTryCatchCatchFinally")));
    }

    @Test
    public void testJdk160ComplexMethodTryCatchCatchFinally() throws Exception {
        checkComplexMethodTryCatchCatchFinally_JDK5(checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.6.0.zip"), "org/jd/core/test/TryCatchFinally", "complexMethodTryCatchCatchFinally")));
    }

    @Test
    public void testJdk170ComplexMethodTryCatchCatchFinally() throws Exception {
        checkComplexMethodTryCatchCatchFinally_JDK5(checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TryCatchFinally", "complexMethodTryCatchCatchFinally")));
    }

    @Test
    public void testJikes1_22_1WindowsComplexMethodTryCatchCatchFinally() throws Exception {
        checkComplexMethodTryCatchCatchFinally_JDK118(checkCFGReduction(searchMethod(getResource("zip/data-java-jikes-1.22-1.windows.zip"), "org/jd/core/test/TryCatchFinally", "complexMethodTryCatchCatchFinally")));
    }

    @Test
    public void testJRockit90_150_06ComplexMethodTryCatchCatchFinally() throws Exception {
        checkComplexMethodTryCatchCatchFinally_JDK5(checkCFGReduction(searchMethod(getResource("zip/data-java-jrockit-90_150_06.zip"), "org/jd/core/test/TryCatchFinally", "complexMethodTryCatchCatchFinally")));
    }

    protected void checkComplexMethodTryCatchCatchFinally_JDK5(ControlFlowGraph cfg) throws Exception {
        BasicBlock startBB = cfg.getStart();

        assertNotNull(startBB);

        BasicBlock simpleBB = startBB.getNext();

        assertEquals(simpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock tryBB = simpleBB.getNext();

        assertNotNull(tryBB);
        assertEquals(tryBB.getType(), TYPE_TRY);
        assertNotNull(tryBB.getExceptionHandlers());
        assertEquals(tryBB.getExceptionHandlers().size(), 3);

        BasicBlock bb = tryBB.getSub1();

        assertEquals(bb.getType(), TYPE_TRY);
        assertEquals(bb.getNext().getType(), TYPE_TRY);
        assertEquals(bb.getNext().getNext().getType(), TYPE_END);
        assertEquals(bb.getExceptionHandlers().size(), 3);
        assertEquals(bb.getNext().getExceptionHandlers().size(), 3);

        ExceptionHandler eh0 = tryBB.getExceptionHandlers().get(0);

        assertEquals(eh0.getInternalThrowableName(), "java/lang/RuntimeException");
        assertEquals(eh0.getBasicBlock().getType(), TYPE_STATEMENTS);
        assertEquals(eh0.getBasicBlock().getNext().getType(), TYPE_TRY);
        assertEquals(eh0.getBasicBlock().getNext().getNext().getType(), TYPE_TRY);
        assertEquals(eh0.getBasicBlock().getNext().getNext().getNext().getType(), TYPE_END);
        assertEquals(eh0.getBasicBlock().getNext().getExceptionHandlers().size(), 3);
        assertEquals(eh0.getBasicBlock().getNext().getNext().getExceptionHandlers().size(), 3);

        ExceptionHandler eh1 = tryBB.getExceptionHandlers().get(1);

        assertEquals(eh1.getInternalThrowableName(), "java/lang/Exception");
        assertEquals(eh1.getBasicBlock().getType(), TYPE_STATEMENTS);
        assertEquals(eh1.getBasicBlock().getNext().getType(), TYPE_TRY);
        assertEquals(eh1.getBasicBlock().getNext().getNext().getType(), TYPE_TRY);
        assertEquals(eh1.getBasicBlock().getNext().getNext().getNext().getType(), TYPE_END);
        assertEquals(eh1.getBasicBlock().getNext().getExceptionHandlers().size(), 3);
        assertEquals(eh1.getBasicBlock().getNext().getNext().getExceptionHandlers().size(), 3);

        ExceptionHandler eh2 = tryBB.getExceptionHandlers().get(2);

        assertNull(eh2.getInternalThrowableName());
        assertEquals(eh2.getBasicBlock().getType(), TYPE_STATEMENTS);
        assertEquals(eh2.getBasicBlock().getNext().getType(), TYPE_TRY);
        assertEquals(eh2.getBasicBlock().getNext().getNext().getType(), TYPE_THROW);
        assertEquals(eh2.getBasicBlock().getNext().getExceptionHandlers().size(), 3);

        BasicBlock nextBB = tryBB.getNext();

        assertNotNull(nextBB);
        assertEquals(nextBB.getType(), TYPE_STATEMENTS);
        assertEquals(nextBB.getNext().getType(), TYPE_RETURN);
    }

    protected void checkComplexMethodTryCatchCatchFinally_JDK118(ControlFlowGraph cfg) throws Exception {
        BasicBlock startBB = cfg.getStart();

        assertNotNull(startBB);

        BasicBlock simpleBB = startBB.getNext();

        assertEquals(simpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock tryBB = simpleBB.getNext();

        assertNotNull(tryBB);
        assertEquals(tryBB.getType(), TYPE_TRY_JSR);
        assertNotNull(tryBB.getExceptionHandlers());
        assertEquals(tryBB.getExceptionHandlers().size(), 3);

        BasicBlock sub1 = tryBB.getSub1();

        assertEquals(sub1.getType(), TYPE_TRY_JSR);
        assertEquals(sub1.getNext().getType(), TYPE_END);
        assertEquals(sub1.getExceptionHandlers().size(), 3);

        ExceptionHandler eh0 = tryBB.getExceptionHandlers().get(0);

        assertEquals(eh0.getInternalThrowableName(), "java/lang/RuntimeException");
        assertEquals(eh0.getBasicBlock().getType(), TYPE_STATEMENTS);
        assertEquals(eh0.getBasicBlock().getNext().getType(), TYPE_TRY_JSR);
        assertEquals(eh0.getBasicBlock().getNext().getNext().getType(), TYPE_END);
        assertEquals(eh0.getBasicBlock().getNext().getExceptionHandlers().size(), 3);

        ExceptionHandler eh1 = tryBB.getExceptionHandlers().get(1);

        assertEquals(eh1.getInternalThrowableName(), "java/lang/Exception");
        assertEquals(eh1.getBasicBlock().getType(), TYPE_STATEMENTS);
        assertEquals(eh1.getBasicBlock().getNext().getType(), TYPE_TRY_JSR);
        assertEquals(eh1.getBasicBlock().getNext().getNext().getType(), TYPE_END);
        assertEquals(eh1.getBasicBlock().getNext().getExceptionHandlers().size(), 3);

        ExceptionHandler eh2 = tryBB.getExceptionHandlers().get(2);

        assertNull(eh2.getInternalThrowableName());
        assertEquals(eh2.getBasicBlock().getType(), TYPE_STATEMENTS);
        assertEquals(eh2.getBasicBlock().getNext().getType(), TYPE_JSR);
        assertEquals(eh2.getBasicBlock().getNext().getNext().getType(), TYPE_THROW);
        assertEquals(eh2.getBasicBlock().getNext().getBranch().getType(), TYPE_STATEMENTS);
        assertEquals(eh2.getBasicBlock().getNext().getBranch().getNext().getType(), TYPE_TRY_JSR);
        assertEquals(eh2.getBasicBlock().getNext().getBranch().getNext().getExceptionHandlers().size(), 3);

        BasicBlock nextBB = tryBB.getNext();

        assertNotNull(nextBB);
        assertEquals(nextBB.getType(), TYPE_STATEMENTS);
        assertEquals(nextBB.getNext().getType(), TYPE_RETURN);
    }


    // --- methodIfIfTryCatch --- //
    @Test
    public void testJdk118MethodIfIfTryCatch() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.1.8.zip"), "org/jd/core/test/TryCatchFinally", "methodIfIfTryCatch"));
        BasicBlock startBB = cfg.getStart();

        assertNotNull(startBB);

        BasicBlock ifBB = startBB.getNext();

        assertEquals(ifBB.getType(), TYPE_IF);
        assertEquals(ifBB.getNext().getType(), TYPE_RETURN);

        BasicBlock ifElseBB = ifBB.getSub1();

        assertEquals(ifElseBB.getType(), TYPE_IF_ELSE);

        assertEquals(ifElseBB.getSub1().getType(), TYPE_STATEMENTS);
        assertEquals(ifElseBB.getSub1().getNext().getType(), TYPE_TRY);
        assertEquals(ifElseBB.getSub1().getNext().getNext().getType(), TYPE_END);

        assertEquals(ifElseBB.getSub2().getType(), TYPE_STATEMENTS);
        assertEquals(ifElseBB.getSub2().getNext().getType(), TYPE_END);
    }


    // --- methodTryCatchFinallyReturn --- //
    @Test
    public void testJdk170MethodTryCatchFinallyReturn() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryCatchFinallyReturn"));
        BasicBlock startBB = cfg.getStart();

        assertNotNull(startBB);

        BasicBlock simpleBB = startBB.getNext();

        assertEquals(simpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock tryBB = simpleBB.getNext();

        assertNotNull(tryBB);
        assertEquals(tryBB.getType(), TYPE_TRY);
        assertNotNull(tryBB.getExceptionHandlers());
        assertEquals(tryBB.getExceptionHandlers().size(), 2);

        BasicBlock bb = tryBB.getSub1();

        assertEquals(bb.getType(), TYPE_RETURN_VALUE);

        ExceptionHandler eh0 = tryBB.getExceptionHandlers().get(0);

        assertEquals(eh0.getInternalThrowableName(), "java/lang/RuntimeException");
        assertEquals(eh0.getBasicBlock().getType(), TYPE_RETURN_VALUE);

        ExceptionHandler eh1 = tryBB.getExceptionHandlers().get(1);

        assertNull(eh1.getInternalThrowableName());
        assertEquals(eh1.getBasicBlock().getType(), TYPE_THROW);

        BasicBlock nextBB = tryBB.getNext();

        assertNotNull(nextBB);
        assertEquals(nextBB.getType(), TYPE_END);
    }

    @Test
    public void testEclipseJavaCompiler370MethodTryCatchFinallyReturn() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-eclipse-java-compiler-3.7.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryCatchFinallyReturn"));
        BasicBlock startBB = cfg.getStart();

        assertNotNull(startBB);

        BasicBlock simpleBB = startBB.getNext();

        assertEquals(simpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock tryBB = simpleBB.getNext();

        assertNotNull(tryBB);
        assertEquals(tryBB.getType(), TYPE_TRY);
        assertNotNull(tryBB.getExceptionHandlers());
        assertEquals(tryBB.getExceptionHandlers().size(), 2);

        BasicBlock bb = tryBB.getSub1();

        assertEquals(bb.getType(), TYPE_STATEMENTS);

        ExceptionHandler eh0 = tryBB.getExceptionHandlers().get(0);

        assertEquals(eh0.getInternalThrowableName(), "java/lang/RuntimeException");
        assertEquals(eh0.getBasicBlock().getType(), TYPE_STATEMENTS);

        ExceptionHandler eh1 = tryBB.getExceptionHandlers().get(1);

        assertNull(eh1.getInternalThrowableName());
        assertEquals(eh1.getBasicBlock().getType(), TYPE_THROW);

        BasicBlock nextBB = tryBB.getNext();

        assertNotNull(nextBB);
        assertEquals(nextBB.getType(), TYPE_END);
    }

    @Test
    public void testEclipseJavaCompiler3130MethodTryCatchFinallyReturn() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-eclipse-java-compiler-3.13.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryCatchFinallyReturn"));
        BasicBlock startBB = cfg.getStart();

        assertNotNull(startBB);

        BasicBlock simpleBB = startBB.getNext();

        assertEquals(simpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock tryBB = simpleBB.getNext();

        assertNotNull(tryBB);
        assertEquals(tryBB.getType(), TYPE_TRY);
        assertNotNull(tryBB.getExceptionHandlers());
        assertEquals(tryBB.getExceptionHandlers().size(), 2);

        BasicBlock bb = tryBB.getSub1();

        assertEquals(bb.getType(), TYPE_STATEMENTS);

        ExceptionHandler eh0 = tryBB.getExceptionHandlers().get(0);

        assertEquals(eh0.getInternalThrowableName(), "java/lang/RuntimeException");
        assertEquals(eh0.getBasicBlock().getType(), TYPE_STATEMENTS);

        ExceptionHandler eh1 = tryBB.getExceptionHandlers().get(1);

        assertNull(eh1.getInternalThrowableName());
        assertEquals(eh1.getBasicBlock().getType(), TYPE_THROW);

        BasicBlock nextBB = tryBB.getNext();

        assertNotNull(nextBB);
        assertEquals(nextBB.getType(), TYPE_END);
    }


    // --- complexMethodTryFinallyReturn --- //
    @Test
    public void testJdk170MethodComplexTryCatchCatchFinally() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryTryFinallyFinallyTryFinallyReturn"));
        BasicBlock startBB = cfg.getStart();

        assertNotNull(startBB);

        BasicBlock simpleBB = startBB.getNext();

        assertEquals(simpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock tryBB = simpleBB.getNext();

        assertNotNull(tryBB);
        assertEquals(tryBB.getType(), TYPE_TRY);
        assertNotNull(tryBB.getExceptionHandlers());
        assertEquals(tryBB.getExceptionHandlers().size(), 1);
        assertEquals(tryBB.getNext().getType(), TYPE_STATEMENTS);
        assertEquals(tryBB.getNext().getNext().getType(), TYPE_RETURN);

        BasicBlock bb = tryBB.getSub1();

        assertEquals(bb.getType(), TYPE_TRY);

        ExceptionHandler eh0 = tryBB.getExceptionHandlers().get(0);

        assertNull(eh0.getInternalThrowableName());
        assertEquals(eh0.getBasicBlock().getType(), TYPE_STATEMENTS);

        BasicBlock subTryBB = tryBB.getSub1();

        assertNotNull(subTryBB);
        assertEquals(subTryBB.getType(), TYPE_TRY);
        assertEquals(subTryBB.getNext().getType(), TYPE_TRY);
        assertEquals(subTryBB.getNext().getNext().getType(), TYPE_END);
    }

    @Test
    public void testJdk170MethodTryCatchTryCatchThrow() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.7.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryCatchTryCatchThrow"));
    }

    // --- methodTryTryFinallyFinallyTryFinally --- //
    @Test
    public void testEclipseJavaCompiler370MethodTryTryFinallyFinallyTryFinally() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-eclipse-java-compiler-3.7.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryTryFinallyFinallyTryFinally"));
        BasicBlock startBB = cfg.getStart();

        assertNotNull(startBB);

        BasicBlock simpleBB = startBB.getNext();

        assertEquals(simpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock tryBB = simpleBB.getNext();

        assertNotNull(tryBB);
        assertEquals(tryBB.getType(), TYPE_TRY_ECLIPSE);
        assertNotNull(tryBB.getExceptionHandlers());
        assertEquals(tryBB.getExceptionHandlers().size(), 1);
        assertEquals(tryBB.getNext().getType(), TYPE_TRY_ECLIPSE);
        assertEquals(tryBB.getNext().getNext().getType(), TYPE_STATEMENTS);
        assertEquals(tryBB.getNext().getNext().getNext().getType(), TYPE_RETURN);

        ExceptionHandler eh0 = tryBB.getExceptionHandlers().get(0);

        assertNull(eh0.getInternalThrowableName());
        assertEquals(eh0.getBasicBlock().getType(), TYPE_STATEMENTS);

        BasicBlock subTryBB = tryBB.getSub1();

        assertEquals(subTryBB.getType(), TYPE_TRY_ECLIPSE);
        assertNotNull(subTryBB.getExceptionHandlers());
        assertEquals(subTryBB.getExceptionHandlers().size(), 1);
        assertEquals(subTryBB.getNext().getType(), TYPE_STATEMENTS);
        assertEquals(subTryBB.getNext().getNext().getType(), TYPE_END);

        eh0 = subTryBB.getExceptionHandlers().get(0);

        assertNull(eh0.getInternalThrowableName());
        assertEquals(eh0.getBasicBlock().getType(), TYPE_THROW);
    }

    @Test
    public void testEclipseJavaCompiler3130MethodTryTryFinallyFinallyTryFinally() throws Exception {
        ControlFlowGraph cfg = checkCFGReduction(searchMethod(getResource("zip/data-java-eclipse-java-compiler-3.13.0.zip"), "org/jd/core/test/TryCatchFinally", "methodTryTryFinallyFinallyTryFinally"));
        BasicBlock startBB = cfg.getStart();

        assertNotNull(startBB);

        BasicBlock simpleBB = startBB.getNext();

        assertEquals(simpleBB.getType(), TYPE_STATEMENTS);

        BasicBlock tryBB = simpleBB.getNext();

        assertNotNull(tryBB);
        assertEquals(tryBB.getType(), TYPE_TRY_ECLIPSE);
        assertNotNull(tryBB.getExceptionHandlers());
        assertEquals(tryBB.getExceptionHandlers().size(), 1);
        assertEquals(tryBB.getNext().getType(), TYPE_TRY_ECLIPSE);
        assertEquals(tryBB.getNext().getNext().getType(), TYPE_STATEMENTS);
        assertEquals(tryBB.getNext().getNext().getNext().getType(), TYPE_RETURN);

        ExceptionHandler eh0 = tryBB.getExceptionHandlers().get(0);

        assertNull(eh0.getInternalThrowableName());
        assertEquals(eh0.getBasicBlock().getType(), TYPE_STATEMENTS);

        BasicBlock subTryBB = tryBB.getSub1();

        assertEquals(subTryBB.getType(), TYPE_TRY_ECLIPSE);
        assertNotNull(subTryBB.getExceptionHandlers());
        assertEquals(subTryBB.getExceptionHandlers().size(), 1);
        assertEquals(subTryBB.getNext().getType(), TYPE_STATEMENTS);
        assertEquals(subTryBB.getNext().getNext().getType(), TYPE_END);

        eh0 = subTryBB.getExceptionHandlers().get(0);

        assertNull(eh0.getInternalThrowableName());
        assertEquals(eh0.getBasicBlock().getType(), TYPE_THROW);
    }

    @Test
    public void testJdk118MethodTryTryFinallyFinallyTryFinally() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.1.8.zip"), "org/jd/core/test/TryCatchFinally", "methodTryTryFinallyFinallyTryFinally"));
    }

    @Test
    public void testJdk131MethodTryTryFinallyFinallyTryFinally() throws Exception {
        checkCFGReduction(searchMethod(getResource("zip/data-java-jdk-1.3.1.zip"), "org/jd/core/test/TryCatchFinally", "methodTryTryFinallyFinallyTryFinally"));
    }


    protected ControlFlowGraph checkCFGReduction(Method method) throws Exception {
        ControlFlowGraph cfg = ControlFlowGraphMaker.make(method);

        assertNotNull(cfg);

        BasicBlock root = cfg.getStart();

        assertNotNull(root);

        String plantuml = ControlFlowGraphPlantUMLWriter.write(cfg);
        System.out.println("Step 0: " + ControlFlowGraphPlantUMLWriter.writePlantUMLUrl(plantuml));

        ControlFlowGraphGotoReducer.reduce(cfg);

        plantuml = ControlFlowGraphPlantUMLWriter.write(cfg);
        System.out.println("Step 1: " + ControlFlowGraphPlantUMLWriter.writePlantUMLUrl(plantuml));

        // --- Test natural loops --- //
        BitSet[] dominators = ControlFlowGraphLoopReducer.buildDominatorIndexes(cfg);
        List<Loop> naturalLoops = ControlFlowGraphLoopReducer.identifyNaturalLoops(cfg, dominators);

        for (Loop loop : naturalLoops) {
            System.out.println(loop);
        }

        ControlFlowGraphLoopReducer.reduce(cfg);
        plantuml = ControlFlowGraphPlantUMLWriter.write(cfg);
        System.out.println("Step 2: " + ControlFlowGraphPlantUMLWriter.writePlantUMLUrl(plantuml));

        BitSet visited = new BitSet();
        BitSet jsrTargets = new BitSet();

        for (int i=0, count=3; i<5; i++, count++) {
            boolean reduced = ControlFlowGraphReducer.reduce(visited, cfg.getStart(), jsrTargets);

            System.out.println("# of visited blocks: " + visited.cardinality());
            visited.clear();
            plantuml = ControlFlowGraphPlantUMLWriter.write(cfg);
            System.out.println("Step " + count + ": " + ControlFlowGraphPlantUMLWriter.writePlantUMLUrl(plantuml));

            if (reduced) {
                break;
            }
        }

        checkFinalCFG(cfg);

        return cfg;
    }

    protected static void checkFinalCFG(ControlFlowGraph cfg) {
        List<BasicBlock> list = cfg.getBasicBlocks();

        for (int i=0, len=list.size(); i<len; i++) {
            BasicBlock basicBlock = list.get(i);

            if (basicBlock.getType() == TYPE_DELETED) {
                continue;
            }

            assertTrue("#" + basicBlock.getIndex() + " contains an invalid index", basicBlock.getIndex() == i);
            assertTrue("#" + basicBlock.getIndex() + " is a TRY_DECLARATION -> reduction failed", basicBlock.getType() != TYPE_TRY_DECLARATION);
            assertTrue("#" + basicBlock.getIndex() + " is a SWITCH_DECLARATION -> reduction failed", basicBlock.getType() != TYPE_SWITCH_DECLARATION);
            assertTrue("#" + basicBlock.getIndex() + " is a CONDITIONAL -> reduction failed", basicBlock.getType() != TYPE_CONDITIONAL_BRANCH);
            assertTrue("#" + basicBlock.getIndex() + " is a GOTO -> reduction failed", basicBlock.getType() != TYPE_GOTO);

            if (!basicBlock.matchType(GROUP_CONDITION)) {
                for (BasicBlock predecessor : basicBlock.getPredecessors()) {
                    assertTrue("#" + predecessor.getIndex() + " is a predecessor of #" + basicBlock.getIndex() + " but #" + predecessor.getIndex() + " does not refer it", predecessor.contains(basicBlock));
                }
            }

            if (basicBlock.matchType(TYPE_IF|TYPE_IF_ELSE)) {
                assertTrue("#" + basicBlock.getIndex() + " is a IF or a IF_ELSE with a 'then' branch jumping to itself", basicBlock.getSub1() != basicBlock);
            }

            if (basicBlock.getType() == TYPE_IF_ELSE) {
                assertTrue("#" + basicBlock.getIndex() + " is a IF_ELSE with a 'else' branch jumping to itself", basicBlock.getSub2() != basicBlock);
            }

            if (basicBlock.matchType(TYPE_TRY|TYPE_TRY_JSR)) {
                boolean containsFinally = false;
                HashSet<String> exceptionNames = new HashSet<>();
                int maxOffset = 0;

                for (ExceptionHandler exceptionHandler : basicBlock.getExceptionHandlers()) {
                    String name = exceptionHandler.getInternalThrowableName();
                    int offset = exceptionHandler.getBasicBlock().getFromOffset();

                    if (name == null) {
                        assertFalse("#" + basicBlock.getIndex() + " contains multiple finally handlers", containsFinally);
                        containsFinally = true;
                    } else {
                        assertFalse("#" + basicBlock.getIndex() + " contains multiple handlers for " + name, exceptionNames.contains(name));
                        exceptionNames.add(name);
                    }

                    assertTrue("#" + basicBlock.getIndex() + " have an invalid exception handler", !exceptionHandler.getBasicBlock().matchType(GROUP_CONDITION));

                    if (maxOffset < offset) {
                        maxOffset = offset;
                    }
                }

                for (ExceptionHandler exceptionHandler : basicBlock.getExceptionHandlers()) {
                    BasicBlock bb = exceptionHandler.getBasicBlock();
                    int offset = bb.getFromOffset();

                    if (maxOffset != offset) {
                        // Search last offset
                        BasicBlock next = bb.getNext();

                        while ((bb != next) && next.matchType(GROUP_SINGLE_SUCCESSOR|TYPE_RETURN|TYPE_RETURN_VALUE|TYPE_THROW) && (next.getPredecessors().size() == 1)) {
                            bb = next;
                            next = next.getNext();
                            offset = bb.getFromOffset();
                        }

                        assertTrue("#" + basicBlock.getIndex() + " is a TRY or TRY_WITH_JST -> #" + exceptionHandler.getBasicBlock().getIndex() + " handler reduction failed", offset < maxOffset);
                    }
                }
            }

            if (basicBlock.getType() == TYPE_SWITCH) {
                for (SwitchCase switchCase : basicBlock.getSwitchCases()) {
                    assertTrue("#" + basicBlock.getIndex() + " have an invalid switch case", !switchCase.getBasicBlock().matchType(GROUP_CONDITION));
                }
            }

            if (!basicBlock.matchType(GROUP_CONDITION)) {
                assertTrue("#" + basicBlock.getIndex() + " have an invalid next basic block", (basicBlock.getNext() == null) || !basicBlock.getNext().matchType(GROUP_CONDITION));
                assertTrue("#" + basicBlock.getIndex() + " have an invalid branch basic block", (basicBlock.getBranch() == null) || !basicBlock.getBranch().matchType(GROUP_CONDITION));
                assertTrue("#" + basicBlock.getIndex() + " have an invalid sub1 basic block", (basicBlock.getSub1() == null) || !basicBlock.getSub1().matchType(GROUP_CONDITION));
                assertTrue("#" + basicBlock.getIndex() + " have an invalid sub2 basic block", (basicBlock.getSub2() == null) || !basicBlock.getSub2().matchType(GROUP_CONDITION));
            }
        }

        BitSet visited = new BitSet(list.size());
        SilentWatchDog watchdog = new SilentWatchDog();
        BasicBlock result = checkBasicBlock(visited, cfg.getStart(), watchdog);

        assertFalse("DELETED basic block detected -> reduction failed", (result != null) && (result.getType() == TYPE_DELETED));
        assertFalse("Cycle detected -> reduction failed", (result != null) && (result.getType() != TYPE_DELETED));
    }

    protected static BasicBlock checkBasicBlock(BitSet visited, BasicBlock basicBlock, SilentWatchDog watchdog) {
        if ((basicBlock == null) || basicBlock.matchType(TYPE_END|TYPE_SWITCH_BREAK|TYPE_LOOP_START|TYPE_LOOP_CONTINUE|TYPE_LOOP_END|TYPE_RETURN)) {
            return null;
        } else {
            BasicBlock result;

            visited.set(basicBlock.getIndex());

            switch (basicBlock.getType()) {
                case TYPE_DELETED:
                    return basicBlock;
                case TYPE_SWITCH_DECLARATION:
                case TYPE_SWITCH:
                    for (SwitchCase switchCase : basicBlock.getSwitchCases()) {
                        result = checkBasicBlock(visited, basicBlock, switchCase.getBasicBlock(), watchdog);
                        if (result != null)
                            return result;
                    }
                    return null;
                case TYPE_TRY:
                case TYPE_TRY_JSR:
                    result = checkBasicBlock(visited, basicBlock, basicBlock.getSub1(), watchdog);
                    if (result != null)
                        return result;
                case TYPE_TRY_DECLARATION:
                    for (ExceptionHandler exceptionHandler : basicBlock.getExceptionHandlers()) {
                        result = checkBasicBlock(visited, basicBlock, exceptionHandler.getBasicBlock(), watchdog);
                        if (result != null)
                            return result;
                    }
                    return checkBasicBlock(visited, basicBlock, basicBlock.getNext(), watchdog);
                case TYPE_CONDITIONAL_BRANCH:
                case TYPE_JSR:
                    result = checkBasicBlock(visited, basicBlock, basicBlock.getNext(), watchdog);
                    if (result != null)
                        return result;
                    return checkBasicBlock(visited, basicBlock, basicBlock.getBranch(), watchdog);
                case TYPE_IF_ELSE:
                case TYPE_TERNARY_OPERATOR:
                    result = checkBasicBlock(visited, basicBlock, basicBlock.getSub2(), watchdog);
                    if (result != null)
                        return result;
                case TYPE_IF:
                    result = checkBasicBlock(visited, basicBlock, basicBlock.getCondition(), watchdog);
                    if (result != null)
                        return result;
                case TYPE_LOOP:
                    result = checkBasicBlock(visited, basicBlock, basicBlock.getSub1(), watchdog);
                    if (result != null)
                        return result;
                case TYPE_START:
                case TYPE_STATEMENTS:
                case TYPE_GOTO:
                    return checkBasicBlock(visited, basicBlock, basicBlock.getNext(), watchdog);
                default:
                    return null;
            }
        }
    }

    protected static BasicBlock checkBasicBlock(BitSet visited, BasicBlock parent, BasicBlock child, SilentWatchDog watchdog) {
        if (!child.matchType(BasicBlock.GROUP_END) && !watchdog.silentCheck(parent, child)) {
            return parent;
        }

        return checkBasicBlock(visited, child, watchdog);
    }

    protected static class SilentWatchDog extends WatchDog {
        public boolean silentCheck(BasicBlock parent, BasicBlock child) {
            if (!child.matchType(BasicBlock.GROUP_END)) {
                Link link = new Link(parent, child);

                if (links.contains(link)) {
                    return false;
                }

                links.add(link);
            }

            return true;
        }
    }

    protected InputStream getResource(String zipName) {
        return this.getClass().getResourceAsStream("/" + zipName);
    }

    protected InputStream loadFile(String zipName) throws IOException {
        return new FileInputStream(zipName);
    }

    protected Method searchMethod(String internalTypeName, String methodName) throws Exception {
        return searchMethod(loader, typeMaker, internalTypeName, methodName, null);
    }

    protected Method searchMethod(InputStream is, String internalTypeName, String methodName) throws Exception {
        return searchMethod(is, internalTypeName, methodName, null);
    }

    protected Method searchMethod(InputStream is, String internalTypeName, String methodName, String methodDescriptor) throws Exception {
        if (is == null) {
            return null;
        } else {
            ZipLoader loader = new ZipLoader(is);
            TypeMaker typeMaker = new TypeMaker(loader);
            return searchMethod(loader, typeMaker, internalTypeName, methodName, methodDescriptor);
        }
    }

    protected Method searchMethod(Loader loader, TypeMaker typeMaker, String internalTypeName, String methodName, String methodDescriptor) throws Exception {
        Message message = new Message();
        message.setHeader("mainInternalTypeName", internalTypeName);
        message.setHeader("loader", loader);
        message.setHeader("typeMaker", typeMaker);

        deserializer.process(message);
        converter.process(message);

        CompilationUnit compilationUnit = message.getBody();

        assertNotNull(compilationUnit);

        BaseTypeDeclaration typeDeclarations = compilationUnit.getTypeDeclarations();
        BodyDeclaration bodyDeclaration = null;

        if (typeDeclarations instanceof EnumDeclaration) {
            bodyDeclaration = ((EnumDeclaration)typeDeclarations).getBodyDeclaration();
        } else if (typeDeclarations instanceof AnnotationDeclaration) {
            bodyDeclaration = ((AnnotationDeclaration)typeDeclarations).getBodyDeclaration();
        } else if (typeDeclarations instanceof InterfaceDeclaration) {
            bodyDeclaration = ((InterfaceDeclaration)typeDeclarations).getBodyDeclaration();
        }

        if (bodyDeclaration != null) {
            ClassFileBodyDeclaration cfbd = (ClassFileBodyDeclaration) bodyDeclaration;

            for (ClassFileMemberDeclaration md : cfbd.getMethodDeclarations()) {
                if (md instanceof ClassFileMethodDeclaration) {
                    ClassFileMethodDeclaration cfmd = (ClassFileMethodDeclaration) md;
                    if (cfmd.getName().equals(methodName)) {
                        if ((methodDescriptor == null) || cfmd.getDescriptor().equals(methodDescriptor)) {
                            return cfmd.getMethod();
                        }
                    }
                } else if (md instanceof ClassFileConstructorDeclaration) {
                    ClassFileConstructorDeclaration cfcd = (ClassFileConstructorDeclaration) md;
                    if (cfcd.getMethod().getName().equals(methodName)) {
                        if ((methodDescriptor == null) || cfcd.getDescriptor().equals(methodDescriptor)) {
                            return cfcd.getMethod();
                        }
                    }
                } else if (md instanceof ClassFileStaticInitializerDeclaration) {
                    ClassFileStaticInitializerDeclaration cfsid = (ClassFileStaticInitializerDeclaration) md;
                    if (cfsid.getMethod().getName().equals(methodName)) {
                        return cfsid.getMethod();
                    }
                }
            }
        }

        return null;
    }
}
