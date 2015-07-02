/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.spi;

import org.jd.gui.api.API;

public interface PasteHandler {
    public boolean accept(Object obj);

    public void paste(API api, Object obj);
}
