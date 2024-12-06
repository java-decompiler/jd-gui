/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1;

import junit.framework.TestCase;
import org.jd.core.v1.loader.ClassPathLoader;
import org.jd.core.v1.loader.ZipLoader;
import org.jd.core.v1.model.classfile.ClassFile;
import org.jd.core.v1.model.javasyntax.type.*;
import org.jd.core.v1.model.message.Message;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.TypeMaker;
import org.jd.core.v1.service.deserializer.classfile.DeserializeClassFileProcessor;
import org.jd.core.v1.services.javasyntax.type.visitor.PrintTypeVisitor;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;

public class SignatureParserTest extends TestCase {
    protected DeserializeClassFileProcessor deserializer = new DeserializeClassFileProcessor();

    @Test
    public void testAnnotatedClass() throws Exception {
        PrintTypeVisitor visitor = new PrintTypeVisitor();
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.7.0.zip");
        ZipLoader loader = new ZipLoader(is);
        TypeMaker typeMaker = new TypeMaker(loader);

        Message message = new Message();
        message.setHeader("mainInternalTypeName", "org/jd/core/test/AnnotatedClass");
        message.setHeader("loader", loader);

        deserializer.process(message);

        ClassFile classFile = message.getBody();

        // Check type
        TypeMaker.TypeTypes typeTypes = typeMaker.parseClassFileSignature(classFile);

        // Check type parameterTypes
        assertNull(typeTypes.typeParameters);

        // Check super type
        assertNotNull(typeTypes.superType);
        visitor.reset();

        BaseType superType = typeTypes.superType;

        superType.accept(visitor);
        String source = visitor.toString();

        Assert.assertEquals("java.util.ArrayList", source);

        // Check interfaces
        assertNotNull(typeTypes.interfaces);
        visitor.reset();
        typeTypes.interfaces.accept(visitor);
        source = visitor.toString();

        Assert.assertEquals("java.io.Serializable, java.lang.Cloneable", source);

        // Check field 'list1'
        //  public List<List<? extends Generic>> list1
        BaseType type = typeMaker.parseFieldSignature(classFile, classFile.getFields()[0]);
        visitor.reset();
        type.accept(visitor);
        source = visitor.toString();

        Assert.assertEquals("boolean", source);

        // Check method 'add'
        //  public int add(int i1, int i2)
        TypeMaker.MethodTypes methodTypes = typeMaker.parseMethodSignature(classFile, classFile.getMethods()[1]);

        // Check type parameterTypes
        assertNull(methodTypes.typeParameters);

        // Check parameterTypes
        assertNotNull(methodTypes.parameterTypes);
        assertEquals(2, methodTypes.parameterTypes.size());

        type = methodTypes.parameterTypes.getFirst();
        visitor.reset();
        type.accept(visitor);
        source = visitor.toString();

        Assert.assertEquals("int", source);

        // Check return type
        assertNotNull(methodTypes.returnedType);

        BaseType returnedType = methodTypes.returnedType;
        visitor.reset();
        returnedType.accept(visitor);
        source = visitor.toString();

        Assert.assertEquals("int", source);

        // Check exceptions
        assertNull(methodTypes.exceptionTypes);

        // Check method 'ping'
        //  public void ping(String host) throws UnknownHostException, UnsatisfiedLinkError
        methodTypes = typeMaker.parseMethodSignature(classFile, classFile.getMethods()[2]);

        // Check type parameterTypes
        assertNull(methodTypes.typeParameters);

        // Check parameterTypes
        assertNotNull(methodTypes.parameterTypes);
        assertEquals(3, methodTypes.parameterTypes.size());

        type = methodTypes.parameterTypes.getList().get(1);
        visitor.reset();
        type.accept(visitor);
        source = visitor.toString();

        Assert.assertEquals("java.lang.String", source);

        // Check return type
        assertNotNull(methodTypes.returnedType);

        returnedType = methodTypes.returnedType;
        visitor.reset();
        returnedType.accept(visitor);
        source = visitor.toString();

        Assert.assertEquals("void", source);

        // Check exceptions
        assertNotNull(methodTypes.exceptionTypes);

        visitor.reset();
        methodTypes.exceptionTypes.accept(visitor);
        source = visitor.toString();

        Assert.assertEquals("java.net.UnknownHostException, java.lang.UnsatisfiedLinkError", source);
    }

    @Test
    public void testGenericClass() throws Exception {
        PrintTypeVisitor visitor = new PrintTypeVisitor();
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.7.0.zip");
        ZipLoader loader = new ZipLoader(is);
        TypeMaker typeMaker = new TypeMaker(loader);

        Message message = new Message();
        message.setHeader("mainInternalTypeName", "org/jd/core/test/GenericClass");
        message.setHeader("loader", loader);

        deserializer.process(message);

        ClassFile classFile = message.getBody();

        // Check type
        TypeMaker.TypeTypes typeTypes = typeMaker.parseClassFileSignature(classFile);

        // Check type parameterTypes
        // See "org.jd.core.test.resources.java.Generic"
        //  T1:Ljava/lang/Object;
        //  T2:Ljava/lang/Object;
        //  T3:Lorg/jd/core/v1/test/resources/java/AnnotatedClass;
        //  T4::Ljava/io/Serializable;
        //  T5::Ljava/io/Serializable;:Ljava/lang/Comparable;
        //  T6:Lorg/jd/core/v1/test/resources/java/AnnotatedClass;:Ljava/io/Serializable;:Ljava/lang/Comparable<Lorg/jd/core/v1/test/resources/java/GenericClass;>;
        //  T7::Ljava/util/Map<**>;
        //  T8::Ljava/util/Map<+Ljava/lang/Number;-Ljava/io/Serializable;>;
        //  T9:TT8;
        assertNotNull(typeTypes.typeParameters);
        typeTypes.typeParameters.accept(visitor);

        String source = visitor.toString();
        String expected =
                "T1, " +
                        "T2, " +
                        "T3 extends org.jd.core.test.AnnotatedClass, " +
                        "T4 extends java.io.Serializable, " +
                        "T5 extends java.io.Serializable & java.lang.Comparable, " +
                        "T6 extends org.jd.core.test.AnnotatedClass & java.io.Serializable & java.lang.Comparable<org.jd.core.test.GenericClass>, " +
                        "T7 extends java.util.Map<?, ?>, " +
                        "T8 extends java.util.Map<? extends java.lang.Number, ? super java.io.Serializable>, " +
                        "T9 extends T8";

        Assert.assertEquals(expected, source);

        // Check super type
        BaseType superType = typeTypes.superType;
        assertNotNull(superType);
        visitor.reset();
        superType.accept(visitor);
        source = visitor.toString();

        Assert.assertEquals("java.util.ArrayList<T7>", source);

        // Check interfaces
        assertNotNull(typeTypes.interfaces);
        visitor.reset();
        typeTypes.interfaces.accept(visitor);
        source = visitor.toString();

        Assert.assertEquals("java.io.Serializable, java.lang.Comparable<T1>", source);

        // Check field 'list1'
        //  public List<List<? extends Generic>> list1
        BaseType type = typeMaker.parseFieldSignature(classFile, classFile.getFields()[0]);
        visitor.reset();
        type.accept(visitor);
        source = visitor.toString();

        Assert.assertEquals("java.util.List<java.util.List<? extends org.jd.core.test.GenericClass>>", source);

        // Check method 'copy2'
        //  public <T, S extends T> List<? extends Number> copy2(List<? super T> dest, List<S> src) throws InvalidParameterException, ClassCastException
        TypeMaker.MethodTypes methodTypes = typeMaker.parseMethodSignature(classFile, classFile.getMethods()[3]);

        // Check type parameterTypes
        assertNotNull(methodTypes.typeParameters);
        visitor.reset();
        methodTypes.typeParameters.accept(visitor);
        source = visitor.toString();

        Assert.assertEquals("T, S extends T", source);

        // Check parameterTypes
        assertNotNull(methodTypes.parameterTypes);
        assertEquals(2, methodTypes.parameterTypes.size());

        type = methodTypes.parameterTypes.getFirst();
        visitor.reset();
        type.accept(visitor);
        source = visitor.toString();

        Assert.assertEquals("java.util.List<? super T>", source);

        // Check return type
        assertNotNull(methodTypes.returnedType);

        BaseType returnedType = methodTypes.returnedType;
        visitor.reset();
        returnedType.accept(visitor);
        source = visitor.toString();

        Assert.assertEquals("java.util.List<? extends java.lang.Number>", source);

        // Check exceptions
        assertNotNull(methodTypes.exceptionTypes);

        visitor.reset();
        methodTypes.exceptionTypes.accept(visitor);
        source = visitor.toString();

        Assert.assertEquals("java.security.InvalidParameterException, java.lang.ClassCastException", source);

        // Check method 'print'
        //  public <T1, T2 extends Exception> List<? extends Number> print(List<? super T1> list) throws InvalidParameterException, T2
        methodTypes = typeMaker.parseMethodSignature(classFile, classFile.getMethods()[4]);

        // Check type parameterTypes
        assertNotNull(methodTypes.typeParameters);
        visitor.reset();
        methodTypes.typeParameters.accept(visitor);
        source = visitor.toString();

        Assert.assertEquals("T1, T2 extends java.lang.Exception", source);

        // Check parameterTypes
        assertNotNull(methodTypes.parameterTypes);
        assertEquals(1, methodTypes.parameterTypes.size());

        type = methodTypes.parameterTypes.getFirst();
        visitor.reset();
        type.accept(visitor);
        source = visitor.toString();

        Assert.assertEquals("java.util.List<? super T1>", source);

        // Check return type
        assertNotNull(methodTypes.returnedType);

        returnedType = methodTypes.returnedType;
        visitor.reset();
        returnedType.accept(visitor);
        source = visitor.toString();

        Assert.assertEquals("java.util.List<? extends java.lang.Number>", source);

        // Check exceptions
        assertNotNull(methodTypes.exceptionTypes);

        visitor.reset();
        methodTypes.exceptionTypes.accept(visitor);
        source = visitor.toString();

        Assert.assertEquals("T2, java.security.InvalidParameterException", source);
    }

    @Test
    public void testParseReturnedVoid() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.7.0.zip");
        ZipLoader loader = new ZipLoader(is);
        TypeMaker typeMaker = new TypeMaker(loader);

        Assert.assertEquals(typeMaker.makeMethodTypes("()V").returnedType, PrimitiveType.TYPE_VOID);
    }

    @Test
    public void testParseReturnedPrimitiveType() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.7.0.zip");
        ZipLoader loader = new ZipLoader(is);
        TypeMaker typeMaker = new TypeMaker(loader);

        Assert.assertEquals(typeMaker.makeMethodTypes("()Z").returnedType, PrimitiveType.TYPE_BOOLEAN);
    }

    @Test
    public void testParseReturnedStringType() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.7.0.zip");
        ZipLoader loader = new ZipLoader(is);
        TypeMaker typeMaker = new TypeMaker(loader);

        Assert.assertEquals(typeMaker.makeMethodTypes("()Ljava/lang/String;").returnedType, ObjectType.TYPE_STRING);
    }

    @Test
    public void testGenericInnerClass() throws Exception {
        ClassPathLoader loader = new ClassPathLoader();
        TypeMaker typeMaker = new TypeMaker(loader);

        Type type = typeMaker.makeFromSignature("Lorg/apache/commons/collections4/multimap/AbstractMultiValuedMap<TK;TV;>.AsMap.AsMapEntrySetIterator;");

        Assert.assertEquals(type.getDescriptor(), "Lorg/apache/commons/collections4/multimap/AbstractMultiValuedMap$AsMap$AsMapEntrySetIterator;");
        Assert.assertEquals(type.getDescriptor(), "Lorg/apache/commons/collections4/multimap/AbstractMultiValuedMap$AsMap$AsMapEntrySetIterator;");

        ObjectType ot = (ObjectType)type;

        Assert.assertEquals(ot.getInternalName(), "org/apache/commons/collections4/multimap/AbstractMultiValuedMap$AsMap$AsMapEntrySetIterator");
        Assert.assertEquals(ot.getQualifiedName(), "org.apache.commons.collections4.multimap.AbstractMultiValuedMap.AsMap.AsMapEntrySetIterator");
        Assert.assertEquals(ot.getName(), "AsMapEntrySetIterator");
        Assert.assertNull(ot.getTypeArguments());

        ot = ((InnerObjectType)ot).getOuterType();

        Assert.assertEquals(ot.getInternalName(), "org/apache/commons/collections4/multimap/AbstractMultiValuedMap$AsMap");
        Assert.assertEquals(ot.getQualifiedName(), "org.apache.commons.collections4.multimap.AbstractMultiValuedMap.AsMap");
        Assert.assertEquals(ot.getName(), "AsMap");
        Assert.assertNull(ot.getTypeArguments());

        ot = ((InnerObjectType)ot).getOuterType();

        Assert.assertEquals(ot.getInternalName(), "org/apache/commons/collections4/multimap/AbstractMultiValuedMap");
        Assert.assertEquals(ot.getQualifiedName(), "org.apache.commons.collections4.multimap.AbstractMultiValuedMap");
        Assert.assertEquals(ot.getName(), "AbstractMultiValuedMap");
        Assert.assertNotNull(ot.getTypeArguments());

        TypeArguments typeArguments = (TypeArguments)ot.getTypeArguments();

        Assert.assertEquals(typeArguments.size(), 2);
        Assert.assertEquals(typeArguments.getFirst().toString(), "GenericType{K}");
        Assert.assertEquals(typeArguments.getLast().toString(), "GenericType{V}");
    }
}