/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.service.preferencespanel

import org.jd.gui.spi.PreferencesPanel

@Singleton(lazy = true)
class PreferencesPanelService {
    final List<PreferencesPanel> providers = ServiceLoader.load(PreferencesPanel).toList().grep { it.isActivated() }
}
