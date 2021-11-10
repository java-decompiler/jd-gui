/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.util.decompiler;

import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.api.loader.LoaderException;
import org.jd.gui.api.model.Container;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ContainerLoader implements Loader {
    protected byte[] buffer = new byte[1024 * 4];
    protected Container.Entry entry;

    public ContainerLoader() { this.entry = null; }
    public ContainerLoader(Container.Entry entry) {
        this.entry = entry;
    }

    public void setEntry(Container.Entry e) { this.entry = e; }

    protected Container.Entry getEntry(String internalPath) {
        String path = internalPath + ".class";

        if (entry.getPath().equals(path)) {
            return entry;
        }
        for (Container.Entry e : entry.getParent().getChildren()) {
            if (e.getPath().equals(path)) {
                return e;
            }
        }
        return null;
    }

    // --- Loader --- //
    @Override
    public boolean canLoad(String internalName) {
        return getEntry(internalName) != null;
    }

    @Override
    public byte[] load(String internalName) throws LoaderException {
        Container.Entry entry = getEntry(internalName);

        if (entry == null) {
            return null;
        } else {
            try (InputStream input=entry.getInputStream(); ByteArrayOutputStream output=new ByteArrayOutputStream()) {
                int len = input.read(buffer);

                while (len > 0) {
                    output.write(buffer, 0, len);
                    len = input.read(buffer);
                }

                return output.toByteArray();
            } catch (IOException e) {
                throw new LoaderException(e);
            }
        }
    }
}
