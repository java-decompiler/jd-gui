/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view

dialog(
        id:'saveAllSourcesDialog',
        owner:mainFrame,
        title:'Save All Sources',
        modal:false,
        resizable:false) {
    vbox(border:emptyBorder(15)) {
        hbox {
            label(id: 'saveAllSourcesLabel', text: ' ')
            hglue()
        }
        vstrut(10)
        progressBar(id:'saveAllSourcesProgressBar')
        vstrut(15)
        hbox {
            hglue()
            button {
                action(id:'saveAllSourcesCancelAction', name:'Cancel')
            }
        }
    }
}
