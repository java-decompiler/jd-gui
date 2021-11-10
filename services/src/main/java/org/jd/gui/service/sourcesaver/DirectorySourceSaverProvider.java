/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.sourcesaver;

import org.jd.gui.api.API;
import org.jd.gui.api.model.Container;
import org.jd.gui.spi.SourceSaver;
import org.jd.gui.util.exception.ExceptionUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

public class DirectorySourceSaverProvider extends AbstractSourceSaverProvider {

    @Override public String[] getSelectors() { return appendSelectors("*:dir:*"); }

    @Override public String getSourcePath(Container.Entry entry) { return entry.getPath() + ".src.zip"; }

    @Override public int getFileCount(API api, Container.Entry entry) { return getFileCount(api, entry.getChildren()); }

    protected int getFileCount(API api, Collection<Container.Entry> entries) {
        int count = 0;

        for (Container.Entry e : entries) {
            SourceSaver sourceSaver = api.getSourceSaver(e);

            if (sourceSaver != null) {
                count += sourceSaver.getFileCount(api, e);
            }
        }

        return count;
    }

    @Override
    public void save(API api, SourceSaver.Controller controller, SourceSaver.Listener listener, Path rootPath, Container.Entry entry) {
        Path path = rootPath.resolve(entry.getPath());

        try {
            Files.createDirectories(path);
            saveContent(api, controller, listener, rootPath, path, entry);
        } catch (IOException e) {
            assert ExceptionUtil.printStackTrace(e);
        }
    }

    @Override
    public void saveContent(API api, SourceSaver.Controller controller, SourceSaver.Listener listener, Path rootPath, Path path, Container.Entry entry) {
        for (Container.Entry e : getChildren(entry)) {
            if (controller.isCancelled()) {
                break;
            }

            SourceSaver sourceSaver = api.getSourceSaver(e);

            if (sourceSaver != null) {
                sourceSaver.save(api, controller, listener, rootPath, e);
            }
        }
    }

    protected Collection<Container.Entry> getChildren(Container.Entry entry) { return entry.getChildren(); }
}
