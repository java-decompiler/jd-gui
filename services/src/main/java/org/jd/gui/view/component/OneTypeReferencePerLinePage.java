/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view.component;

import org.jd.gui.api.API;
import org.jd.gui.api.feature.IndexesChangeListener;
import org.jd.gui.api.feature.UriGettable;
import org.jd.gui.api.model.Container;
import org.jd.gui.api.model.Indexes;
import org.jd.gui.util.exception.ExceptionUtil;
import org.jd.gui.util.index.IndexesUtil;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;
import java.util.concurrent.Future;

public class OneTypeReferencePerLinePage extends TypeReferencePage implements UriGettable, IndexesChangeListener {
    protected API api;
    protected Container.Entry entry;
    protected Collection<Future<Indexes>> collectionOfFutureIndexes = Collections.emptyList();

    public OneTypeReferencePerLinePage(API api, Container.Entry entry) {
        this.api = api;
        this.entry = entry;
        // Load content file & Create hyperlinks
        StringBuilder sb = new StringBuilder();
        int offset = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(entry.getInputStream()))) {
            String line;

            while ((line = br.readLine()) != null) {
                String trim = line.trim();

                if (trim.length() > 0) {
                    int startIndex = offset + line.indexOf(trim);
                    int endIndex = startIndex + trim.length();
                    String internalTypeName = trim.replace('.', '/');

                    addHyperlink(new TypeReferencePage.TypeHyperlinkData(startIndex, endIndex, internalTypeName));
                }

                offset += line.length() + 1;
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            assert ExceptionUtil.printStackTrace(e);
        }

        // Display
        setText(sb.toString());
    }

    protected boolean isHyperlinkEnabled(HyperlinkData hyperlinkData) { return ((TypeHyperlinkData)hyperlinkData).enabled; }

    protected void openHyperlink(int x, int y, HyperlinkData hyperlinkData) {
        TypeHyperlinkData data = (TypeHyperlinkData)hyperlinkData;

        if (data.enabled) {
            try {
                // Save current position in history
                Point location = textArea.getLocationOnScreen();
                int offset = textArea.viewToModel(new Point(x-location.x, y-location.y));
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
}
