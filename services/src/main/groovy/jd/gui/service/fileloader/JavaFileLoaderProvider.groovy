/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.fileloader

import jd.gui.api.API

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
