/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.api.feature;

import org.jd.gui.api.API;

import java.io.OutputStream;

public interface ContentSavable {
    public String getFileName();

    public void save(API api, OutputStream os);
}
