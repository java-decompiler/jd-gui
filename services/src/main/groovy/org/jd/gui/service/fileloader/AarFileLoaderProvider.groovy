/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.service.fileloader

import org.jd.gui.api.API

class AarFileLoaderProvider extends ZipFileLoaderProvider {

	String[] getExtensions() { ['aar'] }
	String getDescription() { 'Android archive files (*.aar)' }

	boolean accept(API api, File file) {
        return file.exists() && file.canRead() && file.name.toLowerCase().endsWith('.aar')
	}
}
