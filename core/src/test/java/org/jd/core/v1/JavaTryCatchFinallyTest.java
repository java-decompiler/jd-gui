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

public class JavaTryCatchFinallyTest extends TestCase {
    protected DeserializeClassFileProcessor deserializer = new DeserializeClassFileProcessor();
    protected ClassFileToJavaSyntaxProcessor converter = new ClassFileToJavaSyntaxProcessor();
    protected JavaSyntaxToJavaFragmentProcessor fragmenter = new JavaSyntaxToJavaFragmentProcessor();
    protected LayoutFragmentProcessor layouter = new LayoutFragmentProcessor();
    //protected TestTokenizeJavaFragmentProcessor tokenizer = new TestTokenizeJavaFragmentProcessor();
    protected JavaFragmentToTokenProcessor tokenizer = new JavaFragmentToTokenProcessor();
    protected WriteTokenProcessor writer = new WriteTokenProcessor();

    @Test
    public void testJdk170TryWithResources() throws Exception {
        String internalClassName = "org/jd/core/test/TryWithResources";
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.7.0.zip");
        Loader loader = new ZipLoader(is);
        Map<String, Object> configuration = Collections.singletonMap("realignLineNumbers", Boolean.TRUE);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName, configuration);

        // Check decompiled source code
        assertTrue(source.matches(PatternMaker.make(":  12 */", "try (FileInputStream input = new FileInputStream(path))")));

        assertTrue(source.matches(PatternMaker.make(":  49 */", "try (FileInputStream input = new FileInputStream(path))")));
        assertTrue(source.matches(PatternMaker.make(":  57 */", "e.printStackTrace();")));
        assertTrue(source.matches(PatternMaker.make(":  59 */", "System.out.println(\"finally\");")));

        assertTrue(source.matches(PatternMaker.make(": 121 */", "try(FileInputStream input = new FileInputStream(pathIn);")));
        assertTrue(source.matches(PatternMaker.make(": 122 */", "BufferedInputStream bufferedInput = new BufferedInputStream(input);")));
        assertTrue(source.matches(PatternMaker.make(": 123 */", "FileOutputStream output = new FileOutputStream(pathOut);")));
        assertTrue(source.matches(PatternMaker.make(": 124 */", "BufferedOutputStream bufferedOutput = new BufferedOutputStream(output))")));
        assertTrue(source.matches(PatternMaker.make(": 132 */", "if (data == -7)")));
        assertTrue(source.matches(PatternMaker.make(": 133 */", "return 1;")));
        assertTrue(source.matches(PatternMaker.make(": 142 */", "return 2;")));
        assertTrue(source.matches(PatternMaker.make(": 144 */", "e.printStackTrace();")));
        assertTrue(source.matches(PatternMaker.make(": 150 */", "e.printStackTrace();")));
        assertTrue(source.matches(PatternMaker.make(": 152 */", "System.out.println(\"finally, before loop\");")));
        assertTrue(source.matches(PatternMaker.make(": 156 */", "System.out.println(\"finally, after loop\");")));
        assertTrue(source.matches(PatternMaker.make(": 159 */", "System.out.println(\"finally\");")));
        assertTrue(source.matches(PatternMaker.make(": 162 */", "return 3;")));

        assertTrue(source.indexOf("/* 162: 162 */") != -1);

        // Recompile decompiled source code and check errors
        assertTrue(CompilerUtil.compile("1.7", new JavaSourceFileObject(internalClassName, source)));
    }

    @Test
    public void testJdk180TryWithResources() throws Exception {
        String internalClassName = "org/jd/core/test/TryWithResources";
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.8.0.zip");
        Loader loader = new ZipLoader(is);
        Map<String, Object> configuration = Collections.singletonMap("realignLineNumbers", Boolean.TRUE);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName, configuration);

        // Check decompiled source code
        assertTrue(source.matches(PatternMaker.make(":  12 */", "try (FileInputStream input = new FileInputStream(path))")));

        assertTrue(source.matches(PatternMaker.make(":  49 */", "try (FileInputStream input = new FileInputStream(path))")));
        assertTrue(source.matches(PatternMaker.make(":  57 */", "e.printStackTrace();")));
        assertTrue(source.matches(PatternMaker.make(":  59 */", "System.out.println(\"finally\");")));

        assertTrue(source.matches(PatternMaker.make(": 121 */", "try(FileInputStream input = new FileInputStream(pathIn);")));
        assertTrue(source.matches(PatternMaker.make(": 122 */", "BufferedInputStream bufferedInput = new BufferedInputStream(input);")));
        assertTrue(source.matches(PatternMaker.make(": 123 */", "FileOutputStream output = new FileOutputStream(pathOut);")));
        assertTrue(source.matches(PatternMaker.make(": 124 */", "BufferedOutputStream bufferedOutput = new BufferedOutputStream(output))")));
        assertTrue(source.matches(PatternMaker.make(": 132 */", "if (data == -7)")));
        assertTrue(source.matches(PatternMaker.make(": 133 */", "return 1;")));
        assertTrue(source.matches(PatternMaker.make(": 142 */", "return 2;")));
        assertTrue(source.matches(PatternMaker.make(": 144 */", "e.printStackTrace();")));
        assertTrue(source.matches(PatternMaker.make(": 150 */", "e.printStackTrace();")));
        assertTrue(source.matches(PatternMaker.make(": 152 */", "System.out.println(\"finally, before loop\");")));
        assertTrue(source.matches(PatternMaker.make(": 156 */", "System.out.println(\"finally, after loop\");")));
        assertTrue(source.matches(PatternMaker.make(": 159 */", "System.out.println(\"finally\");")));
        assertTrue(source.matches(PatternMaker.make(": 162 */", "return 3;")));

        assertTrue(source.indexOf("/* 162: 162 */") != -1);

        // Recompile decompiled source code and check errors
        assertTrue(CompilerUtil.compile("1.8", new JavaSourceFileObject(internalClassName, source)));
    }

    @Test
    public void testEclipseJavaCompiler321TryCatchFinally() throws Exception {
        String internalClassName = "org/jd/core/test/TryCatchFinally";
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-eclipse-java-compiler-3.2.1.zip");
        Loader loader = new ZipLoader(is);
        Map<String, Object> configuration = Collections.singletonMap("realignLineNumbers", Boolean.TRUE);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName, configuration);

        // Check decompiled source code
        assertTrue(source.indexOf("catch (RuntimeException runtimeexception)") != -1);
        assertTrue(source.matches(PatternMaker.make("/*  45:  45 */", "inCatch1();")));
        assertTrue(source.matches(PatternMaker.make("/*  60:   0 */", "return;")));

        assertTrue(source.matches(PatternMaker.make(": 166 */", "return System.currentTimeMillis();")));

        // TODO assertTrue(source.matches(PatternMaker.make("/* 217:", "inCatch1();")));

        assertTrue(source.indexOf("/* 888: 888 */") != -1);

        assertTrue(source.indexOf("long l = System.currentTimeMillis(); return l;") == -1);
        assertTrue(source.indexOf("catch (RuntimeException null)") == -1);
        assertTrue(source.indexOf("Object object;") == -1);
        assertTrue(source.indexOf("RuntimeException runtimeexception4;") == -1);
        assertTrue(source.indexOf("Exception exception8;") == -1);

        // Recompile decompiled source code and check errors
        assertTrue(CompilerUtil.compile("1.5", new JavaSourceFileObject(internalClassName, source)));

        System.out.println(JavaTryCatchFinallyTest.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        System.out.println(System.getProperty("java.class.path"));
    }

    @Test
    public void testEclipseJavaCompiler370TryCatchFinally() throws Exception {
        String internalClassName = "org/jd/core/test/TryCatchFinally";
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-eclipse-java-compiler-3.7.0.zip");
        Loader loader = new ZipLoader(is);
        Map<String, Object> configuration = Collections.singletonMap("realignLineNumbers", Boolean.TRUE);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName, configuration);

        // Check decompiled source code
        assertTrue(source.indexOf("catch (RuntimeException runtimeException)") != -1);
        assertTrue(source.matches(PatternMaker.make("/*  48:  48 */", "inCatch2();")));
        assertTrue(source.matches(PatternMaker.make("/*  60:   0 */", "return;")));

        assertTrue(source.matches(PatternMaker.make(": 166 */", "return System.currentTimeMillis();")));

        assertTrue(source.matches(PatternMaker.make(": 393 */", "inCatch1();")));
        assertTrue(source.matches(PatternMaker.make(": 395 */", "inCatch2();")));
        assertTrue(source.matches(PatternMaker.make(": 397 */", "inCatch3();")));
        assertTrue(source.matches(PatternMaker.make(": 399 */", "inFinally();")));
        assertTrue(source.indexOf("/* 400:   0]     inFinally();") == -1);

        assertTrue(source.matches(PatternMaker.make(": 424 */", "inTry();")));
        assertTrue(source.matches(PatternMaker.make(": 427 */", "inFinally();")));
        assertTrue(source.matches(PatternMaker.make(": 431 */", "inTryA();")));
        assertTrue(source.matches(PatternMaker.make(": 434 */", "inFinallyA();")));
        assertTrue(source.matches(PatternMaker.make(": 439 */", "inTryC();")));
        assertTrue(source.matches(PatternMaker.make(": 442 */", "inFinallyC();")));
        assertTrue(source.matches(PatternMaker.make(": 445 */", "inFinally();")));

        assertTrue(source.indexOf("/* 888: 888 */") != -1);

        assertTrue(source.indexOf("long l = System.currentTimeMillis(); return l;") == -1);
        assertTrue(source.indexOf("catch (RuntimeException null)") == -1);
        assertTrue(source.indexOf("Object object;") == -1);
        assertTrue(source.indexOf("RuntimeException runtimeexception4;") == -1);
        assertTrue(source.indexOf("Exception exception8;") == -1);

        // Recompile decompiled source code and check errors
        assertTrue(CompilerUtil.compile("1.5", new JavaSourceFileObject(internalClassName, source)));
    }

    @Test
    public void testEclipseJavaCompiler3130TryCatchFinally() throws Exception {
        String internalClassName = "org/jd/core/test/TryCatchFinally";
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-eclipse-java-compiler-3.13.0.zip");
        Loader loader = new ZipLoader(is);
        Map<String, Object> configuration = Collections.singletonMap("realignLineNumbers", Boolean.TRUE);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName, configuration);

        // Check decompiled source code
        assertTrue(source.indexOf("catch (RuntimeException runtimeException)") != -1);
        assertTrue(source.matches(PatternMaker.make("/*  48:  48 */", "inCatch2();")));
        assertTrue(source.matches(PatternMaker.make("/*  60:   0 */", "return;")));

        assertTrue(source.matches(PatternMaker.make(": 166 */", "return System.currentTimeMillis();")));

        assertTrue(source.matches(PatternMaker.make(": 393 */", "inCatch1();")));
        assertTrue(source.matches(PatternMaker.make(": 395 */", "inCatch2();")));
        assertTrue(source.matches(PatternMaker.make(": 397 */", "inCatch3();")));
        assertTrue(source.matches(PatternMaker.make(": 399 */", "inFinally();")));

        assertTrue(source.matches(PatternMaker.make(": 424 */", "inTry();")));
        assertTrue(source.matches(PatternMaker.make(": 427 */", "inFinally();")));
        assertTrue(source.matches(PatternMaker.make(": 431 */", "inTryA();")));
        assertTrue(source.matches(PatternMaker.make(": 434 */", "inFinallyA();")));
        assertTrue(source.matches(PatternMaker.make(": 439 */", "inTryC();")));
        assertTrue(source.matches(PatternMaker.make(": 442 */", "inFinallyC();")));
        assertTrue(source.matches(PatternMaker.make(": 445 */", "inFinally();")));

        assertTrue(source.indexOf("/* 888: 888 */") != -1);

        assertTrue(source.indexOf("long l = System.currentTimeMillis(); return l;") == -1);
        assertTrue(source.indexOf("catch (RuntimeException null)") == -1);
        assertTrue(source.indexOf("Object object;") == -1);
        assertTrue(source.indexOf("RuntimeException runtimeexception4;") == -1);
        assertTrue(source.indexOf("Exception exception8;") == -1);

        // Recompile decompiled source code and check errors
        assertTrue(CompilerUtil.compile("1.5", new JavaSourceFileObject(internalClassName, source)));
    }

    @Test
    public void testJdk118TryCatchFinally() throws Exception {
        String internalClassName = "org/jd/core/test/TryCatchFinally";
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.1.8.zip");
        Loader loader = new ZipLoader(is);
        Map<String, Object> configuration = Collections.singletonMap("realignLineNumbers", Boolean.TRUE);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName, configuration);

        // Check decompiled source code
        assertTrue(source.indexOf("catch (RuntimeException runtimeexception)") != -1);
        assertTrue(source.matches(PatternMaker.make("/*  48:  48 */", "inCatch2();")));
        assertTrue(source.matches(PatternMaker.make("/*  60:   0 */", "return;")));

        assertTrue(source.matches(PatternMaker.make(": 166 */", "return System.currentTimeMillis();")));

        assertTrue(source.matches(PatternMaker.make(": 393 */", "inCatch1();")));
        assertTrue(source.matches(PatternMaker.make(": 395 */", "inCatch2();")));
        assertTrue(source.matches(PatternMaker.make(": 397 */", "inCatch3();")));
        assertTrue(source.matches(PatternMaker.make(": 399 */", "inFinally();")));

        assertTrue(source.matches(PatternMaker.make(": 424 */", "inTry();")));
        assertTrue(source.matches(PatternMaker.make(": 427 */", "inFinally();")));
        assertTrue(source.matches(PatternMaker.make(": 431 */", "inTryA();")));
        assertTrue(source.matches(PatternMaker.make(": 434 */", "inFinallyA();")));
        assertTrue(source.matches(PatternMaker.make(": 439 */", "inTryC();")));
        assertTrue(source.matches(PatternMaker.make(": 442 */", "inFinallyC();")));
        assertTrue(source.matches(PatternMaker.make(": 445 */", "inFinally();")));

        assertTrue(source.indexOf("/* 902: 902 */") != -1);

        assertTrue(source.indexOf("long l = System.currentTimeMillis(); return l;") == -1);
        assertTrue(source.indexOf("catch (RuntimeException null)") == -1);
        assertTrue(source.indexOf("Object object;") == -1);
        assertTrue(source.indexOf("RuntimeException runtimeexception4;") == -1);
        assertTrue(source.indexOf("Exception exception8;") == -1);

        // Recompile decompiled source code and check errors
        assertTrue(CompilerUtil.compile("1.3", new JavaSourceFileObject(internalClassName, source)));
    }

    @Test
    public void testJdk131TryCatchFinally() throws Exception {
        String internalClassName = "org/jd/core/test/TryCatchFinally";
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.3.1.zip");
        Loader loader = new ZipLoader(is);
        Map<String, Object> configuration = Collections.singletonMap("realignLineNumbers", Boolean.TRUE);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName, configuration);

        // Check decompiled source code
        assertTrue(source.indexOf("catch (RuntimeException runtimeexception)") != -1);
        assertTrue(source.matches(PatternMaker.make("/*  48:  48 */", "inCatch2();")));
        assertTrue(source.matches(PatternMaker.make("/*  60:   0 */", "return;")));

        assertTrue(source.matches(PatternMaker.make(": 166 */", "return System.currentTimeMillis();")));

        assertTrue(source.matches(PatternMaker.make(": 393 */", "inCatch1();")));
        assertTrue(source.matches(PatternMaker.make(": 395 */", "inCatch2();")));
        assertTrue(source.matches(PatternMaker.make(": 397 */", "inCatch3();")));
        assertTrue(source.matches(PatternMaker.make(": 399 */", "inFinally();")));

        assertTrue(source.matches(PatternMaker.make(": 424 */", "inTry();")));
        assertTrue(source.matches(PatternMaker.make(": 427 */", "inFinally();")));
        assertTrue(source.matches(PatternMaker.make(": 445 */", "inFinally();")));

        assertTrue(source.indexOf("/* 902: 902 */") != -1);

        assertTrue(source.indexOf("long l = System.currentTimeMillis(); return l;") == -1);
        assertTrue(source.indexOf("catch (RuntimeException null)") == -1);
        assertTrue(source.indexOf("Object object;") == -1);
        assertTrue(source.indexOf("RuntimeException runtimeexception4;") == -1);
        assertTrue(source.indexOf("Exception exception8;") == -1);

        // Recompile decompiled source code and check errors
        assertTrue(CompilerUtil.compile("1.3", new JavaSourceFileObject(internalClassName, source)));
    }

    @Test
    public void testJdk170TryCatchFinally() throws Exception {
        String internalClassName = "org/jd/core/test/TryCatchFinally";
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.7.0.zip");
        Loader loader = new ZipLoader(is);
        Map<String, Object> configuration = Collections.singletonMap("realignLineNumbers", Boolean.TRUE);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName, configuration);

        // Check decompiled source code
        assertTrue(source.indexOf("catch (RuntimeException runtimeexception)") != -1);
        assertTrue(source.matches(PatternMaker.make("/*  48:  48 */", "inCatch2();")));
        assertTrue(source.matches(PatternMaker.make("/*  60:   0 */", "return;")));

        assertTrue(source.matches(PatternMaker.make(": 166 */", "return System.currentTimeMillis();")));

        assertTrue(source.matches(PatternMaker.make(": 192 */", "catch (RuntimeException e) {}")));
        assertTrue(source.matches(PatternMaker.make("/* 204:   0 */", "finally {}")));

        assertTrue(source.matches(PatternMaker.make(": 393 */", "inCatch1();")));
        assertTrue(source.matches(PatternMaker.make(": 395 */", "inCatch2();")));
        assertTrue(source.matches(PatternMaker.make(": 397 */", "inCatch3();")));
        assertTrue(source.matches(PatternMaker.make(": 399 */", "inFinally();")));

        assertTrue(source.matches(PatternMaker.make(": 424 */", "inTry();")));
        assertTrue(source.matches(PatternMaker.make(": 427 */", "inFinally();")));
        assertTrue(source.matches(PatternMaker.make(": 431 */", "inTryA();")));
        assertTrue(source.matches(PatternMaker.make(": 434 */", "inFinallyA();")));
        assertTrue(source.matches(PatternMaker.make(": 439 */", "inTryC();")));
        assertTrue(source.matches(PatternMaker.make(": 442 */", "inFinallyC();")));
        assertTrue(source.matches(PatternMaker.make(": 445 */", "inFinally();")));

        assertTrue(source.indexOf("/* 902: 902 */") != -1);

        assertTrue(source.indexOf("long l = System.currentTimeMillis(); return l;") == -1);
        assertTrue(source.indexOf("catch (RuntimeException null)") == -1);
        assertTrue(source.indexOf("Object object;") == -1);
        assertTrue(source.indexOf("RuntimeException runtimeexception4;") == -1);
        assertTrue(source.indexOf("Exception exception8;") == -1);

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
