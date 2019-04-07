/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.indexer;

import org.jd.gui.api.API;
import org.jd.gui.api.model.Container;
import org.jd.gui.api.model.Indexes;
import org.jd.gui.util.exception.ExceptionUtil;
import org.objectweb.asm.*;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import java.io.InputStream;
import java.util.*;

/**
 * Unsafe thread implementation of class file indexer.
 */
public class ClassFileIndexerProvider extends AbstractIndexerProvider {
    protected HashSet<String> typeDeclarationSet = new HashSet<>();
    protected HashSet<String> constructorDeclarationSet = new HashSet<>();
    protected HashSet<String> methodDeclarationSet = new HashSet<>();
    protected HashSet<String> fieldDeclarationSet = new HashSet<>();
    protected HashSet<String> typeReferenceSet = new HashSet<>();
    protected HashSet<String> constructorReferenceSet = new HashSet<>();
    protected HashSet<String> methodReferenceSet = new HashSet<>();
    protected HashSet<String> fieldReferenceSet = new HashSet<>();
    protected HashSet<String> stringSet = new HashSet<>();
    protected HashSet<String> superTypeNameSet = new HashSet<>();
    protected HashSet<String> descriptorSet = new HashSet<>();

    protected ClassIndexer classIndexer = new ClassIndexer(
        typeDeclarationSet, constructorDeclarationSet, methodDeclarationSet,
        fieldDeclarationSet, typeReferenceSet, superTypeNameSet, descriptorSet);
    protected SignatureIndexer signatureIndexer = new SignatureIndexer(typeReferenceSet);

    @Override public String[] getSelectors() { return appendSelectors("*:file:*.class"); }

    @Override
    @SuppressWarnings("unchecked")
    public void index(API api, Container.Entry entry, Indexes indexes) {
        // Cleaning sets...
        typeDeclarationSet.clear();
        constructorDeclarationSet.clear();
        methodDeclarationSet.clear();
        fieldDeclarationSet.clear();
        typeReferenceSet.clear();
        constructorReferenceSet.clear();
        methodReferenceSet.clear();
        fieldReferenceSet.clear();
        stringSet.clear();
        superTypeNameSet.clear();
        descriptorSet.clear();

        try (InputStream inputStream = entry.getInputStream()) {
            // Index field, method, interfaces & super type
            ClassReader classReader = new ClassReader(inputStream);
            classReader.accept(classIndexer, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

            // Index descriptors
            for (String descriptor : descriptorSet) {
                new SignatureReader(descriptor).accept(signatureIndexer);
            }

            // Index references
            char[] buffer = new char[classReader.getMaxStringLength()];

            for (int i=classReader.getItemCount()-1; i>0; i--) {
                int startIndex = classReader.getItem(i);

                if (startIndex != 0) {
                    int tag = classReader.readByte(startIndex-1);

                    switch (tag) {
                        case 7: // CONSTANT_Class
                            String className = classReader.readUTF8(startIndex, buffer);
                            if (className.startsWith("[")) {
                                new SignatureReader(className).acceptType(signatureIndexer);
                            } else {
                                typeReferenceSet.add(className);
                            }
                            break;
                        case 8: // CONSTANT_String
                            String str = classReader.readUTF8(startIndex, buffer);
                            stringSet.add(str);
                            break;
                        case 9: // CONSTANT_Fieldref
                            int nameAndTypeItem = classReader.readUnsignedShort(startIndex+2);
                            int nameAndTypeIndex = classReader.getItem(nameAndTypeItem);
                            tag = classReader.readByte(nameAndTypeIndex-1);
                            if (tag == 12) { // CONSTANT_NameAndType
                                String fieldName = classReader.readUTF8(nameAndTypeIndex, buffer);
                                fieldReferenceSet.add(fieldName);
                            }
                            break;
                        case 10: // CONSTANT_Methodref:
                        case 11: // CONSTANT_InterfaceMethodref:
                            nameAndTypeItem = classReader.readUnsignedShort(startIndex+2);
                            nameAndTypeIndex = classReader.getItem(nameAndTypeItem);
                            tag = classReader.readByte(nameAndTypeIndex-1);
                            if (tag == 12) { // CONSTANT_NameAndType
                                String methodName = classReader.readUTF8(nameAndTypeIndex, buffer);
                                if ("<init>".equals(methodName)) {
                                    int classItem = classReader.readUnsignedShort(startIndex);
                                    int classIndex = classReader.getItem(classItem);
                                    className = classReader.readUTF8(classIndex, buffer);
                                    constructorReferenceSet.add(className);
                                } else {
                                    methodReferenceSet.add(methodName);
                                }
                            }
                            break;
                    }
                }
            }

            String typeName = classIndexer.name;

            // Append sets to indexes
            addToIndex(indexes, "typeDeclarations", typeDeclarationSet, entry);
            addToIndex(indexes, "constructorDeclarations", constructorDeclarationSet, entry);
            addToIndex(indexes, "methodDeclarations", methodDeclarationSet, entry);
            addToIndex(indexes, "fieldDeclarations", fieldDeclarationSet, entry);
            addToIndex(indexes, "typeReferences", typeReferenceSet, entry);
            addToIndex(indexes, "constructorReferences", constructorReferenceSet, entry);
            addToIndex(indexes, "methodReferences", methodReferenceSet, entry);
            addToIndex(indexes, "fieldReferences", fieldReferenceSet, entry);
            addToIndex(indexes, "strings", stringSet, entry);

            // Populate map [super type name : [sub type name]]
            if (superTypeNameSet.size() > 0) {
                Map<String, Collection> index = indexes.getIndex("subTypeNames");

                for (String superTypeName : superTypeNameSet) {
                    index.get(superTypeName).add(typeName);
                }
            }
        } catch (Exception e) {
            assert ExceptionUtil.printStackTrace(e);
        }
    }

    protected static class ClassIndexer extends ClassVisitor {
        protected HashSet<String> typeDeclarationSet;
        protected HashSet<String> constructorDeclarationSet;
        protected HashSet<String> methodDeclarationSet;
        protected HashSet<String> fieldDeclarationSet;
        protected HashSet<String> typeReferenceSet;
        protected HashSet<String> superTypeNameSet;
        protected HashSet<String> descriptorSet;

        protected AnnotationIndexer annotationIndexer;
        protected FieldIndexer fieldIndexer;
        protected MethodIndexer methodIndexer;

        protected String name;

        public ClassIndexer(
                HashSet<String> typeDeclarationSet, HashSet<String> constructorDeclarationSet,
                HashSet<String> methodDeclarationSet, HashSet<String> fieldDeclarationSet,
                HashSet<String> typeReferenceSet, HashSet<String> superTypeNameSet, HashSet<String> descriptorSet) {
            super(Opcodes.ASM7);

            this.typeDeclarationSet = typeDeclarationSet;
            this.constructorDeclarationSet = constructorDeclarationSet;
            this.methodDeclarationSet = methodDeclarationSet;
            this.fieldDeclarationSet = fieldDeclarationSet;
            this.typeReferenceSet = typeReferenceSet;
            this.superTypeNameSet = superTypeNameSet;
            this.descriptorSet = descriptorSet;

            this.annotationIndexer = new AnnotationIndexer(descriptorSet);
            this.fieldIndexer = new FieldIndexer(descriptorSet, annotationIndexer);
            this.methodIndexer = new MethodIndexer(descriptorSet, annotationIndexer);
        }

        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.name = name;
            typeDeclarationSet.add(name);
            superTypeNameSet.add(superName);

            if (interfaces != null) {
                for (int i=interfaces.length-1; i>=0; i--) {
                    superTypeNameSet.add(interfaces[i]);
                }
            }
        }

        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            descriptorSet.add(desc);
            return annotationIndexer;
        }

        public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
            descriptorSet.add(desc);
            return annotationIndexer;
        }

        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            fieldDeclarationSet.add(name);
            descriptorSet.add(signature==null ? desc : signature);
            return fieldIndexer;
        }

        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if ("<init>".equals(name)) {
                constructorDeclarationSet.add(this.name);
            } else if (! "<clinit>".equals(name)) {
                methodDeclarationSet.add(name);
            }

            descriptorSet.add(signature==null ? desc : signature);

            if (exceptions != null) {
                for (int i=exceptions.length-1; i>=0; i--) {
                    typeReferenceSet.add(exceptions[i]);
                }
            }
            return methodIndexer;
        }
    }

    protected static class SignatureIndexer extends SignatureVisitor {
        protected HashSet<String> typeReferenceSet;

        SignatureIndexer(HashSet<String> typeReferenceSet) {
            super(Opcodes.ASM7);
            this.typeReferenceSet = typeReferenceSet;
        }

        public void visitClassType(String name) {
            typeReferenceSet.add(name);
        }
    }

    protected static class AnnotationIndexer extends AnnotationVisitor {
        protected HashSet<String> descriptorSet;

        public AnnotationIndexer(HashSet<String> descriptorSet) {
            super(Opcodes.ASM7);
            this.descriptorSet = descriptorSet;
        }

        public void visitEnum(String name, String desc, String value) {
            descriptorSet.add(desc);
        }

        public AnnotationVisitor visitAnnotation(String name, String desc) {
            descriptorSet.add(desc);
            return this;
        }
    }

    protected static class FieldIndexer extends FieldVisitor {
        protected HashSet<String> descriptorSet;
        protected AnnotationIndexer annotationIndexer;

        public FieldIndexer(HashSet<String> descriptorSet, AnnotationIndexer annotationInexer) {
            super(Opcodes.ASM7);
            this.descriptorSet = descriptorSet;
            this.annotationIndexer = annotationInexer;
        }

        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            descriptorSet.add(desc);
            return annotationIndexer;
        }

        public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
            descriptorSet.add(desc);
            return annotationIndexer;
        }
    }

    protected static class MethodIndexer extends MethodVisitor {
        protected HashSet<String> descriptorSet;
        protected AnnotationIndexer annotationIndexer;

        public MethodIndexer(HashSet<String> descriptorSet, AnnotationIndexer annotationIndexer) {
            super(Opcodes.ASM7);
            this.descriptorSet = descriptorSet;
            this.annotationIndexer = annotationIndexer;
        }

        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            descriptorSet.add(desc);
            return annotationIndexer;
        }

        public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
            descriptorSet.add(desc);
            return annotationIndexer;
        }

        public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
            descriptorSet.add(desc);
            return annotationIndexer;
        }
    }
}
