/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.controller

import groovy.swing.SwingBuilder
import org.jd.gui.api.API
import org.jd.gui.model.configuration.Configuration
import org.jd.gui.spi.PreferencesPanel
import org.jd.gui.view.PreferencesView

class PreferencesController {
    PreferencesView preferencesView

    PreferencesController(SwingBuilder swing, Configuration configuration, API api, Collection<PreferencesPanel> panels) {
        // Create UI
        preferencesView = new PreferencesView(swing, configuration, panels)
    }

    void show(Closure okClosure) {
        // Show
        preferencesView.show(okClosure)
    }
}
