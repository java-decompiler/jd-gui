/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.service.configuration

@Singleton
class ConfigurationPersisterService {
    private ConfigurationPersister provider = new ConfigurationXmlPersisterProvider()

    ConfigurationPersister get() {
        return provider
    }
}
