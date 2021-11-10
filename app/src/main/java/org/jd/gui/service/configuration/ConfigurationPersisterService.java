/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.configuration;

public class ConfigurationPersisterService {
    protected static final ConfigurationPersisterService CONFIGURATION_PERSISTER_SERVICE = new ConfigurationPersisterService();

    protected ConfigurationPersister provider = new ConfigurationXmlPersisterProvider();

    public static ConfigurationPersisterService getInstance() { return CONFIGURATION_PERSISTER_SERVICE; }

    protected ConfigurationPersisterService() {}

    public ConfigurationPersister get() {
        return provider;
    }
}
