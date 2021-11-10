/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.preferencespanel;

import org.jd.gui.service.platform.PlatformService;
import org.jd.gui.spi.PreferencesPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * JTabbedPane.WRAP_TAB_LAYOUT is not supported by Aqua L&F.
 * This panel is not activated on Mac OSX.
 */
public class UITabsPreferencesProvider extends JPanel implements PreferencesPanel {
    protected static final String TAB_LAYOUT = "UITabsPreferencesProvider.singleLineTabs";

    protected JCheckBox singleLineTabsCheckBox;

    public UITabsPreferencesProvider() {
        super(new GridLayout(0,1));

        singleLineTabsCheckBox = new JCheckBox("Tabs on a single line");

        add(singleLineTabsCheckBox);
    }

    // --- PreferencesPanel --- //
    @Override public String getPreferencesGroupTitle() { return "User Interface"; }
    @Override public String getPreferencesPanelTitle() { return "Tabs"; }
    @Override public JComponent getPanel() { return this; }

    @Override public void init(Color errorBackgroundColor) {}

    @Override public boolean isActivated() { return !PlatformService.getInstance().isMac(); }

    @Override public void loadPreferences(Map<String, String> preferences) {
        singleLineTabsCheckBox.setSelected("true".equals(preferences.get(TAB_LAYOUT)));
    }

    @Override public void savePreferences(Map<String, String> preferences) {
        preferences.put(TAB_LAYOUT, Boolean.toString(singleLineTabsCheckBox.isSelected()));
    }

    @Override public boolean arePreferencesValid() { return true; }

    @Override public void addPreferencesChangeListener(PreferencesPanel.PreferencesPanelChangeListener listener) {}
}
