/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use, 
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.fileloader

import org.jd.gui.api.API

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
