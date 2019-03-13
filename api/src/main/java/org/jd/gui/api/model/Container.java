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
    public String getType();

    public Entry getRoot();

    /**
     * File or directory
     */
    public interface Entry {
        public Container getContainer();

        public Entry getParent();

        public URI getUri();

        public String getPath();

        public boolean isDirectory();

        public long length();

        public InputStream getInputStream();

        public Collection<Entry> getChildren();
    }
}
