/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.spi;

import jd.gui.api.API;
import jd.gui.api.feature.UriGettable;
import jd.gui.api.model.Container;

import javax.swing.JComponent;

public interface PanelFactory {
	public String[] getTypes();
	
	public <T extends JComponent & UriGettable> T make(API api, Container container);
}
