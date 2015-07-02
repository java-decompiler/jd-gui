/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.view.renderer

import org.jd.gui.view.bean.OpenTypeListCellBean

import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.ListCellRenderer
import javax.swing.UIManager
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component

class OpenTypeListCellRenderer implements ListCellRenderer<OpenTypeListCellBean> {
    Color textSelectionColor
    Color textNonSelectionColor
    Color infoSelectionColor
    Color infoNonSelectionColor
    Color backgroundSelectionColor
    Color backgroundNonSelectionColor

    JPanel panel
    JLabel label, info

    StringBuffer sb = new StringBuffer()

    OpenTypeListCellRenderer() {
        textSelectionColor = UIManager.getColor("List.selectionForeground")
        textNonSelectionColor = UIManager.getColor("List.foreground")
        backgroundSelectionColor = UIManager.getColor("List.selectionBackground")
        backgroundNonSelectionColor = UIManager.getColor("List.background")

        infoSelectionColor = infoColor(textSelectionColor)
        infoNonSelectionColor = infoColor(textNonSelectionColor)

        panel = new JPanel(new BorderLayout())
        panel.add(label = new JLabel(), BorderLayout.WEST)
        panel.add(info = new JLabel(), BorderLayout.CENTER)
    }

    static protected Color infoColor(Color c) {
        if (c.red + c.green + c.blue > (3*127)) {
            return new Color(
                    (int)((c.red-127)  *0.7 + 127),
                    (int)((c.green-127)*0.7 + 127),
                    (int)((c.blue-127) *0.7 + 127),
                    c.alpha)
        } else {
            return new Color(
                    (int)(127 - (127-c.red)  *0.7),
                    (int)(127 - (127-c.green)*0.7),
                    (int)(127 - (127-c.blue) *0.7),
                    c.alpha)
        }
    }

    Component getListCellRendererComponent(
            JList<? extends OpenTypeListCellBean> list, OpenTypeListCellBean value,
            int index, boolean selected, boolean hasFocus) {

        if (value) {
            // Display first level item
            label.text = value.label
            label.icon = value.icon

            sb.setLength(0)
            if (value.packag) {
                sb.append(' - ').append(value.packag)
            }
            info.text = sb.toString()

            if (selected) {
                label.foreground = textSelectionColor
                info.foreground = infoSelectionColor
                panel.background = backgroundSelectionColor
            } else {
                label.foreground = textNonSelectionColor
                info.foreground = infoNonSelectionColor
                panel.background = backgroundNonSelectionColor
            }
        } else {
            label.text = ' ...'
            label.icon = null
            info.text = ''
            label.foreground = textNonSelectionColor
            panel.background = backgroundNonSelectionColor
        }

        return panel
    }
}
