/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.configuration;

import org.jd.gui.model.configuration.Configuration;

public interface ConfigurationPersister {
    Configuration load();

    void save(Configuration configuration);
}
