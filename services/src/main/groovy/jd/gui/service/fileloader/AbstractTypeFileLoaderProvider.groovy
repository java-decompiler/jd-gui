/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.fileloader

import jd.gui.api.API
import jd.gui.api.feature.UriOpenable

import java.nio.file.Paths

abstract class AbstractTypeFileLoaderProvider extends AbstractFileLoaderProvider {

    protected boolean load(API api, File file, String pathInFile) {
        // Search root path
        String pathSuffix = pathInFile
        String path = file.path

        while (! path.endsWith(pathSuffix)) {
            int index = pathSuffix.indexOf(File.separator)

            if (index == -1) {
                pathSuffix = ''
            } else {
                pathSuffix = pathSuffix.substring(index+1)
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
                def rootUri = rootFile.toURI()
                def uri = new URI(rootUri.scheme, rootUri.host, rootUri.path + '!' + pathSuffix, null)
                mainPanel.openUri(uri)
                return true
            } else {
                return mainPanel != null
            }
        }
    }
}
