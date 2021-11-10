/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view.component;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.jd.gui.api.API;
import org.jd.gui.api.feature.IndexesChangeListener;
import org.jd.gui.api.feature.UriGettable;
import org.jd.gui.api.model.Container;
import org.jd.gui.api.model.Indexes;
import org.jd.gui.util.exception.ExceptionUtil;
import org.jd.gui.util.index.IndexesUtil;
import org.jd.gui.util.io.TextReader;
import org.jd.gui.util.xml.AbstractXmlPathFinder;

import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;
import java.util.concurrent.Future;

public class EjbJarXmlFilePage extends TypeReferencePage implements UriGettable, IndexesChangeListener {
    protected API api;
    protected Container.Entry entry;
    protected Collection<Future<Indexes>> collectionOfFutureIndexes = Collections.emptyList();

    public EjbJarXmlFilePage(API api, Container.Entry entry) {
        this.api = api;
        this.entry = entry;
        // Load content file
        String text = TextReader.getText(entry.getInputStream());
        // Create hyperlinks
        new PathFinder().find(text);
        // Display
        setText(text);
    }

    public String getSyntaxStyle() { return SyntaxConstants.SYNTAX_STYLE_XML; }

    protected boolean isHyperlinkEnabled(HyperlinkData hyperlinkData) { return ((TypeHyperlinkData)hyperlinkData).enabled; }

    protected void openHyperlink(int x, int y, HyperlinkData hyperlinkData) {
        TypeHyperlinkData data = (TypeHyperlinkData)hyperlinkData;

        if (data.enabled) {
            try {
                // Save current position in history
                Point location = textArea.getLocationOnScreen();
                int offset = textArea.viewToModel(new Point(x - location.x, y - location.y));
                URI uri = entry.getUri();
                api.addURI(new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), "position=" + offset, null));

                // Open link
                String internalTypeName = data.internalTypeName;
                List<Container.Entry> entries = IndexesUtil.findInternalTypeName(collectionOfFutureIndexes, internalTypeName);
                String rootUri = entry.getContainer().getRoot().getUri().toString();
                ArrayList<Container.Entry> sameContainerEntries = new ArrayList<>();

                for (Container.Entry entry : entries) {
                    if (entry.getUri().toString().startsWith(rootUri)) {
                        sameContainerEntries.add(entry);
                    }
                }

                if (sameContainerEntries.size() > 0) {
                    api.openURI(x, y, sameContainerEntries, null, data.internalTypeName);
                } else if (entries.size() > 0) {
                    api.openURI(x, y, entries, null, data.internalTypeName);
                }
            } catch (URISyntaxException e) {
                assert ExceptionUtil.printStackTrace(e);
            }
        }
    }

    // --- UriGettable --- //
    public URI getUri() { return entry.getUri(); }

    // --- ContentSavable --- //
    public String getFileName() {
        String path = entry.getPath();
        int index = path.lastIndexOf('/');
        return path.substring(index+1);
    }

    // --- IndexesChangeListener --- //
    public void indexesChanged(Collection<Future<Indexes>> collectionOfFutureIndexes) {
        // Update the list of containers
        this.collectionOfFutureIndexes = collectionOfFutureIndexes;
        // Refresh links
        boolean refresh = false;

        for (Map.Entry<Integer, HyperlinkData> entry : hyperlinks.entrySet()) {
            TypeHyperlinkData entryData = (TypeHyperlinkData)entry.getValue();
            String internalTypeName = entryData.internalTypeName;
            boolean enabled = IndexesUtil.containsInternalTypeName(collectionOfFutureIndexes, internalTypeName);

            if (entryData.enabled != enabled) {
                entryData.enabled = enabled;
                refresh = true;
            }
        }

        if (refresh) {
            textArea.repaint();
        }
    }

    public static final List<String> typeHyperlinkPaths = Arrays.asList(
        "ejb-jar/assembly-descriptor/application-exception/exception-class",
        "ejb-jar/assembly-descriptor/interceptor-binding/interceptor-class",

        "ejb-jar/enterprise-beans/entity/home",
        "ejb-jar/enterprise-beans/entity/remote",
        "ejb-jar/enterprise-beans/entity/ejb-class",
        "ejb-jar/enterprise-beans/entity/prim-key-class",

        "ejb-jar/enterprise-beans/message-driven/ejb-class",
        "ejb-jar/enterprise-beans/message-driven/messaging-type",
        "ejb-jar/enterprise-beans/message-driven/resource-ref/injection-target/injection-target-class",
        "ejb-jar/enterprise-beans/message-driven/resource-env-ref/injection-target/injection-target-class",

        "ejb-jar/enterprise-beans/session/home",
        "ejb-jar/enterprise-beans/session/local",
        "ejb-jar/enterprise-beans/session/remote",
        "ejb-jar/enterprise-beans/session/business-local",
        "ejb-jar/enterprise-beans/session/business-remote",
        "ejb-jar/enterprise-beans/session/service-endpoint",
        "ejb-jar/enterprise-beans/session/ejb-class",
        "ejb-jar/enterprise-beans/session/ejb-ref/home",
        "ejb-jar/enterprise-beans/session/ejb-ref/remote",

        "ejb-jar/interceptors/interceptor/around-invoke/class",
        "ejb-jar/interceptors/interceptor/ejb-ref/home",
        "ejb-jar/interceptors/interceptor/ejb-ref/remote",
        "ejb-jar/interceptors/interceptor/interceptor-class"
    );

    public class PathFinder extends AbstractXmlPathFinder {
        public PathFinder() {
            super(typeHyperlinkPaths);
        }

        public void handle(String path, String text, int position) {
            String trim = text.trim();
            if (trim != null) {
                int startIndex = position + text.indexOf(trim);
                int endIndex = startIndex + trim.length();
                String internalTypeName = trim.replace(".", "/");
                addHyperlink(new TypeHyperlinkData(startIndex, endIndex, internalTypeName));
            }
        }
    }
}
