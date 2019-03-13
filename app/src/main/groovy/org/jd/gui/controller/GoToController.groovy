/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.controller

import groovy.swing.SwingBuilder
import org.jd.gui.api.feature.LineNumberNavigable
import org.jd.gui.model.configuration.Configuration
import org.jd.gui.view.GoToView

class GoToController {
    GoToView goToView

    GoToController(SwingBuilder swing, Configuration configuration) {
        // Create UI
        goToView = new GoToView(swing, configuration)
    }

    void show(LineNumberNavigable navigator, Closure okClosure) {
        // Show
        goToView.show(navigator, okClosure)
    }
}
