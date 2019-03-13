/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.api.model;

import javax.swing.*;

public interface TreeNodeData {
    public String getLabel();

    public String getTip();

    public Icon getIcon();

    public Icon getOpenIcon();
}