/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.service.type;

import groovyjarjarasm.asm.ClassReader;
import groovyjarjarasm.asm.Opcodes;
import groovyjarjarasm.asm.tree.ClassNode;
import groovyjarjarasm.asm.tree.FieldNode;
import groovyjarjarasm.asm.tree.InnerClassNode;
import groovyjarjarasm.asm.tree.MethodNode;
import org.jd.gui.api.API;
import org.jd.gui.api.model.Container;
import org.jd.gui.api.model.Type;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

public class ClassFileTypeFactoryProvider extends AbstractTypeFactoryProvider {

    static {
        // Early class loading
        JavaType.class.getName();
    }

    // Create cache
    protected Cache<URI, JavaType> cache = new Cache();

    /**
     * @return local + optional external selectors
     */
    public String[] getSelectors() {
        List<String> externalSelectors = getExternalSelectors();

        if (externalSelectors == null) {
            return new String[] { "*:file:*.class" };
        } else {
            int size = externalSelectors.size();
            String[] selectors = new String[size+1];
            externalSelectors.toArray(selectors);
            selectors[size] = "*:file:*.class";
            return selectors;
        }
    }

    public Collection<Type> make(API api, Container.Entry entry) {
        return Collections.singletonList(make(api, entry, null));
    }

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
                                } catch (IOException ignore) {
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
            } catch (IOException ignore) {
                type = null;
            }

            cache.put(key, type);
            return type;
        }
    }

    static class JavaType implements Type {

        protected ClassNode classNode;
        protected Container.Entry entry;
        protected int access;
        protected String name;
        protected String superName;
        protected String outerName;

        protected String displayTypeName;
        protected String displayInnerTypeName;
        protected String displayPackageName;

        protected List<Type> innerTypes = null;
        protected List<Type.Field> fields = null;
        protected List<Type.Method> methods = null;

        @SuppressWarnings("unchecked")
        protected JavaType(Container.Entry entry, ClassReader classReader, int outerAccess) {
            this.classNode = new ClassNode();
            this.entry = entry;

            classReader.accept(classNode, ClassReader.SKIP_CODE|ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);

            this.access = (outerAccess == -1) ? classNode.access : outerAccess;
            this.name = classNode.name;

            this.superName = ((access & Opcodes.ACC_INTERFACE) != 0) && "java/lang/Object".equals(classNode.superName) ? null : classNode.superName;
            this.outerName = null;
            this.displayInnerTypeName = null;

            int indexDollar = name.lastIndexOf('$');

            if (indexDollar != -1) {
                int indexSeparator = name.lastIndexOf('/');
                if (indexDollar > indexSeparator) {
                    for (final InnerClassNode innerClassNode : (List<InnerClassNode>)classNode.innerClasses) {
                        if (name.equals(innerClassNode.name)) {
                            // Inner class path found
                            if (innerClassNode.outerName != null) {
                                this.outerName = innerClassNode.outerName;
                                this.displayInnerTypeName = this.name.substring(this.outerName.length() + 1);
                            }
                        }
                    }
                }
            }

            int lastPackageSeparatorIndex = name.lastIndexOf('/');

            if (lastPackageSeparatorIndex == -1) {
                this.displayPackageName = "";
                this.displayTypeName = (this.outerName == null) ? this.name : null;
            } else {
                this.displayPackageName = this.name.substring(0, lastPackageSeparatorIndex).replace('/', '.');
                this.displayTypeName = (this.outerName == null) ? this.name.substring(lastPackageSeparatorIndex+1) : null;
            }
        }

        public int getFlags() { return access; }
        public String getName() { return name; }
        public String getSuperName() { return superName; }
        public String getOuterName() { return outerName; }
        public String getDisplayPackageName() { return displayPackageName; }

        public String getDisplayTypeName() {
            if (displayTypeName == null) {
                displayTypeName = getDisplayTypeName(outerName, displayPackageName.length()) + '.' + displayInnerTypeName;
            }
            return displayTypeName;
        }

        @SuppressWarnings("unchecked")
        protected String getDisplayTypeName(String name, int packageLength) {
            int indexDollar = name.lastIndexOf('$');

            if (indexDollar > packageLength) {
                Container.Entry outerEntry = getEntry(name);

                if (outerEntry != null) {
                    try (InputStream is = outerEntry.getInputStream()) {
                        ClassReader classReader = new ClassReader(is);
                        ClassNode classNode = new ClassNode();
                        classReader.accept(classNode, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

                        for (final InnerClassNode innerClassNode : (List<InnerClassNode>)classNode.innerClasses) {
                            if (name.equals(innerClassNode.name)) {
                                // Inner class path found => Recursive call
                                return getDisplayTypeName(innerClassNode.outerName, packageLength) + '.' + innerClassNode.innerName;
                            }
                        }
                    } catch (IOException ignore) {
                    }
                }
            }

            return packageLength > 0 ? name.substring(packageLength+1) : name;
        }

        public String getDisplayInnerTypeName() { return displayInnerTypeName; }
        public Icon getIcon() { return getTypeIcon(access); }

        @SuppressWarnings("unchecked")
        public List<Type> getInnerTypes() {
            if (innerTypes == null) {
                innerTypes = new ArrayList<>(classNode.innerClasses.size());

                for (final InnerClassNode innerClassNode : (List<InnerClassNode>)classNode.innerClasses) {
                    if (((innerClassNode.access & (Opcodes.ACC_SYNTHETIC|Opcodes.ACC_BRIDGE)) == 0) && this.name.equals(innerClassNode.outerName)) {
                        Container.Entry innerEntry = getEntry(innerClassNode.name);

                        if (innerEntry != null) {
                            try (InputStream is = innerEntry.getInputStream()) {
                                ClassReader classReader = new ClassReader(is);
                                innerTypes.add(new JavaType(innerEntry, classReader, innerClassNode.access));
                            } catch (IOException ignore) {
                            }
                        }
                    }
                }
            }
            return innerTypes;
        }

        protected Container.Entry getEntry(String typeName) {
            String pathToFound = typeName + ".class";

            for (Container.Entry e : entry.getParent().getChildren()) {
                if (e.getPath().equals(pathToFound)) {
                    return e;
                }
            }

            return null;
        }

        @SuppressWarnings("unchecked")
        public List<Type.Field> getFields() {
            if (fields == null) {
                fields = new ArrayList<>(classNode.fields.size());

                for (final FieldNode fieldNode : (List<FieldNode>)classNode.fields) {
                    if ((fieldNode.access & (Opcodes.ACC_SYNTHETIC|Opcodes.ACC_ENUM)) == 0) {
                        fields.add(new Type.Field() {
                            public int getFlags() { return fieldNode.access; }
                            public String getName() { return fieldNode.name; }
                            public String getDescriptor() { return fieldNode.desc; }
                            public Icon getIcon() { return getFieldIcon(fieldNode.access); }

                            public String getDisplayName() {
                                StringBuffer sb = new StringBuffer();
                                sb.append(fieldNode.name).append(" : ");
                                writeSignature(sb, fieldNode.desc, fieldNode.desc.length(), 0, false);
                                return sb.toString();
                            }
                        });
                    }
                }
            }
            return fields;
        }

        @SuppressWarnings("unchecked")
        public List<Type.Method> getMethods() {
            if (methods == null) {
                methods = new ArrayList<>(classNode.methods.size());

                for (final MethodNode methodNode : (List<MethodNode>)classNode.methods) {
                    if ((methodNode.access & (Opcodes.ACC_SYNTHETIC|Opcodes.ACC_ENUM|Opcodes.ACC_BRIDGE)) == 0) {
                        methods.add(new Type.Method() {
                            public int getFlags() { return methodNode.access; }
                            public String getName() { return methodNode.name; }
                            public String getDescriptor() { return methodNode.desc; }
                            public Icon getIcon() { return getMethodIcon(methodNode.access); }

                            public String getDisplayName() {
                                String constructorName = displayInnerTypeName;
                                boolean isInnerClass = (constructorName != null);

                                if (constructorName == null)
                                    constructorName = getDisplayTypeName();

                                StringBuffer sb = new StringBuffer();
                                writeMethodSignature(sb, access, methodNode.access, isInnerClass, constructorName, methodNode.name, methodNode.desc);
                                return sb.toString();
                            }
                        });
                    }
                }
            }

            return methods;
        }
    }
}
