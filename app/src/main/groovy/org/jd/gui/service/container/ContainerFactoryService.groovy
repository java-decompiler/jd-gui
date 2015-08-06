/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
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
