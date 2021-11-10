/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.fileloader;

import org.jd.gui.api.API;
import org.jd.gui.service.extension.ExtensionService;
import org.jd.gui.spi.FileLoader;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;

public class FileLoaderService {
    protected static final FileLoaderService FILE_LOADER_SERVICE = new FileLoaderService();

    public static FileLoaderService getInstance() { return FILE_LOADER_SERVICE; }

    protected final Collection<FileLoader> providers = ExtensionService.getInstance().load(FileLoader.class);

    protected HashMap<String, FileLoader> mapProviders = new HashMap<>();

    protected FileLoaderService() {
        for (FileLoader provider : providers) {
            for (String extension : provider.getExtensions()) {
                mapProviders.put(extension, provider);
            }
        }
    }

    public FileLoader get(API api, File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        String extension = name.substring(lastDot+1);
        FileLoader provider = mapProviders.get(extension);
        return provider;
    }

    public HashMap<String, FileLoader> getMapProviders() {
        return mapProviders;
    }
}
