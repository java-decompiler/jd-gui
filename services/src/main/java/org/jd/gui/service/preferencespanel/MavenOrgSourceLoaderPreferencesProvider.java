/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.preferencespanel;

import org.jd.gui.spi.PreferencesPanel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.regex.Pattern;

public class MavenOrgSourceLoaderPreferencesProvider extends JPanel implements PreferencesPanel, DocumentListener, ActionListener {
    public static final String ACTIVATED = "MavenOrgSourceLoaderPreferencesProvider.activated";
    public static final String FILTERS = "MavenOrgSourceLoaderPreferencesProvider.filters";

    public static final String DEFAULT_FILTERS_VALUE =
            "+org +com.google +com.springsource +com.sun -com +java +javax +sun +sunw " +
            "+spring +springframework +springmodules +tomcat +maven +edu";

    protected static final Pattern CONTROL_PATTERN = Pattern.compile("([+-][a-zA-Z_0-9$_.]+(\\s+[+-][a-zA-Z_0-9$_.]+)*)?\\s*");

    protected JCheckBox enableCheckBox;
    protected JTextArea filtersTextArea;
    protected JButton resetButton;
    protected Color errorBackgroundColor = Color.RED;
    protected Color defaultBackgroundColor;

    protected PreferencesPanel.PreferencesPanelChangeListener listener;

    public MavenOrgSourceLoaderPreferencesProvider() {
        super(new BorderLayout());

        enableCheckBox = new JCheckBox("Search source code on maven.org for:");
        enableCheckBox.addActionListener(this);

        filtersTextArea = new JTextArea();
        filtersTextArea.setFont(getFont());
        filtersTextArea.setLineWrap(true);
        filtersTextArea.getDocument().addDocumentListener(this);
        defaultBackgroundColor = filtersTextArea.getBackground();

        JComponent spacer = new JComponent() {};
        JScrollPane scrollPane = new JScrollPane(filtersTextArea);

        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("windows")) {
            spacer.setPreferredSize(new Dimension(22, -1));
            scrollPane.setPreferredSize(new Dimension(-1, 50));
        } else if (osName.contains("mac os")) {
            spacer.setPreferredSize(new Dimension(28, -1));
            scrollPane.setPreferredSize(new Dimension(-1, 56));
        } else {
            spacer.setPreferredSize(new Dimension(22, -1));
            scrollPane.setPreferredSize(new Dimension(-1, 56));
        }

        resetButton = new JButton("Reset");
        resetButton.addActionListener(this);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(resetButton, BorderLayout.EAST);

        add(enableCheckBox, BorderLayout.NORTH);
        add(spacer, BorderLayout.WEST);
        add(scrollPane, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
    }

    // --- PreferencesPanel --- //
    @Override public String getPreferencesGroupTitle() { return "Source loader"; }
    @Override public String getPreferencesPanelTitle() { return "maven.org"; }
    @Override public JComponent getPanel() { return this; }

    @Override
    public void init(Color errorBackgroundColor) {
        this.errorBackgroundColor = errorBackgroundColor;
    }

    @Override public boolean isActivated() { return true; }

    @Override
    public void loadPreferences(Map<String, String> preferences) {
        boolean enabled = !"false".equals(preferences.get(ACTIVATED));

        enableCheckBox.setSelected(enabled);
        filtersTextArea.setEnabled(enabled);
        resetButton.setEnabled(enabled);

        String filters = preferences.get(FILTERS);

        filtersTextArea.setText((filters == null) || filters.isEmpty() ? DEFAULT_FILTERS_VALUE : filters);
    }

    @Override
    public void savePreferences(Map<String, String> preferences) {
        preferences.put(ACTIVATED, Boolean.toString(enableCheckBox.isSelected()));
        preferences.put(FILTERS, filtersTextArea.getText().trim());
    }

    @Override public boolean arePreferencesValid() {
        return CONTROL_PATTERN.matcher(filtersTextArea.getText()).matches();
    }

    @Override public void addPreferencesChangeListener(PreferencesPanelChangeListener listener) {
        this.listener = listener;
    }


    // --- DocumentListener --- //
    @Override public void insertUpdate(DocumentEvent e) { onTextChange(); }
    @Override public void removeUpdate(DocumentEvent e) { onTextChange(); }
    @Override public void changedUpdate(DocumentEvent e) { onTextChange(); }

    protected void onTextChange() {
        filtersTextArea.setBackground(arePreferencesValid() ? defaultBackgroundColor : errorBackgroundColor);

        if (listener != null) {
            listener.preferencesPanelChanged(this);
        }
    }

    // --- ActionListener --- //
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == enableCheckBox) {
            boolean enabled = enableCheckBox.isSelected();
            filtersTextArea.setEnabled(enabled);
            resetButton.setEnabled(enabled);
        } else {
            // Reset button
            filtersTextArea.setText(DEFAULT_FILTERS_VALUE);
            filtersTextArea.requestFocus();
        }
    }
}
