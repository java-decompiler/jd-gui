/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view.component;

import org.jd.gui.api.API;
import org.jd.gui.api.feature.*;
import org.jd.gui.api.model.Container;
import org.jd.gui.api.model.Indexes;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Future;

public class DynamicPage
        extends JPanel
        implements ContentCopyable, ContentSavable, ContentSearchable, ContentSelectable, FocusedTypeGettable,
                   IndexesChangeListener, LineNumberNavigable, PreferencesChangeListener, UriGettable, UriOpenable,
                   API.LoadSourceListener
{
    protected API api;
    protected Container.Entry entry;
    protected TypePage page;
    protected URI lastOpenedUri;
    protected Collection<Future<Indexes>> lastCollectionOfFutureIndexes;

    public DynamicPage(API api, Container.Entry entry) {
        super(new BorderLayout());
        this.api = api;
        this.entry = entry;

        String source = api.getSource(entry);

        if (source == null) {
            // Display the decompiled source code
            add(page = new ClassFilePage(api, entry));
            // Try to load source in background
            api.loadSource(entry, this);
        } else {
            // Display original source code
            add(page = new JavaFilePage(api, new DelegatedEntry(entry, source)));
        }
    }

    // --- ContentCopyable --- //
    @Override public void copy() { page.copy(); }

    // --- ContentSavable --- //
    @Override public String getFileName() { return page.getFileName(); }
    @Override public void save(API api, OutputStream outputStream) { page.save(api, outputStream); }

    // --- ContentSearchable --- //
    @Override public boolean highlightText(String text, boolean caseSensitive) { return page.highlightText(text, caseSensitive); }
    @Override public void findNext(String text, boolean caseSensitive) { page.findNext(text, caseSensitive); }
    @Override public void findPrevious(String text, boolean caseSensitive) { page.findPrevious(text, caseSensitive); }

    // --- ContentSelectable --- //
    @Override public void selectAll() { page.selectAll(); }

    // --- FocusedTypeGettable --- //
    @Override public String getFocusedTypeName() { return page.getFocusedTypeName(); }

    // --- ContainerEntryGettable --- //
    @Override public Container.Entry getEntry() { return entry; }

    // --- IndexesChangeListener --- //
    @Override public void indexesChanged(Collection<Future<Indexes>> collectionOfFutureIndexes) {
        page.indexesChanged(lastCollectionOfFutureIndexes = collectionOfFutureIndexes);
    }

    // --- LineNumberNavigable --- //
    @Override public int getMaximumLineNumber() { return page.getMaximumLineNumber(); }
    @Override public void goToLineNumber(int lineNumber) { page.goToLineNumber(lineNumber); }
    @Override public boolean checkLineNumber(int lineNumber) { return page.checkLineNumber(lineNumber); }

    // --- PreferencesChangeListener --- //
    @Override public void preferencesChanged(Map<String, String> preferences) { page.preferencesChanged(preferences); }

    // --- UriGettable --- //
    @Override public URI getUri() { return entry.getUri(); }

    // --- UriOpenable --- //
    @Override public boolean openUri(URI uri) { return page.openUri(lastOpenedUri = uri); }

    // --- LoadSourceListener --- //
    @Override public void sourceLoaded(String source) {
        SwingUtilities.invokeLater(() -> {
            // Replace the decompiled source code by the original
            Point viewPosition = page.getScrollPane().getViewport().getViewPosition();

            removeAll();
            add(page = new JavaFilePage(api, new DelegatedEntry(entry, source)));
            page.getScrollPane().getViewport().setViewPosition(viewPosition);

            if (lastOpenedUri != null) {
                page.openUri(lastOpenedUri);
            }

            if (lastCollectionOfFutureIndexes != null) {
                page.indexesChanged(lastCollectionOfFutureIndexes);
            }
        });
    }

    protected static class DelegatedEntry implements Container.Entry {
        protected Container.Entry entry;
        protected String source;

        DelegatedEntry(Container.Entry entry, String source) {
            this.entry = entry;
            this.source = source;
        }

        @Override public Container getContainer() { return entry.getContainer(); }
        @Override public Container.Entry getParent() { return entry.getParent(); }
        @Override public URI getUri() { return entry.getUri(); }
        @Override public String getPath() { return entry.getPath(); }
        @Override public boolean isDirectory() { return entry.isDirectory(); }
        @Override public long length() { return entry.length(); }
        @Override public InputStream getInputStream() { return new ByteArrayInputStream(source.getBytes()); }
        @Override public Collection<Container.Entry> getChildren() { return entry.getChildren(); }
    }
}
