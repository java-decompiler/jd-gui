/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.controller

import groovy.swing.SwingBuilder
import jd.gui.api.feature.LineNumberNavigable
import jd.gui.model.configuration.Configuration
import jd.gui.view.GoToView

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
