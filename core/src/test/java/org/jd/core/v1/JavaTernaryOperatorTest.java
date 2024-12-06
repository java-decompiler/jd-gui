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

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

public class JavaTernaryOperatorTest extends TestCase {
    protected DeserializeClassFileProcessor deserializer = new DeserializeClassFileProcessor();
    protected ClassFileToJavaSyntaxProcessor converter = new ClassFileToJavaSyntaxProcessor();
    protected JavaSyntaxToJavaFragmentProcessor fragmenter = new JavaSyntaxToJavaFragmentProcessor();
    protected LayoutFragmentProcessor layouter = new LayoutFragmentProcessor();
    //protected TestTokenizeJavaFragmentProcessor tokenizer = new TestTokenizeJavaFragmentProcessor();
    protected JavaFragmentToTokenProcessor tokenizer = new JavaFragmentToTokenProcessor();
    protected WriteTokenProcessor writer = new WriteTokenProcessor();

    @Test
    public void testJdk118TernaryOperator() throws Exception {
        String internalClassName = "org/jd/core/test/TernaryOperator";
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.1.8.zip");
        Loader loader = new ZipLoader(is);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName);

        // Check decompiled source code
        assertTrue(source.matches(PatternMaker.make(":  13 */", "this.str =")));
        assertTrue(source.matches(PatternMaker.make(":  14 */", "(s == null) ?")));
        assertTrue(source.matches(PatternMaker.make(":  15 */", "\"1\"")));
        assertTrue(source.matches(PatternMaker.make(":  16 */", "\"2\";")));
        assertTrue(source.matches(PatternMaker.make(":  24 */", "s = (s == null) ? ((s == null) ? \"1\" : \"2\") : ((s == null) ? \"3\" : \"4\");")));
        assertTrue(source.matches(PatternMaker.make(":  34 */", "return !(s != s || time < time);")));
        assertTrue(source.matches(PatternMaker.make(":  40 */", "if ((s1 == null) ? (s2 == null) : s1.equals(s2))")));
        assertTrue(source.matches(PatternMaker.make(":  60 */", "if ((s1 == null) ? (s2 == null) : s1.equals(s2))")));
        assertTrue(source.matches(PatternMaker.make(":  71 */", "if ((s1 == null) ? false : (s1.length() > 0))")));
        assertTrue(source.matches(PatternMaker.make(":  82 */", "if (s1 != null && s1.length() > 0)")));
        assertTrue(source.matches(PatternMaker.make(": 126 */", "if (s1 == null && false)")));
        assertTrue(source.matches(PatternMaker.make(": 137 */", "if (s1 == s2 && ((s1 == null) ? (s2 == null) : s1.equals(s2)) && s1 == s2)")));
        assertTrue(source.matches(PatternMaker.make(": 148 */", "if (s1 == s2 || ((s1 == null) ? (s2 == null) : s1.equals(s2)) || s1 == s2)")));
        assertTrue(source.matches(PatternMaker.make(": 157 */", "return Short.toString((short)((this == null) ? 1 : 2));")));

        // Recompile decompiled source code and check errors
        assertTrue(CompilerUtil.compile("1.3", new JavaSourceFileObject(internalClassName, source)));
    }

    @Test
    public void testJdk170TernaryOperator() throws Exception {
        String internalClassName = "org/jd/core/test/TernaryOperator";
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.7.0.zip");
        Loader loader = new ZipLoader(is);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName);

        // Check decompiled source code
        assertTrue(source.matches(PatternMaker.make(":  13 */", "this.str = (s == null) ? \"1\" : \"2\";")));
        assertTrue(source.matches(PatternMaker.make(":  24 */", "s = (s == null) ? ((s == null) ? \"1\" : \"2\") : ((s == null) ? \"3\" : \"4\");")));
        assertTrue(source.matches(PatternMaker.make(":  34 */", "return (s == s && time >= time);")));
        assertTrue(source.matches(PatternMaker.make(":  40 */", "if ((s1 == null) ? (s2 == null) : s1.equals(s2))")));
        assertTrue(source.matches(PatternMaker.make(":  60 */", "if ((s1 == null) ? (s2 == null) : s1.equals(s2))")));
        assertTrue(source.matches(PatternMaker.make(":  71 */", "if (s1 != null && s1.length() > 0)")));
        assertTrue(source.matches(PatternMaker.make(":  82 */", "if (s1 != null && s1.length() > 0)")));
        assertTrue(source.matches(PatternMaker.make(": 126 */", "if (s1 == null);")));
        assertTrue(source.matches(PatternMaker.make(": 137 */", "if (s1 == s2 && ((s1 == null) ? (s2 == null) : s1.equals(s2)) && s1 == s2)")));
        assertTrue(source.matches(PatternMaker.make(": 148 */", "if (s1 == s2 || ((s1 == null) ? (s2 == null) : s1.equals(s2)) || s1 == s2)")));
        assertTrue(source.matches(PatternMaker.make(": 157 */", "return Short.toString((short)((this == null) ? 1 : 2));")));

        // Recompile decompiled source code and check errors
        assertTrue(CompilerUtil.compile("1.7", new JavaSourceFileObject(internalClassName, source)));
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
