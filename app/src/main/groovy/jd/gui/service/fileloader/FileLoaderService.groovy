/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.fileloader

import jd.gui.api.API
import jd.gui.spi.FileLoader

@Singleton(lazy = true)
class FileLoaderService {
	final List<FileLoader> providers = ServiceLoader.load(FileLoader).toList()
	
	private Map<String, FileLoader> mapProviders = providers.collectEntries { provider ->
		provider.extensions.collectEntries { [it, provider] }
	}

    FileLoader get(API api, File file) {
        String name = file.name
        int lastDot = name.lastIndexOf('.')
        String extension = name.substring(lastDot+1)
        def provider = mapProviders[extension]

        if (provider.accept(api, file)) {
            return provider
        } else {
            return null
        }
    }
}
