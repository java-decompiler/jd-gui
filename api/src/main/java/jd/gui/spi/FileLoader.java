/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.spi;

import jd.gui.api.API;

import java.io.File;

public interface FileLoader {
	public String[] getExtensions();
	
	public String getDescription();
	
	public boolean accept(API api, File file);
	
	public boolean load(API api, File file);
}
