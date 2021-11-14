package org.jd.gui.util.sys;

/**
 * Created by jianhua.fengjh on 27/11/2015.
 */
public final class SystemUtils {

    public static boolean isLinux() {
        return System.getProperty("os.name").startsWith("Linux");
    }

    public static boolean isMacOS() {
        return System.getProperty("os.name").startsWith("Mac");
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

}
