/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use, 
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.fileloader;

import org.jd.gui.api.API;
import org.jd.gui.util.io.TextReader;
import org.jd.gui.view.component.LogPage;

import java.io.File;

public class LogFileLoaderProvider extends ZipFileLoaderProvider {
    protected static final String[] EXTENSIONS = { "log" };

    @Override public String[] getExtensions() { return EXTENSIONS; }
    @Override public String getDescription() { return "Log files (*.log)"; }

    @Override
    public boolean accept(API api, File file) {
        return file.exists() && file.isFile() && file.canRead() && file.getName().toLowerCase().endsWith(".log");
    }

    @Override
    public boolean load(API api, File file) {
        api.addPanel(file.getName(), null, "Location: " + file.getAbsolutePath(), new LogPage(api, file.toURI(), TextReader.getText(file)));
        return true;
    }
}
