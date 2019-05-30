/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use, 
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.container;

import org.jd.gui.api.API;
import org.jd.gui.api.model.Container;
import org.jd.gui.model.container.GenericContainer;
import org.jd.gui.spi.ContainerFactory;

import java.nio.file.Path;

public class GenericContainerFactoryProvider implements ContainerFactory {
    @Override
    public String getType() { return "generic"; }

    @Override
    public boolean accept(API api, Path rootPath) { return true; }

    @Override
    public Container make(API api, Container.Entry parentEntry, Path rootPath) {
        return new GenericContainer(api, parentEntry, rootPath);
    }
}
