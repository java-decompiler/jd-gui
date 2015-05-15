/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.model.container

import groovy.transform.CompileStatic
import jd.gui.api.API
import jd.gui.api.model.Container

import java.nio.file.Path

@CompileStatic
class EarContainer extends GenericContainer {

    EarContainer(API api, Container.Entry parentEntry, Path rootPath) {
        super(api, parentEntry, rootPath)
    }

    String getType() { 'ear' }
}
