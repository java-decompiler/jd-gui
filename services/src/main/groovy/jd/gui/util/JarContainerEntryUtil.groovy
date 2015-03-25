/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.util

import groovyjarjarasm.asm.ClassReader
import groovyjarjarasm.asm.tree.ClassNode
import groovyjarjarasm.asm.tree.InnerClassNode
import jd.gui.api.model.Container

class JarContainerEntryUtil {
    static Collection<Container.Entry> removeInnerTypeEntries(Collection<Container.Entry> entries) {
        def potentialOuterTypePaths = new HashSet<String>()
        def filtredSubEntries

        for (def e : entries) {
            if (!e.isDirectory()) {
                String p = e.path

                if (p.toLowerCase().endsWith('.class')) {
                    int lastSeparatorIndex = p.lastIndexOf('/')
                    int dollarIndex = p.substring(lastSeparatorIndex+1).indexOf('$')

                    if (dollarIndex != -1) {
                        potentialOuterTypePaths.add(p.substring(0, lastSeparatorIndex+1+dollarIndex) + '.class')
                    }
                }
            }
        }

        if (potentialOuterTypePaths.size() == 0) {
            filtredSubEntries = entries
        } else {
            def innerTypePaths = new HashSet<String>()

            for (def e : entries) {
                if (!e.isDirectory() && potentialOuterTypePaths.contains(e.path)) {
                    populateInnerTypePaths(innerTypePaths, e)
                }
            }

            filtredSubEntries = new ArrayList<Container.Entry>()

            for (def e : entries) {
                if (!e.isDirectory()) {
                    String p = e.path

                    if (p.toLowerCase().endsWith('.class')) {
                        int indexDollar = p.lastIndexOf('$')

                        if (indexDollar != -1) {
                            int indexSeparator = p.lastIndexOf('/')

                            if (indexDollar > indexSeparator) {
                                if (innerTypePaths.contains(p)) {
                                    // Inner class found -> Skip
                                    continue
                                } else {
                                    populateInnerTypePaths(innerTypePaths, e)

                                    if (innerTypePaths.contains(p)) {
                                        // Inner class found -> Skip
                                        continue
                                    }
                                }
                            }
                        }
                    }
                }
                // Valid path
                filtredSubEntries.add(e)
            }
        }

        return filtredSubEntries.sort()
    }

    protected static void populateInnerTypePaths(HashSet<String> innerTypePaths, Container.Entry entry) {
        def classNode = new ClassNode()
        def classReader = entry.inputStream.withStream { InputStream is -> new ClassReader(is) }
        def p = entry.path
        def prefixPath = p.substring(0, p.length() - classReader.className.length() - 6)

        classReader.accept(classNode, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES)

        for (def obj : classNode.innerClasses) {
            def innerClass = obj as InnerClassNode
            innerTypePaths.add(prefixPath + innerClass.name + '.class')
        }
    }
}
