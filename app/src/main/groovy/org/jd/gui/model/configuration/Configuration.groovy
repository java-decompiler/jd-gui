/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.model.configuration

import java.awt.Dimension
import java.awt.Point

class Configuration {
	Point mainWindowLocation
	Dimension mainWindowSize
	boolean mainWindowMaximize
    String lookAndFeel

	List<File> recentFiles = []
	
	File configRecentLoadDirectory
	File configRecentSaveDirectory

    Map<String, String> preferences = [:]

    void addRecentFile(File file) {
        recentFiles.remove(file)
        recentFiles.add(0, file)
        if (recentFiles.size() > 10) {
            recentFiles.remove(10)
        }
    }
}
