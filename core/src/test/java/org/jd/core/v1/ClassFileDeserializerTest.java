/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1;

import junit.framework.TestCase;

import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.api.loader.LoaderException;
import org.jd.core.v1.loader.ZipLoader;
import org.jd.core.v1.model.classfile.ClassFile;
import org.jd.core.v1.model.classfile.Field;
import org.jd.core.v1.model.classfile.attribute.Annotations;
import org.jd.core.v1.model.classfile.attribute.ElementValueAnnotationValue;
import org.jd.core.v1.model.classfile.attribute.ElementValuePrimitiveType;
import org.jd.core.v1.model.classfile.constant.ConstantInteger;
import org.jd.core.v1.model.classfile.constant.ConstantUtf8;
import org.jd.core.v1.model.message.Message;
import org.jd.core.v1.service.deserializer.classfile.ClassFileDeserializer;
import org.jd.core.v1.service.deserializer.classfile.DeserializeClassFileProcessor;
import org.junit.Test;

import java.io.InputStream;

public class ClassFileDeserializerTest extends TestCase {
    @Test
    public void testMissingClass() throws Exception {
        class NoOpLoader implements Loader {
            @Override
            public boolean canLoad(String internalName) {
                return false;
            }

            @Override
            public byte[] load(String internalName) throws LoaderException {
                fail("Loader cannot load anything");
                return null;
            }
        }

        ClassFileDeserializer deserializer = new ClassFileDeserializer();
        try {
            deserializer.loadClassFile(new NoOpLoader(), "DoesNotExist");
            fail("Expected exception");
        }
        // Expecting exception because class cannot be loaded
        catch (IllegalArgumentException expected) { }
    }

    @Test
    public void testAnnotatedClass() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.7.0.zip");
        ZipLoader loader = new ZipLoader(is);
        DeserializeClassFileProcessor deserializer = new DeserializeClassFileProcessor();

        Message message = new Message();
        message.setHeader("mainInternalTypeName", "org/jd/core/test/AnnotatedClass");
        message.setHeader("loader", loader);

        deserializer.process(message);

        ClassFile classFile = message.getBody();

        // Check class
        assertNotNull(classFile);
        assertEquals("org/jd/core/test/AnnotatedClass", classFile.getInternalTypeName());
        assertEquals("java/util/ArrayList", classFile.getSuperTypeName());

        Annotations invAttr = classFile.getAttribute("RuntimeInvisibleAnnotations");
        assertNotNull(invAttr.getAnnotations());
        assertEquals(2, invAttr.getAnnotations().length);
        assertNotNull(invAttr.getAnnotations()[0].getElementValuePairs());
        assertEquals(1, invAttr.getAnnotations()[0].getElementValuePairs().length);

        ElementValueAnnotationValue annotationValue = invAttr.getAnnotations()[1].getElementValuePairs()[0].getElementValue();
        assertEquals("Lorg/jd/core/test/annotation/Name;", annotationValue.getAnnotationValue().getDescriptor());
        assertNotNull(annotationValue.getAnnotationValue().getElementValuePairs());
        assertEquals(3, annotationValue.getAnnotationValue().getElementValuePairs().length);
        assertEquals("salutation", annotationValue.getAnnotationValue().getElementValuePairs()[0].getElementName());

        ElementValuePrimitiveType primitiveType = annotationValue.getAnnotationValue().getElementValuePairs()[1].getElementValue();
        ConstantUtf8 cu = primitiveType.getConstValue();
        assertEquals("Donald", cu.getValue());

        // Check fields
        assertNotNull(classFile.getFields());
        assertEquals(10, classFile.getFields().length);

        // Check 1st field
        Field field = classFile.getFields()[1];
        assertEquals("b1", field.getName());
        assertEquals("B", field.getDescriptor());

        Annotations attr = field.getAttribute("RuntimeVisibleAnnotations");
        assertNotNull(attr.getAnnotations());
        assertEquals(1, attr.getAnnotations().length);
        assertNotNull(attr.getAnnotations()[0].getElementValuePairs());
        assertEquals(1, attr.getAnnotations()[0].getElementValuePairs().length);
        assertEquals("b", attr.getAnnotations()[0].getElementValuePairs()[0].getElementName());

        primitiveType = attr.getAnnotations()[0].getElementValuePairs()[0].getElementValue();
        ConstantInteger ci = primitiveType.getConstValue();
        assertEquals(-15, ci.getValue());

        // Check 8th field
        field = classFile.getFields()[8];
        assertEquals("str2", field.getName());
        assertEquals("Ljava/lang/String;", field.getDescriptor());

        attr = field.getAttribute("RuntimeVisibleAnnotations");
        assertNotNull(attr.getAnnotations());
        assertEquals(1, attr.getAnnotations().length);
        assertNotNull(attr.getAnnotations()[0].getElementValuePairs());
        assertEquals(1, attr.getAnnotations()[0].getElementValuePairs().length);
        assertEquals("str", attr.getAnnotations()[0].getElementValuePairs()[0].getElementName());

        primitiveType = attr.getAnnotations()[0].getElementValuePairs()[0].getElementValue();
        cu = primitiveType.getConstValue();
        assertEquals("str \u0083 \u0909 \u1109", cu.getValue());

        // Check getters
        assertNotNull(classFile.getMethods());
        assertEquals(3, classFile.getMethods().length);

        // Check constructor
        assertEquals("<init>", classFile.getMethods()[0].getName());
        assertEquals("()V", classFile.getMethods()[0].getDescriptor());
        assertNotNull(classFile.getMethods()[0].getAttribute("Code"));
    }
}
