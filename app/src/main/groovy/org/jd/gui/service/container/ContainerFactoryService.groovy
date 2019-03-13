/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.container

import org.jd.gui.api.API
import org.jd.gui.service.extension.ExtensionService
import org.jd.gui.spi.ContainerFactory

import java.nio.file.Path

@Singleton(lazy = true)
class ContainerFactoryService {
    protected final Collection<ContainerFactory> providers = ExtensionService.instance.load(ContainerFactory)
	
    ContainerFactory get(API api, Path rootPath) {
        return providers.find { it.accept(api, rootPath) }
    }
}
