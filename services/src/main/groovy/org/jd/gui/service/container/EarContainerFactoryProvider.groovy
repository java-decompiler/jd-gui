/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use, 
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.container

import org.jd.gui.api.API
import org.jd.gui.api.model.Container
import org.jd.gui.model.container.EarContainer
import org.jd.gui.spi.ContainerFactory

import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path

class EarContainerFactoryProvider implements ContainerFactory {

    String getType() { 'ear' }

    boolean accept(API api, Path rootPath) {
        if (rootPath.toUri().toString().toLowerCase().endsWith('.ear!/')) {
            return true
        } else {
            // Extension: accept uncompressed EAR file containing a folder 'META-INF/application.xml'
            try {
                return rootPath.fileSystem.provider().scheme.equals('file') && Files.exists(rootPath.resolve('META-INF/application.xml'))
            } catch (InvalidPathException e) {
                return false
            }
        }
    }

    Container make(API api, Container.Entry parentEntry, Path rootPath) {
        return new EarContainer(api, parentEntry, rootPath)
    }
}
