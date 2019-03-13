/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.preferencespanel

import groovy.transform.CompileStatic
import org.jd.gui.spi.PreferencesPanel

import javax.swing.JCheckBox
import javax.swing.JPanel
import java.awt.Color
import java.awt.GridLayout

@CompileStatic
class ClassFileViewerPreferencesProvider extends JPanel implements PreferencesPanel {
    static final String ESCAPE_UNICODE_CHARACTERS = 'ClassFileViewerPreferences.escapeUnicodeCharacters'
    static final String OMIT_THIS_PREFIX = 'ClassFileViewerPreferences.omitThisPrefix'
    static final String REALIGN_LINE_NUMBERS = 'ClassFileViewerPreferences.realignLineNumbers'
    static final String DISPLAY_DEFAULT_CONSTRUCTOR = 'ClassFileViewerPreferences.displayDefaultConstructor'

    PreferencesPanel.PreferencesPanelChangeListener listener = null
    JCheckBox escapeUnicodeCharactersCheckBox
    JCheckBox omitThisPrefixCheckBox
    JCheckBox realignLineNumbersCheckBox
    JCheckBox displayDefaultConstructorCheckBox

    ClassFileViewerPreferencesProvider() {
        super(new GridLayout(0,1))

        escapeUnicodeCharactersCheckBox = new JCheckBox('Escape unicode characters')
        omitThisPrefixCheckBox = new JCheckBox("Omit the prefix 'this' if possible")
        realignLineNumbersCheckBox = new JCheckBox('Realign line numbers')
        displayDefaultConstructorCheckBox = new JCheckBox('Display default constructor')

        add(escapeUnicodeCharactersCheckBox)
        add(omitThisPrefixCheckBox)
        add(realignLineNumbersCheckBox)
        add(displayDefaultConstructorCheckBox)
    }

    // --- PreferencesPanel --- //
    String getPreferencesGroupTitle() { 'Viewer' }
    String getPreferencesPanelTitle() { 'Class file' }

    public void init(Color errorBackgroundColor) {}

    public boolean isActivated() { true }

    void loadPreferences(Map<String, String> preferences) {
        escapeUnicodeCharactersCheckBox.selected = !'false'.equals(preferences.get(ESCAPE_UNICODE_CHARACTERS))
        omitThisPrefixCheckBox.selected = 'true'.equals(preferences.get(OMIT_THIS_PREFIX))
        realignLineNumbersCheckBox.selected = 'true'.equals(preferences.get(REALIGN_LINE_NUMBERS))
        displayDefaultConstructorCheckBox.selected = 'true'.equals(preferences.get(DISPLAY_DEFAULT_CONSTRUCTOR))
    }

    void savePreferences(Map<String, String> preferences) {
        preferences.put(ESCAPE_UNICODE_CHARACTERS, Boolean.toString(escapeUnicodeCharactersCheckBox.selected))
        preferences.put(OMIT_THIS_PREFIX, Boolean.toString(omitThisPrefixCheckBox.selected))
        preferences.put(REALIGN_LINE_NUMBERS, Boolean.toString(realignLineNumbersCheckBox.selected))
        preferences.put(DISPLAY_DEFAULT_CONSTRUCTOR, Boolean.toString(displayDefaultConstructorCheckBox.selected))
    }

    boolean arePreferencesValid() { true }

    void addPreferencesChangeListener(PreferencesPanel.PreferencesPanelChangeListener listener) {}
}
