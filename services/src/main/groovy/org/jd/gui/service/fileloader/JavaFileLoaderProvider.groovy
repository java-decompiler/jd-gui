/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use, 
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.fileloader

import org.jd.gui.api.API

import java.util.regex.Pattern

class JavaFileLoaderProvider extends AbstractTypeFileLoaderProvider {

    String[] getExtensions() { ['java'] }
    String getDescription() { 'Java files (*.java)' }

    boolean accept(API api, File file) {
        return file.exists() && file.canRead() && file.name.toLowerCase().endsWith('.java')
    }

    boolean load(API api, File file) {
        def pattern = Pattern.compile('(?s)(.*\\s)?package\\s+(\\S+)\\s*;.*')
        def matcher = file.text =~ pattern

        if (matcher.matches()) {
            // Package name found
            def pathInFile = matcher[0][2].replace('.', File.separator) + File.separator + file.name

            return load(api, file, pathInFile)
        } else {
            // Package name not found
            return load(api, file, file.name)
        }
    }
}
