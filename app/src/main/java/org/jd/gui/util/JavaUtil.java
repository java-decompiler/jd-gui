package org.jd.gui.util;

public class JavaUtil {
    /**
     * Retrieves the "major" Java version
     * 1.8.0_211 == 8, 9.0.1 = 9
     * From a post in
     * https://stackoverflow.com/questions/2591083/getting-java-version-at-runtime
     * @return an integer representing the major Java version of the runtime.
     */
    public static int runtimeVersion() {
        String version = System.getProperty("java.version");
        if(version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if(dot != -1) { version = version.substring(0, dot); }
        } return Integer.parseInt(version);
    }
}
