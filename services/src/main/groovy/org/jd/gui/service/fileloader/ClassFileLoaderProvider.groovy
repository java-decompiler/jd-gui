/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use, 
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.fileloader

import groovyjarjarasm.asm.ClassReader
import org.jd.gui.api.API

class ClassFileLoaderProvider extends AbstractTypeFileLoaderProvider {

    String[] getExtensions() { ['class'] }
    String getDescription() { 'Class files (*.class)' }

    boolean accept(API api, File file) {
        return file.exists() && file.canRead() && file.name.toLowerCase().endsWith('.class')
    }

    boolean load(API api, File file) {
        file.withInputStream { is ->
            def classReader = new ClassReader(is)
            def pathInFile = classReader.className.replace('/', File.separator) + '.class'

            return load(api, file, pathInFile)
        }
    }
}
