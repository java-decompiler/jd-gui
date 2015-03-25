/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.view

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
