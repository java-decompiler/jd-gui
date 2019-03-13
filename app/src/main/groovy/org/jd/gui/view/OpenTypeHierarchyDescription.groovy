/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view

import javax.swing.ScrollPaneConstants
import java.awt.BorderLayout
import java.awt.Dimension

dialog(
        id:'openTypeHierarchyDialog',
        owner:mainFrame,
        title:'Hierarchy Type',
        modal:false) {
    panel(border:emptyBorder(15)) {
        borderLayout()
        scrollPane(
                id:'openTypeHierarchyScrollPane',
                constraints:BorderLayout.CENTER,
                horizontalScrollBarPolicy:ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER,
                preferredSize:new Dimension(400, 150)) {
            tree(id:'openTypeHierarchyTree')
        }
        vbox(constraints:BorderLayout.SOUTH) {
            vstrut(25)
            hbox {
                hglue()
                button(id:'openTypeHierarchyOpenButton') {
                    action(id:'openTypeHierarchyOpenAction', name:'Open', enabled:false)
                }
                hstrut(5)
                button {
                    action(id:'openTypeHierarchyCancelAction', name:'Cancel', closure:{ openTypeHierarchyDialog.visible = false })
                }
            }
        }
    }
}
