/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.preferencespanel;

import org.jd.gui.spi.PreferencesPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class ClassFileViewerPreferencesProvider extends JPanel implements PreferencesPanel {
    protected static final String ESCAPE_UNICODE_CHARACTERS = "ClassFileViewerPreferences.escapeUnicodeCharacters";
    protected static final String OMIT_THIS_PREFIX = "ClassFileViewerPreferences.omitThisPrefix";
    protected static final String REALIGN_LINE_NUMBERS = "ClassFileViewerPreferences.realignLineNumbers";
    protected static final String DISPLAY_DEFAULT_CONSTRUCTOR = "ClassFileViewerPreferences.displayDefaultConstructor";

    protected PreferencesPanel.PreferencesPanelChangeListener listener = null;
    protected JCheckBox escapeUnicodeCharactersCheckBox;
    protected JCheckBox omitThisPrefixCheckBox;
    protected JCheckBox realignLineNumbersCheckBox;
    protected JCheckBox displayDefaultConstructorCheckBox;

    public ClassFileViewerPreferencesProvider() {
        super(new GridLayout(0,1));

        escapeUnicodeCharactersCheckBox = new JCheckBox("Escape unicode characters");
        omitThisPrefixCheckBox = new JCheckBox("Omit the prefix 'this' if possible");
        realignLineNumbersCheckBox = new JCheckBox("Realign line numbers");
        displayDefaultConstructorCheckBox = new JCheckBox("Display default constructor");

        add(escapeUnicodeCharactersCheckBox);
        add(omitThisPrefixCheckBox);
        add(realignLineNumbersCheckBox);
        add(displayDefaultConstructorCheckBox);
    }

    // --- PreferencesPanel --- //
    @Override public String getPreferencesGroupTitle() { return "Viewer"; }
    @Override public String getPreferencesPanelTitle() { return "Class file"; }
    @Override public JComponent getPanel() { return this; }

    @Override public void init(Color errorBackgroundColor) {}

    @Override public boolean isActivated() { return true; }

    @Override
    public void loadPreferences(Map<String, String> preferences) {
        escapeUnicodeCharactersCheckBox.setSelected(!"false".equals(preferences.get(ESCAPE_UNICODE_CHARACTERS)));
        omitThisPrefixCheckBox.setSelected("true".equals(preferences.get(OMIT_THIS_PREFIX)));
        realignLineNumbersCheckBox.setSelected("true".equals(preferences.get(REALIGN_LINE_NUMBERS)));
        displayDefaultConstructorCheckBox.setSelected("true".equals(preferences.get(DISPLAY_DEFAULT_CONSTRUCTOR)));
    }

    @Override
    public void savePreferences(Map<String, String> preferences) {
        preferences.put(ESCAPE_UNICODE_CHARACTERS, Boolean.toString(escapeUnicodeCharactersCheckBox.isSelected()));
        preferences.put(OMIT_THIS_PREFIX, Boolean.toString(omitThisPrefixCheckBox.isSelected()));
        preferences.put(REALIGN_LINE_NUMBERS, Boolean.toString(realignLineNumbersCheckBox.isSelected()));
        preferences.put(DISPLAY_DEFAULT_CONSTRUCTOR, Boolean.toString(displayDefaultConstructorCheckBox.isSelected()));
    }

    @Override public boolean arePreferencesValid() { return true; }

    @Override public void addPreferencesChangeListener(PreferencesPanel.PreferencesPanelChangeListener listener) {}
}
