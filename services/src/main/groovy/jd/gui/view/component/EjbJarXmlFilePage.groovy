/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.view.component

import jd.gui.api.API
import jd.gui.api.feature.IndexesChangeListener
import jd.gui.api.feature.UriGettable
import jd.gui.api.model.Container
import jd.gui.api.model.Indexes
import jd.gui.util.xml.AbstractXmlPathFinder
import org.fife.ui.rsyntaxtextarea.SyntaxConstants

import java.awt.*

class EjbJarXmlFilePage extends TypeHyperlinkPage implements UriGettable, IndexesChangeListener {
    protected API api
    protected Container.Entry entry
    protected Collection<Indexes> collectionOfIndexes

    EjbJarXmlFilePage(API api, Container.Entry entry) {
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

    class PathFinder extends AbstractXmlPathFinder {
        static HashSet<String> typeHyperlinkPaths = [
            'ejb-jar/assembly-descriptor/application-exception/exception-class',
            'ejb-jar/assembly-descriptor/interceptor-binding/interceptor-class',

            'ejb-jar/enterprise-beans/entity/home',
            'ejb-jar/enterprise-beans/entity/remote',
            'ejb-jar/enterprise-beans/entity/ejb-class',
            'ejb-jar/enterprise-beans/entity/prim-key-class',

            'ejb-jar/enterprise-beans/message-driven/ejb-class',
            'ejb-jar/enterprise-beans/message-driven/messaging-type',
            'ejb-jar/enterprise-beans/message-driven/resource-ref/injection-target/injection-target-class',
            'ejb-jar/enterprise-beans/message-driven/resource-env-ref/injection-target/injection-target-class',

            'ejb-jar/enterprise-beans/session/home',
            'ejb-jar/enterprise-beans/session/local',
            'ejb-jar/enterprise-beans/session/remote',
            'ejb-jar/enterprise-beans/session/business-local',
            'ejb-jar/enterprise-beans/session/business-remote',
            'ejb-jar/enterprise-beans/session/service-endpoint',
            'ejb-jar/enterprise-beans/session/ejb-class',
            'ejb-jar/enterprise-beans/session/ejb-ref/home',
            'ejb-jar/enterprise-beans/session/ejb-ref/remote',

            'ejb-jar/interceptors/interceptor/around-invoke/class',
            'ejb-jar/interceptors/interceptor/ejb-ref/home',
            'ejb-jar/interceptors/interceptor/ejb-ref/remote',
            'ejb-jar/interceptors/interceptor/interceptor-class'
        ]

        PathFinder() {
            super(typeHyperlinkPaths)
        }

        void handle(String path, String text, int position) {
            def trim = text.trim()
            if (trim) {
                int startIndex = position + text.indexOf(trim)
                int endIndex = startIndex + trim.length()
                def internalTypeName = trim.replace('.', '/')
                addHyperlink(new TypeHyperlinkPage.TypeHyperlinkData(startIndex, endIndex, internalTypeName))
            }
        }
    }
}
