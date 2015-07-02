/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.api.feature;

import org.jd.gui.api.API;

import javax.swing.*;

public interface PageCreator {
    public <T extends JComponent & UriGettable> T createPage(API api);
}
