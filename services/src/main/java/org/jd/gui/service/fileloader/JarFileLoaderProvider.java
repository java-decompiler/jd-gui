/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use, 
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.fileloader;

import org.jd.gui.api.API;

import java.io.File;

public class JarFileLoaderProvider extends ZipFileLoaderProvider {
	protected static final String[] EXTENSIONS = { "jar" };

	public String[] getExtensions() { return EXTENSIONS; }
	public String getDescription() { return "Jar files (*.jar)"; }

	public boolean accept(API api, File file) {
        return file.exists() && file.canRead() && file.getName().toLowerCase().endsWith(".jar");
	}
}
