/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.service.pastehandler

import org.jd.gui.service.extension.ExtensionService
import org.jd.gui.spi.PasteHandler

@Singleton(lazy = true)
class PasteHandlerService {
    protected final Collection<PasteHandler> providers = ExtensionService.instance.load(PasteHandler)

    PasteHandler get(Object obj) {
        for (def provider : providers) {
            if (provider.accept(obj)) {
                return provider
            }
        }
        return null
    }
}
