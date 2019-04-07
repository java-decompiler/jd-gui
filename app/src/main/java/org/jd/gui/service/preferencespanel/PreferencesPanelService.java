/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.preferencespanel;

import org.jd.gui.service.extension.ExtensionService;
import org.jd.gui.spi.PreferencesPanel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class PreferencesPanelService {
    protected static final PreferencesPanelService PREFERENCES_PANEL_SERVICE = new PreferencesPanelService();

    public static PreferencesPanelService getInstance() { return PREFERENCES_PANEL_SERVICE; }

    protected final Collection<PreferencesPanel> providers;

    protected PreferencesPanelService() {
        Collection<PreferencesPanel> list = ExtensionService.getInstance().load(PreferencesPanel.class);
        Iterator<PreferencesPanel> iterator = list.iterator();

        while (iterator.hasNext()) {
            if (!iterator.next().isActivated()) {
                iterator.remove();
            }
        }

        HashMap<String, PreferencesPanel> map = new HashMap<>();

        for (PreferencesPanel panel : list) {
            map.put(panel.getPreferencesGroupTitle() + '$' + panel.getPreferencesPanelTitle(), panel);
        }

        providers = map.values();
    }

    public Collection<PreferencesPanel> getProviders() {
        return providers;
    }
}
