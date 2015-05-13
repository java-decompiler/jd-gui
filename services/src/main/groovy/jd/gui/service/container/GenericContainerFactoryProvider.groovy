/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.container

import jd.gui.api.API
import jd.gui.api.model.Container
import jd.gui.model.container.GenericContainer
import jd.gui.spi.ContainerFactory

import java.nio.file.Path

class GenericContainerFactoryProvider implements ContainerFactory {

	String getType() { 'generic' }
	
	boolean accept(API api, Path rootPath) { true }

    Container make(API api, Container.Entry parentEntry, Path rootPath) {
		return new GenericContainer(api, parentEntry, rootPath)
	}
}
