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

import java.util.Collections;
import java.util.Map;

public class JavaMethodOverloadingTest extends TestCase {
    protected DeserializeClassFileProcessor deserializer = new DeserializeClassFileProcessor();
    protected ClassFileToJavaSyntaxProcessor converter = new ClassFileToJavaSyntaxProcessor();
    protected JavaSyntaxToJavaFragmentProcessor fragmenter = new JavaSyntaxToJavaFragmentProcessor();
    protected LayoutFragmentProcessor layouter = new LayoutFragmentProcessor();
    //protected TestTokenizeJavaFragmentProcessor tokenizer = new TestTokenizeJavaFragmentProcessor();
    protected JavaFragmentToTokenProcessor tokenizer = new JavaFragmentToTokenProcessor();
    protected WriteTokenProcessor writer = new WriteTokenProcessor();

    @Test
    // https://github.com/java-decompiler/jd-core/issues/33
    public void testArrayMethodOverloading() throws Exception {
        class ArrayMethodOverloading {
            void use(Object[] o) { }
            void use(Object o) { }

            void test1() {
                use("string");
            }
            void test2() {
                use((Object) new Object[] {""});
            }
            void test3() {
                use(null);
            }
            void test4() {
                use((Object)null);
            }
        }

        String internalClassName = ArrayMethodOverloading.class.getName().replace('.', '/');
        String source = decompile(new ClassPathLoader(), new PlainTextPrinter(), internalClassName);

        // Check decompiled source code
        assertTrue(source.matches(PatternMaker.make(": 47 */", "use(\"string\");")));
        assertTrue(source.matches(PatternMaker.make(": 50 */", "use((Object)new Object[] { \"\" });")));
        assertTrue(source.matches(PatternMaker.make(": 53 */", "use((Object[])null);")));
        assertTrue(source.matches(PatternMaker.make(": 56 */", "use((Object)null);")));

        // Recompile decompiled source code and check errors
        assertTrue(CompilerUtil.compile("1.8", new JavaSourceFileObject(internalClassName, source)));
    }

    @Test
    // https://github.com/java-decompiler/jd-core/issues/32
//    public void testGenericParameterMethod() throws Exception {
//        class GenericParameterMethod {
//            /* static */ void use(Integer i) {
//                System.out.println("use(Integer)");
//            }
//            /* static */ <T> void use(T t) {
//                System.out.println("use(T)");
//            }
//
//            public /* static */ void main(String... args) {
//                use(1);
//                use((Object) 1); // Calls use(T)
//            }
//        }
//
//        String internalClassName = GenericParameterMethod.class.getName().replace('.', '/');
//        String source = decompile(new ClassPathLoader(), new PlainTextPrinter(), internalClassName);
//
//        // Check decompiled source code
//        assertTrue(source.matches(PatternMaker.make(": 85 */", "use(1);")));
//        assertTrue(source.matches(PatternMaker.make(": 86 */", "use((Object)1);")));
//
//        // Recompile decompiled source code and check errors
//        assertTrue(CompilerUtil.compile("1.8", new JavaSourceFileObject(internalClassName, source)));
//    }

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
