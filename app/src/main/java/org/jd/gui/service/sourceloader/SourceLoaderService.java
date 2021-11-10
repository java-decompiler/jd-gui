/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.sourceloader;

import org.jd.gui.api.API;
import org.jd.gui.api.model.Container;
import org.jd.gui.service.extension.ExtensionService;
import org.jd.gui.spi.SourceLoader;

import java.io.File;
import java.util.Collection;

public class SourceLoaderService {
    protected static final SourceLoaderService SOURCE_LOADER_SERVICE = new SourceLoaderService();

    public static SourceLoaderService getInstance() { return SOURCE_LOADER_SERVICE; }

    protected Collection<SourceLoader> providers = ExtensionService.getInstance().load(SourceLoader.class);

    public String getSource(API api, Container.Entry entry) {
        for (SourceLoader provider : providers) {
            String source = provider.getSource(api, entry);

            if ((source != null) && !source.isEmpty()) {
                return source;
            }
        }

        return null;
    }

    public String loadSource(API api, Container.Entry entry) {
        for (SourceLoader provider : providers) {
            String source = provider.loadSource(api, entry);

            if ((source != null) && !source.isEmpty()) {
                return source;
            }
        }

        return null;
    }

    public File getSourceFile(API api, Container.Entry entry) {
        for (SourceLoader provider : providers) {
            File file = provider.loadSourceFile(api, entry);

            if (file != null) {
                return file;
            }
        }

        return null;
    }
}
