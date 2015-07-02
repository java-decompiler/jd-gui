/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
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
