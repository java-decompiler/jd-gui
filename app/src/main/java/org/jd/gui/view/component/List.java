/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view.component;

import org.jd.gui.api.model.TreeNodeData;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.KeyEvent;

public class List extends JList {

    @SuppressWarnings("unchecked")
    public List() {
        super();

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        KeyStroke ctrlA = KeyStroke.getKeyStroke(KeyEvent.VK_A, toolkit.getMenuShortcutKeyMask());
        KeyStroke ctrlC = KeyStroke.getKeyStroke(KeyEvent.VK_C, toolkit.getMenuShortcutKeyMask());
        KeyStroke ctrlV = KeyStroke.getKeyStroke(KeyEvent.VK_V, toolkit.getMenuShortcutKeyMask());

        InputMap inputMap = getInputMap();
        inputMap.put(ctrlA, "none");
        inputMap.put(ctrlC, "none");
        inputMap.put(ctrlV, "none");

        setCellRenderer(new Renderer());
    }

    protected class Renderer implements ListCellRenderer {
        protected Color textSelectionColor;
        protected Color backgroundSelectionColor;
        protected Color textNonSelectionColor;
        protected Color backgroundNonSelectionColor;

        protected JLabel label;

        public Renderer() {
            label = new JLabel();
            label.setOpaque(true);

            textSelectionColor = UIManager.getColor("List.dropCellForeground");
            backgroundSelectionColor = UIManager.getColor("List.dropCellBackground");
            textNonSelectionColor = UIManager.getColor("List.foreground");
            backgroundNonSelectionColor = UIManager.getColor("List.background");
            Insets margins = UIManager.getInsets("List.contentMargins");

            if (textSelectionColor == null)
                textSelectionColor = List.this.getSelectionForeground();
            if (backgroundSelectionColor == null)
                backgroundSelectionColor = List.this.getSelectionBackground();

            if (margins != null) {
                label.setBorder(BorderFactory.createEmptyBorder(margins.top, margins.left, margins.bottom, margins.right));
            } else {
                label.setBorder(BorderFactory.createEmptyBorder(0, 2, 1, 2));
            }
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean selected, boolean hasFocus) {
            Object data = ((DefaultMutableTreeNode)value).getUserObject();

            if (data instanceof TreeNodeData) {
                TreeNodeData tnd = (TreeNodeData)data;
                label.setIcon(tnd.getIcon());
                label.setText(tnd.getLabel());
            } else {
                label.setIcon(null);
                label.setText("" + data);
            }

            if (selected) {
                label.setForeground(textSelectionColor);
                label.setBackground(backgroundSelectionColor);
            } else {
                label.setForeground(textNonSelectionColor);
                label.setBackground(backgroundNonSelectionColor);
            }

            return label;
        }
    }
}
