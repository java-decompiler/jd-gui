/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.uriloader

import org.jd.gui.api.API
import org.jd.gui.spi.UriLoader

class FileUriLoaderProvider implements UriLoader {

	String[] getSchemes() { ['file'] }
	
	boolean accept(API api, URI uri) { 'file'.equals(uri.scheme) }
	
	boolean load(API api, URI uri) {
        File file = new File(uri.path)
        return api.getFileLoader(file)?.load(api, file)
	}
}
