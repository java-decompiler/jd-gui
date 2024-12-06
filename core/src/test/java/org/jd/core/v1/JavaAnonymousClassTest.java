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

public class JavaAnonymousClassTest extends TestCase {
    protected DeserializeClassFileProcessor deserializer = new DeserializeClassFileProcessor();
    protected ClassFileToJavaSyntaxProcessor converter = new ClassFileToJavaSyntaxProcessor();
    protected JavaSyntaxToJavaFragmentProcessor fragmenter = new JavaSyntaxToJavaFragmentProcessor();
    protected LayoutFragmentProcessor layouter = new LayoutFragmentProcessor();
    //protected TestTokenizeJavaFragmentProcessor tokenizer = new TestTokenizeJavaFragmentProcessor();
    protected JavaFragmentToTokenProcessor tokenizer = new JavaFragmentToTokenProcessor();
    protected WriteTokenProcessor writer = new WriteTokenProcessor();

    @Test
    public void testJdk150AnonymousClass() throws Exception {
        String internalClassName = "org/jd/core/test/AnonymousClass";
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.5.0.zip");
        Loader loader = new ZipLoader(is);
        Map<String, Object> configuration = Collections.singletonMap("realignLineNumbers", Boolean.TRUE);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName, configuration);

        // Check decompiled source code
        assertTrue(source.matches(PatternMaker.make(":  21 */", "Object object = new Object()")));
        assertTrue(source.matches(PatternMaker.make(":  23 */", "return \"toString() return \" + super.toString() + \" at \" + AnonymousClass.this.time;")));

        assertTrue(source.matches(PatternMaker.make(":  37 */", "final long l1 = System.currentTimeMillis();")));
        assertTrue(source.matches(PatternMaker.make(":  39 */", "Enumeration enumeration = new Enumeration()")));
        assertTrue(source.matches(PatternMaker.make(":  40 */", "Iterator<String> i = AnonymousClass.this.list.iterator();")));
        assertTrue(source.matches(PatternMaker.make(":  44 */", "return (this.i.hasNext() && s1 == s2 && i1 > l1);")));
        assertTrue(source.indexOf("return this.i.next();") != -1);
        assertTrue(source.matches(PatternMaker.make(":  52 */", "test(enumeration, \"test\");")));
        assertTrue(source.matches(PatternMaker.make(":  55 */", "System.out.println(\"end\");")));

        assertTrue(source.matches(PatternMaker.make(":  67 */", "if (s1 == s2 && i == 5)")));

        assertTrue(source.matches(PatternMaker.make(":  90 */", "Serializable serializable = new Serializable()")));
        assertTrue(source.matches(PatternMaker.make(":  96 */", "return (abc.equals(param2Object) || def.equals(param2Object) || str1.equals(param2Object) || str2.equals(param2Object));")));
        assertTrue(source.matches(PatternMaker.make(": 104 */", "System.out.println(\"end\");")));

        assertTrue(source.indexOf("/* 111: 111 */") != -1);

        assertTrue(source.indexOf("{ ;") == -1);

        // Recompile decompiled source code and check errors
        assertTrue(CompilerUtil.compile(
                "1.5",
                new JavaSourceFileObject(internalClassName, source),
                new JavaSourceFileObject("org/jd/core/test/annotation/Name", "package org.jd.core.test.annotation; public @interface Name {String value();}")
        ));
    }

    @Test
    public void testJdk170AnonymousClass() throws Exception {
        String internalClassName = "org/jd/core/test/AnonymousClass";
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.7.0.zip");
        Loader loader = new ZipLoader(is);
        Map<String, Object> configuration = Collections.singletonMap("realignLineNumbers", Boolean.TRUE);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName, configuration);

        // Check decompiled source code
        assertTrue(source.matches(PatternMaker.make(":  21 */", "Object obj = new Object()")));
        assertTrue(source.matches(PatternMaker.make(":  23 */", "return \"toString() return \" + super.toString() + \" at \" + AnonymousClass.this.time;")));

        assertTrue(source.matches(PatternMaker.make(":  39 */", "Enumeration e = new Enumeration()")));
        assertTrue(source.matches(PatternMaker.make(":  40 */", "Iterator<String> i = AnonymousClass.this.list.iterator();")));
        assertTrue(source.matches(PatternMaker.make(":  44 */", "return (this.i.hasNext() && s1 == s2 && i1 > l1);")));

        assertTrue(source.matches(PatternMaker.make(":  61 */", "final int i = s1.length();")));
        assertTrue(source.matches(PatternMaker.make(":  63 */", "System.out.println(\"2\" + new StringWrapper(123456L)")));
        assertTrue(source.matches(PatternMaker.make(":  67 */", "if (s1 == s2 && i == 5)")));
        assertTrue(source.matches(PatternMaker.make("/*  72:   0 */", "} + \"3\");")));

        assertTrue(source.matches(PatternMaker.make(":  81 */", "final Object abc = \"abc\";")));
        assertTrue(source.matches(PatternMaker.make(":  82 */", "final Object def = \"def\";")));
        assertTrue(source.matches(PatternMaker.make(":  84 */", "Serializable serializable = new Serializable()")));
        assertTrue(source.matches(PatternMaker.make(":  90 */", "Serializable serializable = new Serializable()")));
        assertTrue(source.matches(PatternMaker.make(":  96 */", "return (abc.equals(obj) || def.equals(obj) || ghi.equals(obj) || jkl.equals(obj));")));
        assertTrue(source.matches(PatternMaker.make(": 100 */", "return (abc.equals(obj) || def.equals(obj));")));
        assertTrue(source.matches(PatternMaker.make("/* 102:   0 */", "};")));

        assertTrue(source.matches(PatternMaker.make(": 111 */", "this.l = l & 0x80L;")));

        assertTrue(source.indexOf("{ ;") == -1);
        assertTrue(source.indexOf("} ;") == -1);
        assertTrue(source.indexOf("// Byte code:") == -1);

        // Recompile decompiled source code and check errors
        assertTrue(CompilerUtil.compile(
                "1.7",
                new JavaSourceFileObject(internalClassName, source),
                new JavaSourceFileObject("org/jd/core/test/annotation/Name", "package org.jd.core.test.annotation; public @interface Name {String value();}")
            ));
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
