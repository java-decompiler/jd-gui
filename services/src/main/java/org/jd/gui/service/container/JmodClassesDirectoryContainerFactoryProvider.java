/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use, 
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.container;

import org.jd.gui.api.API;
import org.jd.gui.api.model.Container;
import org.jd.gui.model.container.JmodClassesDirectoryContainer;
import org.jd.gui.spi.ContainerFactory;

import java.nio.file.Files;
import java.nio.file.Path;

public class JmodClassesDirectoryContainerFactoryProvider implements ContainerFactory {
    @Override
    public String getType() { return "jmodClassesDirectory"; }

    @Override
    public boolean accept(API api, Path rootPath) {
        if (Files.isDirectory(rootPath)) {
            Path fileName = rootPath.getFileName();

            if ((fileName != null) && "classes".equals(rootPath.getFileName().toString())) {
                String fileStoreName = rootPath.getFileSystem().getFileStores().iterator().next().name();
                return  fileStoreName.endsWith(".jmod/");
            }
        }

        return false;
    }

    @Override
    public Container make(API api, Container.Entry parentEntry, Path rootPath) {
        return new JmodClassesDirectoryContainer(api, parentEntry, rootPath);
    }
}
