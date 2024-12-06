/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1;

import org.jd.core.v1.model.javasyntax.declaration.FieldDeclarator;
import org.jd.core.v1.model.javasyntax.declaration.FieldDeclarators;
import org.jd.core.v1.model.javasyntax.type.PrimitiveType;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.declaration.ClassFileFieldDeclaration;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.AggregateFieldsUtil;
import org.jd.core.v1.util.DefaultList;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class AggregateFieldsUtilTest {

    @Test
    public void test1() {
        DefaultList<ClassFileFieldDeclaration> fields = new DefaultList<>();

        fields.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("b"), 10));
        fields.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("c"), 10));

        DefaultList<ClassFileFieldDeclaration> expected = new DefaultList<>();

        expected.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarators(
                new FieldDeclarator("b"),
                new FieldDeclarator("c")
        ), 10));

        AggregateFieldsUtil.aggregate(fields);

        Assert.assertEquals(expected, fields);
    }

    @Test
    public void test2() {
        DefaultList<ClassFileFieldDeclaration> fields = new DefaultList<>();

        fields.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("a")));
        fields.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("b"), 10));
        fields.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("c"), 10));

        DefaultList<ClassFileFieldDeclaration> expected = new DefaultList<>();

        expected.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("a")));
        expected.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarators(
                new FieldDeclarator("b"),
                new FieldDeclarator("c")
        ), 10));

        AggregateFieldsUtil.aggregate(fields);

        Assert.assertEquals(expected, fields);
    }

    @Test
    public void test3() {
        DefaultList<ClassFileFieldDeclaration> fields = new DefaultList<>();

        fields.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("a")));
        fields.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("b"), 10));
        fields.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("c"), 10));
        fields.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("d")));

        DefaultList<ClassFileFieldDeclaration> expected = new DefaultList<>();

        expected.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("a")));
        expected.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarators(
                new FieldDeclarator("b"),
                new FieldDeclarator("c")
        ), 10));
        expected.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("d")));

        AggregateFieldsUtil.aggregate(fields);

        Assert.assertEquals(expected, fields);
    }

    @Test
    public void test4() {
        DefaultList<ClassFileFieldDeclaration> fields = new DefaultList<>();

        fields.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("a")));
        fields.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("b"), 10));
        fields.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("c")));
        fields.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("d"), 10));
        fields.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("e")));
        fields.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("f"), 10));
        fields.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("g")));
        fields.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("h"), 15));
        fields.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("i")));

        DefaultList<ClassFileFieldDeclaration> expected = new DefaultList<>();

        expected.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("a")));
        expected.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarators(
                new FieldDeclarator("b"),
                new FieldDeclarator("c"),
                new FieldDeclarator("d"),
                new FieldDeclarator("e"),
                new FieldDeclarator("f")
        ), 10));
        expected.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("g")));
        expected.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("h"), 15));
        expected.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("i")));

        AggregateFieldsUtil.aggregate(fields);

        Assert.assertEquals(expected, fields);
    }

    @Test
    public void test5() {
        DefaultList<ClassFileFieldDeclaration> fields = new DefaultList<>();

        fields.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("a")));
        fields.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("b"), 10));
        fields.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("c")));
        fields.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_LONG, new FieldDeclarator("d"), 10));
        fields.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("e")));
        fields.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("f"), 10));
        fields.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("g")));
        fields.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("h"), 15));
        fields.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("i")));

        DefaultList<ClassFileFieldDeclaration> expected = new DefaultList<>();

        expected.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("a")));
        expected.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("b"), 10));
        expected.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("c")));
        expected.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_LONG, new FieldDeclarator("d"), 10));
        expected.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("e")));
        expected.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("f"), 10));
        expected.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("g")));
        expected.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("h"), 15));
        expected.add(new ClassFileFieldDeclaration(0, PrimitiveType.TYPE_INT, new FieldDeclarator("i")));

        AggregateFieldsUtil.aggregate(fields);

        Assert.assertEquals(expected, fields);
    }
}
