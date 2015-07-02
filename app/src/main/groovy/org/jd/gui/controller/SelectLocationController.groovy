/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.controller

import groovy.swing.SwingBuilder
import org.jd.gui.api.API
import org.jd.gui.api.model.Container
import org.jd.gui.model.configuration.Configuration
import org.jd.gui.model.container.FilteredContainerWrapper
import org.jd.gui.service.type.TypeFactoryService
import org.jd.gui.view.SelectLocationView

import java.awt.Point

class SelectLocationController {

    API api
    SelectLocationView selectLocationView

    SelectLocationController(SwingBuilder swing, Configuration configuration, API api) {
        this.api = api
        // Create UI
        selectLocationView = new SelectLocationView(swing, configuration, api)
    }

    void show(Point location, Collection<Container.Entry> entries, Closure locationSelectedClosure, Closure closeClosure) {
        // Show UI
        def map = [:].withDefault { [] }

        for (def entry : entries) {
            def container = entry.container

            // Search root container
            while (true) {
                def parentContainer = container.root.parent.container
                if (parentContainer.root == null) {
                    break
                } else {
                    container = parentContainer
                }
            }

            map[container].add(entry)
        }

        def filteredContainerWrappers = new HashSet<FilteredContainerWrapper>()

        for (def mapEntry : map.entrySet()) {
            def container = mapEntry.key
            def parentEntry = container.root.parent

            // Dummy parent entry wrapper
            def parentEntryWrapper = new Container.Entry() {
                Collection<Container.Entry> children

                Container getContainer() { parentEntry.container }
                Container.Entry getParent() { null }
                URI getUri() { parentEntry.uri }
                String getPath() { parentEntry.path }
                boolean isDirectory() { parentEntry.isDirectory() }
                long length() { 0 }
                InputStream getInputStream() { null }
                Collection<Container.Entry> getChildren() { children }
            }
            // Create a filtered container
            // TODO In a future release, display matching types and inner-types, not only matching files
            def outerEntries = getOuterEntries(mapEntry.value)

            def containerWrapper = new FilteredContainerWrapper(container, parentEntryWrapper, outerEntries)
            // Initialization of 'children' of dummy parent entry wrapper
            parentEntryWrapper.children = containerWrapper.root.children

            filteredContainerWrappers.add(containerWrapper)
        }

        selectLocationView.show(
                location, filteredContainerWrappers, entries.size(),
                { uri -> onLocationSelected(filteredContainerWrappers, uri, locationSelectedClosure) },
                closeClosure)
    }

    Collection<Container.Entry> getOuterEntries(Collection<Container.Entry> entries) {
        def innerTypeEntryToOuterTypeEntry = [:]
        def outerEntriesSet = new HashSet<Container.Entry>()

        for (def entry : entries) {
            def type = TypeFactoryService.instance.get(entry)?.make(api, entry, null)

            if (type?.outerName) {
                def outerTypeEntry = innerTypeEntryToOuterTypeEntry.get(entry)

                if (outerTypeEntry == null) {
                    def typeNameToEntry = [:]
                    def innerTypeNameToOuterTypeName = [:]

                    // Populate "typeNameToEntry" and "innerTypeNameToOuterTypeName"
                    for (def e : entry.parent.children) {
                        type = TypeFactoryService.instance.get(e)?.make(api, e, null)

                        if (type) {
                            typeNameToEntry.put(type.name, e)
                            if (type.outerName) {
                                innerTypeNameToOuterTypeName.put(type.name, type.outerName)
                            }
                        }
                    }

                    // Search outer type entries and populate "innerTypeEntryToOuterTypeEntry"
                    for (def e : innerTypeNameToOuterTypeName.entrySet()) {
                        def innerTypeEntry = typeNameToEntry.get(e.key)

                        if (innerTypeEntry) {
                            def outerTypeName = e.value

                            for (;;) {
                                def typeName = innerTypeNameToOuterTypeName.get(outerTypeName)
                                if (typeName) {
                                    outerTypeName = typeName
                                } else {
                                    break
                                }
                            }

                            outerTypeEntry = typeNameToEntry.get(outerTypeName)

                            if (outerTypeEntry) {
                                innerTypeEntryToOuterTypeEntry.put(innerTypeEntry, outerTypeEntry)
                            }
                        }
                    }

                    // Get outer type entry
                    outerTypeEntry = innerTypeEntryToOuterTypeEntry.get(entry) ?: entry
                }

                outerEntriesSet.add(outerTypeEntry)
            } else {
                outerEntriesSet.add(entry)
            }
        }

        // Return outer type entries sorted by path
        return outerEntriesSet.sort { e1, e2 -> e1.path.compareTo(e2.path) }
    }

    void onLocationSelected(Set<FilteredContainerWrapper> filteredContainerWrappers, URI uri, Closure locationSelectedClosure) {
        // Open the single entry uri
        def entry = null

        for (def container : filteredContainerWrappers) {
            entry = container.getEntry(uri)
            if (entry) {
                break
            }
        }

        if (entry) {
            locationSelectedClosure(entry)
        }
    }
}
