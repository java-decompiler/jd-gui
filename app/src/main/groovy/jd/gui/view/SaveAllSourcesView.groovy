/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.view

import groovy.swing.SwingBuilder
import jd.gui.api.API
import jd.gui.model.configuration.Configuration

import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent

class SaveAllSourcesView {
    SwingBuilder swing
    API api

    SaveAllSourcesView(SwingBuilder swing, Configuration configuration, API api, Closure cancelClosure) {
        this.swing = swing
        this.api = api
        // Load GUI description
        swing.edt {
            // Load GUI description
            build(SaveAllSourcesDescription)
            saveAllSourcesDialog.with {
                saveAllSourcesCancelAction.closure = cancelClosure
                addWindowListener(new WindowAdapter() {
                    void windowClosing(WindowEvent e) {
                        cancelClosure()
                    }
                })
                pack()
            }
        }
    }

    void show(File file) {
        swing.doLater {
            // Init GUI
            saveAllSourcesLabel.text = "Saving '$file.absolutePath'..."
            saveAllSourcesProgressBar.value = 0
            saveAllSourcesProgressBar.maximum = 10
            saveAllSourcesProgressBar.indeterminate = true
            saveAllSourcesDialog.pack()
            // Show
            saveAllSourcesDialog.locationRelativeTo = saveAllSourcesDialog.parent
            saveAllSourcesDialog.visible = true
        }
    }

    boolean isVisible() { swing.saveAllSourcesDialog.isVisible() }

    void setMaxValue(int maxValue) {
        swing.doLater {
            if (maxValue > 0) {
                saveAllSourcesProgressBar.maximum = maxValue
                saveAllSourcesProgressBar.indeterminate = false
            } else {
                saveAllSourcesProgressBar.indeterminate = true
            }
        }
    }

    void updateProgressBar(int value) {
        swing.doLater {
            saveAllSourcesProgressBar.value = value
        }
    }

    void hide() {
        swing.doLater {
            saveAllSourcesDialog.visible = false
        }
    }
}
