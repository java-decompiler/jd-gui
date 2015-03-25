/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.platform

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
