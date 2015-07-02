/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.service.container

import org.jd.gui.api.API
import org.jd.gui.api.model.Container
import org.jd.gui.model.container.GenericContainer
import org.jd.gui.spi.ContainerFactory

import java.nio.file.Path

class GenericContainerFactoryProvider implements ContainerFactory {

	String getType() { 'generic' }
	
	boolean accept(API api, Path rootPath) { true }

    Container make(API api, Container.Entry parentEntry, Path rootPath) {
		return new GenericContainer(api, parentEntry, rootPath)
	}
}
