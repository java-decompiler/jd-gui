/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.spi;

import org.jd.gui.api.API;
import org.jd.gui.api.model.Container;

import java.io.File;

public interface SourceLoader {
    String getSource(API api, Container.Entry entry);

    String loadSource(API api, Container.Entry entry);

    File loadSourceFile(API api, Container.Entry entry);
}
