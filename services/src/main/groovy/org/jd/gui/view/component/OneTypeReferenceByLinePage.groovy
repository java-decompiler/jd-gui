/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */
package org.jd.gui.view.component

import org.jd.gui.api.API
import org.jd.gui.api.feature.IndexesChangeListener
import org.jd.gui.api.feature.UriGettable
import org.jd.gui.api.model.Container
import org.jd.gui.api.model.Indexes

import java.awt.Point

class OneTypeReferenceByLinePage extends TypeReferencePage implements UriGettable, IndexesChangeListener {
    protected API api
    protected Container.Entry entry
    protected Collection<Indexes> collectionOfIndexes

    OneTypeReferenceByLinePage(API api, Container.Entry entry) {
        this.api = api
        this.entry = entry
        // Load content file
        def text = entry.inputStream.text.replace('\r\n', '\n').replace('\r', '\n')
        // Create hyperlinks
        int offset = 0

        text.eachLine { String line ->
            def trim = line.trim()

            if (trim) {
                int startIndex = offset + line.indexOf(trim)
                int endIndex = startIndex + trim.length()
                def internalTypeName = trim.replace('.', '/')

                addHyperlink(new TypeReferencePage.TypeHyperlinkData(startIndex, endIndex, internalTypeName))
            }

            offset += line.length() + 1
        }
        // Display
        setText(text)
        // Show hyperlinks
        indexesChanged(api.collectionOfIndexes)
    }

    protected boolean isHyperlinkEnabled(HyperlinkPage.HyperlinkData hyperlinkData) { hyperlinkData.enabled }

    protected void openHyperlink(int x, int y, HyperlinkPage.HyperlinkData hyperlinkData) {
        if (hyperlinkData.enabled) {
            // Save current position in history
            def location = textArea.getLocationOnScreen()
            int offset = textArea.viewToModel(new Point(x-location.x as int, y-location.y as int))
            def uri = entry.uri
            api.addURI(new URI(uri.scheme, uri.authority, uri.path, 'position=' + offset, null))

            // Open link
            def internalTypeName = hyperlinkData.internalTypeName
            def entries = collectionOfIndexes?.collect { it.getIndex('typeDeclarations')?.get(internalTypeName) }.flatten().grep { it!=null }
            def rootUri = entry.container.root.uri.toString()
            def sameContainerEntries = entries?.grep { it.uri.toString().startsWith(rootUri) }

            if (sameContainerEntries) {
                api.openURI(x, y, sameContainerEntries, null, hyperlinkData.internalTypeName)
            } else if (entries) {
                api.openURI(x, y, entries, null, hyperlinkData.internalTypeName)
            }
        }
    }

    // --- UriGettable --- //
    URI getUri() { entry.uri }

    // --- ContentSavable --- //
    String getFileName() {
        def path = entry.path
        int index = path.lastIndexOf('/')
        return path.substring(index+1)
    }

    // --- IndexesChangeListener --- //
    void indexesChanged(Collection<Indexes> collectionOfIndexes) {
        // Update the list of containers
        this.collectionOfIndexes = collectionOfIndexes
        // Refresh links
        boolean refresh = false

        for (def entry : hyperlinks.entrySet()) {
            def data = entry.value
            def internalTypeName = data.internalTypeName
            boolean enabled = collectionOfIndexes.find { it.getIndex('typeDeclarations')?.get(internalTypeName) } != null

            if (data.enabled != enabled) {
                data.enabled = enabled
                refresh = true
            }
        }

        if (refresh) {
            textArea.repaint()
        }
    }
}
