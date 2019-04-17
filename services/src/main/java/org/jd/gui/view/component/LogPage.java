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
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class LogPage extends HyperlinkPage implements UriGettable, IndexesChangeListener {
    protected API api;
    protected URI uri;
    protected Collection<Future<Indexes>> collectionOfFutureIndexes = Collections.emptyList();

    public LogPage(API api, URI uri, String content) {
        this.api = api;
        this.uri = uri;
        // Parse
        int index = 0;
        int eol = content.indexOf('\n');

        while (eol != -1) {
            parseLine(content, index, eol);
            index = eol + 1;
            eol = content.indexOf('\n', index);
        }

        parseLine(content, index, content.length());
        // Display
        setText(content);
    }

    protected void parseLine(String content, int index, int eol) {
        int start = content.indexOf("at ", index);

        if ((start != -1) && (start < eol)) {
            int leftParenthesisIndex = content.indexOf('(', start);

            if ((leftParenthesisIndex != -1) && (leftParenthesisIndex < eol)) {
                addHyperlink(new LogHyperlinkData(start+3, leftParenthesisIndex));
            }
        }
    }

    protected boolean isHyperlinkEnabled(HyperlinkData hyperlinkData) { return ((LogHyperlinkData)hyperlinkData).enabled; }

    protected void openHyperlink(int x, int y, HyperlinkData hyperlinkData) {
        LogHyperlinkData logHyperlinkData = (LogHyperlinkData)hyperlinkData;

        if (logHyperlinkData.enabled) {
            try {
                // Save current position in history
                Point location = textArea.getLocationOnScreen();
                int offset = textArea.viewToModel(new Point(x - location.x, y - location.y));
                api.addURI(new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), "position=" + offset, null));

                // Open link
                String text = getText();
                String typeAndMethodNames = text.substring(hyperlinkData.startPosition, hyperlinkData.endPosition);
                int lastDotIndex = typeAndMethodNames.lastIndexOf('.');
                String methodName = typeAndMethodNames.substring(lastDotIndex + 1);
                String internalTypeName = typeAndMethodNames.substring(0, lastDotIndex).replace('.', '/');
                List<Container.Entry> entries = IndexesUtil.findInternalTypeName(collectionOfFutureIndexes, internalTypeName);
                int leftParenthesisIndex = hyperlinkData.endPosition + 1;
                int rightParenthesisIndex = text.indexOf(')', leftParenthesisIndex);
                String lineNumberOrNativeMethodFlag = text.substring(leftParenthesisIndex, rightParenthesisIndex);

                if (lineNumberOrNativeMethodFlag.equals("Native Method")) {
                    // Example: at java.security.AccessController.doPrivileged(Native Method)
                    lastDotIndex = internalTypeName.lastIndexOf('/');
                    String shortTypeName = internalTypeName.substring(lastDotIndex + 1);
                    api.openURI(x, y, entries, null, shortTypeName + '-' + methodName + "-(*)?");
                } else {
                    // Example: at sun.misc.Launcher$AppClassLoader.loadClass(Launcher.java:294)
                    int colonIndex = lineNumberOrNativeMethodFlag.indexOf(':');
                    String lineNumber = lineNumberOrNativeMethodFlag.substring(colonIndex + 1);
                    api.openURI(x, y, entries, "lineNumber=" + lineNumber, null);
                }
            } catch (Exception e) {
                assert ExceptionUtil.printStackTrace(e);
            }
        }
    }

    // --- UriGettable --- //
    public URI getUri() { return uri; }

    // --- ContentSavable --- //
    public String getFileName() {
        String path = uri.getPath();
        int index = path.lastIndexOf('/');
        return path.substring(index + 1);
    }

    // --- IndexesChangeListener --- //
    public void indexesChanged(Collection<Future<Indexes>> collectionOfFutureIndexes) {
        // Update the list of containers
        this.collectionOfFutureIndexes = collectionOfFutureIndexes;
        // Refresh links
        boolean refresh = false;
        String text = getText();

        for (Map.Entry<Integer, HyperlinkData> entry : hyperlinks.entrySet()) {
            LogHyperlinkData entryData = (LogHyperlinkData)entry.getValue();
            String typeAndMethodNames = text.substring(entryData.startPosition, entryData.endPosition);
            int lastDotIndex = typeAndMethodNames.lastIndexOf('.');
            String internalTypeName = typeAndMethodNames.substring(0, lastDotIndex).replace('.', '/');
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

    public static class LogHyperlinkData extends HyperlinkData {
        public boolean enabled = false;

        public LogHyperlinkData(int startPosition, int endPosition) {
            super(startPosition, endPosition);
        }
    }
}
