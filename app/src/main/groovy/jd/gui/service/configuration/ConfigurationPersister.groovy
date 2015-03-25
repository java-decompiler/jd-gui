/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.configuration

import jd.gui.model.configuration.Configuration

interface ConfigurationPersister {
    Configuration load();

    void save(Configuration configuration);
}
