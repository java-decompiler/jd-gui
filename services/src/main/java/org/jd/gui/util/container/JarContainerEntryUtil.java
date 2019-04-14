/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.util.container;

import org.jd.gui.api.model.Container;
import org.jd.gui.model.container.ContainerEntryComparator;
import org.jd.gui.util.exception.ExceptionUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class JarContainerEntryUtil {
    public static Collection<Container.Entry> removeInnerTypeEntries(Collection<Container.Entry> entries) {
        HashSet<String> potentialOuterTypePaths = new HashSet<>();
        Collection<Container.Entry> filtredSubEntries;

        for (Container.Entry e : entries) {
            if (!e.isDirectory()) {
                String p = e.getPath();

                if (p.toLowerCase().endsWith(".class")) {
                    int lastSeparatorIndex = p.lastIndexOf('/');
                    int dollarIndex = p.substring(lastSeparatorIndex+1).indexOf('$');

                    if (dollarIndex != -1) {
                        potentialOuterTypePaths.add(p.substring(0, lastSeparatorIndex+1+dollarIndex) + ".class");
                    }
                }
            }
        }

        if (potentialOuterTypePaths.size() == 0) {
            filtredSubEntries = entries;
        } else {
            HashSet<String> innerTypePaths = new HashSet<>();

            for (Container.Entry e : entries) {
                if (!e.isDirectory() && potentialOuterTypePaths.contains(e.getPath())) {
                    populateInnerTypePaths(innerTypePaths, e);
                }
            }

            filtredSubEntries = new ArrayList<>();

            for (Container.Entry e : entries) {
                if (!e.isDirectory()) {
                    String p = e.getPath();

                    if (p.toLowerCase().endsWith(".class")) {
                        int indexDollar = p.lastIndexOf('$');

                        if (indexDollar != -1) {
                            int indexSeparator = p.lastIndexOf('/');

                            if (indexDollar > indexSeparator) {
                                if (innerTypePaths.contains(p)) {
                                    // Inner class found -> Skip
                                    continue;
                                } else {
                                    populateInnerTypePaths(innerTypePaths, e);

                                    if (innerTypePaths.contains(p)) {
                                        // Inner class found -> Skip
                                        continue;
                                    }
                                }
                            }
                        }
                    }
                }
                // Valid path
                filtredSubEntries.add(e);
            }
        }

        List<Container.Entry> list = new ArrayList<>(filtredSubEntries);
        list.sort(ContainerEntryComparator.COMPARATOR);

        return list;
    }

    protected static void populateInnerTypePaths(final HashSet<String> innerTypePaths, Container.Entry entry) {
        try (InputStream is = entry.getInputStream()) {
            ClassReader classReader = new ClassReader(is);
            String p = entry.getPath();
            final String prefixPath = p.substring(0, p.length() - classReader.getClassName().length() - 6);

            ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM7) {
                public void visitInnerClass(final String name, final String outerName, final String innerName, final int access) {
                    innerTypePaths.add(prefixPath + name + ".class");
                }
            };

            classReader.accept(classVisitor, ClassReader.SKIP_CODE|ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
        } catch (Exception e) {
            assert ExceptionUtil.printStackTrace(e);
        }
    }
}
