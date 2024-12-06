/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1;

import junit.framework.TestCase;
import org.jd.core.v1.compiler.CompilerUtil;
import org.jd.core.v1.compiler.JavaSourceFileObject;
import org.jd.core.v1.loader.ZipLoader;
import org.jd.core.v1.model.message.Message;
import org.jd.core.v1.printer.PlainTextPrinter;
import org.jd.core.v1.service.converter.classfiletojavasyntax.ClassFileToJavaSyntaxProcessor;
import org.jd.core.v1.service.deserializer.classfile.DeserializeClassFileProcessor;
import org.jd.core.v1.service.fragmenter.javasyntaxtojavafragment.JavaSyntaxToJavaFragmentProcessor;
import org.jd.core.v1.service.layouter.LayoutFragmentProcessor;
import org.jd.core.v1.service.tokenizer.javafragmenttotoken.JavaFragmentToTokenProcessor;
import org.jd.core.v1.service.writer.WriteTokenProcessor;
import org.jd.core.v1.util.DefaultList;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;

public class JarFileToJavaSourceTest extends TestCase {
    protected DeserializeClassFileProcessor deserializer = new DeserializeClassFileProcessor();
    protected ClassFileToJavaSyntaxProcessor converter = new ClassFileToJavaSyntaxProcessor();
    protected JavaSyntaxToJavaFragmentProcessor fragmenter = new JavaSyntaxToJavaFragmentProcessor();
    protected LayoutFragmentProcessor layouter = new LayoutFragmentProcessor();
    //protected TestTokenizeJavaFragmentProcessor tokenizer = new TestTokenizeJavaFragmentProcessor();
    protected JavaFragmentToTokenProcessor tokenizer = new JavaFragmentToTokenProcessor();
    protected WriteTokenProcessor writer = new WriteTokenProcessor();

    @Test
    public void testCommonsCodec() throws Exception {
        // Decompile and recompile 'commons-codec:commons-codec:1.13'
        test(org.apache.commons.codec.Charsets.class);
    }

    @Test
    public void testCommonsCollections4() throws Exception {
        // Decompile and recompile 'commons-collections4-4.1.jar'
        test(org.apache.commons.collections4.CollectionUtils.class);
    }

    @Test
    public void testCommonsImaging() throws Exception {
        // Decompile and recompile 'org.apache.commons:commons-imaging:1.0-alpha1'
        test(org.apache.commons.collections4.CollectionUtils.class);
    }

    @Test
    public void testCommonsLang3() throws Exception {
        // Decompile and recompile 'org.apache.commons:commons-lang3:3.9'
        test(org.apache.commons.lang3.JavaVersion.class);
    }

    @Test
    public void testDiskLruCache() throws Exception {
        // Decompile and recompile 'com.jakewharton:disklrucache:2.0.2'
        test(com.jakewharton.disklrucache.DiskLruCache.class);
    }

    @Test
    public void testJavaPoet() throws Exception {
        // Decompile and recompile 'com.squareup:javapoet:1.11.1'
        test(com.squareup.javapoet.JavaFile.class);
    }

    @Test
    public void testJavaWriter() throws Exception {
        // Decompile and recompile 'com.squareup:javawriter:2.5.1'
        test(com.squareup.javawriter.JavaWriter.class);
    }

    // TODO In progress
//    @Test
//    public void testJodaTime() throws Exception {
//        // Decompile and recompile 'joda-time:joda-time:2.10.5'
//        test(org.joda.time.DateTime.class);
//    }

    @Test
    public void testJSoup() throws Exception {
        // Decompile and recompile 'org.jsoup:jsoup:1.12.1'
        test(org.jsoup.Jsoup.class);
    }

    @Test
    public void testJUnit4() throws Exception {
        // Decompile and recompile 'junit:junit:4.12'
        test(org.junit.Test.class);
    }

    @Test
    public void testMimecraft() throws Exception {
        // Decompile and recompile 'com.squareup.mimecraft:mimecraft:1.1.1'
        test(com.squareup.mimecraft.Part.class);
    }

    @Test
    public void testScribe() throws Exception {
        // Decompile and recompile 'org.scribe:scribe:1.3.7'
        test(org.scribe.oauth.OAuthService.class);
    }

    @Test
    public void testSparkCore() throws Exception {
        // Decompile and recompile 'com.sparkjava:spark-core:2.9.1'
        test(spark.Spark.class);
    }

    @Test
    public void testLog4j() throws Exception {
        // Decompile and recompile 'log4j:log4j:1.2.17'
        test(org.apache.log4j.Category.class);
    }

    // TODO In progress
//    @Test
//    public void testGuava() throws Exception {
//        // Decompile and recompile 'com.google.guava:guava:12.0'
//        test(com.google.common.collect.Collections2.class);
//    }

    protected void test(Class clazz) throws Exception {
        test(new FileInputStream(Paths.get(clazz.getProtectionDomain().getCodeSource().getLocation().toURI()).toFile()));
    }

    protected void test(InputStream inputStream) throws Exception {
        long fileCounter = 0;
        long exceptionCounter = 0;
        long assertFailedCounter = 0;
        long recompilationFailedCounter = 0;

        try (InputStream is = inputStream) {
            ZipLoader loader = new ZipLoader(is);
            CounterPrinter printer = new CounterPrinter();
            HashMap<String, Integer> statistics = new HashMap<>();
            HashMap<String, Object> configuration = new HashMap<>();

            configuration.put("realignLineNumbers", Boolean.TRUE);

            Message message = new Message();
            message.setHeader("loader", loader);
            message.setHeader("printer", printer);
            message.setHeader("configuration", configuration);

            long time0 = System.currentTimeMillis();

            for (String path : loader.getMap().keySet()) {
                if (path.endsWith(".class") && (path.indexOf('$') == -1)) {
                    String internalTypeName = path.substring(0, path.length() - 6); // 6 = ".class".length()

                    // TODO DEBUG if (!internalTypeName.endsWith("/Debug")) continue;
                    //if (!internalTypeName.endsWith("/MapUtils")) continue;

                    message.setHeader("mainInternalTypeName", internalTypeName);
                    printer.init();

                    fileCounter++;

                    try {
                        // Decompile class
                        deserializer.process(message);
                        converter.process(message);
                        fragmenter.process(message);
                        layouter.process(message);
                        tokenizer.process(message);
                        writer.process(message);
                    } catch (AssertionError e) {
                        String msg = (e.getMessage() == null) ? "<?>" : e.getMessage();
                        Integer counter = statistics.get(msg);
                        statistics.put(msg, (counter == null) ? 1 : counter + 1);
                        assertFailedCounter++;
                    } catch (Throwable t) {
                        String msg = t.getMessage() == null ? t.getClass().toString() : t.getMessage();
                        Integer counter = statistics.get(msg);
                        statistics.put(msg, (counter == null) ? 1 : counter + 1);
                        exceptionCounter++;
                    }

                    // Recompile source
                    String source = printer.toString();

                    if (!CompilerUtil.compile("1.8", new JavaSourceFileObject(internalTypeName, source))) {
                        recompilationFailedCounter++;
                    }
                }
            }

            long time9 = System.currentTimeMillis();

            System.out.println("Time: " + (time9-time0) + " ms");

            System.out.println("Counters:");
            System.out.println("  fileCounter             =" + fileCounter);
            System.out.println("  class+innerClassCounter =" + printer.classCounter);
            System.out.println("  methodCounter           =" + printer.methodCounter);
            System.out.println("  exceptionCounter        =" + exceptionCounter);
            System.out.println("  assertFailedCounter     =" + assertFailedCounter);
            System.out.println("  errorInMethodCounter    =" + printer.errorInMethodCounter);
            System.out.println("  accessCounter           =" + printer.accessCounter);
            System.out.println("  recompilationFailed     =" + recompilationFailedCounter);
            System.out.println("Percentages:");
            System.out.println("  % exception             =" + (exceptionCounter * 100F / fileCounter));
            System.out.println("  % assert failed         =" + (assertFailedCounter * 100F / fileCounter));
            System.out.println("  % error in method       =" + (printer.errorInMethodCounter * 100F / printer.methodCounter));
            System.out.println("  % recompilation failed  =" + (recompilationFailedCounter * 100F / fileCounter));

            System.out.println("Errors:");
            DefaultList<String> stats = new DefaultList<>();
            for (Map.Entry<String, Integer> stat : statistics.entrySet()) {
                stats.add("  " + stat.getValue() + " \t: " + stat.getKey());
            }
            stats.sort((s1, s2) -> Integer.parseInt(s2.substring(0, 5).trim()) - Integer.parseInt(s1.substring(0, 5).trim()));
            for (String stat : stats) {
                System.out.println(stat);
            }

            assertTrue(exceptionCounter == 0);
            assertTrue(assertFailedCounter == 0);
            assertTrue(printer.errorInMethodCounter == 0);
            assertTrue(recompilationFailedCounter == 0);
        }
    }

    protected static class CounterPrinter extends PlainTextPrinter {
        public long classCounter = 0;
        public long methodCounter = 0;
        public long errorInMethodCounter = 0;
        public long accessCounter = 0;

        public void printText(String text) {
            if (text != null) {
                if ("// Byte code:".equals(text) || text.startsWith("/* monitor enter ") || text.startsWith("/* monitor exit ")) {
                    errorInMethodCounter++;
                }
            }
            super.printText(text);
        }

        public void printDeclaration(int type, String internalTypeName, String name, String descriptor) {
            if (type == TYPE) classCounter++;
            if ((type == METHOD) || (type == CONSTRUCTOR)) methodCounter++;
            super.printDeclaration(type, internalTypeName, name, descriptor);
        }

        public void printReference(int type, String internalTypeName, String name, String descriptor, String ownerInternalName) {
            if ((name != null) && name.startsWith("access$")) {
                accessCounter++;
            }
            super.printReference(type, internalTypeName, name, descriptor, ownerInternalName);
        }
    }
}
