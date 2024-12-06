/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.util;

import org.jd.core.v1.model.javasyntax.expression.*;
import org.jd.core.v1.model.javasyntax.type.*;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.expression.*;

public class JavaTypeParametersToTypeArgumentsBinder extends AbstractTypeParametersToTypeArgumentsBinder {
    @Override
    public ClassFileConstructorInvocationExpression newConstructorInvocationExpression(
            int lineNumber, ObjectType objectType, String descriptor, TypeMaker.MethodTypes methodTypes, BaseExpression parameters) {
        return new ClassFileConstructorInvocationExpression(lineNumber, objectType, descriptor, clone(methodTypes.parameterTypes), parameters);
    }

    @Override
    public ClassFileSuperConstructorInvocationExpression newSuperConstructorInvocationExpression(
            int lineNumber, ObjectType objectType, String descriptor, TypeMaker.MethodTypes methodTypes, BaseExpression parameters) {
        return new ClassFileSuperConstructorInvocationExpression(lineNumber, objectType, descriptor, clone(methodTypes.parameterTypes), parameters);
    }

    @Override
    public ClassFileMethodInvocationExpression newMethodInvocationExpression(
            int lineNumber, Expression expression, ObjectType objectType, String name, String descriptor,
            TypeMaker.MethodTypes methodTypes, BaseExpression parameters) {
        return new ClassFileMethodInvocationExpression(
            lineNumber, methodTypes.typeParameters, methodTypes.returnedType, expression,
            objectType.getInternalName(), name, descriptor, clone(methodTypes.parameterTypes), parameters);
    }

    @Override
    public FieldReferenceExpression newFieldReferenceExpression(
            int lineNumber, Type type, Expression expression, ObjectType objectType, String name, String descriptor) {
        return new FieldReferenceExpression(lineNumber, type, expression, objectType.getInternalName(), name, descriptor);
    }

    @Override
    public void bindParameterTypesWithArgumentTypes(Type type, Expression expression) {}
}
