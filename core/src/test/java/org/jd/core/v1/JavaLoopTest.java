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

public class JavaLoopTest extends TestCase {
    protected DeserializeClassFileProcessor deserializer = new DeserializeClassFileProcessor();
    protected ClassFileToJavaSyntaxProcessor converter = new ClassFileToJavaSyntaxProcessor();
    protected JavaSyntaxToJavaFragmentProcessor fragmenter = new JavaSyntaxToJavaFragmentProcessor();
    protected LayoutFragmentProcessor layouter = new LayoutFragmentProcessor();
    //protected TestTokenizeJavaFragmentProcessor tokenizer = new TestTokenizeJavaFragmentProcessor();
    protected JavaFragmentToTokenProcessor tokenizer = new JavaFragmentToTokenProcessor();
    protected WriteTokenProcessor writer = new WriteTokenProcessor();

    @Test
    public void testJdk170While() throws Exception {
        String internalClassName = "org/jd/core/test/While";
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.7.0.zip");
        Loader loader = new ZipLoader(is);
        Map<String, Object> configuration = Collections.singletonMap("realignLineNumbers", Boolean.TRUE);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName, configuration);

        // Check decompiled source code
        assertTrue(source.matches(PatternMaker.make(":  15 */", "while (i-- > 0)")));
        assertTrue(source.matches(PatternMaker.make(":  23 */", "while (i < 10)")));
        assertTrue(source.matches(PatternMaker.make(":  42 */", "while (i0 > 20)")));
        assertTrue(source.matches(PatternMaker.make("/* 113:   0 */", "continue;")));
        assertTrue(source.matches(PatternMaker.make("/* 128:   0 */", "break;")));
        assertTrue(source.matches(PatternMaker.make("/* 158:   0 */", "while (true)")));
        assertTrue(source.matches(PatternMaker.make(": 232 */", "while (++i < 10)")));
        assertTrue(source.indexOf("while (i == 4 && i == 5 && i == 6)") != -1);
        assertTrue(source.indexOf("while ((i == 1 || (i == 5 && i == 6 && i == 7) || i == 8 || (i == 9 && i == 10 && i == 11)) && (i == 4 || i % 200 > 50) && (i > 3 || i > 4))") != -1);
        assertTrue(source.indexOf("while ((i == 1 && (i == 5 || i == 6 || i == 7) && i == 8 && (i == 9 || i == 10 || i == 11)) || (i == 4 && i % 200 > 50) || (i > 3 && i > 4))") != -1);
        assertFalse(source.matches(PatternMaker.make("[ 348:   0 */", "default:")));
        assertFalse(source.matches(PatternMaker.make("[ 350: 348 */", "continue;")));
        assertTrue(source.matches(PatternMaker.make("/* 404: 404 */", "System.out.println(\"a\");")));
        assertTrue(source.matches(PatternMaker.make("/* 431: 431 */", "System.out.println(\"a\");")));

        // Recompile decompiled source code and check errors
        assertTrue(CompilerUtil.compile("1.7", new JavaSourceFileObject(internalClassName, source)));
    }

    @Test
    public void testJdk901While() throws Exception {
        String internalClassName = "org/jd/core/test/While";
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-9.0.1.zip");
        Loader loader = new ZipLoader(is);
        Map<String, Object> configuration = Collections.singletonMap("realignLineNumbers", Boolean.TRUE);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName, configuration);

        // Check decompiled source code
        assertTrue(source.matches(PatternMaker.make(":  15 */", "while (i-- > 0)")));
        assertTrue(source.matches(PatternMaker.make(":  23 */", "while (i < 10)")));
        assertTrue(source.matches(PatternMaker.make(":  42 */", "while (i0 > 20)")));
        assertTrue(source.matches(PatternMaker.make("/* 113:   0 */", "continue;")));
        assertTrue(source.matches(PatternMaker.make("/* 128:   0 */", "break;")));
        assertTrue(source.matches(PatternMaker.make("/* 158:   0 */", "while (true)")));
        assertTrue(source.matches(PatternMaker.make(": 232 */", "while (++i < 10)")));
        assertTrue(source.indexOf("while (i == 4 && i == 5 && i == 6)") != -1);
        assertTrue(source.indexOf("while ((i == 1 || (i == 5 && i == 6 && i == 7) || i == 8 || (i == 9 && i == 10 && i == 11)) && (i == 4 || i % 200 > 50) && (i > 3 || i > 4))") != -1);
        assertTrue(source.indexOf("while ((i == 1 && (i == 5 || i == 6 || i == 7) && i == 8 && (i == 9 || i == 10 || i == 11)) || (i == 4 && i % 200 > 50) || (i > 3 && i > 4))") != -1);
        assertFalse(source.matches(PatternMaker.make("[ 348:   0 */", "default:")));
        assertFalse(source.matches(PatternMaker.make("[ 350: 348 */", "continue;")));
        assertTrue(source.matches(PatternMaker.make("/* 404: 404 */", "System.out.println(\"a\");")));
        assertTrue(source.matches(PatternMaker.make("/* 431: 431 */", "System.out.println(\"a\");")));

        // Recompile decompiled source code and check errors
        assertTrue(CompilerUtil.compile("1.8", new JavaSourceFileObject(internalClassName, source)));
    }

    @Test
    public void testJdk1002While() throws Exception {
        String internalClassName = "org/jd/core/test/While";
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-10.0.2.zip");
        Loader loader = new ZipLoader(is);
        Map<String, Object> configuration = Collections.singletonMap("realignLineNumbers", Boolean.TRUE);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName, configuration);

        // Check decompiled source code
        assertTrue(source.matches(PatternMaker.make(":  15 */", "while (i-- > 0)")));
        assertTrue(source.matches(PatternMaker.make(":  23 */", "while (i < 10)")));
        assertTrue(source.matches(PatternMaker.make(":  42 */", "while (i0 > 20)")));
        assertTrue(source.matches(PatternMaker.make("/* 113:   0 */", "continue;")));
        assertTrue(source.matches(PatternMaker.make("/* 128:   0 */", "break;")));
        assertTrue(source.matches(PatternMaker.make("/* 158:   0 */", "while (true)")));
        assertTrue(source.matches(PatternMaker.make(": 232 */", "while (++i < 10)")));
        assertTrue(source.indexOf("while (i == 4 && i == 5 && i == 6)") != -1);
        assertTrue(source.indexOf("while ((i == 1 || (i == 5 && i == 6 && i == 7) || i == 8 || (i == 9 && i == 10 && i == 11)) && (i == 4 || i % 200 > 50) && (i > 3 || i > 4))") != -1);
        assertTrue(source.indexOf("while ((i == 1 && (i == 5 || i == 6 || i == 7) && i == 8 && (i == 9 || i == 10 || i == 11)) || (i == 4 && i % 200 > 50) || (i > 3 && i > 4))") != -1);
        assertFalse(source.matches(PatternMaker.make("[ 348:   0 */", "default:")));
        assertFalse(source.matches(PatternMaker.make("[ 350: 348 */", "continue;")));
        assertTrue(source.matches(PatternMaker.make("/* 404: 404 */", "System.out.println(\"a\");")));
        assertTrue(source.matches(PatternMaker.make("/* 431: 431 */", "System.out.println(\"a\");")));

        // Recompile decompiled source code and check errors
        assertTrue(CompilerUtil.compile("1.8", new JavaSourceFileObject(internalClassName, source)));
    }

    @Test
    public void testJdk170DoWhile() throws Exception {
        String internalClassName = "org/jd/core/test/DoWhile";
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.7.0.zip");
        Loader loader = new ZipLoader(is);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName);

        // Check decompiled source code
        assertTrue(source.matches(PatternMaker.make(":  24 */", "} while (i < 10);")));
        assertTrue(source.matches(PatternMaker.make(":  32 */", "} while (this == null);")));
        assertTrue(source.matches(PatternMaker.make(":  44 */", "++i;")));
        assertTrue(source.matches(PatternMaker.make(":  46 */", "while (i < 10);")));
        assertTrue(source.matches(PatternMaker.make(":  72 */", "while (i0 < 10)")));
        assertTrue(source.matches(PatternMaker.make(":  77 */", "i1--;")));
        assertTrue(source.matches(PatternMaker.make(":  79 */", "while (i1 > 0);")));
        assertTrue(source.matches(PatternMaker.make(":  98 */", "while (--i > 0.0F);")));
        assertTrue(source.matches(PatternMaker.make(": 108 */", "while (i-- > 0.0F);")));
        assertTrue(source.indexOf("while ((i == 1 || (i == 5 && i == 6 && i == 7) || i == 8 || (i == 9 && i == 10 && i == 11)) && (i == 4 || i % 200 > 50) && (i > 3 || i > 4));") != -1);
        assertTrue(source.indexOf("while ((i == 1 && (i == 5 || i == 6 || i == 7) && i == 8 && (i == 9 || i == 10 || i == 11)) || (i == 4 && i % 200 > 50) || (i > 3 && i > 4));") != -1);

        // Recompile decompiled source code and check errors
        assertTrue(CompilerUtil.compile("1.7", new JavaSourceFileObject(internalClassName, source)));
    }

    @Test
    public void testJdk901DoWhile() throws Exception {
        String internalClassName = "org/jd/core/test/DoWhile";
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-9.0.1.zip");
        Loader loader = new ZipLoader(is);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName);

        // Check decompiled source code
        assertTrue(source.matches(PatternMaker.make(":  24 */", "} while (i < 10);")));
        assertTrue(source.matches(PatternMaker.make(":  32 */", "} while (this == null);")));
        assertTrue(source.matches(PatternMaker.make(":  44 */", "++i;")));
        assertTrue(source.matches(PatternMaker.make(":  46 */", "while (i < 10);")));
        assertTrue(source.matches(PatternMaker.make(":  72 */", "while (i0 < 10)")));
        assertTrue(source.matches(PatternMaker.make(":  77 */", "i1--;")));
        assertTrue(source.matches(PatternMaker.make(":  79 */", "while (i1 > 0);")));
        assertTrue(source.matches(PatternMaker.make(":  98 */", "while (--i > 0.0F);")));
        assertTrue(source.matches(PatternMaker.make(": 108 */", "while (i-- > 0.0F);")));
        assertTrue(source.indexOf("while ((i == 1 || (i == 5 && i == 6 && i == 7) || i == 8 || (i == 9 && i == 10 && i == 11)) && (i == 4 || i % 200 > 50) && (i > 3 || i > 4));") != -1);
        assertTrue(source.indexOf("while ((i == 1 && (i == 5 || i == 6 || i == 7) && i == 8 && (i == 9 || i == 10 || i == 11)) || (i == 4 && i % 200 > 50) || (i > 3 && i > 4));") != -1);

        // Recompile decompiled source code and check errors
        assertTrue(CompilerUtil.compile("1.8", new JavaSourceFileObject(internalClassName, source)));
    }

    @Test
    public void testJdk1002DoWhile() throws Exception {
        String internalClassName = "org/jd/core/test/DoWhile";
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-10.0.2.zip");
        Loader loader = new ZipLoader(is);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName);

        // Check decompiled source code
        assertTrue(source.matches(PatternMaker.make(":  24 */", "} while (i < 10);")));
        assertTrue(source.matches(PatternMaker.make(":  32 */", "} while (this == null);")));
        assertTrue(source.matches(PatternMaker.make(":  44 */", "++i;")));
        assertTrue(source.matches(PatternMaker.make(":  46 */", "while (i < 10);")));
        assertTrue(source.matches(PatternMaker.make(":  72 */", "while (i0 < 10)")));
        assertTrue(source.matches(PatternMaker.make(":  77 */", "i1--;")));
        assertTrue(source.matches(PatternMaker.make(":  79 */", "while (i1 > 0);")));
        assertTrue(source.matches(PatternMaker.make(":  98 */", "while (--i > 0.0F);")));
        assertTrue(source.matches(PatternMaker.make(": 108 */", "while (i-- > 0.0F);")));
        assertTrue(source.indexOf("while ((i == 1 || (i == 5 && i == 6 && i == 7) || i == 8 || (i == 9 && i == 10 && i == 11)) && (i == 4 || i % 200 > 50) && (i > 3 || i > 4));") != -1);
        assertTrue(source.indexOf("while ((i == 1 && (i == 5 || i == 6 || i == 7) && i == 8 && (i == 9 || i == 10 || i == 11)) || (i == 4 && i % 200 > 50) || (i > 3 && i > 4));") != -1);

        // Recompile decompiled source code and check errors
        assertTrue(CompilerUtil.compile("1.8", new JavaSourceFileObject(internalClassName, source)));
    }

    @Test
    public void testJdk170BreakContinue() throws Exception {
        String internalClassName = "org/jd/core/test/BreakContinue";
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.7.0.zip");
        Loader loader = new ZipLoader(is);
        Map<String, Object> configuration = Collections.singletonMap("realignLineNumbers", Boolean.TRUE);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName, configuration);

        // Check decompiled source code
        assertTrue(source.matches(PatternMaker.make("/*  15:  15 */", "if (i == 1)")));
        assertTrue(source.matches(PatternMaker.make("/*  16:   0 */", "continue;")));
        assertTrue(source.matches(PatternMaker.make("/*  18:  18 */", "if (i == 2)")));
        assertTrue(source.matches(PatternMaker.make("/*  19:   0 */", "continue;")));

        assertTrue(source.matches(PatternMaker.make("/*  31:  31 */", "label18: while (i > 1)")));
        assertTrue(source.matches(PatternMaker.make("/*  37:   0 */", "continue label18;")));
        assertTrue(source.matches(PatternMaker.make("/*  40:   0 */", "break label18;")));

        assertTrue(source.matches(PatternMaker.make("/*  54:  54 */", "label17: while (i > 1)")));
        assertTrue(source.matches(PatternMaker.make("/*  60:   0 */", "break;")));
        assertTrue(source.matches(PatternMaker.make("/*  63:   0 */", "continue label17;")));

        assertTrue(source.matches(PatternMaker.make("/*  78:   0 */", "label13:")));
        assertTrue(source.matches(PatternMaker.make("/*  83:   0 */", "break;")));
        assertTrue(source.matches(PatternMaker.make("/*  86:   0 */", "break label13;")));

        assertTrue(source.matches(PatternMaker.make("/* 101:   0 */", "label15:", "do {")));
        assertTrue(source.matches(PatternMaker.make("/* 106:   0 */", "break;")));
        assertTrue(source.matches(PatternMaker.make("/* 109:   0 */", "break label15;")));

        assertTrue(source.matches(PatternMaker.make("/* 123:   0 */", "label24:", "do {")));
        assertTrue(source.matches(PatternMaker.make("/* 133:   0 */", "continue label24;")));
        assertTrue(source.matches(PatternMaker.make("/* 135:   0 */", "break label24;")));
        assertTrue(source.matches(PatternMaker.make("/* 138:   0 */", "break label23;")));

        assertTrue(source.matches(PatternMaker.make("/* 155:   0 */", "label16:", "do {")));
        assertTrue(source.matches(PatternMaker.make("/* 162:   0 */", "break label16;")));

        // Recompile decompiled source code and check errors
        assertTrue(CompilerUtil.compile("1.7", new JavaSourceFileObject(internalClassName, source)));
    }

    @Test
    public void testJdk170For() throws Exception {
        String internalClassName = "org/jd/core/test/For";
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.7.0.zip");
        Loader loader = new ZipLoader(is);
        Map<String, Object> configuration = Collections.singletonMap("realignLineNumbers", Boolean.TRUE);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName, configuration);

        // Check decompiled source code
        assertTrue(source.matches(PatternMaker.make(":  20 */", "for (int i = 0; i < 10; i++)")));
        assertTrue(source.matches(PatternMaker.make(":  42 */", "int i = 0;")));
        assertTrue(source.matches(PatternMaker.make(":  44 */", "for (; i < 10; i++)")));
        assertTrue(source.matches(PatternMaker.make(":  54 */", "for (; i < 10; i++)")));
        assertTrue(source.matches(PatternMaker.make(":  64 */", "for (int i = 0;; i++)")));
        assertTrue(source.matches(PatternMaker.make(":  72 */", "for (;; i++)")));
        assertTrue(source.matches(PatternMaker.make(":  80 */", "for (int i = 0; i < 10;)")));
        assertTrue(source.matches(PatternMaker.make(":  88 */", "while (i < 10)")));
        assertTrue(source.matches(PatternMaker.make(":  96 */", "int i = 0;")));
        assertTrue(source.matches(PatternMaker.make("/* 104:   0 */", "while (true)")));
        assertTrue(source.matches(PatternMaker.make(": 112 */", "for (int i = 0, j = i, size = 10; i < size; j += ++i)")));
        assertTrue(source.matches(PatternMaker.make(": 122 */", "int i = 0, j = i, size = 10;")));
        assertTrue(source.matches(PatternMaker.make(": 123 */", "for (; i < size;")));
        assertTrue(source.matches(PatternMaker.make(": 124 */", "j += ++i)")));
        assertTrue(source.matches(PatternMaker.make(": 134 */", "int i = 0;")));
        assertTrue(source.matches(PatternMaker.make(": 135 */", "int j = i;")));
        assertTrue(source.matches(PatternMaker.make(": 136 */", "int size = 10;")));
        assertTrue(source.matches(PatternMaker.make(": 137 */", "for (; i < size;")));
        assertTrue(source.matches(PatternMaker.make(": 138 */", "i++,")));
        assertTrue(source.matches(PatternMaker.make(": 139 */", "j += i)")));
        assertTrue(source.matches(PatternMaker.make(": 149 */", "int i = 0;")));
        assertTrue(source.matches(PatternMaker.make(": 151 */", "int j = i;")));
        assertTrue(source.matches(PatternMaker.make(": 153 */", "int size = 10;")));
        assertTrue(source.matches(PatternMaker.make(": 155 */", "for (; i < size;")));
        assertTrue(source.matches(PatternMaker.make(": 157 */", "i++,")));
        assertTrue(source.matches(PatternMaker.make(": 159 */", "j += i)")));
        assertTrue(source.matches(PatternMaker.make(": 169 */", "for (int i = 0; i < 10; i++);")));
        assertTrue(source.matches(PatternMaker.make(": 177 */", "for (; i < 10; i++);")));
        assertTrue(source.matches(PatternMaker.make(": 185 */", "for (int i = 0;; i++);")));
        assertTrue(source.matches(PatternMaker.make("/* 190:   0 */", "while (true)")));
        assertTrue(source.matches(PatternMaker.make(": 191 */", "i++;")));
        assertTrue(source.matches(PatternMaker.make(": 197 */", "for (int i = 0; i < 10;);")));
        assertTrue(source.matches(PatternMaker.make(": 203 */", "for (int[] i = { 0 }; i.length < 10;);")));
        assertTrue(source.matches(PatternMaker.make(": 209 */", "for (int i = 0, j = i, k = i; i < 10;);")));
        assertTrue(source.matches(PatternMaker.make(": 215 */", "for (int[] i = { 0 }, j = i, k = j; i.length < 10;);")));
        assertTrue(source.matches(PatternMaker.make(": 221 */", "for (int i = 0, j[] = { 1 }; i < 10;);")));
        assertTrue(source.matches(PatternMaker.make(": 227 */", "while (i < 10);")));
        assertTrue(source.matches(PatternMaker.make(": 233 */", "int i = 0;")));
        assertTrue(source.matches(PatternMaker.make("/* 234:   0 */", "while (true);")));
        assertTrue(source.matches(PatternMaker.make(": 245 */", "for (int i = 0, j = i, size = 10; i < size; j += ++i);")));
        assertTrue(source.matches(PatternMaker.make("/* 253:   0 */", "while (true) {")));
        assertTrue(source.matches(PatternMaker.make(": 264 */", "for (String s : list)")));
        assertTrue(source.matches(PatternMaker.make(": 310 */", "for (int j : new int[] { 4 })")));
        assertTrue(source.matches(PatternMaker.make(": 389 */", "for (String s : array)")));
        assertTrue(source.matches(PatternMaker.make(": 403 */", "for (String s : list)")));
        assertTrue(source.matches(PatternMaker.make(": 411 */", "Iterator<Class<?>> iterator = Arrays.<Class<?>>asList(getClass().getInterfaces()).iterator()")));

        assertTrue(source.indexOf("/* 524: 524 */") != -1);

        // Recompile decompiled source code and check errors
        assertTrue(CompilerUtil.compile("1.7", new JavaSourceFileObject(internalClassName, source)));
    }

    @Test
    public void testJdk170NoDebugInfoFor() throws Exception {
        String internalClassName = "org/jd/core/test/For";
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.7.0-no-debug-info.zip");
        Loader loader = new ZipLoader(is);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName);

        // Check decompiled source code
        assertTrue(source.matches(PatternMaker.make("for (int i = 0; i < 10; i++)")));
        assertTrue(source.matches(PatternMaker.make("for (int i = 0;; i++)")));
        assertTrue(source.matches(PatternMaker.make("for (String str : paramList)")));
        assertTrue(source.matches(PatternMaker.make("for (paramInt = 0; paramInt < 10; paramInt++)")));
        assertTrue(source.matches(PatternMaker.make("for (int j : new int[] { 4 })")));
        assertTrue(source.matches(PatternMaker.make("for (String str : paramArrayOfString)")));
        assertTrue(source.matches(PatternMaker.make("for (String str : paramList)")));

        // Recompile decompiled source code and check errors
        //assertTrue(CompilerUtil.compile("1.7", new JavaSourceFileObject(internalClassName, source)));
    }

    @Test
    public void testJdk150For() throws Exception {
        String internalClassName = "org/jd/core/test/For";
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.5.0.zip");
        Loader loader = new ZipLoader(is);
        Map<String, Object> configuration = Collections.singletonMap("realignLineNumbers", Boolean.TRUE);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName, configuration);

        // Check decompiled source code
        assertTrue(source.matches(PatternMaker.make(":  20 */", "for (int i = 0; i < 10; i++)")));
        assertTrue(source.matches(PatternMaker.make(":  88 */", "while (paramInt < 10)")));
        assertTrue(source.matches(PatternMaker.make(": 273 */", "for (paramInt = 0; paramInt < 10; paramInt++)")));
        assertTrue(source.matches(PatternMaker.make(": 310 */", "for (int j : new int[] { 4 })")));
        assertTrue(source.matches(PatternMaker.make("/* 347:   0 */", "do {")));
        assertTrue(source.matches(PatternMaker.make(": 349 */", "while (i < 10);")));
        assertTrue(source.matches(PatternMaker.make(": 385 */", "for (String str : paramArrayOfString)")));
        assertTrue(source.matches(PatternMaker.make(": 399 */", "for (String str : paramList)")));
        assertTrue(source.matches(PatternMaker.make(": 411 */", "Iterator<Class<?>> iterator = Arrays.<Class<?>>asList(getClass().getInterfaces()).iterator()")));
        assertTrue(source.matches(PatternMaker.make(": 427 */", "for (int i = 0; i < 3; i++)")));

        // Recompile decompiled source code and check errors
        assertTrue(CompilerUtil.compile("1.5", new JavaSourceFileObject(internalClassName, source)));
    }

    @Test
    public void testJdk160For() throws Exception {
        String internalClassName = "org/jd/core/test/For";
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.6.0.zip");
        Loader loader = new ZipLoader(is);
        Map<String, Object> configuration = Collections.singletonMap("realignLineNumbers", Boolean.TRUE);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName, configuration);

        // Check decompiled source code
        assertTrue(source.matches(PatternMaker.make(":  20 */", "for (int i = 0; i < 10; i++)")));
        assertTrue(source.matches(PatternMaker.make(":  88 */", "while (i < 10)")));
        assertTrue(source.matches(PatternMaker.make(": 273 */", "for (i = 0; i < 10; i++)")));
        assertTrue(source.matches(PatternMaker.make(": 310 */", "for (int j : new int[] { 4 })")));
        assertTrue(source.matches(PatternMaker.make("/* 347:   0 */", "do {")));
        assertTrue(source.matches(PatternMaker.make(": 349 */", "while (i < 10);")));
        assertTrue(source.matches(PatternMaker.make(": 385 */", "for (String s : array)")));
        assertTrue(source.matches(PatternMaker.make(": 399 */", "for (String s : list)")));
        assertTrue(source.matches(PatternMaker.make(": 411 */", "Iterator<Class<?>> iterator = Arrays.<Class<?>>asList(getClass().getInterfaces()).iterator()")));
        assertTrue(source.matches(PatternMaker.make(": 427 */", "for (int i = 0; i < 3; i++)")));

        // Recompile decompiled source code and check errors
        assertTrue(CompilerUtil.compile("1.6", new JavaSourceFileObject(internalClassName, source)));
    }

    @Test
    public void testIbmJ9For() throws Exception {
        String internalClassName = "org/jd/core/test/For";
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-ibm-j9_vm.zip");
        Loader loader = new ZipLoader(is);
        Map<String, Object> configuration = Collections.singletonMap("realignLineNumbers", Boolean.TRUE);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName, configuration);

        // Check decompiled source code
        assertTrue(source.matches(PatternMaker.make(":  88 */", "while (i < 10)")));
        assertTrue(source.matches(PatternMaker.make(": 273 */", "for (i = 0; i < 10; i++)")));
        assertTrue(source.matches(PatternMaker.make(": 310 */", "for (int j : new int[] { 4 })")));
        assertTrue(source.matches(PatternMaker.make("/* 347:   0 */", "do")));
        assertTrue(source.matches(PatternMaker.make(": 349 */", "while (i < 10);")));
        assertTrue(source.matches(PatternMaker.make(": 385 */", "for (String s : array)")));
        assertTrue(source.matches(PatternMaker.make(": 399 */", "for (String s : list)")));
        assertTrue(source.matches(PatternMaker.make(": 411 */", "Iterator<Class<?>> iterator = Arrays.<Class<?>>asList(getClass().getInterfaces()).iterator()")));
        assertTrue(source.matches(PatternMaker.make(": 427 */", "for (int i = 0; i < 3; i++)")));

        // Recompile decompiled source code and check errors
        assertTrue(CompilerUtil.compile("1.5", new JavaSourceFileObject(internalClassName, source)));
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
