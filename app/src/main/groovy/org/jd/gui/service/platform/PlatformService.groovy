/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.platform

@Singleton
class PlatformService {
	enum OS { Linux, MacOSX, Windows }
	
	final OS os = initOS()

	final isLinux = (os == OS.Linux)
	final isMac = (os == OS.MacOSX)
	final isWindows = (os == OS.Windows)
	
	OS initOS() {
		String osName = System.getProperty('os.name').toLowerCase()
		
		if (osName.contains('windows')) {
			return OS.Windows
		} else if (osName.contains('mac os')) {
			return OS.MacOSX
		} else {
			return OS.Linux
		}
	}
}
