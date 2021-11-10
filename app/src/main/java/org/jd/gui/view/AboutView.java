/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view;

import org.jd.gui.util.exception.ExceptionUtil;
import org.jd.gui.util.swing.SwingUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class AboutView {
    protected JDialog aboutDialog;
    protected JButton aboutOkButton;

    public AboutView(JFrame mainFrame) {
        // Build GUI
        SwingUtil.invokeLater(() -> {
            aboutDialog = new JDialog(mainFrame, "About Java Decompiler", false);
            aboutDialog.setResizable(false);

            JPanel panel = new JPanel();
            panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            panel.setLayout(new BorderLayout());
            aboutDialog.add(panel);

            Box vbox = Box.createVerticalBox();
            panel.add(vbox, BorderLayout.NORTH);
            JPanel subpanel = new JPanel();
            vbox.add(subpanel);
            subpanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            subpanel.setBackground(Color.WHITE);
            subpanel.setLayout(new BorderLayout());
            JLabel logo = new JLabel(new ImageIcon(SwingUtil.getImage("/org/jd/gui/images/jd_icon_64.png")));
            logo.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            subpanel.add(logo, BorderLayout.WEST);
            Box subvbox = Box.createVerticalBox();
            subvbox.setBorder(BorderFactory.createEmptyBorder(15,0,15,15));
            subpanel.add(subvbox, BorderLayout.EAST);
            Box hbox = Box.createHorizontalBox();
            subvbox.add(hbox);
            JLabel mainLabel = new JLabel("Java Decompiler");
            mainLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 14));
            hbox.add(mainLabel);
            hbox.add(Box.createHorizontalGlue());
            hbox = Box.createHorizontalBox();
            subvbox.add(hbox);
            JPanel subsubpanel = new JPanel();
            hbox.add(subsubpanel);
            subsubpanel.setLayout(new GridLayout(2,2));
            subsubpanel.setOpaque(false);
            subsubpanel.setBorder(BorderFactory.createEmptyBorder(5,10,5,5));

            String jdGuiVersion = "SNAPSHOT";
            String jdCoreVersion = "SNAPSHOT";

            try {
                Enumeration<URL> enumeration = AboutView.class.getClassLoader().getResources("META-INF/MANIFEST.MF");

                while (enumeration.hasMoreElements()) {
                    try (InputStream is = enumeration.nextElement().openStream()) {
                        Attributes attributes = new Manifest(is).getMainAttributes();
                        String attribute = attributes.getValue("JD-GUI-Version");

                        if (attribute != null) {
                            jdGuiVersion = attribute;
                        }

                        attribute = attributes.getValue("JD-Core-Version");

                        if (attribute != null) {
                            jdCoreVersion = attribute;
                        }
                    }
                }
            } catch (IOException e) {
                assert ExceptionUtil.printStackTrace(e);
            }

            subsubpanel.add(new JLabel("JD-GUI"));
            subsubpanel.add(new JLabel("version " + jdGuiVersion));
            subsubpanel.add(new JLabel("JD-Core"));
            subsubpanel.add(new JLabel("version " + jdCoreVersion));

            hbox.add(Box.createHorizontalGlue());

            hbox = Box.createHorizontalBox();
            hbox.add(new JLabel("Copyright © 2008, 2019 Emmanuel Dupuy"));
            hbox.add(Box.createHorizontalGlue());
            subvbox.add(hbox);

            vbox.add(Box.createVerticalStrut(10));

            hbox = Box.createHorizontalBox();
            panel.add(hbox, BorderLayout.SOUTH);
            hbox.add(Box.createHorizontalGlue());
            aboutOkButton = new JButton("    Ok    ");
            Action aboutOkActionListener = new AbstractAction() {
                @Override public void actionPerformed(ActionEvent actionEvent) { aboutDialog.setVisible(false); }
            };
            aboutOkButton.addActionListener(aboutOkActionListener);
            hbox.add(aboutOkButton);
            hbox.add(Box.createHorizontalGlue());

            // Last setup
            JRootPane rootPane = aboutDialog.getRootPane();
            rootPane.setDefaultButton(aboutOkButton);
            rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "AboutView.ok");
            rootPane.getActionMap().put("AboutView.ok", aboutOkActionListener);

            // Prepare to display
            aboutDialog.pack();
        });
    }

    public void show() {
        SwingUtil.invokeLater(() -> {
            // Show
            aboutDialog.setLocationRelativeTo(aboutDialog.getParent());
            aboutDialog.setVisible(true);
            aboutOkButton.requestFocus();
        });
    }
}
