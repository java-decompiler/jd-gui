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

import java.io.*;

public class ClassFileLoaderProvider extends AbstractTypeFileLoaderProvider {
    protected static final String[] EXTENSIONS = { "class" };
    
    public String[] getExtensions() { return EXTENSIONS; }
    public String getDescription() { return "Class files (*.class)"; }

    public boolean accept(API api, File file) {
        return file.exists() && file.canRead() && file.getName().toLowerCase().endsWith(".class");
    }

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
