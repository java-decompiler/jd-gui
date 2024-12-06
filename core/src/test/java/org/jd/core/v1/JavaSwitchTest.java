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

public class JavaSwitchTest extends TestCase {
    protected DeserializeClassFileProcessor deserializer = new DeserializeClassFileProcessor();
    protected ClassFileToJavaSyntaxProcessor converter = new ClassFileToJavaSyntaxProcessor();
    protected JavaSyntaxToJavaFragmentProcessor fragmenter = new JavaSyntaxToJavaFragmentProcessor();
    protected LayoutFragmentProcessor layouter = new LayoutFragmentProcessor();
    //protected TestTokenizeJavaFragmentProcessor tokenizer = new TestTokenizeJavaFragmentProcessor();
    protected JavaFragmentToTokenProcessor tokenizer = new JavaFragmentToTokenProcessor();
    protected WriteTokenProcessor writer = new WriteTokenProcessor();

    @Test
    public void testJdk170Switch() throws Exception {
        String internalClassName = "org/jd/core/test/Switch";
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.7.0.zip");
        Loader loader = new ZipLoader(is);
        Map<String, Object> configuration = Collections.singletonMap("realignLineNumbers", Boolean.TRUE);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName, configuration);

        // Check decompiled source code
        assertTrue(source.matches(PatternMaker.make("/*  15:  15 */", "switch (i)")));
        assertTrue(source.matches(PatternMaker.make("/*  16:   0 */", "case 0:")));
        assertTrue(source.matches(PatternMaker.make("/*  17:  17 */", "System.out.println(\"0\");")));
        assertTrue(source.matches(PatternMaker.make("/*  18:   0 */", "break;")));

        assertTrue(source.matches(PatternMaker.make("/*  34:   0 */", "case 0:")));
        assertTrue(source.matches(PatternMaker.make("/*  35:  35 */", "System.out.println(\"0\");")));
        assertTrue(source.matches(PatternMaker.make("/*  36:   0 */", "case 1:")));

        assertTrue(source.matches(PatternMaker.make("/*  56:   0 */", "default:")));

        assertTrue(source.matches(PatternMaker.make("/* 110:   0 */", "break;")));
        assertTrue(source.matches(PatternMaker.make("/* 111:   0 */", "case 1:")));
        assertTrue(source.matches(PatternMaker.make("/* 112: 112 */", "System.out.println(\"1\");")));
        assertTrue(source.matches(PatternMaker.make("/* 113: 113 */", "throw new RuntimeException(\"boom\");")));

        assertTrue(source.matches(PatternMaker.make("/* 134:   0 */", "return;")));

        assertTrue(source.matches(PatternMaker.make("/* 171:   0 */", "case 3:")));
        assertTrue(source.matches(PatternMaker.make("/* 172:   0 */", "case 4:")));
        assertTrue(source.matches(PatternMaker.make("/* 173: 173 */", "System.out.println(\"3 or 4\");")));
        assertTrue(source.matches(PatternMaker.make("/* 174:   0 */", "break;")));

        assertTrue(source.matches(PatternMaker.make("/* 265:   0 */", "case 1:")));
        assertTrue(source.matches(PatternMaker.make("/* 266:   0 */", "break;")));
        assertTrue(source.matches(PatternMaker.make("/* 267:   0 */", "default:")));

        assertTrue(source.matches(PatternMaker.make("/* 283:   0 */", "case 1:")));
        assertTrue(source.matches(PatternMaker.make("/* 284:   0 */", "case 2:")));
        assertTrue(source.matches(PatternMaker.make("/* 285:   0 */", "case 3:")));
        assertTrue(source.matches(PatternMaker.make("/* 286:   0 */", "break;")));
        assertTrue(source.matches(PatternMaker.make("/* 288:   0 */", "default:")));

        // Recompile decompiled source code and check errors
        assertTrue(CompilerUtil.compile("1.7", new JavaSourceFileObject(internalClassName, source)));
    }

    @Test
    public void testJdk170AdvancedSwitch() throws Exception {
        String internalClassName = "org/jd/core/test/AdvancedSwitch";
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.7.0.zip");
        Loader loader = new ZipLoader(is);
        Map<String, Object> configuration = Collections.singletonMap("realignLineNumbers", Boolean.TRUE);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName, configuration);

        // Check decompiled source code
        assertTrue(source.matches(PatternMaker.make("/* 13: 13 */", "A,", "B,", "C;")));

        assertTrue(source.matches(PatternMaker.make("/* 19: 19 */", "switch (te)")));
        assertTrue(source.matches(PatternMaker.make("/* 20:  0 */", "case A:")));
        assertTrue(source.matches(PatternMaker.make("/* 22:  0 */", "case B:")));
        assertTrue(source.matches(PatternMaker.make("/* 25:  0 */", "case C:")));

        assertTrue(source.matches(PatternMaker.make("/* 39:  0 */", "case A:")));
        assertTrue(source.matches(PatternMaker.make("/* 40:  0 */", "case B:")));
        assertTrue(source.matches(PatternMaker.make("/* 41: 41 */", "System.out.println(\"A or B\");")));

        assertTrue(source.matches(PatternMaker.make("/* 56: 56 */", "switch (str)")));
        assertTrue(source.matches(PatternMaker.make("/* 57:  0 */", "case \"One\":")));
        assertTrue(source.matches(PatternMaker.make("/* 58: 58 */", "System.out.println(1);")));

        assertTrue(source.matches(PatternMaker.make("/* 78:  0 */", "case \"One\":")));
        assertTrue(source.matches(PatternMaker.make("/* 79:  0 */", "case \"POe\":")));
        assertTrue(source.matches(PatternMaker.make("/* 80: 80 */", "System.out.println(\"'One' or 'POe'\");")));

        // Recompile decompiled source code and check errors
        assertTrue(CompilerUtil.compile("1.7", new JavaSourceFileObject(internalClassName, source)));
    }

    @Test
    public void testEclipseJavaCompiler321Switch() throws Exception {
        String internalClassName = "org/jd/core/test/Switch";
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-eclipse-java-compiler-3.2.1.zip");
        Loader loader = new ZipLoader(is);
        Map<String, Object> configuration = Collections.singletonMap("realignLineNumbers", Boolean.TRUE);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName, configuration);

        // Check decompiled source code
        assertTrue(source.indexOf("/* 239: 239 */") != -1);

        // Recompile decompiled source code and check errors
        assertTrue(CompilerUtil.compile("1.5", new JavaSourceFileObject(internalClassName, source)));
    }

    @Test
    public void testEclipseJavaCompiler3130Switch() throws Exception {
        String internalClassName = "org/jd/core/test/Switch";
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-eclipse-java-compiler-3.13.0.zip");
        Loader loader = new ZipLoader(is);
        Map<String, Object> configuration = Collections.singletonMap("realignLineNumbers", Boolean.TRUE);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName, configuration);

        // Check decompiled source code
        assertTrue(source.indexOf("/* 239: 239 */") != -1);

        // Recompile decompiled source code and check errors
        assertTrue(CompilerUtil.compile("1.5", new JavaSourceFileObject(internalClassName, source)));
    }

    @Test
    // Test switch-enum in an other switch-enum
    public void testBstMutationResult() throws Exception {
        String internalClassName = "com/google/common/collect/BstMutationResult";
        Class mainClass = com.google.common.collect.Collections2.class;
        InputStream is = new FileInputStream(Paths.get(mainClass.getProtectionDomain().getCodeSource().getLocation().toURI()).toFile());
        Loader loader = new ZipLoader(is);
        Map<String, Object> configuration = Collections.singletonMap("realignLineNumbers", Boolean.TRUE);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName, configuration);

        // Check decompiled source code
        assertTrue(source.indexOf("N resultLeft, resultRight;") != -1);
        assertTrue(source.indexOf("assert false;") == -1);
        assertTrue(source.matches(PatternMaker.make("/* 131:", "resultLeft = liftOriginalRoot.childOrNull(BstSide.LEFT);")));
        assertTrue(source.matches(PatternMaker.make("/* 134:", "case LEFT:")));

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
