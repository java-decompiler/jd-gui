/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view.renderer;

import org.jd.gui.view.bean.OpenTypeListCellBean;

import javax.swing.*;
import java.awt.*;

public class OpenTypeListCellRenderer implements ListCellRenderer<OpenTypeListCellBean> {
    protected Color textSelectionColor;
    protected Color textNonSelectionColor;
    protected Color infoSelectionColor;
    protected Color infoNonSelectionColor;
    protected Color backgroundSelectionColor;
    protected Color backgroundNonSelectionColor;

    protected JPanel panel;
    protected JLabel label, info;

    public OpenTypeListCellRenderer() {
        textSelectionColor = UIManager.getColor("List.selectionForeground");
        textNonSelectionColor = UIManager.getColor("List.foreground");
        backgroundSelectionColor = UIManager.getColor("List.selectionBackground");
        backgroundNonSelectionColor = UIManager.getColor("List.background");

        infoSelectionColor = infoColor(textSelectionColor);
        infoNonSelectionColor = infoColor(textNonSelectionColor);

        panel = new JPanel(new BorderLayout());
        panel.add(label = new JLabel(), BorderLayout.WEST);
        panel.add(info = new JLabel(), BorderLayout.CENTER);
    }

    static protected Color infoColor(Color c) {
        if (c.getRed() + c.getGreen() + c.getBlue() > (3*127)) {
            return new Color(
                    (int)((c.getRed()-127)  *0.7 + 127),
                    (int)((c.getGreen()-127)*0.7 + 127),
                    (int)((c.getBlue()-127) *0.7 + 127),
                    c.getAlpha());
        } else {
            return new Color(
                    (int)(127 - (127-c.getRed())  *0.7),
                    (int)(127 - (127-c.getGreen())*0.7),
                    (int)(127 - (127-c.getBlue()) *0.7),
                    c.getAlpha());
        }
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends OpenTypeListCellBean> list, OpenTypeListCellBean value, int index, boolean selected, boolean hasFocus) {
        if (value != null) {
            // Display first level item
            label.setText(value.label);
            label.setIcon(value.icon);

            info.setText((value.packag != null) ? " - "+value.packag : "");

            if (selected) {
                label.setForeground(textSelectionColor);
                info.setForeground(infoSelectionColor);
                panel.setBackground(backgroundSelectionColor);
            } else {
                label.setForeground(textNonSelectionColor);
                info.setForeground(infoNonSelectionColor);
                panel.setBackground(backgroundNonSelectionColor);
            }
        } else {
            label.setText(" ...");
            label.setIcon(null);
            info.setText("");
            label.setForeground(textNonSelectionColor);
            panel.setBackground(backgroundNonSelectionColor);
        }

        return panel;
    }
}
