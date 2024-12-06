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

public abstract class AbstractTypeParametersToTypeArgumentsBinder {
    public abstract ClassFileConstructorInvocationExpression newConstructorInvocationExpression(
            int lineNumber, ObjectType objectType, String descriptor,
            TypeMaker.MethodTypes methodTypes, BaseExpression parameters);

    public abstract ClassFileSuperConstructorInvocationExpression newSuperConstructorInvocationExpression(
            int lineNumber, ObjectType objectType, String descriptor,
            TypeMaker.MethodTypes methodTypes, BaseExpression parameters);

    public abstract ClassFileMethodInvocationExpression newMethodInvocationExpression(
            int lineNumber, Expression expression, ObjectType objectType, String name, String descriptor,
            TypeMaker.MethodTypes methodTypes, BaseExpression parameters);

    public abstract FieldReferenceExpression newFieldReferenceExpression(
            int lineNumber, Type type, Expression expression, ObjectType objectType, String name, String descriptor);

    public abstract void bindParameterTypesWithArgumentTypes(Type type, Expression expression);

    public void updateNewExpression(ClassFileNewExpression ne, String descriptor, TypeMaker.MethodTypes methodTypes, BaseExpression parameters) {
        ne.set(descriptor, clone(methodTypes.parameterTypes), parameters);
    }

    protected static BaseType clone(BaseType parameterTypes) {
        if ((parameterTypes != null) && parameterTypes.isList()) {
            switch (parameterTypes.size()) {
                case 0: parameterTypes = null; break;
                case 1: parameterTypes = parameterTypes.getFirst(); break;
                default: parameterTypes = new Types(parameterTypes.getList()); break;
            }
        }

        return parameterTypes;
    }
}
