/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.spi;

import org.jd.gui.api.API;

import java.io.File;

public interface FileLoader {
	String[] getExtensions();
	
	String getDescription();
	
	boolean accept(API api, File file);
	
	boolean load(API api, File file);
}
