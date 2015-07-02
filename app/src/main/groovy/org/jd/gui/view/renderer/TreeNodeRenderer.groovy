/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.view.renderer

import org.jd.gui.api.model.TreeNodeData

import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.UIManager
import javax.swing.tree.TreeCellRenderer
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Insets

class TreeNodeRenderer  implements TreeCellRenderer {
    Color textSelectionColor
    Color backgroundSelectionColor
    Color textNonSelectionColor
    Color backgroundNonSelectionColor
    Color textDisabledColor
    Color backgroundDisabledColor

    JPanel panel
    JLabel icon, label

    TreeNodeRenderer() {
        panel = new JPanel(new BorderLayout())
        panel.add(icon = new JLabel(), BorderLayout.WEST)
        panel.add(label = new JLabel(), BorderLayout.CENTER)
        panel.opaque = false

        textSelectionColor = UIManager.getColor("Tree.selectionForeground")
        backgroundSelectionColor = UIManager.getColor("Tree.selectionBackground")
        textNonSelectionColor = UIManager.getColor("Tree.textForeground")
        backgroundNonSelectionColor = UIManager.getColor("Tree.textBackground")
        textDisabledColor = UIManager.getColor("Tree.disabledText")
        backgroundDisabledColor = UIManager.getColor("Tree.disabled")
        Insets margins = UIManager.getInsets("Tree.rendererMargins")

        icon.foreground = textNonSelectionColor
        icon.opaque = label.opaque = false
        icon.border = BorderFactory.createEmptyBorder(0, 0, 0, 2)

        if (margins) {
            label.border = BorderFactory.createEmptyBorder(margins.top, margins.left, margins.bottom, margins.right)
        } else {
            label.border = BorderFactory.createEmptyBorder(0, 4, 0, 4)
        }
    }

    Component getTreeCellRendererComponent(
            JTree tree, Object value,
            boolean selected, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {
        def data = value.userObject

        if (data instanceof TreeNodeData) {
            icon.icon = expanded ? (data.openIcon ?: data.icon) : data.icon
            label.text = data.label
        } else {
            icon.icon = null
            label.text = '' + data
        }

        if (selected) {
            if (hasFocus) {
                label.foreground = textSelectionColor
                label.background = backgroundSelectionColor
            } else {
                label.foreground = textDisabledColor
                label.background = backgroundDisabledColor
            }
            label.opaque = true
        } else {
            label.foreground = textNonSelectionColor
            label.opaque = false
        }

        return panel
    }
}
