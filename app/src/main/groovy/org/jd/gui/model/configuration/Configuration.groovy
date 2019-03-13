/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
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
