/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.api.feature;

import jd.gui.api.API;
import java.nio.file.Path;

public interface SourcesSavable {
    public String getSourceFileName();

    public int getFileCount();

    public void save(API api, Controller controller, Listener listener, Path path);

    public interface Controller {
        public boolean isCancelled();
    };

    public interface Listener {
        public void pathSaved(Path path);
    };
}
