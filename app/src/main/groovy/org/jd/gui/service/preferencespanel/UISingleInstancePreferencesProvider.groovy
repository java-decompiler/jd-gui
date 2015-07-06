/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.service.preferencespanel

import org.jd.gui.spi.PreferencesPanel

import javax.swing.*
import java.awt.*

/**
 * JTabbedPane.WRAP_TAB_LAYOUT is not supported by Aqua L&F.
 * This panel is not activated on Mac OSX.
 */
class UISingleInstancePreferencesProvider extends JPanel implements PreferencesPanel {

    static final String SINGLE_INSTANCE = 'UIMainWindowPreferencesProvider.singleInstance'

    JCheckBox singleInstanceTabsCheckBox

    UISingleInstancePreferencesProvider() {
        super(new GridLayout(0,1))

        singleInstanceTabsCheckBox = new JCheckBox('Single instance')

        add(singleInstanceTabsCheckBox)
    }

    // --- PreferencesPanel --- //
    String getPreferencesGroupTitle() { 'User Interface' }
    String getPreferencesPanelTitle() { 'Main window' }

    public void init(Color errorBackgroundColor) {}

    public boolean isActivated() {
        !System.getProperty('os.name').toLowerCase().contains('mac os')
    }

    void loadPreferences(Map<String, String> preferences) {
        singleInstanceTabsCheckBox.selected = 'true'.equals(preferences.get(SINGLE_INSTANCE))
    }

    void savePreferences(Map<String, String> preferences) {
        preferences.put(SINGLE_INSTANCE, Boolean.toString(singleInstanceTabsCheckBox.selected))
    }

    boolean arePreferencesValid() { true }

    void addPreferencesChangeListener(PreferencesPanel.PreferencesPanelChangeListener listener) {}
}
