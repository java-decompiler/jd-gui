/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.view

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
