/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.pastehandler;

import org.jd.gui.service.extension.ExtensionService;
import org.jd.gui.spi.PasteHandler;

import java.util.Collection;

public class PasteHandlerService {
    protected static final PasteHandlerService PASTE_HANDLER_SERVICE = new PasteHandlerService();

    public static PasteHandlerService getInstance() { return PASTE_HANDLER_SERVICE; }

    protected final Collection<PasteHandler> providers = ExtensionService.getInstance().load(PasteHandler.class);

    public PasteHandler get(Object obj) {
        for (PasteHandler provider : providers) {
            if (provider.accept(obj)) {
                return provider;
            }
        }
        return null;
    }
}
