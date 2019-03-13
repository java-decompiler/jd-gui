/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view

dialog(
        id:'goToDialog',
        owner:mainFrame,
        title:'Go to Line',
        modal:false,
        resizable:false) {
    vbox(border:emptyBorder(15)) {
        hbox {
            label(id: 'goToEnterLineNumberLabel')
            hglue()
        }
        vstrut(10)
        textField(id:'goToEnterLineNumberTextField', columns:30)
        vstrut(10)
        hbox {
            label(id:'goToEnterLineNumberErrorLabel', text:' ')
            hglue()
        }
        vstrut(15)
        hbox {
            hglue()
            button(id: 'goToOkButton') {
                action(id:'goToOkAction', name:'Ok', enabled:false)
            }
            hstrut(5)
            button {
                action(id:'goToCancelAction', name:'Cancel', closure:{ goToDialog.visible = false })
            }
        }
        vstrut(13)
    }
}