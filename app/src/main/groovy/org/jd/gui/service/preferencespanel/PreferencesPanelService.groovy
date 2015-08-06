/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
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
