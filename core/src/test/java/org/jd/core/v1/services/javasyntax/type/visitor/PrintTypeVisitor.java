/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.services.javasyntax.type.visitor;

import org.jd.core.v1.model.javasyntax.type.*;

import java.util.List;

import static org.jd.core.v1.model.javasyntax.type.PrimitiveType.*;

public class PrintTypeVisitor implements TypeVisitor, TypeArgumentVisitor, TypeParameterVisitor {
    protected StringBuilder sb = new StringBuilder();

    public void reset() {
        sb.setLength(0);
    }

    public String toString() {
        return sb.toString();
    }

    @Override
    public void visit(TypeArguments arguments) {
        int size = arguments.size();

        if (size > 0) {
            arguments.get(0).accept(this);

            for (int i=1; i<size; i++) {
                sb.append(", ");
                arguments.get(i).accept(this);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void visit(Types types) {
        printList(types, ", ");
    }

    @Override
    public void visit(DiamondTypeArgument argument) {}

    @Override
    public void visit(WildcardExtendsTypeArgument argument) {
        sb.append("? extends ");

        BaseType type = argument.getType();

        type.accept(this);
    }

    @Override
    public void visit(PrimitiveType type) {
        switch (type.getJavaPrimitiveFlags()) {
            case FLAG_BOOLEAN: sb.append("boolean"); break;
            case FLAG_CHAR: sb.append("char");    break;
            case FLAG_FLOAT: sb.append("float");   break;
            case FLAG_DOUBLE: sb.append("double");  break;
            case FLAG_BYTE: sb.append("byte");    break;
            case FLAG_SHORT: sb.append("short");   break;
            case FLAG_INT: sb.append("int");     break;
            case FLAG_LONG: sb.append("long");    break;
            case FLAG_VOID: sb.append("void");    break;
        }

        printDimension(type.getDimension());
    }

    @Override
    public void visit(ObjectType type) {
        sb.append(type.getQualifiedName());
        printTypeArguments(type);
        printDimension(type.getDimension());
    }

    public void visit(InnerObjectType type) {
        BaseType outerType = type.getOuterType();

        outerType.accept(this);

        sb.append('.').append(type.getName());
        printTypeArguments(type);
        printDimension(type.getDimension());
    }

    protected void printTypeArguments(ObjectType type) {
        BaseTypeArgument ta = type.getTypeArguments();

        if (ta != null) {
            sb.append('<');
            ta.accept(this);
            sb.append('>');
        }
    }

    @Override
    public void visit(WildcardSuperTypeArgument argument) {
        sb.append("? super ");

        BaseType type = argument.getType();

        type.accept(this);
    }

    @Override
    public void visit(TypeParameter parameter) {
        sb.append(parameter.getIdentifier());
    }

    @Override
    public void visit(TypeParameterWithTypeBounds parameter) {
        sb.append(parameter.getIdentifier());
        sb.append(" extends ");

        BaseType types = parameter.getTypeBounds();

        if (types.isList()) {
            printList(types.getList(), " & ");
        } else {
            BaseType type = types.getFirst();

            type.accept(this);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void visit(TypeParameters parameters) {
        int size = parameters.size();

        if (size > 0) {
            parameters.get(0).accept(this);

            for (int i=1; i<size; i++) {
                sb.append(", ");
                parameters.get(i).accept(this);
            }
        }
    }

    @Override public void visit(GenericType type) {
        sb.append(type.getName());
        printDimension(type.getDimension());
    }

    @Override
    public void visit(WildcardTypeArgument argument) {
        sb.append('?');
    }

    protected <T extends TypeVisitable> void printList(List<T> visitables, String separator) {
        int size = visitables.size();

        if (size > 0) {
            visitables.get(0).accept(this);

            for (int i=1; i<size; i++) {
                sb.append(separator);
                visitables.get(i).accept(this);
            }
        }
    }

    protected void printDimension(int dimension) {
        switch (dimension) {
            case 0:
                break;
            case 1:
                sb.append("[]");
                break;
            case 2:
                sb.append("[][]");
                break;
            default:
                sb.append(new String(new char[dimension]).replaceAll("\0", "[]"));
        }
    }
}
