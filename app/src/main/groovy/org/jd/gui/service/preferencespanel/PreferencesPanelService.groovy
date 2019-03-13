/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.preferencespanel

import org.jd.gui.service.extension.ExtensionService
import org.jd.gui.spi.PreferencesPanel

@Singleton(lazy = true)
class PreferencesPanelService {
    final Collection<PreferencesPanel> providers = populate()

    protected Collection<PreferencesPanel> populate() {
        Collection<PreferencesPanel> list = ExtensionService.instance.load(PreferencesPanel).grep { it.isActivated() }
        Map<String, PreferencesPanel> map = [:]

        for (def provider : list) {
            map.put(provider.preferencesGroupTitle + '$' + provider.preferencesPanelTitle, provider)
        }

        return map.values()
    }
}
