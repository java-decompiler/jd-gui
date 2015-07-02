/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.service.preferencespanel

import org.jd.gui.spi.PreferencesPanel

import javax.swing.JCheckBox
import javax.swing.JPanel
import java.awt.Color
import java.awt.GridLayout

class ClassFileSaverPreferencesProvider extends JPanel implements PreferencesPanel {

    static final String ESCAPE_UNICODE_CHARACTERS = 'ClassFileSaverPreferences.escapeUnicodeCharacters'
    static final String OMIT_THIS_PREFIX = 'ClassFileSaverPreferences.omitThisPrefix'
    static final String REALIGN_LINE_NUMBERS = 'ClassFileSaverPreferences.realignLineNumbers'
    static final String WRITE_DEFAULT_CONSTRUCTOR = 'ClassFileSaverPreferences.writeDefaultConstructor'
    static final String WRITE_LINE_NUMBERS = 'ClassFileSaverPreferences.writeLineNumbers'
    static final String WRITE_METADATA = 'ClassFileSaverPreferences.writeMetadata'

    JCheckBox escapeUnicodeCharactersCheckBox
    JCheckBox omitThisPrefixCheckBox
    JCheckBox realignLineNumbersCheckBox
    JCheckBox writeDefaultConstructorCheckBox
    JCheckBox writeLineNumbersCheckBox
    JCheckBox writeMetadataCheckBox

    ClassFileSaverPreferencesProvider() {
        super(new GridLayout(0,1))

        escapeUnicodeCharactersCheckBox = new JCheckBox('Escape unicode characters')
        omitThisPrefixCheckBox = new JCheckBox("Omit the prefix 'this' if possible")
        realignLineNumbersCheckBox = new JCheckBox('Realign line numbers')
        writeDefaultConstructorCheckBox = new JCheckBox('Write default constructor')
        writeLineNumbersCheckBox = new JCheckBox('Write original line numbers')
        writeMetadataCheckBox = new JCheckBox('Write metadata')

        add(escapeUnicodeCharactersCheckBox)
        add(omitThisPrefixCheckBox)
        add(realignLineNumbersCheckBox)
        add(writeDefaultConstructorCheckBox)
        add(writeLineNumbersCheckBox)
        add(writeMetadataCheckBox)
    }

    // --- PreferencesPanel --- //
    String getPreferencesGroupTitle() { 'Source Saver' }
    String getPreferencesPanelTitle() { 'Class file' }

    public void init(Color errorBackgroundColor) {}

    public boolean isActivated() { true }

    void loadPreferences(Map<String, String> preferences) {
        escapeUnicodeCharactersCheckBox.selected = 'true'.equals(preferences.get(ESCAPE_UNICODE_CHARACTERS))
        omitThisPrefixCheckBox.selected = 'true'.equals(preferences.get(OMIT_THIS_PREFIX))
        realignLineNumbersCheckBox.selected = !'false'.equals(preferences.get(REALIGN_LINE_NUMBERS))
        writeDefaultConstructorCheckBox.selected = 'true'.equals(preferences.get(WRITE_DEFAULT_CONSTRUCTOR))
        writeLineNumbersCheckBox.selected = !'false'.equals(preferences.get(WRITE_LINE_NUMBERS))
        writeMetadataCheckBox.selected = !'false'.equals(preferences.get(WRITE_METADATA))
    }

    void savePreferences(Map<String, String> preferences) {
        preferences.put(ESCAPE_UNICODE_CHARACTERS, Boolean.toString(escapeUnicodeCharactersCheckBox.selected))
        preferences.put(OMIT_THIS_PREFIX, Boolean.toString(omitThisPrefixCheckBox.selected))
        preferences.put(REALIGN_LINE_NUMBERS, Boolean.toString(realignLineNumbersCheckBox.selected))
        preferences.put(WRITE_DEFAULT_CONSTRUCTOR, Boolean.toString(writeDefaultConstructorCheckBox.selected))
        preferences.put(WRITE_LINE_NUMBERS, Boolean.toString(writeLineNumbersCheckBox.selected))
        preferences.put(WRITE_METADATA, Boolean.toString(writeMetadataCheckBox.selected))
    }

    boolean arePreferencesValid() { true }

    void addPreferencesChangeListener(PreferencesPanel.PreferencesPanelChangeListener listener) {}
}
