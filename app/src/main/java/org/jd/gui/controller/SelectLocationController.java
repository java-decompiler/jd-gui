/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.controller;

import org.jd.gui.api.API;
import org.jd.gui.api.model.Container;
import org.jd.gui.api.model.Type;
import org.jd.gui.model.container.DelegatingFilterContainer;
import org.jd.gui.service.type.TypeFactoryService;
import org.jd.gui.spi.TypeFactory;
import org.jd.gui.view.SelectLocationView;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.util.*;
import java.util.function.Consumer;

public class SelectLocationController {
    protected static final ContainerEntryComparator CONTAINER_ENTRY_COMPARATOR = new ContainerEntryComparator();

    protected API api;
    protected SelectLocationView selectLocationView;

    public SelectLocationController(API api, JFrame mainFrame) {
        this.api = api;
        // Create UI
        selectLocationView = new SelectLocationView(api, mainFrame);
    }

    @SuppressWarnings("unchecked")
    public void show(Point location, Collection<Container.Entry> entries, Consumer<Container.Entry> selectedLocationCallback, Runnable closeCallback) {
        // Show UI
        HashMap<Container, ArrayList<Container.Entry>> map = new HashMap<>();

        for (Container.Entry entry : entries) {
            Container container = entry.getContainer();

            // Search root container
            while (true) {
                Container parentContainer = container.getRoot().getParent().getContainer();
                if (parentContainer.getRoot() == null) {
                    break;
                } else {
                    container = parentContainer;
                }
            }

            ArrayList<Container.Entry> list = map.get(container);

            if (list == null) {
                map.put(container, list=new ArrayList<>());
            }

            list.add(entry);
        }

        HashSet<DelegatingFilterContainer> delegatingFilterContainers = new HashSet<>();

        for (Map.Entry<Container, ArrayList<Container.Entry>> mapEntry : map.entrySet()) {
            Container container = mapEntry.getKey();
            // Create a filtered container
            // TODO In a future release, display matching types and inner-types, not only matching files
            delegatingFilterContainers.add(new DelegatingFilterContainer(container, getOuterEntries(mapEntry.getValue())));
        }

        Consumer<URI> selectedEntryCallback = uri -> onLocationSelected(delegatingFilterContainers, uri, selectedLocationCallback);

        selectLocationView.show(location, delegatingFilterContainers, entries.size(), selectedEntryCallback, closeCallback);
    }

    protected Collection<Container.Entry> getOuterEntries(Collection<Container.Entry> entries) {
        HashMap<Container.Entry, Container.Entry> innerTypeEntryToOuterTypeEntry = new HashMap<>();
        HashSet<Container.Entry> outerEntriesSet = new HashSet<>();

        for (Container.Entry entry : entries) {
            Container.Entry outerTypeEntry = null;
            TypeFactory factory = TypeFactoryService.getInstance().get(entry);

            if (factory != null) {
                Type type = factory.make(api, entry, null);

                if ((type != null) && (type.getOuterName() != null)) {
                    outerTypeEntry = innerTypeEntryToOuterTypeEntry.get(entry);

                    if (outerTypeEntry == null) {
                        HashMap<String, Container.Entry> typeNameToEntry = new HashMap<>();
                        HashMap<String, String> innerTypeNameToOuterTypeName = new HashMap<>();

                        // Populate "typeNameToEntry" and "innerTypeNameToOuterTypeName"
                        for (Container.Entry e : entry.getParent().getChildren()) {
                            factory = TypeFactoryService.getInstance().get(e);

                            if (factory != null) {
                                type = factory.make(api, e, null);

                                if (type != null) {
                                    typeNameToEntry.put(type.getName(), e);
                                    if (type.getOuterName() != null) {
                                        innerTypeNameToOuterTypeName.put(type.getName(), type.getOuterName());
                                    }
                                }
                            }
                        }

                        // Search outer type entries and populate "innerTypeEntryToOuterTypeEntry"
                        for (Map.Entry<String, String> e : innerTypeNameToOuterTypeName.entrySet()) {
                            Container.Entry innerTypeEntry = typeNameToEntry.get(e.getKey());

                            if (innerTypeEntry != null) {
                                String outerTypeName = e.getValue();

                                for (;;) {
                                    String typeName = innerTypeNameToOuterTypeName.get(outerTypeName);
                                    if (typeName != null) {
                                        outerTypeName = typeName;
                                    } else {
                                        break;
                                    }
                                }

                                outerTypeEntry = typeNameToEntry.get(outerTypeName);

                                if (outerTypeEntry != null) {
                                    innerTypeEntryToOuterTypeEntry.put(innerTypeEntry, outerTypeEntry);
                                }
                            }
                        }

                        // Get outer type entry
                        outerTypeEntry = innerTypeEntryToOuterTypeEntry.get(entry);
                    }
                }
            }

            if (outerTypeEntry != null) {
                outerEntriesSet.add(outerTypeEntry);
            } else {
                outerEntriesSet.add(entry);
            }
        }

        // Return outer type entries sorted by path
        ArrayList<Container.Entry> result = new ArrayList<>(outerEntriesSet);

        result.sort(CONTAINER_ENTRY_COMPARATOR);

        return result;
    }

    protected void onLocationSelected(Set<DelegatingFilterContainer> delegatingFilterContainers, URI uri, Consumer<Container.Entry> selectedLocationCallback) {
        // Open the single entry uri
        Container.Entry entry = null;

        for (DelegatingFilterContainer container : delegatingFilterContainers) {
            entry = container.getEntry(uri);
            if (entry != null) {
                break;
            }
        }

        if (entry != null) {
            selectedLocationCallback.accept(entry);
        }
    }

    protected static class ContainerEntryComparator implements Comparator<Container.Entry> {
        @Override
        public int compare(Container.Entry e1, Container.Entry e2) {
            return e1.getPath().compareTo(e2.getPath());
        }
    }
}
