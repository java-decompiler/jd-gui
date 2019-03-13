/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.controller

import groovy.swing.SwingBuilder
import groovy.transform.CompileStatic
import org.jd.gui.api.API
import org.jd.gui.api.feature.SourcesSavable
import org.jd.gui.model.configuration.Configuration
import org.jd.gui.view.SaveAllSourcesView

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ExecutorService

class SaveAllSourcesController implements SourcesSavable.Controller, SourcesSavable.Listener {
    API api
    SaveAllSourcesView saveAllSourcesView
    boolean cancel
    int counter
    int mask

    SaveAllSourcesController(SwingBuilder swing, Configuration configuration, API api) {
        this.api = api
        // Create UI
        this.saveAllSourcesView = new SaveAllSourcesView(swing, configuration, api, { onCanceled() })
    }

    @CompileStatic
    void show(ExecutorService executor, SourcesSavable savable, File file) {
        // Show
        this.saveAllSourcesView.show(file)
        // Execute background task
        executor.execute(new Runnable() {
            void run() {
                int fileCount = savable.fileCount

                saveAllSourcesView.updateProgressBar(0)
                saveAllSourcesView.setMaxValue(fileCount)

                cancel = false
                counter = 0
                mask = 2

                while (fileCount > 64) {
                    fileCount >>= 1
                    mask <<= 1
                }

                mask--

                def path = Paths.get(file.toURI())
                Files.deleteIfExists(path)

                try {
                    savable.save(api, this as SourcesSavable.Controller, this as SourcesSavable.Listener, path)
                } catch (Exception e) {
                    saveAllSourcesView.showActionFailedDialog()
                    cancel = true
                }

                if (cancel) {
                    Files.deleteIfExists(path)
                }

                saveAllSourcesView.hide()
            }
        })
    }

    boolean isActivated() { saveAllSourcesView.isVisible() }

    protected void onCanceled() { cancel = true }

    // --- SourcesSavable.Controller --- //
    boolean isCancelled() { cancel }

    // --- SourcesSavable.Listener --- //
    void pathSaved(Path path) {
        if (((counter++) & mask) == 0) {
            saveAllSourcesView.updateProgressBar(counter)
        }
    }
}
