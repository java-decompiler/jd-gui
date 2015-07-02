/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.service.container

import org.jd.gui.api.API
import org.jd.gui.spi.ContainerFactory

import java.nio.file.Path

@Singleton(lazy = true)
class ContainerFactoryService {
    private List<ContainerFactory> providers = ServiceLoader.load(ContainerFactory).toList()
	
    ContainerFactory get(API api, Path rootPath) {
        return providers.find { it.accept(api, rootPath) }
    }
}
