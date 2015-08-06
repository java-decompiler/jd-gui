/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.service.actions

import groovy.transform.CompileStatic
import org.jd.gui.api.API
import org.jd.gui.api.model.Container
import org.jd.gui.service.extension.ExtensionService
import org.jd.gui.spi.ContextualActionsFactory

import javax.swing.Action

@Singleton(lazy = true)
class ContextualActionsFactoryService {
    protected static final ActionNameComparator COMPARATOR = new ActionNameComparator()

    protected final Collection<ContextualActionsFactory> providers = ExtensionService.instance.load(ContextualActionsFactory)

    @CompileStatic
    Collection<Action> get(API api, Container.Entry entry, String fragment) {
        Map<String, ArrayList<Action>> mapActions = [:].withDefault { [] }

        for (def provider : providers) {
            def actions = provider.make(api, entry, fragment)

            for (def action : actions) {
                mapActions.get(action.getValue(ContextualActionsFactory.GROUP_NAME)).add(action)
            }
        }

        if (mapActions) {
            def result = new ArrayList<Action>()

            // Sort by group names
            for (def groupName : mapActions.keySet().sort()) {
                if (! result.isEmpty()) {
                    // Add 'null' to mark a separator
                    result.add(null)
                }

                // Sort by names
                def actions = mapActions.get(groupName)
                Collections.sort(actions, COMPARATOR)
                result.addAll(actions)
            }

            return result
        } else {
            return Collections.emptyList()
        }
    }

    static class ActionNameComparator implements Comparator<Action> {

        int compare(Action a1, Action a2) {
            String n1 = a1.getValue(Action.NAME) ?: ''
            String n2 = a2.getValue(Action.NAME) ?: ''
            return n1.compareTo(n2)
        }

        boolean equals(Object other) { this == other }
    }
}
