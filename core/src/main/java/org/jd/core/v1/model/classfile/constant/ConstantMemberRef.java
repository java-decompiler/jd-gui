/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.classfile.constant;

/**
 * POJO for CONSTANT_Fieldref, CONSTANT_Methodref and CONSTANT_InterfaceMethodref.
 */
public class ConstantMemberRef extends Constant {
    protected int classIndex;
    protected int nameAndTypeIndex;

    public ConstantMemberRef(int classIndex, int nameAndTypeIndex) {
        super(CONSTANT_MemberRef);
        this.classIndex = classIndex;
        this.nameAndTypeIndex = nameAndTypeIndex;
    }

    public int getClassIndex() {
        return classIndex;
    }

    public int getNameAndTypeIndex() {
        return nameAndTypeIndex;
    }
}
