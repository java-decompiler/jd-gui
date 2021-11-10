/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.platform;

public class PlatformService {
	protected static final PlatformService PLATFORM_SERVICE = new PlatformService();

	public enum OS { Linux, MacOSX, Windows }

	protected OS os;

	protected PlatformService() {
		String osName = System.getProperty("os.name").toLowerCase();

		if (osName.contains("windows")) {
			os = OS.Windows;
		} else if (osName.contains("mac os")) {
			os = OS.MacOSX;
		} else {
			os = OS.Linux;
		}
	}

	public static PlatformService getInstance() { return PLATFORM_SERVICE; }

	public OS getOs() { return os; }

	public boolean isLinux() { return os == OS.Linux; }
	public boolean isMac() { return os == OS.MacOSX; }
	public boolean isWindows() { return os == OS.Windows; }
}
