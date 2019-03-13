/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view.component

import org.jd.gui.api.model.TreeNodeData
import sun.swing.DefaultLookup

import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.KeyStroke
import javax.swing.ListCellRenderer
import java.awt.Color
import java.awt.Component
import java.awt.Insets
import java.awt.Toolkit
import java.awt.event.KeyEvent

class List extends JList {

    List() {
        super()

        def ctrlA = KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.defaultToolkit.menuShortcutKeyMask)
        def ctrlC = KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.defaultToolkit.menuShortcutKeyMask)
        def ctrlV = KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.defaultToolkit.menuShortcutKeyMask)

        inputMap.put(ctrlA, 'none')
        inputMap.put(ctrlC, 'none')
        inputMap.put(ctrlV, 'none')
        setCellRenderer(new Renderer())
    }

    class Renderer implements ListCellRenderer {
        Color textSelectionColor
        Color backgroundSelectionColor
        Color textNonSelectionColor
        Color backgroundNonSelectionColor

        JLabel label

        Renderer() {
            label = new JLabel()
            label.opaque = true

            textSelectionColor = DefaultLookup.getColor(label, ui, "List.dropCellForeground");
            backgroundSelectionColor = DefaultLookup.getColor(label, ui, "List.dropCellBackground");
            textNonSelectionColor = DefaultLookup.getColor(label, ui, "List.foreground");
            backgroundNonSelectionColor = DefaultLookup.getColor(label, ui, "List.background");
            Insets margins = DefaultLookup.getInsets(label, label.ui, "List.contentMargins")

            if (textSelectionColor == null)
                textSelectionColor = List.this.getSelectionForeground()
            if (backgroundSelectionColor == null)
                backgroundSelectionColor = List.this.getSelectionBackground()

            if (margins) {
                label.border = BorderFactory.createEmptyBorder(margins.top, margins.left, margins.bottom, margins.right)
            } else {
                label.border = BorderFactory.createEmptyBorder(0, 2, 1, 2)
            }
        }

        Component getListCellRendererComponent(
                JList list, Object value,
                int index, boolean selected, boolean hasFocus) {
            def data = value.userObject

            if (data instanceof TreeNodeData) {
                label.icon = data.icon


                label.text = data.label


            } else {
                label.icon = null
                label.text = '' + data
            }

            if (selected) {
                label.foreground = textSelectionColor
                label.background = backgroundSelectionColor
            } else {
                label.foreground = textNonSelectionColor
                label.background = backgroundNonSelectionColor
            }

            return label
        }
    }
}
