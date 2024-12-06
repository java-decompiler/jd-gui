/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.visitor;

import org.jd.core.v1.model.classfile.ConstantPool;
import org.jd.core.v1.model.classfile.Method;
import org.jd.core.v1.model.classfile.attribute.AttributeCode;
import org.jd.core.v1.model.classfile.constant.ConstantMemberRef;
import org.jd.core.v1.model.classfile.constant.ConstantNameAndType;
import org.jd.core.v1.model.javasyntax.AbstractJavaSyntaxVisitor;
import org.jd.core.v1.model.javasyntax.declaration.*;
import org.jd.core.v1.model.javasyntax.type.*;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.declaration.ClassFileBodyDeclaration;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.declaration.ClassFileMethodDeclaration;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.TypeMaker;

public class UpdateBridgeMethodTypeVisitor extends AbstractJavaSyntaxVisitor {
    protected TypeMaker typeMaker;

    public UpdateBridgeMethodTypeVisitor(TypeMaker typeMaker) {
        this.typeMaker = typeMaker;
    }

    @Override
    public void visit(BodyDeclaration declaration) {
        ClassFileBodyDeclaration bodyDeclaration = (ClassFileBodyDeclaration)declaration;

        safeAcceptListDeclaration(bodyDeclaration.getMethodDeclarations());
        safeAcceptListDeclaration(bodyDeclaration.getInnerTypeDeclarations());
    }

    @Override
    public void visit(MethodDeclaration declaration) {
        if (declaration.isStatic() && declaration.getReturnedType().isObjectType() && declaration.getName().startsWith("access$")) {
            TypeMaker.TypeTypes typeTypes = typeMaker.makeTypeTypes(declaration.getReturnedType().getInternalName());

            if ((typeTypes != null) && (typeTypes.typeParameters != null)) {
                ClassFileMethodDeclaration cfmd = (ClassFileMethodDeclaration) declaration;
                Method method = cfmd.getMethod();
                byte[] code = method.<AttributeCode>getAttribute("Code").getCode();
                int offset = 0;
                int opcode = code[offset] & 255;

                while (((21 <= opcode) && (opcode <= 45)) || // ILOAD, LLOAD, FLOAD, DLOAD, ..., ILOAD_0 ... ILOAD_3, ..., ALOAD_1, ..., ALOAD_3
                       ((89 <= opcode) && (opcode <= 95))) { // DUP, ..., DUP2_X2, SWAP
                    opcode = code[++offset] & 255;
                }

                switch (opcode) {
                    case 178: case 179: case 180: case 181: // GETSTATIC, PUTSTATIC, GETFIELD, PUTFIELD
                        int index = ((code[++offset] & 255) << 8) | (code[++offset] & 255);
                        ConstantPool constants = method.getConstants();
                        ConstantMemberRef constantMemberRef = constants.getConstant(index);
                        String typeName = constants.getConstantTypeName(constantMemberRef.getClassIndex());
                        ConstantNameAndType constantNameAndType = constants.getConstant(constantMemberRef.getNameAndTypeIndex());
                        String name = constants.getConstantUtf8(constantNameAndType.getNameIndex());
                        String descriptor = constants.getConstantUtf8(constantNameAndType.getDescriptorIndex());
                        Type type = typeMaker.makeFieldType(typeName, name, descriptor);

                        // Update returned generic type of bridge method
                        typeMaker.setMethodReturnedType(typeName, cfmd.getName(), cfmd.getDescriptor(), type);
                        break;
                    case 182: case 183: case 184: case 185: // INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC, INVOKEINTERFACE
                        index = ((code[++offset] & 255) << 8) | (code[++offset] & 255);
                        constants = method.getConstants();
                        constantMemberRef = constants.getConstant(index);
                        typeName = constants.getConstantTypeName(constantMemberRef.getClassIndex());
                        constantNameAndType = constants.getConstant(constantMemberRef.getNameAndTypeIndex());
                        name = constants.getConstantUtf8(constantNameAndType.getNameIndex());
                        descriptor = constants.getConstantUtf8(constantNameAndType.getDescriptorIndex());
                        TypeMaker.MethodTypes methodTypes = typeMaker.makeMethodTypes(typeName, name, descriptor);

                        // Update returned generic type of bridge method
                        typeMaker.setMethodReturnedType(typeName, cfmd.getName(), cfmd.getDescriptor(), methodTypes.returnedType);
                        break;
                }
            }
        }
    }

    @Override public void visit(ConstructorDeclaration declaration) {}
    @Override public void visit(StaticInitializerDeclaration declaration) {}

    @Override
    public void visit(ClassDeclaration declaration) {
        safeAccept(declaration.getBodyDeclaration());
    }

    @Override
    public void visit(InterfaceDeclaration declaration) {
        safeAccept(declaration.getBodyDeclaration());
    }

    @Override public void visit(AnnotationDeclaration declaration) {}
    @Override public void visit(EnumDeclaration declaration) {}
}
