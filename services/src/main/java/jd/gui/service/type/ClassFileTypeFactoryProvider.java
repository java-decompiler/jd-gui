/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.type;

import groovyjarjarasm.asm.ClassReader;
import groovyjarjarasm.asm.Opcodes;
import groovyjarjarasm.asm.tree.ClassNode;
import groovyjarjarasm.asm.tree.FieldNode;
import groovyjarjarasm.asm.tree.InnerClassNode;
import groovyjarjarasm.asm.tree.MethodNode;
import jd.gui.api.API;
import jd.gui.api.model.Container;
import jd.gui.api.model.Type;
import jd.gui.spi.TypeFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ClassFileTypeFactoryProvider implements TypeFactory {

    static {
        // Early class loading
        JavaType.class.getName();
    }

    protected String[] selectors = new String[] { "*:file:*.class" };

    public String[] getSelectors() { return selectors; }

    public Pattern getPathPattern() { return null; }

    public Type make(API api, Container.Entry entry, String fragment) {
        try (InputStream is = entry.getInputStream()) {
            ClassReader classReader = new ClassReader(is);

            if ((fragment != null) && (fragment.length() > 0)) {
                // Search type name in fragment. URI format : see jd.gui.api.feature.UriOpener
                int index = fragment.indexOf('-');
                if (index != -1) {
                    // Keep type name only
                    fragment = fragment.substring(0, index);
                }

                if (! classReader.getClassName().equals(fragment)) {
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

                        entryTypePath = entryTypePath.substring(firstPackageSeparatorIndex+1);
                        fragmentTypePath = fragmentTypePath.substring(fragmentTypePath.indexOf('/')+1);
                    }
                }
            }

            return new JavaType(entry, classReader);
        } catch (IOException ignore) {
            return null;
        }
    }

    static class JavaType implements Type {
        ClassNode classNode;
        Container.Entry entry;
        int access;
        String name;
        String shortName;
        String superName;
        String outerName;

        String displayTypeName;
        String displayInnerTypeName;
        String displayPackageName;

        List<Type> innerTypes;
        List<Type.Field> fields;
        List<Type.Method> methods;

        @SuppressWarnings("unchecked")
        JavaType(Container.Entry entry, ClassReader classReader) {
            this.classNode = new ClassNode();
            this.entry = entry;

            classReader.accept(classNode, ClassReader.SKIP_CODE|ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);

            this.access = classNode.access;
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
                this.shortName = this.name;
                this.displayTypeName = (this.outerName != null) ? null : this.shortName;
            } else {
                this.displayPackageName = this.name.substring(0, lastPackageSeparatorIndex).replace('/', '.');
                this.shortName = this.name.substring(lastPackageSeparatorIndex+1);
                this.displayTypeName = (this.outerName != null) ? null : this.shortName;
            }

            this.innerTypes = null;
            this.fields = null;
            this.methods = null;
        }

        public int getFlags() { return access; }
        public String getName() { return name; }
        public String getShortName() { return shortName; }
        public String getSuperName() { return superName; }
        public String getOuterName() { return outerName; }

        public Container.Entry getOuterEntry() { return (outerName==null) ? null : getEntry(outerName); }

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
        public Icon getIcon() { return getIcon(access); }

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
                                innerTypes.add(new JavaType(innerEntry, classReader));
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
                            public Icon getIcon() { return fieldIcons[accessToIndex(fieldNode.access)]; }
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
                            public Icon getIcon() { return methodIcons[accessToIndex(methodNode.access)]; }
                        });
                    }
                }
            }
            return methods;
        }

        protected static ImageIcon getIcon(int access) {
            if ((access & Opcodes.ACC_ANNOTATION) != 0)
                return annotationIcon;
            else if ((access & Opcodes.ACC_INTERFACE) != 0)
                return interfaceIcons[accessToIndex(access)];
            else if ((access & Opcodes.ACC_ENUM) != 0)
                return enumIcon;
            else
                return classIcons[accessToIndex(access)];
        }

        protected static int accessToIndex(int access) {
            int index = 0;

            if ((access & Opcodes.ACC_STATIC) != 0)
                index += 4;

            if ((access & Opcodes.ACC_FINAL) != 0)
                index += 8;

            if ((access & Opcodes.ACC_ABSTRACT) != 0)
                index += 16;

            if ((access & Opcodes.ACC_PUBLIC) != 0)
                return index + 1;
            else if ((access & Opcodes.ACC_PROTECTED) != 0)
                return index + 2;
            else if ((access & Opcodes.ACC_PRIVATE) != 0)
                return index + 3;
            else
                return index;
        }

        // Graphic stuff ...
        protected static ImageIcon mergeIcons(ImageIcon background, ImageIcon overlay, int x, int y) {
            int w = background.getIconWidth();
            int h = background.getIconHeight();
            BufferedImage image = new BufferedImage(w, h,  BufferedImage.TYPE_INT_ARGB);

            if (x + overlay.getIconWidth() > w)
                x = w - overlay.getIconWidth();
            if (y + overlay.getIconHeight() > h)
                y = h - overlay.getIconHeight();

            Graphics2D g2 = image.createGraphics();
            g2.drawImage(background.getImage(), 0, 0, null);
            g2.drawImage(overlay.getImage(), x, y, null);
            g2.dispose();

            return new ImageIcon(image);
        }

        protected static ImageIcon[] mergeIcons(ImageIcon[] backgrounds, ImageIcon overlay, int x, int y) {
            int length = backgrounds.length;
            ImageIcon[] result = new ImageIcon[length*2];

            // Copy source icons
            System.arraycopy(backgrounds, 0, result, 0, length);

            // Add overlays
            for (int i=0; i<length; i++) {
                result[length+i] = mergeIcons(backgrounds[i], overlay, x, y);
            }

            return result;
        }

        protected static final ImageIcon abstractOverlayIcon = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("images/abstract_ovr.png"));
        protected static final ImageIcon finalOverlayIcon = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("images/final_ovr.png"));
        protected static final ImageIcon staticOverlayIcon = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("images/static_ovr.png"));

        protected static final ImageIcon classIcon = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("images/class_default_obj.png"));
        protected static final ImageIcon publicClassIcon = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("images/class_obj.png"));
        protected static final ImageIcon protectedClassIcon = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("images/class_protected_obj.png"));
        protected static final ImageIcon privateClassIcon = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("images/class_private_obj.png"));

        protected static final ImageIcon interfaceIcon = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("images/int_default_obj.png"));
        protected static final ImageIcon publicInterfaceIcon = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("images/int_obj.png"));
        protected static final ImageIcon protectedInterfaceIcon = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("images/int_protected_obj.png"));
        protected static final ImageIcon privateInterfaceIcon = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("images/int_private_obj.png"));

        protected static final ImageIcon annotationIcon = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("images/annotation_obj.png"));
        protected static final ImageIcon enumIcon = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("images/enum_obj.png"));

        protected static final ImageIcon fieldIcon = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("images/field_default_obj.png"));
        protected static final ImageIcon publicFieldIcon = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("images/field_public_obj.png"));
        protected static final ImageIcon protectedFieldIcon = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("images/field_protected_obj.png"));
        protected static final ImageIcon privateFieldIcon = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("images/field_private_obj.png"));

        protected static final ImageIcon methodIcon = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("images/methdef_obj.png"));
        protected static final ImageIcon publicMethodIcon = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("images/methpub_obj.png"));
        protected static final ImageIcon protedtedMethodIcon = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("images/methpro_obj.png"));
        protected static final ImageIcon privateMethodIcon = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("images/methpri_obj.png"));

        // Default icon set
        protected static final ImageIcon[] defaultClassIcons = {
            classIcon,
            publicClassIcon,
            protectedClassIcon,
            privateClassIcon
        };

        protected static final ImageIcon[] defaultInterfaceIcons = {
            interfaceIcon,
            publicInterfaceIcon,
            protectedInterfaceIcon,
            privateInterfaceIcon
        };

        protected static final ImageIcon[] defaultFieldIcons = {
            fieldIcon,
            publicFieldIcon,
            protectedFieldIcon,
            privateFieldIcon
        };

        protected static final ImageIcon[] defaultMethodIcons = {
            methodIcon,
            publicMethodIcon,
            protedtedMethodIcon,
            privateMethodIcon
        };

        // Add static icon set
        protected static final ImageIcon[] staticClassIcons = mergeIcons(defaultClassIcons, staticOverlayIcon, 100, 0);
        protected static final ImageIcon[] staticInterfaceIcons = mergeIcons(defaultInterfaceIcons, staticOverlayIcon, 100, 0);
        protected static final ImageIcon[] staticFieldIcons = mergeIcons(defaultFieldIcons, staticOverlayIcon, 100, 0);
        protected static final ImageIcon[] staticMethodIcons = mergeIcons(defaultMethodIcons, staticOverlayIcon, 100, 0);

        // Add final icon set
        protected static final ImageIcon[] finalStaticClassIcons = mergeIcons(staticClassIcons, finalOverlayIcon, 0, 0);
        protected static final ImageIcon[] finalStaticInterfaceIcons = mergeIcons(staticInterfaceIcons, finalOverlayIcon, 0, 0);
        protected static final ImageIcon[] finalStaticFieldIcons = mergeIcons(staticFieldIcons, finalOverlayIcon, 0, 0);
        protected static final ImageIcon[] finalStaticMethodIcons = mergeIcons(staticMethodIcons, finalOverlayIcon, 0, 0);

        // Add abstract icon set
        protected static final ImageIcon[] classIcons = mergeIcons(finalStaticClassIcons, abstractOverlayIcon, 0, 100);
        protected static final ImageIcon[] interfaceIcons = mergeIcons(finalStaticInterfaceIcons, abstractOverlayIcon, 0, 100);
        protected static final ImageIcon[] fieldIcons = mergeIcons(finalStaticFieldIcons, abstractOverlayIcon, 0, 100);
        protected static final ImageIcon[] methodIcons = mergeIcons(finalStaticMethodIcons, abstractOverlayIcon, 0, 100);
    }
}
