/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use, 
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.fileloader;

import org.jd.gui.api.API;
import org.jd.gui.util.exception.ExceptionUtil;
import org.objectweb.asm.ClassReader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ClassFileLoaderProvider extends AbstractTypeFileLoaderProvider {
    protected static final String[] EXTENSIONS = { "class" };

    @Override public String[] getExtensions() { return EXTENSIONS; }
    @Override public String getDescription() { return "Class files (*.class)"; }

    @Override
    public boolean accept(API api, File file) {
        return file.exists() && file.isFile() && file.canRead() && file.getName().toLowerCase().endsWith(".class");
    }

    @Override
    public boolean load(API api, File file) {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            ClassReader classReader = new ClassReader(bis);
            String pathInFile = classReader.getClassName().replace("/", File.separator) + ".class";

            return load(api, file, pathInFile);
        } catch (IOException e) {
            assert ExceptionUtil.printStackTrace(e);
            return false;
        }
    }
}
