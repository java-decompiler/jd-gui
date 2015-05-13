/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.container

import jd.gui.api.API
import jd.gui.api.model.Container
import jd.gui.model.container.JarContainer
import jd.gui.spi.ContainerFactory

import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path

class JarContainerFactoryProvider implements ContainerFactory {

	String getType() { 'jar' }

	boolean accept(API api, Path rootPath) {
        if (rootPath.toUri().toString().toLowerCase().endsWith('.jar!/')) {
            // Specification: http://docs.oracle.com/javase/6/docs/technotes/guides/jar/jar.html
            return true
        } else {
            // Extension: accept uncompressed JAR file containing a folder 'META-INF'
            try {
                return rootPath.fileSystem.provider().scheme.equals('file') && Files.exists(rootPath.resolve('META-INF'))
            } catch (InvalidPathException e) {
                return false
            }
        }
    }

    Container make(API api, Container.Entry parentEntry, Path rootPath) {
		return new JarContainer(api, parentEntry, rootPath)
	}
}
