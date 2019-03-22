package org.jd.gui.util.sys

/**
 * Created by jianhua.fengjh on 27/11/2015.
 */
final class SystemUtils {

    static boolean isLinux() {
        return System.getProperty("os.name").startsWith("Linux");
    }

    static boolean isMacOS() {
        return System.getProperty("os.name").startsWith("Mac");
    }

    static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

}
