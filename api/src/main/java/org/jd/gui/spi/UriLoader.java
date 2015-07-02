/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.spi;

import org.jd.gui.api.API;

import java.net.URI;

public interface UriLoader {
	public String[] getSchemes();
	
	public boolean accept(API api, URI uri);
	
	public boolean load(API api, URI uri);
}
