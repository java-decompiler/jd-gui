/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.service.sourcesaver

import org.jd.gui.api.model.Container
import org.jd.gui.util.JarContainerEntryUtil

class PackageSourceSaverProvider extends DirectorySourceSaverProvider {
    /**
     * @return local + optional external selectors
     */
    @Override String[] getSelectors() { ['jar:dir:*', 'war:dir:*', 'ear:dir:*'] + externalSelectors }

    @Override
    protected Collection<Container.Entry> getChildren(Container.Entry entry) {
        JarContainerEntryUtil.removeInnerTypeEntries(entry.children)
    }
}
