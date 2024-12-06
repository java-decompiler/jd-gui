/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1;

import junit.framework.TestCase;
import org.jd.core.v1.loader.ClassPathLoader;
import org.jd.core.v1.model.javasyntax.type.*;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.TypeMaker;
import org.jd.core.v1.service.converter.classfiletojavasyntax.visitor.PopulateBindingsWithTypeArgumentVisitor;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;

import static org.jd.core.v1.model.javasyntax.type.ObjectType.TYPE_INTEGER;
import static org.jd.core.v1.model.javasyntax.type.ObjectType.TYPE_STRING;
import static org.jd.core.v1.model.javasyntax.type.PrimitiveType.TYPE_INT;

public class PopulateBindingsForStaticMethodVisitorTest extends TestCase {
    @Test
    public void test() throws Exception {
        // ArrayList<? extends String> method(Set<Integer>, char);
        WildcardExtendsTypeArgument wildcardExtendsString = new WildcardExtendsTypeArgument(TYPE_STRING);
        BaseTypeArgument returnedType = new ObjectType("java/util/List", "java.util.List", "List", wildcardExtendsString);
        BaseTypeArgument parameterTypes = new TypeArguments(Arrays.asList(
                new ObjectType("java/util/Set", "java.util.Set", "Set", TYPE_INTEGER),
                TYPE_INT
        ));

        // <I, O> List<O> method(Set<I> set, char)
        BaseTypeArgument genericReturnedType = new ObjectType("java/util/List", "java.util.List", "List", new GenericType("O"));
        BaseTypeArgument genericParameterTypes = new TypeArguments(Arrays.asList(
                new ObjectType("java/util/Set", "java.util.Set", "Set", new GenericType("I")),
                TYPE_INT
        ));

        // Create bindings
        PopulateBindingsWithTypeArgumentVisitor visitor = new PopulateBindingsWithTypeArgumentVisitor(new TypeMaker(new ClassPathLoader()));
        HashMap<String, TypeArgument> bindings = new HashMap<>();
        HashMap<String, BaseType> typeBounds = new HashMap<>();

        bindings.put("I", null);
        bindings.put("O", null);

        visitor.init(typeBounds, bindings, typeBounds, returnedType);
        genericReturnedType.accept(visitor);

        visitor.init(typeBounds, bindings, typeBounds, parameterTypes);
        genericParameterTypes.accept(visitor);

        // Result: bindings[I:Integer, O:? extends String]
        assertEquals(bindings.get("I"), TYPE_INTEGER);
        assertEquals(bindings.get("O"), wildcardExtendsString);
    }
}
