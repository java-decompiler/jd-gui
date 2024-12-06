/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.declaration;

public abstract class AbstractNopDeclarationVisitor implements DeclarationVisitor {
    @Override public void visit(AnnotationDeclaration declaration) {}
    @Override public void visit(ArrayVariableInitializer declaration) {}
    @Override public void visit(BodyDeclaration declaration) {}
    @Override public void visit(ClassDeclaration declaration) {}
    @Override public void visit(ConstructorDeclaration declaration) {}
    @Override public void visit(EnumDeclaration declaration) {}
    @Override public void visit(EnumDeclaration.Constant declaration) {}
    @Override public void visit(ExpressionVariableInitializer declaration) {}
    @Override public void visit(FieldDeclaration declaration) {}
    @Override public void visit(FieldDeclarator declaration) {}
    @Override public void visit(FieldDeclarators declarations) {}
    @Override public void visit(FormalParameter declaration) {}
    @Override public void visit(FormalParameters declarations) {}
    @Override public void visit(InstanceInitializerDeclaration declaration) {}
    @Override public void visit(InterfaceDeclaration declaration) {}
    @Override public void visit(LocalVariableDeclaration declaration) {}
    @Override public void visit(LocalVariableDeclarator declarator) {}
    @Override public void visit(LocalVariableDeclarators declarators) {}
    @Override public void visit(MemberDeclarations declarations) {}
    @Override public void visit(MethodDeclaration declaration) {}
    @Override public void visit(ModuleDeclaration declarations) {}
    @Override public void visit(StaticInitializerDeclaration declaration) {}
    @Override public void visit(TypeDeclarations declaration) {}
}
