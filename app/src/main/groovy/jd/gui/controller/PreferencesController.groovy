/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.controller

import groovy.swing.SwingBuilder
import jd.gui.api.API
import jd.gui.model.configuration.Configuration
import jd.gui.spi.PreferencesPanel
import jd.gui.view.PreferencesView

class PreferencesController {
    PreferencesView preferencesView

    PreferencesController(SwingBuilder swing, Configuration configuration, API api, List<PreferencesPanel> panels) {
        // Create UI
        preferencesView = new PreferencesView(swing, configuration, panels)
    }

    void show(Closure okClosure) {
        // Show
        preferencesView.show(okClosure)
    }
}
