/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.util.decompiler;

import jd.core.loader.Loader;
import jd.core.loader.LoaderException;
import org.jd.gui.api.model.Container;

import java.io.DataInputStream;

public class ContainerLoader implements Loader {
    protected Container.Entry entry;

    public ContainerLoader() { this.entry = null; }
    public ContainerLoader(Container.Entry entry) {
        this.entry = entry;
    }

    protected Container.Entry getEntry(String internalPath) {
        for (Container.Entry e : entry.getParent().getChildren()) {
            if (e.getPath().equals(internalPath)) {
                return e;
            }
        }
        return null;
    }

    public void setEntry(Container.Entry e) { this.entry = e; }

    // --- Loader --- //
    public DataInputStream load(String internalPath) throws LoaderException {
        return new DataInputStream(getEntry(internalPath).getInputStream());
    }

    public boolean canLoad(String internalPath) {
        return getEntry(internalPath) != null;
    }
}
