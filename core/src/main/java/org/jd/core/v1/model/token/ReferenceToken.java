/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.token;

public class ReferenceToken extends DeclarationToken {

    protected String ownerInternalName;

    /**
     * @param type @see org.jd.core.v1.model.token.DeclarationToken
     * @param internalTypeName
     * @param name
     * @param descriptor
     * @param ownerInternalName
     */
    public ReferenceToken(int type, String internalTypeName, String name, String descriptor, String ownerInternalName) {
        super(type, internalTypeName, name, descriptor);
        this.ownerInternalName = ownerInternalName;
    }

    public String getOwnerInternalName() {
        return ownerInternalName;
    }

    @Override
    public String toString() {
        return "ReferenceToken{'" + name + "'}";
    }

    @Override
    public void accept(TokenVisitor visitor) {
        visitor.visit(this);
    }
}
