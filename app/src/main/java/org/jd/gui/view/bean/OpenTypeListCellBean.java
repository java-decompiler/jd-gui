/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view.bean;

import org.jd.gui.api.model.Container;

import javax.swing.*;
import java.util.Collection;

public class OpenTypeListCellBean {
    public String label;
    public String packag;
    public Icon icon;
    public Collection<Container.Entry> entries;
    public String typeName;

    public OpenTypeListCellBean(String label, Collection<Container.Entry> entries, String typeName) {
        this.label = label;
        this.entries = entries;
        this.typeName = typeName;
    }

    public OpenTypeListCellBean(String label, String packag, Icon icon, Collection<Container.Entry> entries, String typeName) {
        this.label = label;
        this.packag = packag;
        this.icon = icon;
        this.entries = entries;
        this.typeName = typeName;
    }
}
