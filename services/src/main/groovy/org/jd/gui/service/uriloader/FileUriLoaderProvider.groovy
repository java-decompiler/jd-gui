/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
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
