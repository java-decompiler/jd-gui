/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.util.decompiler;

import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.api.loader.LoaderException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ClassPathLoader implements Loader {
    protected byte[] buffer = new byte[1024 * 4];

    @Override
    public boolean canLoad(String internalName) {
        return this.getClass().getResource("/" + internalName + ".class") != null;
    }

    @Override
    public byte[] load(String internalName) throws LoaderException {
        InputStream is = this.getClass().getResourceAsStream("/" + internalName + ".class");

        if (is == null) {
            return null;
        } else {
            try (InputStream input=is; ByteArrayOutputStream output=new ByteArrayOutputStream()) {
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
