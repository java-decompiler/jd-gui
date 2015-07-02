/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.view.component

import org.jd.gui.api.API
import org.jd.gui.api.feature.IndexesChangeListener
import org.jd.gui.api.feature.UriGettable
import org.jd.gui.api.model.Container
import org.jd.gui.api.model.Indexes
import org.jd.gui.util.xml.AbstractXmlPathFinder
import org.fife.ui.rsyntaxtextarea.SyntaxConstants

import java.awt.Point


class WebXmlFilePage extends TypeReferencePage implements UriGettable, IndexesChangeListener {
    protected API api
    protected Container.Entry entry
    protected Collection<Indexes> collectionOfIndexes

    WebXmlFilePage(API api, Container.Entry entry) {
        this.api = api
        this.entry = entry
        // Load content file
        def text = entry.inputStream.text
        // Create hyperlinks
        new PathFinder().find(text)
        // Display
        setText(text)
        // Show hyperlinks
        indexesChanged(api.collectionOfIndexes)
    }

    String getSyntaxStyle() { SyntaxConstants.SYNTAX_STYLE_XML }

    protected boolean isHyperlinkEnabled(HyperlinkPage.HyperlinkData hyperlinkData) { hyperlinkData.enabled }

    protected void openHyperlink(int x, int y, HyperlinkPage.HyperlinkData hyperlinkData) {
        if (hyperlinkData.enabled) {
            // Save current position in history
            def location = textArea.getLocationOnScreen()
            int offset = textArea.viewToModel(new Point(x-location.x as int, y-location.y as int))
            def uri = entry.uri
            api.addURI(new URI(uri.scheme, uri.authority, uri.path, 'position=' + offset, null))

            // Open link
            if (hyperlinkData instanceof TypeReferencePage.TypeHyperlinkData) {
                def internalTypeName = hyperlinkData.internalTypeName
                def entries = collectionOfIndexes?.collect { it.getIndex('typeDeclarations')?.get(internalTypeName) }.flatten().grep { it!=null }
                def rootUri = entry.container.root.uri.toString()
                def sameContainerEntries = entries?.grep { it.uri.toString().startsWith(rootUri) }

                if (sameContainerEntries) {
                    api.openURI(x, y, sameContainerEntries, null, hyperlinkData.internalTypeName)
                } else if (entries) {
                    api.openURI(x, y, entries, null, hyperlinkData.internalTypeName)
                }
            } else {
                String path = hyperlinkData.path
                def entry = searchEntry(this.entry.container.root, path)
                if (entry) {
                    api.openURI(x, y, [entry], null, path)
                }
            }
        }
    }

    static Container.Entry searchEntry(Container.Entry parent, String path) {
        if (path.charAt(0) == '/')
            path = path.substring(1)
        return recursiveSearchEntry(parent, path)
    }

    static Container.Entry recursiveSearchEntry(Container.Entry parent, String path) {
        def entry = parent.children.find { path.equals(it.path) }

        if (entry) {
            return entry
        } else {
            entry = parent.children.find { path.startsWith(it.path + '/') }
            return entry ? searchEntry(entry, path) : null
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
            boolean enabled

            if (data instanceof TypeHyperlinkData) {
                def internalTypeName = data.internalTypeName
                enabled = collectionOfIndexes.find { it.getIndex('typeDeclarations')?.get(internalTypeName) } != null
            } else {
                enabled = searchEntry(this.entry.container.root, data.path) != null
            }

            if (data.enabled != enabled) {
                data.enabled = enabled
                refresh = true
            }
        }

        if (refresh) {
            textArea.repaint()
        }
    }

    static class PathHyperlinkData extends HyperlinkPage.HyperlinkData {
        boolean enabled
        String path

        PathHyperlinkData(int startPosition, int endPosition, String path) {
            super(startPosition, endPosition)
            this.enabled = false
            this.path = path
        }
    }

    class PathFinder extends AbstractXmlPathFinder {
        static HashSet<String> typeHyperlinkPaths = [
            'web-app/filter/filter-class',
            'web-app/listener/listener-class',
            'web-app/servlet/servlet-class']

        static HashSet<String> pathHyperlinkPaths = [
            'web-app/jsp-config/taglib/taglib-location',
            'web-app/welcome-file-list/welcome-file',
            'web-app/login-config/form-login-config/form-login-page',
            'web-app/login-config/form-login-config/form-error-page',
            'web-app/jsp-config/jsp-property-group/include-prelude',
            'web-app/jsp-config/jsp-property-group/include-coda']

        PathFinder() {
            super(typeHyperlinkPaths + pathHyperlinkPaths)
        }

        void handle(String path, String text, int position) {
            def trim = text.trim()
            if (trim) {
                int startIndex = position + text.indexOf(trim)
                int endIndex = startIndex + trim.length()

                if (pathHyperlinkPaths.contains(path)) {
                    addHyperlink(new PathHyperlinkData(startIndex, endIndex, trim))
                } else {
                    def internalTypeName = trim.replace('.', '/')
                    addHyperlink(new TypeReferencePage.TypeHyperlinkData(startIndex, endIndex, internalTypeName))
                }
            }
        }
    }
}
