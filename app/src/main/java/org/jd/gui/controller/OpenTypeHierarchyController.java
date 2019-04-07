/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.controller;

import org.jd.gui.api.API;
import org.jd.gui.api.feature.IndexesChangeListener;
import org.jd.gui.api.model.Container;
import org.jd.gui.api.model.Indexes;
import org.jd.gui.util.net.UriUtil;
import org.jd.gui.view.OpenTypeHierarchyView;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

public class OpenTypeHierarchyController implements IndexesChangeListener {
    protected API api;
    private ScheduledExecutorService executor;

    protected JFrame mainFrame;
    protected OpenTypeHierarchyView openTypeHierarchyView;
    protected SelectLocationController selectLocationController;

    protected Collection<Indexes> collectionOfIndexes;
    protected Consumer<URI> openCallback;

    public OpenTypeHierarchyController(API api, ScheduledExecutorService executor, JFrame mainFrame) {
        this.api = api;
        this.executor = executor;
        this.mainFrame = mainFrame;
        // Create UI
        openTypeHierarchyView = new OpenTypeHierarchyView(api, mainFrame, this::onTypeSelected);
        selectLocationController = new SelectLocationController(api, mainFrame);
    }

    public void show(Collection<Indexes> collectionOfIndexes, Container.Entry entry, String typeName, Consumer<URI> openCallback) {
        // Init attributes
        this.collectionOfIndexes = collectionOfIndexes;
        this.openCallback = openCallback;
        executor.execute(() -> {
            // Waiting the end of indexation...
            openTypeHierarchyView.showWaitCursor();
            for (Indexes indexes : collectionOfIndexes) {
                indexes.waitIndexers();
            }
            SwingUtilities.invokeLater(() -> {
                openTypeHierarchyView.hideWaitCursor();
                // Show
                openTypeHierarchyView.show(collectionOfIndexes, entry, typeName);
            });
        });
    }

    protected void onTypeSelected(Point leftBottom, Collection<Container.Entry> entries, String typeName) {
        if (entries.size() == 1) {
            // Open the single entry uri
            openCallback.accept(UriUtil.createURI(api, collectionOfIndexes, entries.iterator().next(), null, typeName));
        } else {
            // Multiple entries -> Open a "Select location" popup
            selectLocationController.show(
                new Point(leftBottom.x+(16+2), leftBottom.y+2),
                entries,
                (entry) -> openCallback.accept(UriUtil.createURI(api, collectionOfIndexes, entry, null, typeName)), // entry selected
                () -> openTypeHierarchyView.focus());                                                               // popup closeClosure
        }
    }

    // --- IndexesChangeListener --- //
    public void indexesChanged(Collection<Indexes> collectionOfIndexes) {
        if (openTypeHierarchyView.isVisible()) {
            // Update the list of containers
            this.collectionOfIndexes = collectionOfIndexes;
            // And refresh
            openTypeHierarchyView.updateTree(collectionOfIndexes);
        }
    }
}
