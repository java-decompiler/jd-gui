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

/**
 * JTabbedPane.WRAP_TAB_LAYOUT is not supported by Aqua L&F.
 * This panel is not activated on Mac OSX.
 */
@CompileStatic
class UITabsPreferencesProvider extends JPanel implements PreferencesPanel {

    static final String TAB_LAYOUT = 'UITabsPreferencesProvider.singleLineTabs'

    JCheckBox singleLineTabsCheckBox

    UITabsPreferencesProvider() {
        super(new GridLayout(0,1))

        singleLineTabsCheckBox = new JCheckBox('Tabs on a single line')

        add(singleLineTabsCheckBox)
    }

    // --- PreferencesPanel --- //
    String getPreferencesGroupTitle() { 'User Interface' }
    String getPreferencesPanelTitle() { 'Tabs' }

    public void init(Color errorBackgroundColor) {}

    public boolean isActivated() {
        !System.getProperty('os.name').toLowerCase().contains('mac os')
    }

    void loadPreferences(Map<String, String> preferences) {
        singleLineTabsCheckBox.selected = 'true'.equals(preferences.get(TAB_LAYOUT))
    }

    void savePreferences(Map<String, String> preferences) {
        preferences.put(TAB_LAYOUT, Boolean.toString(singleLineTabsCheckBox.selected))
    }

    boolean arePreferencesValid() { true }

    void addPreferencesChangeListener(PreferencesPanel.PreferencesPanelChangeListener listener) {}
}
