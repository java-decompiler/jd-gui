/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.fileloader

import groovyjarjarasm.asm.ClassReader
import jd.gui.api.API
import jd.gui.api.feature.UriOpenable

import java.nio.file.Paths

class ClassFileLoaderProvider extends AbstractFileLoaderProvider {

    String[] getExtensions() { ['class'] }
    String getDescription() { 'Class files (*.class)' }

    boolean accept(API api, File file) {
        return file.exists() && file.canRead() && file.name.toLowerCase().endsWith('.class')
    }

    boolean load(API api, File file) {
        file.withInputStream { is ->
            ClassReader classReader = new ClassReader(is)

            // Search root path
            def pathSuffix = classReader.className.replace('/', File.separator) + '.class'
            def path = file.path

            while (! path.endsWith(pathSuffix)) {
                int index = pathSuffix.indexOf(File.separatorChar)

                if (index == -1) {
                    pathSuffix = ''
                } else {
                    pathSuffix = pathSuffix.substring(index)
                }
            }

            if (pathSuffix) {
                // Init root file
                File rootFile = file
                int index = pathSuffix.indexOf(File.separator)

                while (index != -1) {
                    rootFile = rootFile.parentFile
                    pathSuffix = pathSuffix.substring(index+1)
                    index = pathSuffix.indexOf(File.separator)
                }
                rootFile = rootFile.parentFile

                // Create panel
                def mainPanel = load(api, rootFile, Paths.get(rootFile.toURI()))

                if (mainPanel instanceof UriOpenable) {
                    // Open page
                    pathSuffix = file.absolutePath.substring(rootFile.absolutePath.length()).replace(File.separator, '/')
                    def uri = URI.create(rootFile.toURI().toString() + '!' + pathSuffix)
                    mainPanel.openUri(uri)
                    return true
                } else {
                    return mainPanel != null
                }
            }
        }
    }
}
