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

public class Java9InterfaceTest extends TestCase {
    protected DeserializeClassFileProcessor deserializer = new DeserializeClassFileProcessor();
    protected ClassFileToJavaSyntaxProcessor converter = new ClassFileToJavaSyntaxProcessor();
    protected JavaSyntaxToJavaFragmentProcessor fragmenter = new JavaSyntaxToJavaFragmentProcessor();
    protected LayoutFragmentProcessor layouter = new LayoutFragmentProcessor();
    //protected TestTokenizeJavaFragmentProcessor tokenizer = new TestTokenizeJavaFragmentProcessor();
    protected JavaFragmentToTokenProcessor tokenizer = new JavaFragmentToTokenProcessor();
    protected WriteTokenProcessor writer = new WriteTokenProcessor();

    @Test
    public void testJdk901InterfaceWithDefaultMethods() throws Exception {
        String internalClassName = "org/jd/core/test/InterfaceWithDefaultMethods";
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-9.0.1.zip");
        Loader loader = new ZipLoader(is);
        String source = decompile(loader, new PlainTextPrinter(), internalClassName);

        // Check decompiled source code
        assertTrue(source.matches(PatternMaker.make("public interface InterfaceWithDefaultMethods")));
        assertTrue(source.matches(PatternMaker.make("void setTime(int paramInt1, int paramInt2, int paramInt3);")));
        assertTrue(source.matches(PatternMaker.make("LocalDateTime getLocalDateTime();")));
        assertTrue(source.matches(PatternMaker.make("static ZoneId getZoneId(String zoneString)")));
        assertTrue(source.matches(PatternMaker.make(": 24 */", "return unsafeGetZoneId(zoneString);")));
        assertTrue(source.matches(PatternMaker.make(": 26 */", "System.err.println(\"Invalid time zone: \" + zoneString + \"; using default time zone instead.\");")));
        assertTrue(source.matches(PatternMaker.make(": 27 */", "return ZoneId.systemDefault();")));
        assertTrue(source.matches(PatternMaker.make("default ZonedDateTime getZonedDateTime(String zoneString)")));
        assertTrue(source.matches(PatternMaker.make(": 32 */", "return getZonedDateTime(getLocalDateTime(), getZoneId(zoneString));")));
        assertTrue(source.matches(PatternMaker.make("private static ZoneId unsafeGetZoneId(String zoneString)")));
        assertTrue(source.matches(PatternMaker.make(": 36 */", "return ZoneId.of(zoneString);")));
        assertTrue(source.matches(PatternMaker.make("private ZonedDateTime getZonedDateTime(LocalDateTime localDateTime, ZoneId zoneId)")));
        assertTrue(source.matches(PatternMaker.make(": 40 */", "return ZonedDateTime.of(localDateTime, zoneId);")));

        // Recompile decompiled source code and check errors
        try {
            assertTrue(CompilerUtil.compile("1.9", new JavaSourceFileObject(internalClassName, source)));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("invalid source release: 1.9")) {
                System.err.println("testJdk901InterfaceWithDefaultMethods() need a Java SDK 9+");
            } else {
                assertTrue("Compilation failed: " + e.getMessage(), false);
            }
        }
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
