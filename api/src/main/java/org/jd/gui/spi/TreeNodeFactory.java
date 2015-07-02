/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.spi;

import javax.swing.tree.DefaultMutableTreeNode;

import org.jd.gui.api.API;
import org.jd.gui.api.feature.ContainerEntryGettable;
import org.jd.gui.api.feature.UriGettable;
import org.jd.gui.api.model.Container;

import java.util.regex.Pattern;

public interface TreeNodeFactory {
	public String[] getSelectors();

    public Pattern getPathPattern();

	public <T extends DefaultMutableTreeNode & ContainerEntryGettable & UriGettable> T make(API api, Container.Entry entry);
}
