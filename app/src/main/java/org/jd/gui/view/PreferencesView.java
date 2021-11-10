/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view;

import org.jd.gui.model.configuration.Configuration;
import org.jd.gui.spi.PreferencesPanel;
import org.jd.gui.util.swing.SwingUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.*;

public class PreferencesView implements PreferencesPanel.PreferencesPanelChangeListener {
    protected Map<String, String> preferences;
    protected Collection<PreferencesPanel> panels;
    protected HashMap<PreferencesPanel, Boolean> valids = new HashMap<>();

    protected JDialog preferencesDialog;
    protected JButton preferencesOkButton = new JButton();

    protected Runnable okCallback;

    public PreferencesView(Configuration configuration, JFrame mainFrame, Collection<PreferencesPanel> panels) {
        this.preferences = configuration.getPreferences();
        this.panels = panels;
        // Build GUI
        SwingUtil.invokeLater(() -> {
            preferencesDialog = new JDialog(mainFrame, "Preferences", false);

            JPanel panel = new JPanel();
            panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            panel.setLayout(new BorderLayout());
            preferencesDialog.add(panel);

            // Box for preferences panels
            Box preferencesPanels = Box.createVerticalBox();
            preferencesPanels.setBackground(panel.getBackground());
            preferencesPanels.setOpaque(true);
            Color errorBackgroundColor = Color.decode(configuration.getPreferences().get("JdGuiPreferences.errorBackgroundColor"));

            // Group "PreferencesPanel" by group name
            HashMap<String, ArrayList<PreferencesPanel>> groups = new HashMap<>();
            ArrayList<String> sortedGroupNames = new ArrayList<>();

            for (PreferencesPanel pp : panels) {
                ArrayList<PreferencesPanel> pps = groups.get(pp.getPreferencesGroupTitle());

                pp.init(errorBackgroundColor);
                pp.addPreferencesChangeListener(this);

                if (pps == null) {
                    String groupNames = pp.getPreferencesGroupTitle();
                    groups.put(groupNames, pps=new ArrayList<>());
                    sortedGroupNames.add(groupNames);
                }

                pps.add(pp);
            }

            Collections.sort(sortedGroupNames);

            // Add preferences panels
            for (String groupName : sortedGroupNames) {
                Box vbox = Box.createVerticalBox();
                vbox.setBorder(BorderFactory.createTitledBorder(groupName));

                ArrayList<PreferencesPanel> sortedPreferencesPanels = groups.get(groupName);
                Collections.sort(sortedPreferencesPanels, new PreferencesPanelComparator());

                for (PreferencesPanel pp : sortedPreferencesPanels) {
                    // Add title
                    Box hbox = Box.createHorizontalBox();
                    JLabel title = new JLabel(pp.getPreferencesPanelTitle());
                    title.setFont(title.getFont().deriveFont(Font.BOLD));
                    hbox.add(title);
                    hbox.add(Box.createHorizontalGlue());
                    hbox.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
                    vbox.add(hbox);
                    // Add panel
                    JComponent component = pp.getPanel();
                    component.setMaximumSize(new Dimension(component.getMaximumSize().width, component.getPreferredSize().height));
                    vbox.add(component);
                }

                preferencesPanels.add(vbox);
            }

            JScrollPane preferencesScrollPane = new JScrollPane(preferencesPanels);
            preferencesScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            preferencesScrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            panel.add(preferencesScrollPane, BorderLayout.CENTER);

            Box vbox = Box.createVerticalBox();
            panel.add(vbox, BorderLayout.SOUTH);

            vbox.add(Box.createVerticalStrut(15));

            // Buttons "Ok" and "Cancel"
            Box hbox = Box.createHorizontalBox();
            hbox.add(Box.createHorizontalGlue());
            preferencesOkButton.setText("   Ok   ");
            preferencesOkButton.addActionListener(e -> {
                for (PreferencesPanel pp : panels) {
                    pp.savePreferences(preferences);
                }
                preferencesDialog.setVisible(false);
                okCallback.run();
            });
            hbox.add(preferencesOkButton);
            hbox.add(Box.createHorizontalStrut(5));
            JButton preferencesCancelButton = new JButton("Cancel");
            Action preferencesCancelActionListener = new AbstractAction() {
                public void actionPerformed(ActionEvent actionEvent) { preferencesDialog.setVisible(false); }
            };
            preferencesCancelButton.addActionListener(preferencesCancelActionListener);
            hbox.add(preferencesCancelButton);
            vbox.add(hbox);

            // Last setup
            JRootPane rootPane = preferencesDialog.getRootPane();
            rootPane.setDefaultButton(preferencesOkButton);
            rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "PreferencesDescription.cancel");
            rootPane.getActionMap().put("PreferencesDescription.cancel", preferencesCancelActionListener);

            // Size of the screen
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            // Height of the task bar
            Insets scnMax = Toolkit.getDefaultToolkit().getScreenInsets(preferencesDialog.getGraphicsConfiguration());
            // screen height in pixels without taskbar
            int taskBarHeight = scnMax.bottom + scnMax.top;
            int maxHeight = screenSize.height - taskBarHeight;
            int preferredHeight = preferencesPanels.getPreferredSize().height + 2;

            if (preferredHeight > maxHeight) {
                preferredHeight = maxHeight;
            }

            preferencesScrollPane.setPreferredSize(new Dimension(400, preferredHeight));
            preferencesDialog.setMinimumSize(new Dimension(300, 200));

            // Prepare to display
            preferencesDialog.pack();
            preferencesDialog.setLocationRelativeTo(mainFrame);
        });
    }

    public void show(Runnable okCallback) {
        this.okCallback = okCallback;

        SwingUtilities.invokeLater(() -> {
            // Init
            for (PreferencesPanel pp : panels) {
                pp.loadPreferences(preferences);
            }
            // Show
            preferencesDialog.setVisible(true);
        });
    }

    // --- PreferencesPanel.PreferencesChangeListener --- //
    public void preferencesPanelChanged(PreferencesPanel source) {
        SwingUtil.invokeLater(() -> {
            boolean valid = source.arePreferencesValid();

            valids.put(source, Boolean.valueOf(valid));

            if (valid) {
                for (PreferencesPanel pp : panels) {
                    if (valids.get(pp) == Boolean.FALSE) {
                        preferencesOkButton.setEnabled(false);
                        return;
                    }
                }
                preferencesOkButton.setEnabled(true);
            } else {
                preferencesOkButton.setEnabled(false);
            }
        });
    }

    protected static class PreferencesPanelComparator implements Comparator<PreferencesPanel> {
        @Override
        public int compare(PreferencesPanel pp1, PreferencesPanel pp2) {
            return pp1.getPreferencesPanelTitle().compareTo(pp2.getPreferencesPanelTitle());
        }
    }
}
