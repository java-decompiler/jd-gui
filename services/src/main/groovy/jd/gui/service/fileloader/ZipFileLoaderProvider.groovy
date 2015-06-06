/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.fileloader

import jd.gui.api.API

import java.nio.file.FileSystemNotFoundException
import java.nio.file.FileSystems

class ZipFileLoaderProvider extends AbstractFileLoaderProvider {

	String[] getExtensions() { ['zip'] }
	String getDescription() { 'Zip files (*.zip)' }
	
	boolean accept(API api, File file) {
        return file.exists() && file.canRead() && file.name.toLowerCase().endsWith('.zip')
    }

    boolean load(API api, File file) {
        def fileUri = file.toURI()
        def uri = new URI('jar:' + fileUri.scheme, fileUri.host, fileUri.path + '!/', null)
        def fileSystem

        try {
            fileSystem = FileSystems.getFileSystem(uri)
        } catch (FileSystemNotFoundException e) {
            fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap())
        }

        if (fileSystem) {
            def rootDirectories = fileSystem.rootDirectories.iterator()

            if (rootDirectories.hasNext()) {
                return load(api, file, rootDirectories.next()) != null
            }
        }

        return false
	}
}
