package org.jd.gui.service.fileloader

import org.jd.gui.api.API

class WarFileLoaderProvider extends ZipFileLoaderProvider {

    String[] getExtensions() { ['war'] }
    String getDescription() { 'War files (*.war)' }

    boolean accept(API api, File file) {
        return file.exists() && file.canRead() && file.name.toLowerCase().endsWith('.war')
    }
}
