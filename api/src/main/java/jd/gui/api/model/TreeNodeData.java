/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.api.model;

import javax.swing.*;

public interface TreeNodeData {
    public String getLabel();

    public String getTip();

    public Icon getIcon();

    public Icon getOpenIcon();
}