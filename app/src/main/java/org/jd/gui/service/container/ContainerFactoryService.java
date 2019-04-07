/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.container;

import org.jd.gui.api.API;
import org.jd.gui.service.extension.ExtensionService;
import org.jd.gui.spi.ContainerFactory;

import java.nio.file.Path;
import java.util.Collection;

public class ContainerFactoryService {
    protected static final ContainerFactoryService CONTAINER_FACTORY_SERVICE = new ContainerFactoryService();

    public static ContainerFactoryService getInstance() { return CONTAINER_FACTORY_SERVICE; }

    protected final Collection<ContainerFactory> providers = ExtensionService.getInstance().load(ContainerFactory.class);

    public ContainerFactory get(API api, Path rootPath) {
        for (ContainerFactory containerFactory : providers) {
            if (containerFactory.accept(api, rootPath)) {
                return containerFactory;
            }
        }

        return null;
    }
}
