/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.pastehandler

import org.jd.gui.api.API
import org.jd.gui.spi.PasteHandler
import org.jd.gui.view.component.LogPage

class LogPasteHandler implements PasteHandler {
    protected static int counter = 0

    boolean accept(Object obj) { obj instanceof String }

    void paste(API api, Object obj) {
        def title = 'clipboard-' + (++counter) + '.log'
        def uri = URI.create('memory://' + title)
        def content = obj?.toString()
        api.addPanel(title, null, null, new LogPage(api, uri, content))
    }
}
