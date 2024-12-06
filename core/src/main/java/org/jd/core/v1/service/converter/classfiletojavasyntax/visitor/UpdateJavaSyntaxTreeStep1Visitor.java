/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.visitor;

import org.jd.core.v1.model.javasyntax.AbstractJavaSyntaxVisitor;
import org.jd.core.v1.model.javasyntax.declaration.*;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.declaration.ClassFileBodyDeclaration;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.TypeMaker;

public class UpdateJavaSyntaxTreeStep1Visitor extends AbstractJavaSyntaxVisitor {
    protected CreateInstructionsVisitor createInstructionsVisitor;
    protected InitInnerClassVisitor initInnerClassStep1Visitor;

    public UpdateJavaSyntaxTreeStep1Visitor(TypeMaker typeMaker) {
        createInstructionsVisitor = new CreateInstructionsVisitor(typeMaker);
        initInnerClassStep1Visitor = new InitInnerClassVisitor();
    }

    @Override
    public void visit(BodyDeclaration declaration) {
        ClassFileBodyDeclaration bodyDeclaration = (ClassFileBodyDeclaration)declaration;

        // Visit inner types
        if (bodyDeclaration.getInnerTypeDeclarations() != null) {
            acceptListDeclaration(bodyDeclaration.getInnerTypeDeclarations());
        }

        // Visit declaration
        createInstructionsVisitor.visit(declaration);
        initInnerClassStep1Visitor.visit(declaration);
    }
}
