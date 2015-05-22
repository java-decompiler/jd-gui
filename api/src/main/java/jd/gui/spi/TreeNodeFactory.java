/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.spi;

import javax.swing.tree.DefaultMutableTreeNode;

import jd.gui.api.API;
import jd.gui.api.feature.UriGettable;
import jd.gui.api.model.Container;

import java.util.regex.Pattern;

public interface TreeNodeFactory {
	public String[] getSelectors();

    public Pattern getPathPattern();

	public <T extends DefaultMutableTreeNode & UriGettable> T make(API api, Container.Entry entry);
}
