/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.type;

import java.util.Map;

public class PrimitiveType implements Type {
    public static final int FLAG_BOOLEAN = 1;
    public static final int FLAG_CHAR    = 2;
    public static final int FLAG_FLOAT   = 4;
    public static final int FLAG_DOUBLE  = 8;
    public static final int FLAG_BYTE    = 16;
    public static final int FLAG_SHORT   = 32;
    public static final int FLAG_INT     = 64;
    public static final int FLAG_LONG    = 128;
    public static final int FLAG_VOID    = 256;

    //                                                                                                          type,                                                 type = ...,                                           ... = type
    public static final PrimitiveType TYPE_BOOLEAN                = new PrimitiveType("boolean",                FLAG_BOOLEAN,                                         FLAG_BOOLEAN,                                         FLAG_BOOLEAN);
    public static final PrimitiveType TYPE_BYTE                   = new PrimitiveType("byte",                   FLAG_BYTE,                                            FLAG_BYTE,                                            FLAG_BYTE|FLAG_INT|FLAG_SHORT);
    public static final PrimitiveType TYPE_CHAR                   = new PrimitiveType("char",                   FLAG_CHAR,                                            FLAG_CHAR,                                            FLAG_CHAR|FLAG_INT);
    public static final PrimitiveType TYPE_DOUBLE                 = new PrimitiveType("double",                 FLAG_DOUBLE,                                          FLAG_DOUBLE,                                          FLAG_DOUBLE);
    public static final PrimitiveType TYPE_FLOAT                  = new PrimitiveType("float",                  FLAG_FLOAT,                                           FLAG_FLOAT,                                           FLAG_FLOAT);
    public static final PrimitiveType TYPE_INT                    = new PrimitiveType("int",                    FLAG_INT,                                             FLAG_INT|FLAG_BYTE|FLAG_CHAR|FLAG_SHORT,              FLAG_INT);
    public static final PrimitiveType TYPE_LONG                   = new PrimitiveType("long",                   FLAG_LONG,                                            FLAG_LONG,                                            FLAG_LONG);
    public static final PrimitiveType TYPE_SHORT                  = new PrimitiveType("short",                  FLAG_SHORT,                                           FLAG_SHORT|FLAG_BYTE,                                 FLAG_SHORT|FLAG_INT);
    public static final PrimitiveType TYPE_VOID                   = new PrimitiveType("void",                   FLAG_VOID,                                            FLAG_VOID,                                            FLAG_VOID);

    public static final PrimitiveType MAYBE_CHAR_TYPE             = new PrimitiveType("maybe_char",             FLAG_CHAR|FLAG_INT,                                   FLAG_CHAR|FLAG_INT,                                   FLAG_CHAR|FLAG_INT);                                   //  32768 .. 65535
    public static final PrimitiveType MAYBE_SHORT_TYPE            = new PrimitiveType("maybe_short",            FLAG_CHAR|FLAG_SHORT|FLAG_INT,                        FLAG_CHAR|FLAG_SHORT|FLAG_INT,                        FLAG_CHAR|FLAG_SHORT|FLAG_INT);                        //    128 .. 32767
    public static final PrimitiveType MAYBE_BYTE_TYPE             = new PrimitiveType("maybe_byte",             FLAG_BYTE|FLAG_CHAR|FLAG_SHORT|FLAG_INT,              FLAG_BYTE|FLAG_CHAR|FLAG_SHORT|FLAG_INT,              FLAG_BYTE|FLAG_CHAR|FLAG_SHORT|FLAG_INT);              //      2 .. 127
    public static final PrimitiveType MAYBE_BOOLEAN_TYPE          = new PrimitiveType("maybe_boolean",          FLAG_BOOLEAN|FLAG_BYTE|FLAG_CHAR|FLAG_SHORT|FLAG_INT, FLAG_BOOLEAN|FLAG_BYTE|FLAG_CHAR|FLAG_SHORT|FLAG_INT, FLAG_BOOLEAN|FLAG_BYTE|FLAG_CHAR|FLAG_SHORT|FLAG_INT); //      0 .. 1
    public static final PrimitiveType MAYBE_NEGATIVE_BYTE_TYPE    = new PrimitiveType("maybe_negative_byte",    FLAG_BYTE|FLAG_SHORT|FLAG_INT,                        FLAG_BYTE|FLAG_SHORT|FLAG_INT,                        FLAG_BYTE|FLAG_SHORT|FLAG_INT);                        //   -128 .. -1
    public static final PrimitiveType MAYBE_NEGATIVE_SHORT_TYPE   = new PrimitiveType("maybe_negative_short",   FLAG_SHORT|FLAG_INT,                                  FLAG_SHORT|FLAG_INT,                                  FLAG_SHORT|FLAG_INT);                                  // -32768 .. -129
    public static final PrimitiveType MAYBE_INT_TYPE              = new PrimitiveType("maybe_int",              FLAG_INT,                                             FLAG_INT,                                             FLAG_INT);                                             // Otherwise
    public static final PrimitiveType MAYBE_NEGATIVE_BOOLEAN_TYPE = new PrimitiveType("maybe_negative_boolean", FLAG_BOOLEAN|FLAG_BYTE|FLAG_SHORT|FLAG_INT,           FLAG_BOOLEAN|FLAG_BYTE|FLAG_SHORT|FLAG_INT,           FLAG_BOOLEAN|FLAG_BYTE|FLAG_SHORT|FLAG_INT);           // Boolean or negative

    protected static final PrimitiveType[] descriptorToType = new PrimitiveType['Z' - 'B' + 1];

    static {
        descriptorToType['B' - 'B'] = TYPE_BYTE;
        descriptorToType['C' - 'B'] = TYPE_CHAR;
        descriptorToType['D' - 'B'] = TYPE_DOUBLE;
        descriptorToType['F' - 'B'] = TYPE_FLOAT;
        descriptorToType['I' - 'B'] = TYPE_INT;
        descriptorToType['J' - 'B'] = TYPE_LONG;
        descriptorToType['S' - 'B'] = TYPE_SHORT;
        descriptorToType['V' - 'B'] = TYPE_VOID;
        descriptorToType['Z' - 'B'] = TYPE_BOOLEAN;
    }

    protected final String name;
    protected final int flags;
    protected final int leftFlags;
    protected final int rightFlags;
    protected final String descriptor;

    protected PrimitiveType(PrimitiveType primitiveType) {
        this(primitiveType.name, primitiveType.flags, primitiveType.leftFlags, primitiveType.rightFlags);
    }

    protected PrimitiveType(String name, int flags, int leftFlags, int rightFlags) {
        this.name = name;
        this.flags = flags;
        this.leftFlags = leftFlags;
        this.rightFlags = rightFlags;

        StringBuilder sb = new StringBuilder();

        if ((flags & FLAG_DOUBLE) != 0)
            sb.append('D');
        else if ((flags & FLAG_FLOAT) != 0)
            sb.append('F');
        else if ((flags & FLAG_LONG) != 0)
            sb.append('J');
        else if ((flags & FLAG_BOOLEAN) != 0)
            sb.append('Z');
        else if ((flags & FLAG_BYTE) != 0)
            sb.append('B');
        else if ((flags & FLAG_CHAR) != 0)
            sb.append('C');
        else if ((flags & FLAG_SHORT) != 0)
            sb.append('S');
        else
            sb.append('I');

        this.descriptor = sb.toString();
    }

    public static PrimitiveType getPrimitiveType(char primitiveDescriptor) {
        return descriptorToType[primitiveDescriptor - 'B'];
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescriptor() {
        return descriptor;
    }

    @Override
    public int getDimension() {
        return 0;
    }

    public int getFlags() {
        return flags;
    }

    public int getLeftFlags() {
        return leftFlags;
    }

    public int getRightFlags() {
        return rightFlags;
    }

    @Override
    public Type createType(int dimension) {
        assert dimension >= 0 : "PrimitiveType.createType(dim) : create type with negative dimension";
        if (dimension == 0) {
            return this;
        } else {
            return new ObjectType(descriptor, dimension);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PrimitiveType that = (PrimitiveType) o;

        if (flags != that.flags) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return 750039781 + flags;
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void accept(TypeArgumentVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean isTypeArgumentAssignableFrom(Map<String, BaseType> typeBounds, BaseTypeArgument typeArgument) {
        return equals(typeArgument);
    }

    @Override
    public boolean isPrimitiveType() {
        return true;
    }

    @Override
    public boolean isPrimitiveTypeArgument() {
        return true;
    }

    @Override
    public String toString() {
        return "PrimitiveType{primitive=" + name + "}";
    }

    public int getJavaPrimitiveFlags() {
        if ((flags & FLAG_BOOLEAN) != 0)
            return FLAG_BOOLEAN;
        else if ((flags & FLAG_INT) != 0)
            return FLAG_INT;
        else if ((flags & FLAG_CHAR) != 0)
            return FLAG_CHAR;
        else if ((flags & FLAG_SHORT) != 0)
            return FLAG_SHORT;
        else if ((flags & FLAG_BYTE) != 0)
            return FLAG_BYTE;

        return flags;
    }
}
