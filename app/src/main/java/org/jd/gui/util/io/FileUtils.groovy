package org.jd.gui.util.io

/**
 * Created by jianhua.fengjh on 27/11/2015.
 */
class FileUtils {

    static String ensureTrailingSlash(final String path) {
        if ((path == null) || "".equals(path)) {
            return "";
        }

        StringBuilder buf = new StringBuilder(path);
        while (buf.charAt(buf.length() - 1) == File.separatorChar) {
            buf.deleteCharAt(buf.length() - 1);
        }

        return buf.append(File.separatorChar).toString();
    }
}
