/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.spi;

import org.jd.gui.api.API;
import org.jd.gui.api.model.Container;
import org.jd.gui.api.model.Type;

import java.util.Collection;
import java.util.regex.Pattern;

public interface TypeFactory {
	String[] getSelectors();

    Pattern getPathPattern();

	/**
	 * @return all root types contains in 'entry'
	 */
	Collection<Type> make(API api, Container.Entry entry);

	/**
     * @param fragment @see jd.gui.api.feature.UriOpenable
	 * @return if 'fragment' is null, return the main type in 'entry',
	 *         otherwise, return the type or sub-type matching with 'fragment'
	 */
	Type make(API api, Container.Entry entry, String fragment);
}
