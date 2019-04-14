/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.controller;

import org.jd.gui.api.API;
import org.jd.gui.api.feature.SourcesSavable;
import org.jd.gui.util.exception.ExceptionUtil;
import org.jd.gui.view.SaveAllSourcesView;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ScheduledExecutorService;

public class SaveAllSourcesController implements SourcesSavable.Controller, SourcesSavable.Listener {
    protected API api;
    protected SaveAllSourcesView saveAllSourcesView;
    protected boolean cancel;
    protected int counter;
    protected int mask;

    public SaveAllSourcesController(API api, JFrame mainFrame) {
        this.api = api;
        // Create UI
        this.saveAllSourcesView = new SaveAllSourcesView(mainFrame, this::onCanceled);
    }

    public void show(ScheduledExecutorService executor, SourcesSavable savable, File file) {
        // Show
        this.saveAllSourcesView.show(file);
        // Execute background task
        executor.execute(() -> {
            int fileCount = savable.getFileCount();

            saveAllSourcesView.updateProgressBar(0);
            saveAllSourcesView.setMaxValue(fileCount);

            cancel = false;
            counter = 0;
            mask = 2;

            while (fileCount > 64) {
                fileCount >>= 1;
                mask <<= 1;
            }

            mask--;

            try {
                Path path = Paths.get(file.toURI());
                Files.deleteIfExists(path);

                try {
                    savable.save(api, this, this, path);
                } catch (Exception e) {
                    assert ExceptionUtil.printStackTrace(e);
                    saveAllSourcesView.showActionFailedDialog();
                    cancel = true;
                }

                if (cancel) {
                    Files.deleteIfExists(path);
                }
            } catch (Throwable t) {
                assert ExceptionUtil.printStackTrace(t);
            }

            saveAllSourcesView.hide();
        });
    }

    public boolean isActivated() { return saveAllSourcesView.isVisible(); }

    protected void onCanceled() { cancel = true; }

    // --- SourcesSavable.Controller --- //
    @Override public boolean isCancelled() { return cancel; }

    // --- SourcesSavable.Listener --- //
    @Override
    public void pathSaved(Path path) {
        if (((counter++) & mask) == 0) {
            saveAllSourcesView.updateProgressBar(counter);
        }
    }
}
