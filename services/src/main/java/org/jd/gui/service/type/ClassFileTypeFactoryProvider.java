/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.type;

import org.jd.gui.api.API;
import org.jd.gui.api.model.Container;
import org.jd.gui.api.model.Type;
import org.jd.gui.util.exception.ExceptionUtil;
import org.objectweb.asm.*;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ClassFileTypeFactoryProvider extends AbstractTypeFactoryProvider {

    static {
        // Early class loading
        try {
            Class.forName(JavaType.class.getName());
        } catch (Exception e) {
            assert ExceptionUtil.printStackTrace(e);
        }
    }

    // Create cache
    protected Cache<URI, JavaType> cache = new Cache<>();

    @Override public String[] getSelectors() { return appendSelectors("*:file:*.class"); }

    @Override
    public Collection<Type> make(API api, Container.Entry entry) {
        return Collections.singletonList(make(api, entry, null));
    }

    @Override
    public Type make(API api, Container.Entry entry, String fragment) {
        URI key = entry.getUri();

        if (cache.containsKey(key)) {
            return cache.get(key);
        } else {
            JavaType type;

            try (InputStream is = entry.getInputStream()) {
                ClassReader classReader = new ClassReader(is);

                if ((fragment != null) && (fragment.length() > 0)) {
                    // Search type name in fragment. URI format : see jd.gui.api.feature.UriOpener
                    int index = fragment.indexOf('-');
                    if (index != -1) {
                        // Keep type name only
                        fragment = fragment.substring(0, index);
                    }

                    if (!classReader.getClassName().equals(fragment)) {
                        // Search entry for type name
                        String entryTypePath = classReader.getClassName() + ".class";
                        String fragmentTypePath = fragment + ".class";

                        while (true) {
                            if (entry.getPath().endsWith(entryTypePath)) {
                                // Entry path ends with the internal class name
                                String pathToFound = entry.getPath().substring(0, entry.getPath().length() - entryTypePath.length()) + fragmentTypePath;
                                Container.Entry entryFound = null;

                                for (Container.Entry e : entry.getParent().getChildren()) {
                                    if (e.getPath().equals(pathToFound)) {
                                        entryFound = e;
                                        break;
                                    }
                                }

                                if (entryFound == null)
                                    return null;

                                entry = entryFound;

                                try (InputStream is2 = entry.getInputStream()) {
                                    classReader = new ClassReader(is2);
                                } catch (IOException e) {
                                    assert ExceptionUtil.printStackTrace(e);
                                    return null;
                                }
                                break;
                            }

                            // Truncated path ? Cut first package name and retry
                            int firstPackageSeparatorIndex = entryTypePath.indexOf('/');
                            if (firstPackageSeparatorIndex == -1) {
                                // Nothing to cut -> Stop
                                return null;
                            }

                            entryTypePath = entryTypePath.substring(firstPackageSeparatorIndex + 1);
                            fragmentTypePath = fragmentTypePath.substring(fragmentTypePath.indexOf('/') + 1);
                        }
                    }
                }

                type = new JavaType(entry, classReader, -1);
            } catch (IOException e) {
                assert ExceptionUtil.printStackTrace(e);
                type = null;
            }

            cache.put(key, type);
            return type;
        }
    }

    static class JavaType implements Type {
        protected Container.Entry entry;
        protected int access;
        protected String name;
        protected String superName;
        protected String outerName;

        protected String displayTypeName;
        protected String displayInnerTypeName;
        protected String displayPackageName;

        protected List<Type> innerTypes;
        protected List<Type.Field> fields = new ArrayList<>();
        protected List<Type.Method> methods = new ArrayList<>();

        @SuppressWarnings("unchecked")
        protected JavaType(Container.Entry entry, ClassReader classReader, final int outerAccess) {
            this.entry = entry;

            ClassVisitor classAndInnerClassesVisitor = new ClassVisitor(Opcodes.ASM7) {
                @Override
                public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                    JavaType.this.access = (outerAccess == -1) ? access : outerAccess;
                    JavaType.this.name = name;
                    JavaType.this.superName = ((access & Opcodes.ACC_INTERFACE) != 0) && "java/lang/Object".equals(superName) ? null : superName;
                }

                @Override
                public void visitInnerClass(String name, String outerName, String innerName, int access) {
                    if (JavaType.this.name.equals(name)) {
                        // Inner class path found
                        JavaType.this.outerName = outerName;
                        JavaType.this.displayInnerTypeName = innerName;
                    } else if (((access & (Opcodes.ACC_SYNTHETIC|Opcodes.ACC_BRIDGE)) == 0) && JavaType.this.name.equals(outerName)) {
                        Container.Entry innerEntry = getEntry(name);

                        if (innerEntry != null) {
                            try (InputStream is = innerEntry.getInputStream()) {
                                ClassReader classReader = new ClassReader(is);
                                if (innerTypes == null) {
                                    innerTypes = new ArrayList<>();
                                }
                                innerTypes.add(new JavaType(innerEntry, classReader, access));
                            } catch (IOException e) {
                                assert ExceptionUtil.printStackTrace(e);
                            }
                        }
                    }
                }
            };

            classReader.accept(classAndInnerClassesVisitor, ClassReader.SKIP_CODE|ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);

            int lastPackageSeparatorIndex = name.lastIndexOf('/');

            if (lastPackageSeparatorIndex == -1) {
                displayPackageName = "";

                if (outerName == null) {
                    displayTypeName = name;
                } else {
                    displayTypeName = getDisplayTypeName(outerName, 0) + '.' + displayInnerTypeName;
                }
            } else {
                displayPackageName = name.substring(0, lastPackageSeparatorIndex).replace('/', '.');

                if (outerName == null) {
                    displayTypeName = name;
                } else {
                    displayTypeName = getDisplayTypeName(outerName, lastPackageSeparatorIndex) + '.' + displayInnerTypeName;
                }

                displayTypeName = displayTypeName.substring(lastPackageSeparatorIndex+1);
            }

            ClassVisitor fieldsAndMethodsVisitor = new ClassVisitor(Opcodes.ASM7) {
                @Override
                public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                    if ((access & (Opcodes.ACC_SYNTHETIC|Opcodes.ACC_ENUM)) == 0) {
                        fields.add(new Type.Field() {
                            public int getFlags() { return access; }
                            public String getName() { return name; }
                            public String getDescriptor() { return descriptor; }
                            public Icon getIcon() { return getFieldIcon(access); }

                            public String getDisplayName() {
                                StringBuilder sb = new StringBuilder();
                                sb.append(name).append(" : ");
                                writeSignature(sb, descriptor, descriptor.length(), 0, false);
                                return sb.toString();
                            }
                        });
                    }
                    return null;
                }

                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                    if ((access & (Opcodes.ACC_SYNTHETIC|Opcodes.ACC_ENUM|Opcodes.ACC_BRIDGE)) == 0) {
                        methods.add(new Type.Method() {
                            public int getFlags() { return access; }
                            public String getName() { return name; }
                            public String getDescriptor() { return descriptor; }
                            public Icon getIcon() { return getMethodIcon(access); }

                            public String getDisplayName() {
                                boolean isInnerClass = (JavaType.this.displayInnerTypeName != null);
                                String constructorName = isInnerClass ? JavaType.this.displayInnerTypeName : JavaType.this.displayTypeName;
                                StringBuilder sb = new StringBuilder();
                                writeMethodSignature(sb, JavaType.this.access, access, isInnerClass, constructorName, name, descriptor);
                                return sb.toString();
                            }
                        });
                    }
                    return null;
                }
            };

            classReader.accept(fieldsAndMethodsVisitor, ClassReader.SKIP_CODE|ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
        }

        @SuppressWarnings("unchecked")
        protected String getDisplayTypeName(String name, int packageLength) {
            int indexDollar = name.lastIndexOf('$');

            if (indexDollar > packageLength) {
                Container.Entry entry = getEntry(name);

                if (entry != null) {
                    try (InputStream is = entry.getInputStream()) {
                        ClassReader classReader = new ClassReader(is);
                        InnerClassVisitor classVisitor = new InnerClassVisitor(name);

                        classReader.accept(classVisitor, ClassReader.SKIP_CODE|ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);

                        String outerName = classVisitor.getOuterName();

                        if (outerName != null) {
                            // Inner class path found => Recursive call
                            return getDisplayTypeName(outerName, packageLength) + '.' + classVisitor.getInnerName();
                        }
                    } catch (IOException e) {
                        assert ExceptionUtil.printStackTrace(e);
                    }
                }
            }

            return name;
        }

        protected Container.Entry getEntry(String typeName) {
            String pathToFound = typeName + ".class";

            for (Container.Entry entry : entry.getParent().getChildren()) {
                if (entry.getPath().equals(pathToFound)) {
                    return entry;
                }
            }

            return null;
        }

        @Override public int getFlags() { return access; }
        @Override public String getName() { return name; }
        @Override public String getSuperName() { return superName; }
        @Override public String getOuterName() { return outerName; }
        @Override public String getDisplayPackageName() { return displayPackageName; }
        @Override public String getDisplayTypeName() { return displayTypeName; }
        @Override public String getDisplayInnerTypeName() { return displayInnerTypeName; }
        @Override public Icon getIcon() { return getTypeIcon(access); }
        @Override public List<Type> getInnerTypes() { return innerTypes; }
        @Override public List<Type.Field> getFields() { return fields; }
        @Override public List<Type.Method> getMethods() { return methods; }
    }

    protected static class InnerClassVisitor extends ClassVisitor {
        protected String name;
        protected String outerName;
        protected String innerName;

        public InnerClassVisitor(String name) {
            super(Opcodes.ASM7);
            this.name = name;
        }

        @Override
        public void visitInnerClass(String name, String outerName, String innerName, int access) {
            if (this.name.equals(name)) {
                // Inner class path found
                this.outerName = outerName;
                this.innerName = innerName;
            }
        }

        public String getOuterName() {
            return outerName;
        }

        public String getInnerName() {
            return innerName;
        }
    }
}
