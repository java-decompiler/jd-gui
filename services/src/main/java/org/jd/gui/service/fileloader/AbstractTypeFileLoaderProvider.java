/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use, 
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.fileloader;

import org.jd.gui.api.API;
import org.jd.gui.api.feature.UriOpenable;
import org.jd.gui.util.exception.ExceptionUtil;

import javax.swing.*;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;

public abstract class AbstractTypeFileLoaderProvider extends AbstractFileLoaderProvider {
    protected boolean load(API api, File file, String pathInFile) {
        // Search root path
        String pathSuffix = pathInFile;
        String path = file.getPath();

        while (! path.endsWith(pathSuffix)) {
            int index = pathSuffix.indexOf(File.separator);

            if (index == -1) {
                pathSuffix = "";
            } else {
                pathSuffix = pathSuffix.substring(index+1);
            }
        }

        if (! pathSuffix.isEmpty()) {
            // Init root file
            File rootFile = file;
            int index = pathSuffix.indexOf(File.separator);

            while (index != -1) {
                rootFile = rootFile.getParentFile();
                pathSuffix = pathSuffix.substring(index+1);
                index = pathSuffix.indexOf(File.separator);
            }
            rootFile = rootFile.getParentFile();

            // Create panel
            JComponent mainPanel = load(api, rootFile, Paths.get(rootFile.toURI()));

            if (mainPanel instanceof UriOpenable) {
                try {
                    // Open page
                    pathSuffix = file.getAbsolutePath().substring(rootFile.getAbsolutePath().length()).replace(File.separator, "/");
                    URI rootUri = rootFile.toURI();
                    URI uri = new URI(rootUri.getScheme(), rootUri.getHost(), rootUri.getPath() + '!' + pathSuffix, null);
                    ((UriOpenable)mainPanel).openUri(uri);
                    return true;
                } catch (URISyntaxException e) {
                    assert ExceptionUtil.printStackTrace(e);
                }
            } else {
                return mainPanel != null;
            }
        }

        return false;
    }
}
