/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.controller

import groovy.swing.SwingBuilder
import jd.gui.api.API
import jd.gui.api.feature.IndexesChangeListener
import jd.gui.api.model.Container
import jd.gui.api.model.Indexes
import jd.gui.model.configuration.Configuration
import jd.gui.util.net.UriUtil
import jd.gui.view.OpenTypeHierarchyView

import java.awt.Cursor
import java.awt.Point

class OpenTypeHierarchyController implements IndexesChangeListener {
    API api
    OpenTypeHierarchyView openTypeHierarchyView
    SelectLocationController selectLocationController
    Collection<Indexes> collectionOfIndexes
    Closure openClosure

    OpenTypeHierarchyController(SwingBuilder swing, Configuration configuration, API api) {
        this.api = api
        // Create UI
        openTypeHierarchyView = new OpenTypeHierarchyView(
            swing, configuration, api,
            { leftBottom, entries, tn -> onTypeSelected(leftBottom, entries, tn) }, // onTypeSelectedClosure
            { parentTypeName -> getSubTypeNames(parentTypeName) },                  // getSubTypeNamesClosure
            { childTypeName -> getEntries(childTypeName) }                          // getEntriesClosure
        )
        selectLocationController = new SelectLocationController(swing, configuration, api)
    }

    void show(Collection<Indexes> collectionOfIndexes, Container.Entry entry, String typeName, Closure openClosure) {
        // Init attributes
        this.collectionOfIndexes = collectionOfIndexes
        this.openClosure = openClosure
        // Waiting the end of indexation...
        openTypeHierarchyView.swing.mainFrame.rootPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR))
        for (def indexes : collectionOfIndexes) { indexes.waitIndexers() }
        openTypeHierarchyView.swing.mainFrame.rootPane.setCursor(Cursor.getDefaultCursor())
        // Prepare view
        openTypeHierarchyView.updateTree(entry, typeName)
        // Show
        openTypeHierarchyView.show()
    }

    protected List<String> getSubTypeNames(String typeName) {
        return collectionOfIndexes.collect{ it.getIndex('subTypeNames').get(typeName) }.grep{ it!=null }.flatten()
    }

    protected List<Container.Entry> getEntries(String typeName) {
        return collectionOfIndexes.collect{ it.getIndex('typeDeclarations').get(typeName) }.grep{ it!=null }.flatten()
    }

    protected void onTypeSelected(Point leftBottom, Collection<Container.Entry> entries, String typeName) {
        if (entries.size() == 1) {
            // Open the single entry uri
            openClosure(UriUtil.createURI(api, collectionOfIndexes, entries.iterator().next(), null, typeName))
        } else {
            // Multiple entries -> Open a "Select location" popup
            selectLocationController.show(
                new Point(leftBottom.x+(16+2) as int, leftBottom.y+2 as int),
                entries,
                { entry -> openClosure(UriUtil.createURI(api, collectionOfIndexes, entry, null, typeName)) },   // entry selected
                { openTypeHierarchyView.focus() })                                                              // popup closeClosure
        }
    }

    // --- IndexesChangeListener --- //
    void indexesChanged(Collection<Indexes> collectionOfIndexes) {
        if (openTypeHierarchyView.isVisible()) {
            // Update the list of containers
            this.collectionOfIndexes = collectionOfIndexes
            // And refresh
            openTypeHierarchyView.updateTree(openTypeHierarchyView.entry, openTypeHierarchyView.typeName)
        }
    }
}
