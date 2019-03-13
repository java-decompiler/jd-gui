/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.spi;

import java.awt.*;
import java.util.Map;

public interface PreferencesPanel {
    public String getPreferencesGroupTitle();

    public String getPreferencesPanelTitle();

    public void init(Color errorBackgroundColor);

    public boolean isActivated();

    public void loadPreferences(Map<String, String> preferences);

    public void savePreferences(Map<String, String> preferences);

    public boolean arePreferencesValid();

    public void addPreferencesChangeListener(PreferencesPanelChangeListener listener);

    public interface PreferencesPanelChangeListener {
        public void preferencesPanelChanged(PreferencesPanel source);
    }
}
