/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.model.container

import groovy.transform.CompileStatic
import org.jd.gui.api.API
import org.jd.gui.api.model.Container

import java.nio.file.Path

@CompileStatic
class WarContainer extends GenericContainer {

    WarContainer(API api, Container.Entry parentEntry, Path rootPath) {
        super(api, parentEntry, rootPath)
    }

    String getType() { 'war' }
}
