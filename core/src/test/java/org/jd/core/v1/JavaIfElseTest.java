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

public class JavaIfElseTest extends TestCase {
    protected DeserializeClassFileProcessor deserializer = new DeserializeClassFileProcessor();
    protected ClassFileToJavaSyntaxProcessor converter = new ClassFileToJavaSyntaxProcessor();
    protected JavaSyntaxToJavaFragmentProcessor fragmenter = new JavaSyntaxToJavaFragmentProcessor();
    protected LayoutFragmentProcessor layouter = new LayoutFragmentProcessor();
    //protected TestTokenizeJavaFragmentProcessor tokenizer = new TestTokenizeJavaFragmentProcessor();
    protected JavaFragmentToTokenProcessor tokenizer = new JavaFragmentToTokenProcessor();
    protected WriteTokenProcessor writer = new WriteTokenProcessor();

    @Test
    public void testJdk170IfElse() throws Exception {
        String internalClassName = "org/jd/core/test/IfElse";
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.7.0.zip");
        Loader loader = new ZipLoader(is);
        Map<String, Object> configuration = Collections.singletonMap("realignLineNumbers", Boolean.TRUE);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName, configuration);

        // Check decompiled source code
        assertTrue(source.matches(PatternMaker.make("/*  12:  12 */", "if (this == null)")));
        assertTrue(source.matches(PatternMaker.make("/*  22:  22 */", "if (\"abc\".isEmpty() && \"abc\".isEmpty())")));

        assertTrue(source.matches(PatternMaker.make("/*  32:  32 */", "if (this == null)")));
        assertTrue(source.matches(PatternMaker.make("/*  34:   0 */", "} else {")));

        assertTrue(source.matches(PatternMaker.make("/*  44:  44 */", "if (this == null)")));
        assertTrue(source.matches(PatternMaker.make("/*  46:  46 */", "} else if (this == null) {")));
        assertTrue(source.matches(PatternMaker.make("/*  48:   0 */", "} else {")));

        assertTrue(source.matches(PatternMaker.make("/*  58:  58 */", "if (i == 0)")));
        assertTrue(source.matches(PatternMaker.make("/*  60:  60 */", "if (i == 1)")));

        assertTrue(source.matches(PatternMaker.make("/*  71:  71 */", "if (i == System.currentTimeMillis())")));
        assertTrue(source.matches(PatternMaker.make("/*  73:  73 */", "} else if (i != System.currentTimeMillis()) {")));
        assertTrue(source.matches(PatternMaker.make("/*  75:  75 */", "} else if (i > System.currentTimeMillis()) {")));

        assertTrue(source.matches(PatternMaker.make("/* 123: 123 */", "if (i == 4 && i == 5 && i == 6)")));

        assertTrue(source.matches(PatternMaker.make("/* 135: 135 */", "if (i == 3 || i == 5 || i == 6)")));
        assertTrue(source.matches(PatternMaker.make("/* 137: 137 */", "} else if (i != 4 && i > 7 && i > 8) {")));
        assertTrue(source.matches(PatternMaker.make("/* 139:   0 */", "} else {")));

        assertTrue(source.matches(PatternMaker.make("/* 148: 148 */", "if ((i == 1 && i == 2 && i == 3) || (i == 4 && i == 5 && i == 6) || (i == 7 && i == 8 && i == 9))")));
        assertTrue(source.matches(PatternMaker.make("/* 160: 160 */", "if ((i == 1 || i == 2 || i == 3) && (i == 4 || i == 5 || i == 6) && (i == 7 || i == 8 || i == 9))")));

        assertTrue(source.matches(PatternMaker.make("/* 172: 172 */", "if ((i == 1 || (i == 5 && i == 6 && i == 7) || i == 8 || (i == 9 && i == 10 && i == 11)) && (i == 4 || i % 200 > 50) && (i > 3 || i > 4))")));
        assertTrue(source.matches(PatternMaker.make("/* 184: 184 */", "if ((i == 1 && (i == 5 || i == 6 || i == 7) && i == 8 && (i == 9 || i == 10 || i == 11)) || (i == 4 && i % 200 > 50) || (i > 3 && i > 4))")));

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
