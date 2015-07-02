/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.api.feature;

import javax.swing.*;

public interface PageChangeListener {
    public <T extends JComponent & UriGettable> void pageChanged(T page);
}
