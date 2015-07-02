/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.api.feature;

import java.util.Map;

public interface PreferencesChangeListener {
    public void preferencesChanged(Map<String, String> preferences);
}
