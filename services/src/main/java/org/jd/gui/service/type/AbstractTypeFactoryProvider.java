/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.type;

import org.jd.gui.api.model.Type;
import org.jd.gui.spi.TypeFactory;
import org.jd.gui.util.exception.ExceptionUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public abstract class AbstractTypeFactoryProvider implements TypeFactory {
    protected List<String> externalSelectors;
    protected Pattern externalPathPattern;

    /**
     * Initialize "selectors" and "pathPattern" with optional external properties file
     */
    public AbstractTypeFactoryProvider() {
        Properties properties = new Properties();
        Class clazz = this.getClass();

        try (InputStream is = clazz.getClassLoader().getResourceAsStream(clazz.getName().replace('.', '/') + ".properties")) {
            if (is != null) {
                properties.load(is);
            }
        } catch (IOException e) {
            assert ExceptionUtil.printStackTrace(e);
        }

        init(properties);
    }

    protected void init(Properties properties) {
        String selectors = properties.getProperty("selectors");
        externalSelectors = (selectors == null) ? null : Arrays.asList(selectors.split(","));

        String pathRegExp = properties.getProperty("pathRegExp");
        externalPathPattern = (pathRegExp == null) ? null : Pattern.compile(pathRegExp);
    }

    protected String[] appendSelectors(String selector) {
        if (externalSelectors == null) {
            return new String[] { selector };
        } else {
            int size = externalSelectors.size();
            String[] array = new String[size+1];
            externalSelectors.toArray(array);
            array[size] = selector;
            return array;
        }
    }

    protected String[] appendSelectors(String... selectors) {
        if (externalSelectors == null) {
            return selectors;
        } else {
            int size = externalSelectors.size();
            String[] array = new String[size+selectors.length];
            externalSelectors.toArray(array);
            System.arraycopy(selectors, 0, array, size, selectors.length);
            return array;
        }
    }

    public Pattern getPathPattern() { return externalPathPattern; }

    // Signature writers
    protected static int writeSignature(StringBuilder sb, String descriptor, int  length, int index, boolean varargsFlag) {
        while (true) {
            // Print array : '[[?' ou '[L[?;'
            int dimensionLength = 0;

            if (descriptor.charAt(index) == '[') {
                dimensionLength++;

                while (++index < length) {
                    if ((descriptor.charAt(index) == 'L') && (index+1 < length) && (descriptor.charAt(index+1) == '[')) {
                        index++;
                        length--;
                        dimensionLength++;
                    } else if (descriptor.charAt(index) == '[') {
                        dimensionLength++;
                    } else {
                        break;
                    }
                }
            }

            switch(descriptor.charAt(index)) {
                case 'B': sb.append("byte"); index++; break;
                case 'C': sb.append("char"); index++; break;
                case 'D': sb.append("double"); index++; break;
                case 'F': sb.append("float"); index++; break;
                case 'I': sb.append("int"); index++; break;
                case 'J': sb.append("long"); index++; break;
                case 'L': case '.':
                    int beginIndex = ++index;
                    char c = '.';

                    // Search ; or de <
                    while (index < length) {
                        c = descriptor.charAt(index);
                        if ((c == ';') || (c == '<'))
                            break;
                        index++;
                    }

                    String internalClassName = descriptor.substring(beginIndex, index);
                    int lastPackageSeparatorIndex = internalClassName.lastIndexOf('/');

                    if (lastPackageSeparatorIndex >= 0) {
                        // Cut package name
                        internalClassName = internalClassName.substring(lastPackageSeparatorIndex + 1);
                    }

                    sb.append(internalClassName.replace('$', '.'));

                    if (c == '<') {
                        sb.append('<');
                        index = writeSignature(sb, descriptor, length, index+1, false);

                        while (descriptor.charAt(index) != '>') {
                            sb.append(", ");
                            index = writeSignature(sb, descriptor, length, index, false);
                        }
                        sb.append('>');

                        // pass '>'
                        index++;
                    }

                    // pass ';'
                    if (descriptor.charAt(index) == ';')
                        index++;
                    break;
                case 'S': sb.append("short"); index++; break;
                case 'T':
                    beginIndex = ++index;
                    index = descriptor.substring(beginIndex, length).indexOf(';');
                    sb.append(descriptor.substring(beginIndex, index));
                    index++;
                    break;
                case 'V': sb.append("void"); index++; break;
                case 'Z': sb.append("boolean"); index++; break;
                case '-':
                    sb.append("? super ");
                    index = writeSignature(sb, descriptor, length, index+1, false);
                    break;
                case '+':
                    sb.append("? extends ");
                    index = writeSignature(sb, descriptor, length, index+1, false);
                    break;
                case '*': sb.append('?'); index++; break;
                case 'X': case 'Y': sb.append("int"); index++; break;
                default:
                    throw new RuntimeException("SignatureWriter.WriteSignature: invalid signature '" + descriptor + "'");
            }

            if (varargsFlag) {
                if (dimensionLength > 0) {
                    while (--dimensionLength > 0)
                        sb.append("[]");
                    sb.append("...");
                }
            } else {
                while (dimensionLength-- > 0)
                    sb.append("[]");
            }

            if ((index >= length) || (descriptor.charAt(index) != '.'))
                break;

            sb.append('.');
        }

        return index;
    }

    protected static void writeMethodSignature(
            StringBuilder sb, int typeAccess, int methodAccess, boolean isInnerClass,
            String constructorName, String methodName, String descriptor) {
        if (methodName.equals("<clinit>")) {
            sb.append("{...}");
        } else {
            boolean isAConstructor = methodName.equals("<init>");

            if (isAConstructor) {
                sb.append(constructorName);
            } else {
                sb.append(methodName);
            }

            // Skip generics
            int length = descriptor.length();
            int index = 0;

            while ((index < length) && (descriptor.charAt(index) != '('))
                index++;

            if (descriptor.charAt(index) != '(') {
                throw new RuntimeException("Signature format exception: '" + descriptor + "'");
            }

            sb.append('(');

            // pass '('
            index++;

            if (descriptor.charAt(index) != ')') {
                if (isAConstructor && isInnerClass && ((typeAccess & Type.FLAG_STATIC) == 0)) {
                    // Skip first parameter
                    int lengthBackup = sb.length();
                    index = writeSignature(sb, descriptor, length, index, false);
                    sb.setLength(lengthBackup);
                }

                if (descriptor.charAt(index) != ')') {
                    int varargsParameterIndex;

                    if ((methodAccess & Type.FLAG_VARARGS) == 0) {
                        varargsParameterIndex = Integer.MAX_VALUE;
                    } else {
                        // Count parameters
                        int indexBackup = index;
                        int lengthBackup = sb.length();

                        varargsParameterIndex = -1;

                        while (descriptor.charAt(index) != ')') {
                            index = writeSignature(sb, descriptor, length, index, false);
                            varargsParameterIndex++;
                        }

                        index = indexBackup;
                        sb.setLength(lengthBackup);
                    }

                    // Write parameters
                    index = writeSignature(sb, descriptor, length, index, false);

                    int parameterIndex = 1;

                    while (descriptor.charAt(index) != ')') {
                        sb.append(", ");
                        index = writeSignature(sb, descriptor, length, index, (parameterIndex == varargsParameterIndex));
                        parameterIndex++;
                    }
                }
            }

            if (isAConstructor) {
                sb.append(')');
            } else {
                sb.append(") : ");
                writeSignature(sb, descriptor, length, ++index, false);
            }
        }
    }

    // Icon getters
    protected static ImageIcon getTypeIcon(int access) {
        if ((access & Type.FLAG_ANNOTATION) != 0)
            return ANNOTATION_ICON;
        else if ((access & Type.FLAG_INTERFACE) != 0)
            return INTERFACE_ICONS[accessToIndex(access)];
        else if ((access & Type.FLAG_ENUM) != 0)
            return ENUM_ICON;
        else
            return CLASS_ICONS[accessToIndex(access)];
    }

    protected static ImageIcon getFieldIcon(int access) {
        return FIELD_ICONS[accessToIndex(access)];
    }

    protected static ImageIcon getMethodIcon(int access) {
        return METHOD_ICONS[accessToIndex(access)];
    }

    protected static int accessToIndex(int access) {
        int index = 0;

        if ((access & Type.FLAG_STATIC) != 0)
            index += 4;

        if ((access & Type.FLAG_FINAL) != 0)
            index += 8;

        if ((access & Type.FLAG_ABSTRACT) != 0)
            index += 16;

        if ((access & Type.FLAG_PUBLIC) != 0)
            return index + 1;
        else if ((access & Type.FLAG_PROTECTED) != 0)
            return index + 2;
        else if ((access & Type.FLAG_PRIVATE) != 0)
            return index + 3;
        else
            return index;
    }

    // Internal graphic stuff ...
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

    protected static final ImageIcon ABSTRACT_OVERLAY_ICON = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("org/jd/gui/images/abstract_ovr.png"));
    protected static final ImageIcon FINAL_OVERLAY_ICON = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("org/jd/gui/images/final_ovr.png"));
    protected static final ImageIcon STATIC_OVERLAY_ICON = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("org/jd/gui/images/static_ovr.png"));

    protected static final ImageIcon CLASS_ICON = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("org/jd/gui/images/class_default_obj.png"));
    protected static final ImageIcon PUBLIC_CLASS_ICON = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("org/jd/gui/images/class_obj.png"));
    protected static final ImageIcon PROTECTED_CLASS_ICON = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("org/jd/gui/images/class_protected_obj.png"));
    protected static final ImageIcon PRIVATE_CLASS_ICON = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("org/jd/gui/images/class_private_obj.png"));

    protected static final ImageIcon INTERFACE_ICON = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("org/jd/gui/images/int_default_obj.png"));
    protected static final ImageIcon PUBLIC_INTERFACE_ICON = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("org/jd/gui/images/int_obj.png"));
    protected static final ImageIcon PROTECTED_INTERFACE_ICON = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("org/jd/gui/images/int_protected_obj.png"));
    protected static final ImageIcon PRIVATE_INTERFACE_ICON = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("org/jd/gui/images/int_private_obj.png"));

    protected static final ImageIcon ANNOTATION_ICON = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("org/jd/gui/images/annotation_obj.png"));
    protected static final ImageIcon ENUM_ICON = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("org/jd/gui/images/enum_obj.png"));

    protected static final ImageIcon FIELD_ICON = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("org/jd/gui/images/field_default_obj.png"));
    protected static final ImageIcon PUBLIC_FIELD_ICON = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("org/jd/gui/images/field_public_obj.png"));
    protected static final ImageIcon PROTECTED_FIELD_ICON = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("org/jd/gui/images/field_protected_obj.png"));
    protected static final ImageIcon PRIVATE_FIELD_ICON = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("org/jd/gui/images/field_private_obj.png"));

    protected static final ImageIcon METHOD_ICON = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("org/jd/gui/images/methdef_obj.png"));
    protected static final ImageIcon PUBLIC_METHOD_ICON = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("org/jd/gui/images/methpub_obj.png"));
    protected static final ImageIcon PROTECTED_METHOD_ICON = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("org/jd/gui/images/methpro_obj.png"));
    protected static final ImageIcon PRIVATE_METHOD_ICON = new ImageIcon(ClassFileTypeFactoryProvider.class.getClassLoader().getResource("org/jd/gui/images/methpri_obj.png"));

    // Default icon set
    protected static final ImageIcon[] DEFAULT_CLASS_ICONS = {
            CLASS_ICON,
            PUBLIC_CLASS_ICON,
            PROTECTED_CLASS_ICON,
            PRIVATE_CLASS_ICON
    };

    protected static final ImageIcon[] DEFAULT_INTERFACE_ICONS = {
            INTERFACE_ICON,
            PUBLIC_INTERFACE_ICON,
            PROTECTED_INTERFACE_ICON,
            PRIVATE_INTERFACE_ICON
    };

    protected static final ImageIcon[] DEFAULT_FIELD_ICONS = {
            FIELD_ICON,
            PUBLIC_FIELD_ICON,
            PROTECTED_FIELD_ICON,
            PRIVATE_FIELD_ICON
    };

    protected static final ImageIcon[] DEFAULT_METHOD_ICONS = {
            METHOD_ICON,
            PUBLIC_METHOD_ICON,
            PROTECTED_METHOD_ICON,
            PRIVATE_METHOD_ICON
    };

    // Add static icon set
    protected static final ImageIcon[] STATIC_CLASS_ICONS = mergeIcons(DEFAULT_CLASS_ICONS, STATIC_OVERLAY_ICON, 100, 0);
    protected static final ImageIcon[] STATIC_INTERFACE_ICONS = mergeIcons(DEFAULT_INTERFACE_ICONS, STATIC_OVERLAY_ICON, 100, 0);
    protected static final ImageIcon[] STATIC_FIELD_ICONS = mergeIcons(DEFAULT_FIELD_ICONS, STATIC_OVERLAY_ICON, 100, 0);
    protected static final ImageIcon[] STATIC_METHOD_ICONS = mergeIcons(DEFAULT_METHOD_ICONS, STATIC_OVERLAY_ICON, 100, 0);

    // Add final icon set
    protected static final ImageIcon[] FINAL_STATIC_CLASS_ICONS = mergeIcons(STATIC_CLASS_ICONS, FINAL_OVERLAY_ICON, 0, 0);
    protected static final ImageIcon[] FINAL_STATIC_INTERFACE_ICONS = mergeIcons(STATIC_INTERFACE_ICONS, FINAL_OVERLAY_ICON, 0, 0);
    protected static final ImageIcon[] FINAL_STATIC_FIELD_ICONS = mergeIcons(STATIC_FIELD_ICONS, FINAL_OVERLAY_ICON, 0, 0);
    protected static final ImageIcon[] FINAL_STATIC_METHOD_ICONS = mergeIcons(STATIC_METHOD_ICONS, FINAL_OVERLAY_ICON, 0, 0);

    // Add abstract icon set
    protected static final ImageIcon[] CLASS_ICONS = mergeIcons(FINAL_STATIC_CLASS_ICONS, ABSTRACT_OVERLAY_ICON, 0, 100);
    protected static final ImageIcon[] INTERFACE_ICONS = mergeIcons(FINAL_STATIC_INTERFACE_ICONS, ABSTRACT_OVERLAY_ICON, 0, 100);
    protected static final ImageIcon[] FIELD_ICONS = mergeIcons(FINAL_STATIC_FIELD_ICONS, ABSTRACT_OVERLAY_ICON, 0, 100);
    protected static final ImageIcon[] METHOD_ICONS = mergeIcons(FINAL_STATIC_METHOD_ICONS, ABSTRACT_OVERLAY_ICON, 0, 100);

    // Cache
    protected static class Cache<K, V> extends LinkedHashMap<K, V> {
        public static final int CACHE_MAX_ENTRIES = 100;

        public Cache() {
            super(CACHE_MAX_ENTRIES*3/2, 0.7f, true);
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > CACHE_MAX_ENTRIES;
        }
    }
}
