/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.spi;

import jd.gui.api.API;
import jd.gui.api.model.Container;
import jd.gui.api.model.Type;

import java.util.regex.Pattern;

public interface TypeFactory {
	public String[] getTypes();

    public Pattern getPathPattern();

	public Type make(API api, Container.Entry entry, String fragment);
}
