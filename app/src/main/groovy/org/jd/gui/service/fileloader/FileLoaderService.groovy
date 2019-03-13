/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.fileloader

import org.jd.gui.api.API
import org.jd.gui.service.extension.ExtensionService
import org.jd.gui.spi.FileLoader

@Singleton(lazy = true)
class FileLoaderService {
    protected final Collection<FileLoader> providers = ExtensionService.instance.load(FileLoader)
	
	protected Map<String, FileLoader> mapProviders = providers.collectEntries { provider ->
		provider.extensions.collectEntries { [it, provider] }
	}

    FileLoader get(API api, File file) {
        String name = file.name
        int lastDot = name.lastIndexOf('.')
        String extension = name.substring(lastDot+1)
        def provider = mapProviders[extension]

        if (provider?.accept(api, file)) {
            return provider
        } else {
            return null
        }
    }
}
