/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.container

import jd.gui.api.API
import jd.gui.api.model.Container
import jd.gui.model.container.WarContainer
import jd.gui.spi.ContainerFactory

import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path

class WarContainerFactoryProvider implements ContainerFactory {

    String getType() { 'war' }

    boolean accept(API api, Path rootPath) {
        if (rootPath.toUri().toString().toLowerCase().endsWith('.war!/')) {
            return true
        } else {
            // Extension: accept uncompressed JAR file containing a folder 'WEB-INF'
            try {
                return rootPath.fileSystem.provider().scheme.equals('file') && Files.exists(rootPath.resolve('WEB-INF'))
            } catch (InvalidPathException e) {
                return false
            }
        }
    }

    Container make(API api, Container.Entry parentEntry, Path rootPath) {
        return new WarContainer(api, parentEntry, rootPath)
    }
}
