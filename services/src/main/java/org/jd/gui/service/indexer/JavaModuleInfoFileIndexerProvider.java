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

import java.io.InputStream;
import java.util.HashSet;

import static org.objectweb.asm.ClassReader.*;

/**
 * Unsafe thread implementation of class file indexer.
 */
public class JavaModuleInfoFileIndexerProvider extends AbstractIndexerProvider {
    protected HashSet<String> javaModuleDeclarationSet = new HashSet<>();
    protected HashSet<String> javaModuleReferenceSet = new HashSet<>();
    protected HashSet<String> typeReferenceSet = new HashSet<>();

    protected ClassIndexer classIndexer = new ClassIndexer();

    @Override public String[] getSelectors() { return appendSelectors("jmod:file:classes/module-info.class"); }

    @Override
    @SuppressWarnings("unchecked")
    public void index(API api, Container.Entry entry, Indexes indexes) {
        // Cleaning sets...
        javaModuleDeclarationSet.clear();
        javaModuleReferenceSet.clear();
        typeReferenceSet.clear();

        try (InputStream inputStream = entry.getInputStream()) {
            // Index field, method, interfaces & super type
            ClassReader classReader = new ClassReader(inputStream);
            classReader.accept(classIndexer, SKIP_CODE|SKIP_DEBUG|SKIP_FRAMES);

            // Append sets to indexes
            addToIndexes(indexes, "javaModuleDeclarations", javaModuleDeclarationSet, entry);
            addToIndexes(indexes, "javaModuleReferences", javaModuleReferenceSet, entry);
            addToIndexes(indexes, "typeReferences", typeReferenceSet, entry);
        } catch (Exception e) {
            assert ExceptionUtil.printStackTrace(e);
        }
    }

    protected class ClassIndexer extends ClassVisitor {
        protected ModuleIndexer moduleIndexer = new ModuleIndexer();

        public ClassIndexer() { super(Opcodes.ASM7); }

        @Override
        public ModuleVisitor visitModule(String moduleName, int moduleFlags, String moduleVersion) {
            javaModuleDeclarationSet.add(moduleName);
            return moduleIndexer;
        }
    }

    protected class ModuleIndexer extends ModuleVisitor {
        public ModuleIndexer() { super(Opcodes.ASM7); }

        @Override public void visitMainClass(final String mainClass) { typeReferenceSet.add(mainClass); }
        @Override public void visitRequire(final String module, final int access, final String version) { javaModuleReferenceSet.add(module); }
        @Override public void visitUse(final String service) { typeReferenceSet.add(service); }

        @Override
        public void visitExport(final String packaze, final int access, final String... modules) {
            if (modules != null) {
                for (String module : modules) {
                    javaModuleReferenceSet.add(module);
                }
            }
        }

        @Override
        public void visitOpen(final String packaze, final int access, final String... modules) {
            if (modules != null) {
                for (String module : modules) {
                    javaModuleReferenceSet.add(module);
                }
            }
        }

        @Override
        public void visitProvide(final String service, final String... providers) {
            typeReferenceSet.add(service);

            if (providers != null) {
                for (String provider : providers) {
                    typeReferenceSet.add(provider);
                }
            }
        }
    }
}
