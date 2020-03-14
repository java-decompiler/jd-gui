package org.jd.gui.service.fileloader;

import org.jd.gui.api.API;
import org.jd.gui.util.exception.ExceptionUtil;

import java.io.File;

/**
 * @author cddjr <dengjingren@foxmail.com>
 * Created on 2020/3/14.
 */
public class DirectoryLoaderProvider extends AbstractFileLoaderProvider {
    @Override
    public String[] getExtensions() {
        return new String[]{""};
    }

    @Override
    public String getDescription() {
        return "Directory";
    }

    @Override
    public boolean accept(API api, File file) {
        return file.exists() && file.canRead() && file.isDirectory();
    }

    @Override
    public boolean load(API api, File file) {
        try {
            return load(api, file, file.toPath()) != null;
        } catch (Exception e) {
            assert ExceptionUtil.printStackTrace(e);
        }
        return false;
    }
}
