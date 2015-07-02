/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.spi;

import org.jd.gui.api.API;
import org.jd.gui.api.model.Container;
import org.jd.gui.api.model.Type;

import java.util.Collection;
import java.util.regex.Pattern;

public interface TypeFactory {
	public String[] getSelectors();

    public Pattern getPathPattern();

	/**
	 * @return all root types contains in 'entry'
	 */
	public Collection<Type> make(API api, Container.Entry entry);

	/**
     * @param fragment @see jd.gui.api.feature.UriOpenable
	 * @return if 'fragment' is null, return the main type in 'entry',
	 *         otherwise, return the type or sub-type matching with 'fragment'
	 */
	public Type make(API api, Container.Entry entry, String fragment);
}
