/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.view

import javax.swing.ScrollPaneConstants
import java.awt.BorderLayout

dialog(
        id:'preferencesDialog',
        owner:mainFrame,
        title:'Preferences',
        modal:false) {
    panel(border:emptyBorder(15)) {
        borderLayout()
        scrollPane(
                id:'preferencesScrollPane',
                constraints:BorderLayout.CENTER,
                horizontalScrollBarPolicy:ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER,
                border:emptyBorder(0)) {
            vbox(id:'preferencesPanels')
        }
        vbox(constraints:BorderLayout.SOUTH) {
            vstrut(15)
            hbox {
                hglue()
                button(id:'preferencesOkButton') {
                    action(id:'preferencesOkAction', name:'   Ok   ')
                }
                hstrut(5)
                button {
                    action(id:'preferencesCancelAction', name:'Cancel', closure:{ preferencesDialog.visible = false })
                }
            }
        }
    }
}
