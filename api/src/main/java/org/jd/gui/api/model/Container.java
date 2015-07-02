/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
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
