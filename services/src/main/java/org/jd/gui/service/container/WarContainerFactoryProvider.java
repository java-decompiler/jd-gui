/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use, 
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.container;

import org.jd.gui.api.API;
import org.jd.gui.api.model.Container;
import org.jd.gui.model.container.WarContainer;
import org.jd.gui.spi.ContainerFactory;
import org.jd.gui.util.exception.ExceptionUtil;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class WarContainerFactoryProvider implements ContainerFactory {
    @Override
    public String getType() { return "war"; }

    @Override
    public boolean accept(API api, Path rootPath) {
        if (rootPath.toUri().toString().toLowerCase().endsWith(".war!/")) {
            return true;
        } else {
            // Extension: accept uncompressed WAR file containing a folder 'WEB-INF'
            try {
                return rootPath.getFileSystem().provider().getScheme().equals("file") && Files.exists(rootPath.resolve("WEB-INF"));
            } catch (InvalidPathException e) {
                assert ExceptionUtil.printStackTrace(e);
                return false;
            }
        }
    }

    @Override
    public Container make(API api, Container.Entry parentEntry, Path rootPath) {
        return new WarContainer(api, parentEntry, rootPath);
    }
}
