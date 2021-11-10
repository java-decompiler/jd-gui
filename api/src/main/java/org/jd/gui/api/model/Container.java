/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.api.model;

import java.io.InputStream;
import java.net.URI;
import java.util.Collection;

public interface Container {
    String getType();

    Entry getRoot();

    /**
     * File or directory
     */
    interface Entry {
        Container getContainer();

        Entry getParent();

        URI getUri();

        String getPath();

        boolean isDirectory();

        long length();

        InputStream getInputStream();

        Collection<Entry> getChildren();
    }
}
