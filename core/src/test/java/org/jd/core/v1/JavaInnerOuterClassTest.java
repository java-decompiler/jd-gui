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

public class JavaInnerOuterClassTest extends TestCase {
    protected DeserializeClassFileProcessor deserializer = new DeserializeClassFileProcessor();
    protected ClassFileToJavaSyntaxProcessor converter = new ClassFileToJavaSyntaxProcessor();
    protected JavaSyntaxToJavaFragmentProcessor fragmenter = new JavaSyntaxToJavaFragmentProcessor();
    protected LayoutFragmentProcessor layouter = new LayoutFragmentProcessor();
    //protected TestTokenizeJavaFragmentProcessor tokenizer = new TestTokenizeJavaFragmentProcessor();
    protected JavaFragmentToTokenProcessor tokenizer = new JavaFragmentToTokenProcessor();
    protected WriteTokenProcessor writer = new WriteTokenProcessor();

    @Test
    public void testJdk170InnerOuterClass() throws Exception {
        String internalClassName = "org/jd/core/test/OuterClass";
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.7.0.zip");
        Loader loader = new ZipLoader(is);
        Map<String, Object> configuration = Collections.singletonMap("realignLineNumbers", Boolean.TRUE);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName, configuration);

        // Check decompiled source code
        assertTrue(source.matches(PatternMaker.make(":  10 */", "protected int outerField1 = 0;")));
        assertTrue(source.matches(PatternMaker.make(":  11 */", "protected String[] outerField2 = { \"0\" };")));

        assertTrue(source.indexOf("final int localVariable1 = param1;") != -1);
        assertTrue(source.indexOf("final String[] localVariable2 = param2;") != -1);

        assertTrue(source.matches(PatternMaker.make(":  21 */", "InnerClass innerClass = new InnerClass(param1, param2);")));
        assertTrue(source.matches(PatternMaker.make(":  22 */", "innerClass.innerMethod(localVariable1, localVariable2);")));
        assertTrue(source.matches(PatternMaker.make(":  24 */", "StaticInnerClass staticInnerClass = new StaticInnerClass(param1, param2);")));
        assertTrue(source.matches(PatternMaker.make(":  25 */", "staticInnerClass.innerMethod(localVariable1, localVariable2);")));

        assertTrue(source.matches(PatternMaker.make(":  27 */", "InnerClass anonymousClass = new InnerClass(param1, param2)")));
        assertTrue(source.indexOf("public void innerMethod(int param1, String... param2)") != -1);
        assertTrue(source.matches(PatternMaker.make(":  30 */", "this.innerField2 = param2;")));
        assertTrue(source.matches(PatternMaker.make(":  32 */", "OuterClass.this.outerField1 = param1;")));
        assertTrue(source.matches(PatternMaker.make(":  33 */", "OuterClass.this.outerField2 = param2;")));
        assertTrue(source.matches(PatternMaker.make(":  35 */", "this.innerField1 = localVariable1;")));
        assertTrue(source.matches(PatternMaker.make(":  36 */", "this.innerField2 = localVariable2;")));

        assertTrue(source.matches(PatternMaker.make(":  39 */", "anonymousClass.innerMethod(localVariable1, localVariable2);")));

        assertTrue(source.matches(PatternMaker.make(":  41 */", "StaticInnerClass staticAnonymousClass = new StaticInnerClass(param1, param2)")));
        assertTrue(source.matches(PatternMaker.make(":  44 */", "this.innerField2 = param2;")));
        assertTrue(source.matches(PatternMaker.make(":  46 */", "OuterClass.this.outerField1 = param1;")));
        assertTrue(source.matches(PatternMaker.make(":  47 */", "OuterClass.this.outerField2 = param2;")));
        assertTrue(source.matches(PatternMaker.make(":  49 */", "this.innerField1 = localVariable1;")));
        assertTrue(source.matches(PatternMaker.make(":  50 */", "this.innerField2 = localVariable2;")));

        assertTrue(source.matches(PatternMaker.make(":  53 */", "staticAnonymousClass.innerMethod(localVariable1, localVariable2);")));

        assertTrue(source.matches(PatternMaker.make(":  55 */", "InnerEnum.A.innerMethod(localVariable1, localVariable2);")));

        assertTrue(source.matches(PatternMaker.make("/*  56:   0 */", "class LocalClass")));
        assertTrue(source.matches(PatternMaker.make(":  58 */", "protected int innerField1 = 0;")));
        assertTrue(source.matches(PatternMaker.make(":  59 */", "protected String[] innerField2 = { \"0\" } ;")));
        assertTrue(source.matches(PatternMaker.make(":  69 */", "this.innerField1 = param1;")));
        assertTrue(source.matches(PatternMaker.make(":  70 */", "this.innerField2 = param2;")));
        assertTrue(source.matches(PatternMaker.make(":  72 */", "OuterClass.this.outerField1 = param1;")));
        assertTrue(source.matches(PatternMaker.make(":  73 */", "OuterClass.this.outerField2 = param2;")));
        assertTrue(source.matches(PatternMaker.make(":  75 */", "this.innerField1 = localVariable1;")));
        assertTrue(source.matches(PatternMaker.make(":  76 */", "this.innerField2 = localVariable2;")));
        assertTrue(source.matches(PatternMaker.make(":  94 */", "LocalClass localClass = new LocalClass(param1, param2);")));
        assertTrue(source.matches(PatternMaker.make(":  95 */", "localClass.localMethod(localVariable1, localVariable2);")));

        assertTrue(source.matches(PatternMaker.make(": 114 */", "this(param1, param2);")));
        assertTrue(source.matches(PatternMaker.make(": 144 */", "this(param1, param2);")));

        assertTrue(source.matches(PatternMaker.make(": 158 */", "A,", "B,", "C;")));
        assertTrue(source.indexOf("/* 182: 182 */") != -1);

        assertTrue(source.matches(PatternMaker.make("public class InnerInnerClass", "{", "}")));

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
