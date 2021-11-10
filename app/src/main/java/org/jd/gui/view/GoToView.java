/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view;

import org.jd.gui.api.feature.LineNumberNavigable;
import org.jd.gui.model.configuration.Configuration;
import org.jd.gui.util.swing.SwingUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.IntConsumer;

public class GoToView {
    protected JDialog goToDialog;
    protected JLabel goToEnterLineNumberLabel;
    protected JTextField goToEnterLineNumberTextField;
    protected JLabel goToEnterLineNumberErrorLabel;

    protected LineNumberNavigable navigator;
    protected IntConsumer okCallback;

    public GoToView(Configuration configuration, JFrame mainFrame) {
        // Build GUI
        SwingUtil.invokeLater(() -> {
            goToDialog = new JDialog(mainFrame, "Go to Line", false);
            goToDialog.setResizable(false);

            Box vbox = Box.createVerticalBox();
            vbox.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            goToDialog.add(vbox);

            // First label "Enter line number (1..xxx):"
            Box hbox = Box.createHorizontalBox();
            hbox.add(goToEnterLineNumberLabel = new JLabel());
            hbox.add(Box.createHorizontalGlue());
            vbox.add(hbox);

            vbox.add(Box.createVerticalStrut(10));

            // Text field
            vbox.add(goToEnterLineNumberTextField = new JTextField(30));

            vbox.add(Box.createVerticalStrut(10));

            // Error label
            hbox = Box.createHorizontalBox();
            hbox.add(goToEnterLineNumberErrorLabel = new JLabel(" "));
            goToEnterLineNumberTextField.addKeyListener(new KeyAdapter() {
                @Override public void keyTyped(KeyEvent e) {
                    if (! Character.isDigit(e.getKeyChar())) {
                        e.consume();
                    }
                }
            });
            hbox.add(Box.createHorizontalGlue());
            vbox.add(hbox);

            vbox.add(Box.createVerticalStrut(15));

            // Buttons "Ok" and "Cancel"
            hbox = Box.createHorizontalBox();
            hbox.add(Box.createHorizontalGlue());
            JButton goToOkButton = new JButton("   Ok   ");
            hbox.add(goToOkButton);
            goToOkButton.setEnabled(false);
            goToOkButton.addActionListener(e -> {
                okCallback.accept(Integer.valueOf(goToEnterLineNumberTextField.getText()));
                goToDialog.setVisible(false);
            });
            hbox.add(Box.createHorizontalStrut(5));
            JButton goToCancelButton = new JButton("Cancel");
            hbox.add(goToCancelButton);
            Action goToCancelActionListener = new AbstractAction() {
                public void actionPerformed(ActionEvent actionEvent) { goToDialog.setVisible(false); }
            };
            goToCancelButton.addActionListener(goToCancelActionListener);
            vbox.add(hbox);

            vbox.add(Box.createVerticalStrut(13));

            // Last setup
            JRootPane rootPane = goToDialog.getRootPane();
            rootPane.setDefaultButton(goToOkButton);
            rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "OpenTypeView.cancel");
            rootPane.getActionMap().put("OpenTypeView.cancel", goToCancelActionListener);

            // Add main listener
            goToEnterLineNumberTextField.getDocument().addDocumentListener(new DocumentListener() {
                protected Color backgroundColor = UIManager.getColor("TextField.background");
                protected Color errorBackgroundColor = Color.decode(configuration.getPreferences().get("JdGuiPreferences.errorBackgroundColor"));

                @Override public void insertUpdate(DocumentEvent e) { onTextChange(); }
                @Override public void removeUpdate(DocumentEvent e) { onTextChange(); }
                @Override public void changedUpdate(DocumentEvent e) { onTextChange(); }

                protected void onTextChange() {
                    String text = goToEnterLineNumberTextField.getText();

                    if (text.length() == 0) {
                        goToOkButton.setEnabled(false);
                        clearErrorMessage();
                    } else {
                        try {
                            int lineNumber = Integer.valueOf(text);

                            if (lineNumber > navigator.getMaximumLineNumber()) {
                                goToOkButton.setEnabled(false);
                                showErrorMessage("Line number out of range");
                            } else if (navigator.checkLineNumber(lineNumber)) {
                                goToOkButton.setEnabled(true);
                                clearErrorMessage();
                            } else {
                                goToOkButton.setEnabled(false);
                                showErrorMessage("Line number not found");
                            }
                        } catch (NumberFormatException e) {
                            goToOkButton.setEnabled(false);
                            showErrorMessage("Not a number");
                        }
                    }
                }

                protected void showErrorMessage(String message) {
                    goToEnterLineNumberErrorLabel.setText(message);
                    goToEnterLineNumberTextField.setBackground(errorBackgroundColor);
                }

                protected void clearErrorMessage() {
                    goToEnterLineNumberErrorLabel.setText(" ");
                    goToEnterLineNumberTextField.setBackground(backgroundColor);
                }
            });

            // Prepare to display
            goToDialog.pack();
            goToDialog.setLocationRelativeTo(mainFrame);
        });
    }

    public void show(LineNumberNavigable navigator, IntConsumer okCallback) {
        this.navigator = navigator;
        this.okCallback = okCallback;

        SwingUtil.invokeLater(() -> {
            // Init
            goToEnterLineNumberLabel.setText("Enter line number (1.." + navigator.getMaximumLineNumber() + "):");
            goToEnterLineNumberTextField.setText("");
            // Show
            goToDialog.setVisible(true);
            goToEnterLineNumberTextField.requestFocus();
        });
    }
}
