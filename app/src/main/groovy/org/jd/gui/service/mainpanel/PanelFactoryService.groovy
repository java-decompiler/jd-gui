/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
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
