/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.spi;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public interface PreferencesPanel {
    String getPreferencesGroupTitle();

    String getPreferencesPanelTitle();

    JComponent getPanel();

    void init(Color errorBackgroundColor);

    boolean isActivated();

    void loadPreferences(Map<String, String> preferences);

    void savePreferences(Map<String, String> preferences);

    boolean arePreferencesValid();

    void addPreferencesChangeListener(PreferencesPanelChangeListener listener);

    interface PreferencesPanelChangeListener {
        void preferencesPanelChanged(PreferencesPanel source);
    }
}
