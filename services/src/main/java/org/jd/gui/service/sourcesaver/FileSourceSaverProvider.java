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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FileSourceSaverProvider extends AbstractSourceSaverProvider {

    @Override public String[] getSelectors() { return appendSelectors("*:file:*"); }

    @Override public String getSourcePath(Container.Entry entry) { return entry.getPath(); }

    @Override public int getFileCount(API api, Container.Entry entry) { return 1; }

    @Override
    public void save(API api, SourceSaver.Controller controller, SourceSaver.Listener listener, Path rootPath, Container.Entry entry) {
        saveContent(api, controller, listener, rootPath, rootPath.resolve(entry.getPath()), entry);
    }

    @Override
    public void saveContent(API api, SourceSaver.Controller controller, SourceSaver.Listener listener, Path rootPath, Path path, Container.Entry entry) {
        listener.pathSaved(path);

        try (InputStream is = entry.getInputStream()) {
            Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            assert ExceptionUtil.printStackTrace(e);

            try (BufferedWriter writer = Files.newBufferedWriter(path, Charset.defaultCharset())) {
                writer.write("// INTERNAL ERROR //");
            } catch (IOException ee) {
                assert ExceptionUtil.printStackTrace(ee);
            }
        }
    }
}
