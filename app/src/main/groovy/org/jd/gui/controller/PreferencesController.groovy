/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.controller

import groovy.swing.SwingBuilder
import org.jd.gui.api.API
import org.jd.gui.model.configuration.Configuration
import org.jd.gui.spi.PreferencesPanel
import org.jd.gui.view.PreferencesView

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
