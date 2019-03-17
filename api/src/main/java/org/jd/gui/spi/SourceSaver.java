/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.spi;

import org.jd.gui.api.API;
import org.jd.gui.api.model.Container;

import java.nio.file.Path;
import java.util.regex.Pattern;

public interface SourceSaver {
    String[] getSelectors();

    Pattern getPathPattern();

    String getSourcePath(Container.Entry entry);

    int getFileCount(API api, Container.Entry entry);

    /**
     * Check parent path, build source file name, create NIO path and save the content.
     */
    void save(API api, Controller controller, Listener listener, Path rootPath, Container.Entry entry);

    /**
     * Save content:
     * <ul>
     * <li>For file, save the source content.</li>
     * <li>For directory, call 'save' for each children.</li>
     * </ul>
     */
    void saveContent(API api, Controller controller, Listener listener, Path rootPath, Path path, Container.Entry entry);

    interface Controller {
        boolean isCancelled();
    }

    interface Listener {
        void pathSaved(Path path);
    }
}
