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

public class JavaLambdaTest extends TestCase {
    protected DeserializeClassFileProcessor deserializer = new DeserializeClassFileProcessor();
    protected ClassFileToJavaSyntaxProcessor converter = new ClassFileToJavaSyntaxProcessor();
    protected JavaSyntaxToJavaFragmentProcessor fragmenter = new JavaSyntaxToJavaFragmentProcessor();
    protected LayoutFragmentProcessor layouter = new LayoutFragmentProcessor();
    //protected TestTokenizeJavaFragmentProcessor tokenizer = new TestTokenizeJavaFragmentProcessor();
    protected JavaFragmentToTokenProcessor tokenizer = new JavaFragmentToTokenProcessor();
    protected WriteTokenProcessor writer = new WriteTokenProcessor();

    @Test
    public void testJdk180Lambda() throws Exception {
        String internalClassName = "org/jd/core/test/Lambda";
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.8.0.zip");
        Loader loader = new ZipLoader(is);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName);

        // Check decompiled source code
        assertTrue(source.matches(PatternMaker.make(": 16 */", "list.forEach(System.out::println);")));
        assertTrue(source.matches(PatternMaker.make(": 20 */", "list.stream().filter(s -> (s != null)).forEach(s -> System.out.println(s));")));
        assertTrue(source.indexOf("Predicate<String> filter = s -> (s.length() == length);") != -1);
        assertTrue(source.indexOf("Consumer<String> println = s -> System.out.println(s);") != -1);
        assertTrue(source.matches(PatternMaker.make(": 27 */", "list.stream().filter(filter).forEach(println);")));
        assertTrue(source.matches(PatternMaker.make(": 31 */", "((Map)list.stream()")));
        assertTrue(source.matches(PatternMaker.make(": 32 */", ".collect(Collectors.toMap(lambda -> lambda.index, Function.identity())))")));
        assertTrue(source.matches(PatternMaker.make(": 33 */", ".forEach((key, value) ->")));
        assertTrue(source.matches(PatternMaker.make(": 48 */", "Thread thread = new Thread(() -> {")));
        assertTrue(source.matches(PatternMaker.make(": 58 */", "Consumer<String> staticMethodReference = String::valueOf;")));
        assertTrue(source.matches(PatternMaker.make(": 59 */", "BiFunction<String, String, Integer> methodReference = String::compareTo;")));
        assertTrue(source.matches(PatternMaker.make(": 60 */", "Supplier<String> instanceMethodReference = s::toString;")));
        assertTrue(source.matches(PatternMaker.make(": 61 */", "Supplier<String> constructorReference = String::new;")));
        assertTrue(source.matches(PatternMaker.make(": 65 */", "MethodType mtToString = MethodType.methodType(String.class);")));
        assertTrue(source.matches(PatternMaker.make(": 66 */", "MethodType mtSetter = MethodType.methodType(void.class, Object.class);")));
        assertTrue(source.matches(PatternMaker.make(": 67 */", "MethodType mtStringComparator = MethodType.methodType(int[].class, String.class, new Class<?>[]", "{ String.class")));

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
