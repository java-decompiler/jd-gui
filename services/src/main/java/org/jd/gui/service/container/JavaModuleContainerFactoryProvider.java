/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use, 
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.container;

import org.jd.gui.api.API;
import org.jd.gui.api.model.Container;
import org.jd.gui.model.container.JavaModuleContainer;
import org.jd.gui.spi.ContainerFactory;
import org.jd.gui.util.exception.ExceptionUtil;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class JavaModuleContainerFactoryProvider implements ContainerFactory {
    @Override
    public String getType() { return "jmod"; }

    @Override
    public boolean accept(API api, Path rootPath) {
        if (rootPath.toUri().toString().toLowerCase().endsWith(".jmod!/")) {
            return true;
        } else {
            // Extension: accept uncompressed JMOD file containing a folder 'classes'
            try {
                return rootPath.getFileSystem().provider().getScheme().equals("file") && Files.exists(rootPath.resolve("classes"));
            } catch (InvalidPathException e) {
                assert ExceptionUtil.printStackTrace(e);
                return false;
            }
        }
    }

    @Override
    public Container make(API api, Container.Entry parentEntry, Path rootPath) {
        return new JavaModuleContainer(api, parentEntry, rootPath);
    }
}
