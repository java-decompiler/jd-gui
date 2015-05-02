/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.spi;

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
