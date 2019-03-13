/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.mainpanel

import org.jd.gui.api.model.Container
import org.jd.gui.service.extension.ExtensionService
import org.jd.gui.spi.PanelFactory

@Singleton(lazy = true)
class PanelFactoryService {
    protected Map<String, PanelFactory> mapProviders = populate()

    protected Map<String, PanelFactory> populate() {
        Collection<PanelFactory> providers = ExtensionService.instance.load(PanelFactory)
        Map<String, PanelFactory> mapProviders = [:]

        for (def provider : providers) {
            for (String type : provider.types) {
                mapProviders.put(type, provider)
            }
        }

        return mapProviders
    }

    PanelFactory get(Container container) {
        return mapProviders.get(container.type) ?: mapProviders.get('default')
    }
}
