/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.view.component

import groovy.transform.CompileStatic
import jd.gui.api.API
import jd.gui.api.feature.IndexesChangeListener
import jd.gui.api.feature.UriGettable
import jd.gui.api.model.Container
import jd.gui.api.model.Indexes

import java.awt.Point

class ManifestFilePage extends HyperlinkPage implements UriGettable, IndexesChangeListener {
    protected API api
    protected Container.Entry entry
    protected Collection<Indexes> collectionOfIndexes

    ManifestFilePage(API api, Container.Entry entry) {
        this.api = api
        this.entry = entry
        // Load content file
        def text = entry.inputStream.text
        // Parse hyperlinks. Docs:
        // - http://docs.oracle.com/javase/7/docs/technotes/guides/jar/jar.html
        // - http://docs.oracle.com/javase/7/docs/api/java/lang/instrument/package-summary.html
        int startLineIndex = text.indexOf('Main-Class:')
        if (startLineIndex != -1) {
            // Example: Main-Class: jd.gui.App
            int startIndex = skipSeparators(text, startLineIndex + 'Main-Class:'.size())
            int endIndex = searchEndIndexOfValue(text, startLineIndex, startIndex)
            def typeName = text.substring(startIndex, endIndex)
            def internalTypeName = typeName.replace('.', '/')
            addHyperlink(new ManifestHyperlinkData(startIndex, endIndex, internalTypeName + '-main-([Ljava/lang/String;)V'))
        }

        startLineIndex = text.indexOf('Premain-Class:')
        if (startLineIndex != -1) {
            // Example: Premain-Class: packge.JavaAgent
            int startIndex = skipSeparators(text, startLineIndex + 'Premain-Class:'.size())
            int endIndex = searchEndIndexOfValue(text, startLineIndex, startIndex)
            def typeName = text.substring(startIndex, endIndex)
            def internalTypeName = typeName.replace('.', '/')
            // Undefined parameters : 2 candidate methods
            // http://docs.oracle.com/javase/6/docs/api/java/lang/instrument/package-summary.html
            addHyperlink(new ManifestHyperlinkData(startIndex, endIndex, internalTypeName + '-premain-(?)?'))
        }
        // Display
        setText(text)
        // Show hyperlinks
        indexesChanged(api.collectionOfIndexes)
    }

    @CompileStatic
    int skipSeparators(String text, int index) {
        int length = text.size()

        while (index < length) {
            switch (text.charAt(index)) {
                case ' ': case '\t': case '\n': case '\r':
                    index++
                    break
                default:
                    return index
            }
        }

        return index
    }

    @CompileStatic
    int searchEndIndexOfValue(String text, int startLineIndex, int startIndex) {
        int length = text.size()
        int index = startIndex

        while (index < length) {
            // MANIFEST.MF Specification: max line length = 72
            // http://docs.oracle.com/javase/7/docs/technotes/guides/jar/jar.html
            switch (text.charAt(index)) {
                case '\r':
                    // CR followed by LF ?
                    if ((index-startLineIndex >= 70) && (index+1 < length) && (text.charAt(index+1) == ' ')) {
                        // Multiline value
                        startLineIndex = index+1
                    } else if ((index-startLineIndex >= 70) && (index+2 < length) && (text.charAt(index+1) == '\n') && (text.charAt(index+2) == ' ')) {
                        // Multiline value
                        index++
                        startLineIndex = index+1
                    } else {
                        // (End of file) or (single line value) => return end index
                        return index
                    }
                    break
                case '\n':
                    if ((index-startLineIndex >= 70) && (index+1 < length) && (text.charAt(index+1) == ' ')) {
                        // Multiline value
                        startLineIndex = index+1
                    } else {
                        // (End of file) or (single line value) => return end index
                        return index
                    }
                    break
            }
            index++
        }

        return index
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
            def text = getText()
            def textLink = getValue(text, hyperlinkData.startPosition, hyperlinkData.endPosition)
            def internalTypeName = textLink.replace('.', '/')
            def entries = collectionOfIndexes?.collect { it.getIndex('typeDeclarations')?.get(internalTypeName) }.flatten().grep { it!=null }
            def rootUri = entry.container.root.uri.toString()
            def sameContainerEntries = entries?.grep { it.uri.toString().startsWith(rootUri) }

            if (sameContainerEntries) {
                api.openURI(x, y, sameContainerEntries, null, hyperlinkData.fragment)
            } else if (entries) {
                api.openURI(x, y, entries, null, hyperlinkData.fragment)
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
        def text = getText()

        for (def entry : hyperlinks.entrySet()) {
            def entryData = entry.value as ManifestHyperlinkData
            def textLink = getValue(text, entryData.startPosition, entryData.endPosition)
            def internalTypeName = textLink.replace('.', '/')
            boolean enabled = collectionOfIndexes.find { it.getIndex('typeDeclarations')?.get(internalTypeName) } != null

            if (entryData.enabled != enabled) {
                entryData.enabled = enabled
                refresh = true
            }
        }

        if (refresh) {
            textArea.repaint()
        }
    }

    static String getValue(String text, int startPosition, int endPosition) {
        return text
                // Extract text of link
                .substring(startPosition, endPosition)
                // Convert multiline value
                .replace('\r\n ', '')
                .replace('\r ', '')
                .replace('\n ', '')
    }

    static class ManifestHyperlinkData extends HyperlinkPage.HyperlinkData {
        boolean enabled
        String fragment

        ManifestHyperlinkData(int startPosition, int endPosition, String fragment) {
            super(startPosition, endPosition)
            this.enabled = false
            this.fragment = fragment
        }
    }
}

