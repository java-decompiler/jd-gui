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

public class JavaAnnotationTest extends TestCase {
    protected DeserializeClassFileProcessor deserializer = new DeserializeClassFileProcessor();
    protected ClassFileToJavaSyntaxProcessor converter = new ClassFileToJavaSyntaxProcessor();
    protected JavaSyntaxToJavaFragmentProcessor fragmenter = new JavaSyntaxToJavaFragmentProcessor();
    protected LayoutFragmentProcessor layouter = new LayoutFragmentProcessor();
    //protected TestTokenizeJavaFragmentProcessor tokenizer = new TestTokenizeJavaFragmentProcessor();
    protected JavaFragmentToTokenProcessor tokenizer = new JavaFragmentToTokenProcessor();
    protected WriteTokenProcessor writer = new WriteTokenProcessor();

    @Test
    public void testJdk170AnnotatedClass() throws Exception {
        String internalClassName = "org/jd/core/test/AnnotatedClass";
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.7.0.zip");
        Loader loader = new ZipLoader(is);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName);

        // Check decompiled source code
        assertTrue(source.indexOf("@Quality(Quality.Level.HIGH)") != -1);
        assertTrue(source.indexOf("@Author(value = @Name(salutation = \"Mr\", value = \"Donald\", last = \"Duck\"), contributors = {@Name(\"Huey\"), @Name(\"Dewey\"), @Name(\"Louie\")})") != -1);
        assertTrue(source.indexOf("@Value(z = true)") != -1);
        assertTrue(source.indexOf("@Value(b = -15)") != -1);
        assertTrue(source.indexOf("@Value(s = -15)") != -1);
        assertTrue(source.indexOf("@Value(i = 1)") != -1);
        assertTrue(source.indexOf("@Value(l = 1234567890123456789L)") != -1);
        assertTrue(source.indexOf("@Value(f = 123.456F)") != -1);
        assertTrue(source.indexOf("@Value(d = 789.101112D)") != -1);
        assertTrue(source.indexOf("@Value(str = \"str\")") != -1);
        assertTrue(source.indexOf("@Value(str = \"str \u0083 उ ᄉ\")") != -1);
        assertTrue(source.indexOf("@Value(clazz = String.class)") != -1);
        assertTrue(source.indexOf("public void ping(@Deprecated Writer writer, @Deprecated @Value(str = \"localhost\") String host, long timeout)") != -1);

        // Recompile decompiled source code and check errors
        assertTrue(CompilerUtil.compile(
                "1.7",
                new JavaSourceFileObject(internalClassName, source),
                new JavaSourceFileObject("org/jd/core/test/annotation/Author", "package org.jd.core.test.annotation; public @interface Author {Name value(); Name[] contributors() default {};}"),
                new JavaSourceFileObject("org/jd/core/test/annotation/Name", "package org.jd.core.test.annotation; public @interface Name {String salutation() default \"\"; String value(); String last() default \"\";}"),
                new JavaSourceFileObject("org/jd/core/test/annotation/Quality", "package org.jd.core.test.annotation; public @interface Quality {enum Level {LOW,MIDDLE,HIGH}; Level value();}"),
                new JavaSourceFileObject("org/jd/core/test/annotation/Value", "package org.jd.core.test.annotation; public @interface Value {boolean z() default true; byte b() default 1; short s() default 1; int i() default 1; long l() default 1L; float f() default 1.0F; double d() default 1.0D; String str() default \"str\"; Class clazz() default Object.class;}")
            ));
    }

    @Test
    public void testJdk170AnnotationAuthor() throws Exception {
        String internalClassName = "org/jd/core/test/annotation/Author";
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.7.0.zip");
        Loader loader = new ZipLoader(is);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName);

        // Check decompiled source code
        assertTrue(source.matches(PatternMaker.make("/*   3:   0 */", "public @interface Author")));
        assertTrue(source.matches(PatternMaker.make("/*   4:   0 */", "Name value();")));
        assertTrue(source.matches(PatternMaker.make("/*   6:   0 */", "Name[] contributors() default {};")));

        // Recompile decompiled source code and check errors
        assertTrue(CompilerUtil.compile(
                "1.7",
                new JavaSourceFileObject(internalClassName, source),
                new JavaSourceFileObject("org/jd/core/test/annotation/Name", "package org.jd.core.test.annotation; public @interface Name {String value();}")
            ));
    }

    @Test
    public void testJdk170AnnotationValue() throws Exception {
        String internalClassName = "org/jd/core/test/annotation/Value";
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.7.0.zip");
        Loader loader = new ZipLoader(is);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName);

        // Check decompiled source code
        assertTrue(source.matches(PatternMaker.make("/*   8:   0 */", "@Retention(RetentionPolicy.RUNTIME)")));
        assertTrue(source.matches(PatternMaker.make("/*   9:   0 */", "@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})")));
        assertTrue(source.matches(PatternMaker.make("/*  10:   0 */", "public @interface Value {")));
        assertTrue(source.matches(PatternMaker.make("/*  11:   0 */", "boolean z() default true;")));
        assertTrue(source.matches(PatternMaker.make("/*  13:   0 */", "byte b() default 1;")));
        assertTrue(source.matches(PatternMaker.make("/*  25:   0 */", "String str() default \"str\";")));
        assertTrue(source.matches(PatternMaker.make("/*  27:   0 */", "Class clazz() default Object.class;")));

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
