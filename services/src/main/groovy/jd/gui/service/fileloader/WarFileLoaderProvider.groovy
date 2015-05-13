package jd.gui.service.fileloader

import jd.gui.api.API

class WarFileLoaderProvider extends ZipFileLoaderProvider {

    String[] getExtensions() { ['war'] }
    String getDescription() { 'War files (*.war)' }

    boolean accept(API api, File file) {
        return file.exists() && file.canRead() && file.name.toLowerCase().endsWith('.war')
    }
}
