/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.sourcesaver;

import org.jd.gui.api.model.Container;
import org.jd.gui.util.container.JarContainerEntryUtil;

import java.util.Collection;

public class PackageSourceSaverProvider extends DirectorySourceSaverProvider {

    @Override public String[] getSelectors() { return appendSelectors("jar:dir:*", "war:dir:*", "ear:dir:*"); }

    @Override
    protected Collection<Container.Entry> getChildren(Container.Entry entry) {
        return JarContainerEntryUtil.removeInnerTypeEntries(entry.getChildren());
    }
}
