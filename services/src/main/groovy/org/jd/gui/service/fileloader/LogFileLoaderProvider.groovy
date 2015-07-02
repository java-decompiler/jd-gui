/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.service.fileloader

import org.jd.gui.api.API
import org.jd.gui.view.component.LogPage

class LogFileLoaderProvider extends ZipFileLoaderProvider {

    String[] getExtensions() { ['log'] }
    String getDescription() { 'Log files (*.log)' }

    boolean accept(API api, File file) {
        return file.exists() && file.canRead() && file.name.toLowerCase().endsWith('.log')
    }

    boolean load(API api, File file) {
        api.addPanel(file.name, null, 'Location: ' + file.absolutePath, new LogPage(api, file.toURI(), file.text))
        return true
    }
}
