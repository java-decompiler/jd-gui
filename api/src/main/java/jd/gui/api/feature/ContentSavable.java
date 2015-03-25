/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.api.feature;

import jd.gui.api.API;

import java.io.OutputStream;

public interface ContentSavable {
    public String getFileName();

    public void save(API api, OutputStream os);
}
