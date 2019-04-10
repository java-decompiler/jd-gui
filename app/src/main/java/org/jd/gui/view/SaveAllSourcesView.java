/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view;

import org.jd.gui.util.swing.SwingUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class SaveAllSourcesView {
    protected JDialog saveAllSourcesDialog;
    protected JLabel saveAllSourcesLabel;
    protected JProgressBar saveAllSourcesProgressBar;

    public SaveAllSourcesView(JFrame mainFrame, Runnable cancelCallback) {
        // Build GUI
        SwingUtil.invokeLater(() -> {
            saveAllSourcesDialog = new JDialog(mainFrame, "Save All Sources", false);
            saveAllSourcesDialog.setResizable(false);
            saveAllSourcesDialog.addWindowListener(new WindowAdapter() {
                @Override public void windowClosing(WindowEvent e) {
                    cancelCallback.run();
                }
            });

            Box vbox = Box.createVerticalBox();
            vbox.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            saveAllSourcesDialog.add(vbox);

            // First label "Saving 'file' ..."
            Box hbox = Box.createHorizontalBox();
            hbox.add(saveAllSourcesLabel = new JLabel());
            hbox.add(Box.createHorizontalGlue());
            vbox.add(hbox);

            vbox.add(Box.createVerticalStrut(10));

            vbox.add(saveAllSourcesProgressBar = new JProgressBar());

            vbox.add(Box.createVerticalStrut(15));

            // Button "Cancel"
            hbox = Box.createHorizontalBox();
            hbox.add(Box.createHorizontalGlue());
            JButton saveAllSourcesCancelButton = new JButton("Cancel");
            Action saveAllSourcesCancelActionListener = new AbstractAction() {
                public void actionPerformed(ActionEvent actionEvent) {
                    cancelCallback.run();
                    saveAllSourcesDialog.setVisible(false);
                }
            };
            saveAllSourcesCancelButton.addActionListener(saveAllSourcesCancelActionListener);
            hbox.add(saveAllSourcesCancelButton);
            vbox.add(hbox);

            // Last setup
            JRootPane rootPane = saveAllSourcesDialog.getRootPane();
            rootPane.setDefaultButton(saveAllSourcesCancelButton);
            rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "SaveAllSourcesView.cancel");
            rootPane.getActionMap().put("SaveAllSourcesView.cancel", saveAllSourcesCancelActionListener);

            // Prepare to display
            saveAllSourcesDialog.pack();
        });
    }

    public void show(File file) {
        SwingUtil.invokeLater(() -> {
            // Init
            saveAllSourcesLabel.setText("Saving '" + file.getAbsolutePath() + "'...");
            saveAllSourcesProgressBar.setValue(0);
            saveAllSourcesProgressBar.setMaximum(10);
            saveAllSourcesProgressBar.setIndeterminate(true);
            saveAllSourcesDialog.pack();
            // Show
            saveAllSourcesDialog.setLocationRelativeTo(saveAllSourcesDialog.getParent());
            saveAllSourcesDialog.setVisible(true);
        });
    }

    public boolean isVisible() { return saveAllSourcesDialog.isVisible(); }

    public void setMaxValue(int maxValue) {
        SwingUtil.invokeLater(() -> {
            if (maxValue > 0) {
                saveAllSourcesProgressBar.setMaximum(maxValue);
                saveAllSourcesProgressBar.setIndeterminate(false);
            } else {
                saveAllSourcesProgressBar.setIndeterminate(true);
            }
        });
    }

    public void updateProgressBar(int value) {
        SwingUtil.invokeLater(() -> {
            saveAllSourcesProgressBar.setValue(value);
        });
    }

    public void hide() {
        SwingUtil.invokeLater(() -> {
            saveAllSourcesDialog.setVisible(false);
        });
    }

    public void showActionFailedDialog() {
        SwingUtil.invokeLater(() -> {
            JOptionPane.showMessageDialog(saveAllSourcesDialog, "'Save All Sources' action failed.", "Error", JOptionPane.ERROR_MESSAGE);
        });
    }
}
