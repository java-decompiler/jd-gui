/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.spi;

import org.jd.gui.api.API;
import org.jd.gui.api.model.Container;

import javax.swing.*;
import java.util.Collection;

public interface ContextualActionsFactory {

    public static final String GROUP_NAME = "GroupNameKey";

    /**
     * Build a collection of actions for 'entry' and 'fragment', grouped by GROUP_NAME and sorted by NAME. Null values
     * are added for separators.
     *
     * @param fragment @see jd.gui.api.feature.UriOpenable
     * @return a collection of actions
     */
    public Collection<Action> make(API api, Container.Entry entry, String fragment);
}
