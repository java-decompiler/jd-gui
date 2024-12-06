/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1;

import junit.framework.TestCase;
import org.apache.commons.collections4.iterators.AbstractUntypedIteratorDecorator;
import org.jd.core.v1.loader.ClassPathLoader;
import org.jd.core.v1.loader.ZipLoader;
import org.jd.core.v1.model.javasyntax.type.ObjectType;
import org.jd.core.v1.model.javasyntax.type.TypeArguments;
import org.jd.core.v1.model.javasyntax.type.WildcardExtendsTypeArgument;
import org.jd.core.v1.model.javasyntax.type.WildcardSuperTypeArgument;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.TypeMaker;
import org.junit.Test;

import java.io.InputStream;
import java.util.*;

public class TypeMakerTest extends TestCase {
    protected TypeMaker typeMaker = new TypeMaker(new ClassPathLoader());

    protected ObjectType otAbstractUntypedIteratorDecorator = makeObjectType(AbstractUntypedIteratorDecorator.class);
    protected ObjectType otArrayList = makeObjectType(ArrayList.class);
    protected ObjectType otInteger = makeObjectType(Integer.class);
    protected ObjectType otIterator = makeObjectType(Iterator.class);
    protected ObjectType otList = makeObjectType(List.class);
    protected ObjectType otNumber = makeObjectType(Number.class);
    protected ObjectType otPrimitiveIterator = makeObjectType(PrimitiveIterator.class);

    protected ObjectType makeObjectType(Class<?> clazz) {
        return typeMaker.makeFromInternalTypeName(clazz.getName().replace('.', '/'));
    }

    @Test
    public void testOuterClass() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.7.0.zip");
        TypeMaker typeMaker = new TypeMaker(new ZipLoader(is));
        ObjectType ot = typeMaker.makeFromInternalTypeName("org/jd/core/test/OuterClass");

        assertEquals("org/jd/core/test/OuterClass", ot.getInternalName());
        assertEquals("org.jd.core.test.OuterClass", ot.getQualifiedName());
        assertEquals("OuterClass", ot.getName());
    }

    @Test
    public void testOuterClass$InnerClass() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.7.0.zip");
        TypeMaker typeMaker = new TypeMaker(new ZipLoader(is));
        ObjectType ot = typeMaker.makeFromInternalTypeName("org/jd/core/test/OuterClass$InnerClass");

        assertEquals("org/jd/core/test/OuterClass$InnerClass", ot.getInternalName());
        assertEquals("org.jd.core.test.OuterClass.InnerClass", ot.getQualifiedName());
        assertEquals("InnerClass", ot.getName());
    }

    @Test
    public void testOuterClass$StaticInnerClass() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.7.0.zip");
        TypeMaker typeMaker = new TypeMaker(new ZipLoader(is));
        ObjectType ot = typeMaker.makeFromInternalTypeName("org/jd/core/test/OuterClass$StaticInnerClass");

        assertEquals("org/jd/core/test/OuterClass$StaticInnerClass", ot.getInternalName());
        assertEquals("org.jd.core.test.OuterClass.StaticInnerClass", ot.getQualifiedName());
        assertEquals("StaticInnerClass", ot.getName());
    }

    @Test
    public void testOuterClass$1() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.7.0.zip");
        TypeMaker typeMaker = new TypeMaker(new ZipLoader(is));
        ObjectType ot = typeMaker.makeFromInternalTypeName("org/jd/core/test/OuterClass$1");

        assertEquals("org/jd/core/test/OuterClass$1", ot.getInternalName());
        assertNull(ot.getQualifiedName());
        assertNull(ot.getName());
    }

    @Test
    public void testOuterClass$1LocalClass() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.7.0.zip");
        TypeMaker typeMaker = new TypeMaker(new ZipLoader(is));
        ObjectType ot = typeMaker.makeFromInternalTypeName("org/jd/core/test/OuterClass$1LocalClass");

        assertEquals("org/jd/core/test/OuterClass$1LocalClass", ot.getInternalName());
        assertNull(ot.getQualifiedName());
        assertEquals("LocalClass", ot.getName());
    }

    @Test
    public void testThread() throws Exception {
        ObjectType ot = typeMaker.makeFromInternalTypeName("java/lang/Thread");

        assertEquals("java/lang/Thread", ot.getInternalName());
        assertEquals("java.lang.Thread", ot.getQualifiedName());
        assertEquals("Thread", ot.getName());
    }

    @Test
    public void testThreadState() throws Exception {
        ObjectType ot = typeMaker.makeFromInternalTypeName("java/lang/Thread$State");

        assertEquals("java/lang/Thread$State", ot.getInternalName());
        assertEquals("java.lang.Thread.State", ot.getQualifiedName());
        assertEquals("State", ot.getName());
    }

    @Test
    public void testUnknownClass() throws Exception {
        ObjectType ot = typeMaker.makeFromInternalTypeName("org/unknown/Class");

        assertEquals("org/unknown/Class", ot.getInternalName());
        assertEquals("org.unknown.Class", ot.getQualifiedName());
        assertEquals("Class", ot.getName());
    }

    @Test
    public void testUnknownInnerClass() throws Exception {
        ObjectType ot = typeMaker.makeFromInternalTypeName("org/unknown/Class$InnerClass");

        assertEquals("org/unknown/Class$InnerClass", ot.getInternalName());
        assertEquals("org.unknown.Class.InnerClass", ot.getQualifiedName());
        assertEquals("InnerClass", ot.getName());
    }

    @Test
    public void testListIsAssignableFromArrayList() throws Exception {
        ObjectType parent = typeMaker.makeFromInternalTypeName("java/util/List");
        ObjectType child = typeMaker.makeFromInternalTypeName("java/util/ArrayList");

        assertNotNull(parent);
        assertNotNull(child);
        assertTrue(typeMaker.isAssignable(Collections.emptyMap(), parent, child));
    }

    @Test
    public void testClassIsAssignableFromObject() throws Exception {
        ObjectType parent = typeMaker.makeFromInternalTypeName("java/lang/Class");
        ObjectType child = typeMaker.makeFromInternalTypeName("java/lang/Object");

        assertNotNull(parent);
        assertNotNull(child);
        assertFalse(typeMaker.isAssignable(Collections.emptyMap(), parent, child));
    }

    @Test
    public void testObjectIsAssignableFromSafeNumberComparator() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.7.0.zip");
        TypeMaker typeMaker = new TypeMaker(new ZipLoader(is));
        ObjectType parent = typeMaker.makeFromInternalTypeName("java/lang/Object");
        ObjectType child = typeMaker.makeFromInternalTypeName("org/jd/core/test/OuterClass$SafeNumberComparator");

        assertNotNull(parent);
        assertNotNull(child);
        assertTrue(typeMaker.isAssignable(Collections.emptyMap(), parent, child));
    }

    @Test
    public void testComparatorIsAssignableFromSafeNumberComparator() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.7.0.zip");
        TypeMaker typeMaker = new TypeMaker(new ZipLoader(is));
        ObjectType parent = typeMaker.makeFromInternalTypeName("java/util/Comparator");
        ObjectType child = typeMaker.makeFromInternalTypeName("org/jd/core/test/OuterClass$SafeNumberComparator");

        assertNotNull(parent);
        assertNotNull(child);
        assertTrue(typeMaker.isAssignable(Collections.emptyMap(), parent, child));
    }

    @Test
    public void testNumberComparatorIsAssignableFromSafeNumberComparator() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.7.0.zip");
        TypeMaker typeMaker = new TypeMaker(new ZipLoader(is));
        ObjectType parent = typeMaker.makeFromInternalTypeName("org/jd/core/test/OuterClass$NumberComparator");
        ObjectType child = typeMaker.makeFromInternalTypeName("org/jd/core/test/OuterClass$SafeNumberComparator");

        assertNotNull(parent);
        assertNotNull(child);
        assertTrue(typeMaker.isAssignable(Collections.emptyMap(), parent, child));
    }

    @Test
    public void testOuterClassIsAssignableFromSimpleClass() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/zip/data-java-jdk-1.7.0.zip");
        TypeMaker typeMaker = new TypeMaker(new ZipLoader(is));
        ObjectType parent = typeMaker.makeFromInternalTypeName("org/jd/core/test/OuterClass");
        ObjectType child = typeMaker.makeFromInternalTypeName("org/jd/core/test/SimpleClass");

        assertNotNull(parent);
        assertNotNull(child);
        assertFalse(typeMaker.isAssignable(Collections.emptyMap(), parent, child));
    }
    @Test
    public void testListAssignment() throws Exception {
        List list1 = null;
        List list2 = null;

        ObjectType ot1 = otList;
        ObjectType ot2 = otList;

        // Valid:   list1 = list2;
        assertTrue(typeMaker.isAssignable(Collections.emptyMap(), ot1, ot2));

        // Valid:   list2 = list1;
        assertTrue(typeMaker.isAssignable(Collections.emptyMap(), ot2, ot1));
    }

    @Test
    public void testListAndArrayListAssignment() throws Exception {
        List list1 = null;
        ArrayList list2 = null;

        ObjectType ot1 = otList;
        ObjectType ot2 = otArrayList;

        // Valid:   list1 = list2;
        assertTrue(typeMaker.isAssignable(Collections.emptyMap(), ot1, ot2));

        // Invalid: list2 = list1;
        assertFalse(typeMaker.isAssignable(Collections.emptyMap(), ot2, ot1));
    }

    @Test
    public void testListNumberAndArrayListNumberAssignment() throws Exception {
        List<Number> list1 = null;
        ArrayList<Number> list2 = null;

        ObjectType ot1 = otList.createType(otNumber);
        ObjectType ot2 = otArrayList.createType(otNumber);

        // Valid:   list1 = list2;
        assertTrue(typeMaker.isAssignable(Collections.emptyMap(), ot1, ot2));

        // Invalid: list2 = list1;
        assertFalse(typeMaker.isAssignable(Collections.emptyMap(), ot2, ot1));
    }

    @Test
    public void testListNumberAndListIntegerAssignment() throws Exception {
        List<Number> list1 = null;
        List<Integer> list2 = null;

        ObjectType ot1 = otList.createType(otNumber);
        ObjectType ot2 = otList.createType(otInteger);

        // Invalid:   list1 = list2;
        assertFalse(typeMaker.isAssignable(Collections.emptyMap(), ot1, ot2));

        // Invalid: list2 = list1;
        assertFalse(typeMaker.isAssignable(Collections.emptyMap(), ot2, ot1));
    }

    @Test
    public void testListNumberAndListExtendsNumberAssignment() throws Exception {
        List<Number> list1 = null;
        List<? extends Number> list2 = null;

        ObjectType ot1 = otList.createType(otNumber);
        ObjectType ot2 = otList.createType(new WildcardExtendsTypeArgument(otNumber));

        // Invalid:   list1 = list2;
        assertFalse(typeMaker.isAssignable(Collections.emptyMap(), ot1, ot2));

        // Valid: list2 = list1;
        assertTrue(typeMaker.isAssignable(Collections.emptyMap(), ot2, ot1));
    }

    @Test
    public void testListNumberAndListSuperNumberAssignment() throws Exception {
        List<Number> list1 = null;
        List<? super Number> list2 = null;

        ObjectType ot1 = otList.createType(otNumber);
        ObjectType ot2 = otList.createType(new WildcardSuperTypeArgument(otNumber));

        // Invalid:   list1 = list2;
        assertFalse(typeMaker.isAssignable(Collections.emptyMap(), ot1, ot2));

        // Valid: list2 = list1;
        assertTrue(typeMaker.isAssignable(Collections.emptyMap(), ot2, ot1));
    }

    @Test
    public void testListNumberAndArrayListIntegerAssignment() throws Exception {
        List<Number> list1 = null;
        ArrayList<Integer> list2 = null;

        ObjectType ot1 = otList.createType(otNumber);
        ObjectType ot2 = otArrayList.createType(otInteger);

        // Invalid:   list1 = list2;
        assertFalse(typeMaker.isAssignable(Collections.emptyMap(), ot1, ot2));

        // Invalid: list2 = list1;
        assertFalse(typeMaker.isAssignable(Collections.emptyMap(), ot2, ot1));
    }

    @Test
    public void testIteratorNumberAndPrimitiveIteratorNumberAssignment() throws Exception {
        Iterator<Number> iterator1 = null;
        PrimitiveIterator<Number, List> iterator2 = null;

        TypeArguments tas = new TypeArguments();
        tas.add(otNumber);
        tas.add(otList);

        ObjectType ot1 = otIterator.createType(otNumber);
        ObjectType ot2 = otPrimitiveIterator.createType(tas);

        // Valid:   iterator1 = iterator2;
        assertTrue(typeMaker.isAssignable(Collections.emptyMap(), ot1, ot2));
    }

    @Test
    public void testIteratorNumberAndAbstractUntypedIteratorDecoratorNumberAssignment() throws Exception {
        Iterator<Number> iterator1 = null;
        AbstractUntypedIteratorDecorator<List, Number> iterator2 = null;

        TypeArguments tas = new TypeArguments();
        tas.add(otList);
        tas.add(otNumber);

        ObjectType ot1 = otIterator.createType(otNumber);
        ObjectType ot2 = otAbstractUntypedIteratorDecorator.createType(tas);

        // Valid:   iterator1 = iterator2;
        assertTrue(typeMaker.isAssignable(Collections.emptyMap(), ot1, ot2));
    }
}
