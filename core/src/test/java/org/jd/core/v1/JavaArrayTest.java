/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1;

import junit.framework.TestCase;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.api.printer.Printer;
import org.jd.core.v1.compiler.CompilerUtil;
import org.jd.core.v1.compiler.JavaSourceFileObject;
import org.jd.core.v1.loader.ClassPathLoader;
import org.jd.core.v1.loader.ZipLoader;
import org.jd.core.v1.model.message.Message;
import org.jd.core.v1.printer.PlainTextPrinter;
import org.jd.core.v1.regex.PatternMaker;
import org.jd.core.v1.service.converter.classfiletojavasyntax.ClassFileToJavaSyntaxProcessor;
import org.jd.core.v1.service.deserializer.classfile.DeserializeClassFileProcessor;
import org.jd.core.v1.service.fragmenter.javasyntaxtojavafragment.JavaSyntaxToJavaFragmentProcessor;
import org.jd.core.v1.service.layouter.LayoutFragmentProcessor;
import org.jd.core.v1.service.tokenizer.javafragmenttotoken.JavaFragmentToTokenProcessor;
import org.jd.core.v1.service.writer.WriteTokenProcessor;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

public class JavaArrayTest extends TestCase {
    protected DeserializeClassFileProcessor deserializer = new DeserializeClassFileProcessor();
    protected ClassFileToJavaSyntaxProcessor converter = new ClassFileToJavaSyntaxProcessor();
    protected JavaSyntaxToJavaFragmentProcessor fragmenter = new JavaSyntaxToJavaFragmentProcessor();
    protected LayoutFragmentProcessor layouter = new LayoutFragmentProcessor();
    //protected TestTokenizeJavaFragmentProcessor tokenizer = new TestTokenizeJavaFragmentProcessor();
    protected JavaFragmentToTokenProcessor tokenizer = new JavaFragmentToTokenProcessor();
    protected WriteTokenProcessor writer = new WriteTokenProcessor();

    @Test
    public void testJdk150Array() throws Exception {
        String internalClassName = "org/jd/core/test/Array";
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.5.0.zip");
        Loader loader = new ZipLoader(is);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName);

        // Check decompiled source code
        assertTrue(source.matches(PatternMaker.make(": 13 */", "int[][] arrayOfInt1 = new int[1][];")));
        assertTrue(source.matches(PatternMaker.make(": 30 */", "int[][] arrayOfInt1 = { { 0, 1, 2")));

        assertTrue(source.matches(PatternMaker.make(": 52 */", "testException2(new Exception[][]", "{ { new Exception(\"1\")")));

        assertTrue(source.matches(PatternMaker.make(": 73 */", "testInt2(new int[][] { { 1,")));

        assertTrue(source.matches(PatternMaker.make(": 73 */", "testInt2(new int[][] { { 1,")));
        assertTrue(source.matches(PatternMaker.make(": 75 */", "testInt3(new int[][][] { { { 0, 1")));

        // Recompile decompiled source code and check errors
        assertTrue(CompilerUtil.compile("1.5", new JavaSourceFileObject(internalClassName, source)));
    }

    @Test
    public void testJdk170Array() throws Exception {
        String internalClassName = "org/jd/core/test/Array";
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.7.0.zip");
        Loader loader = new ZipLoader(is);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName);

        // Check decompiled source code
        assertTrue(source.matches(PatternMaker.make(": 12 */", "int[] i1 = new int[1];")));
        assertTrue(source.matches(PatternMaker.make(": 13 */", "int[][] i2 = new int[1][];")));
        assertTrue(source.matches(PatternMaker.make(": 14 */", "int[][][] i3 = new int[1][][];")));
        assertTrue(source.matches(PatternMaker.make(": 15 */", "int[][][] i4 = new int[1][2][];")));
        assertTrue(source.matches(PatternMaker.make(": 22 */", "String[][][][] s5 = new String[1][2][][];")));

        assertTrue(source.matches(PatternMaker.make(": 26 */", "byte[] b1 = { 1, 2 } ;")));
        assertTrue(source.matches(PatternMaker.make(": 27 */", "byte[][] b2 = { { 1, 2 } } ;")));
        assertTrue(source.matches(PatternMaker.make(": 28 */", "byte[][][][] b3 = { { { 3, 4 } } } ;")));

        assertTrue(source.matches(PatternMaker.make(": 48 */", "testException1(new Exception[]", "{ new Exception(\"1\") } );")));

        assertTrue(source.matches(PatternMaker.make(": 73 */", "testInt2(new int[][]", "{ { 1 } ,")));

        // Recompile decompiled source code and check errors
        assertTrue(CompilerUtil.compile("1.7", new JavaSourceFileObject(internalClassName, source)));
    }

    @Test
    public void testInitializedArrayInTernaryOperator() throws Exception {
        class InitializedArrayInTernaryOperator {
            Class[] test0(int i) {
                return (i == 0) ? new Class[] { Object.class } : null;
            }
            Class[] test2(int i) {
                return (i == 0) ? new Class[] { Object.class, String.class, Number.class } : null;
            }
            Class[][] test3(int i) {
                return (i == 0) ? new Class[][] { { Object.class }, { String.class, Number.class} } : null;
            }
            Class[] test4(int i) {
                return (i == 0) ? null : new Class[] { Object.class };
            }
            Class[] test5(int i) {
                return (i == 0) ? null : new Class[] { Object.class, String.class, Number.class };
            }
            Class[][] test6(int i) {
                return (i == 0) ? null : new Class[][] { { Object.class }, { String.class, Number.class} };
            }
            Class[] test7(int i) {
                return (i == 0) ? new Class[] { Object.class } : new Class[] { String.class, Number.class };
            }
        }

        String internalClassName = InitializedArrayInTernaryOperator.class.getName().replace('.', '/');
        String source = decompile(new ClassPathLoader(), new PlainTextPrinter(), internalClassName);

        // Check decompiled source code
        assertTrue(source.matches(PatternMaker.make(":  95 */", "return (i == 0) ? new Class<?>[] { Object.class } : null;")));
        assertTrue(source.matches(PatternMaker.make(":  98 */", "return (i == 0) ? new Class<?>[] { Object.class, String.class, Number.class } : null;")));
        assertTrue(source.matches(PatternMaker.make(": 101 */", "return (i == 0) ? new Class[][] { { Object.class }, { String.class, Number.class} } : null;")));
        assertTrue(source.matches(PatternMaker.make(": 104 */", "return (i == 0) ? null : new Class<?>[] { Object.class };")));
        assertTrue(source.matches(PatternMaker.make(": 107 */", "return (i == 0) ? null : new Class<?>[] { Object.class, String.class, Number.class };")));
        assertTrue(source.matches(PatternMaker.make(": 110 */", "return (i == 0) ? null : new Class[][] { { Object.class }, { String.class, Number.class} };")));
        assertTrue(source.matches(PatternMaker.make(": 113 */", "return (i == 0) ? new Class<?>[] { Object.class } : new Class<?>[] { String.class, Number.class };")));

        // Recompile decompiled source code and check errors
        assertTrue(CompilerUtil.compile("1.8", new JavaSourceFileObject(internalClassName, source)));
    }

    protected String decompile(Loader loader, Printer printer, String internalTypeName) throws Exception {
        return decompile(loader, printer, internalTypeName, Collections.emptyMap());
    }

    protected String decompile(Loader loader, Printer printer, String internalTypeName, Map<String, Object> configuration) throws Exception {
        Message message = new Message();
        message.setHeader("loader", loader);
        message.setHeader("printer", printer);
        message.setHeader("mainInternalTypeName", internalTypeName);
        message.setHeader("configuration", configuration);

        deserializer.process(message);
        converter.process(message);
        fragmenter.process(message);
        layouter.process(message);
        tokenizer.process(message);
        writer.process(message);

        String source = printer.toString();

        printSource(source);

        assertTrue(source.indexOf("// Byte code:") == -1);

        return source;
    }

    protected void printSource(String source) {
        System.out.println("- - - - - - - - ");
        System.out.println(source);
        System.out.println("- - - - - - - - ");
    }
}
