/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.spi;

import jd.gui.api.API;
import jd.gui.api.model.Container;
import jd.gui.api.model.Indexes;

import java.util.regex.Pattern;

public interface Indexer {
    public String[] getTypes();

    public Pattern getPathPattern();

    public void index(API api, Container.Entry entry, Indexes indexes);
}
