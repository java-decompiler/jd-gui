/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.model.container;

import org.jd.gui.api.model.Container;

import java.io.InputStream;
import java.net.URI;
import java.util.*;

public class DelegatingFilterContainer implements Container {
    protected static final URI DEFAULT_ROOT_URI = URI.create("file:.");

    protected Container container;
    protected DelegatedEntry root;

    protected Set<URI> validEntries = new HashSet<>();
    protected Map<URI, DelegatedEntry> uriToDelegatedEntry = new HashMap<>();
    protected Map<URI, DelegatedContainer> uriToDelegatedContainer = new HashMap<>();

    public DelegatingFilterContainer(Container container, Collection<Entry> entries) {
        this.container = container;
        this.root = getDelegatedEntry(container.getRoot());

        for (Entry entry : entries) {
            while ((entry != null) && !validEntries.contains(entry.getUri())) {
                validEntries.add(entry.getUri());
                entry = entry.getParent();
            }
        }
    }

    @Override public String getType() { return container.getType(); }
    @Override public Container.Entry getRoot() { return root; }

    public Container.Entry getEntry(URI uri) { return uriToDelegatedEntry.get(uri); }
    public Set<URI> getUris() { return validEntries; }

    protected DelegatedEntry getDelegatedEntry(Container.Entry entry) {
        URI uri = entry.getUri();
        DelegatedEntry delegatedEntry = uriToDelegatedEntry.get(uri);
        if (delegatedEntry == null) {
            uriToDelegatedEntry.put(uri, delegatedEntry =new DelegatedEntry(entry));
        }
        return delegatedEntry;
    }

    protected DelegatedContainer getDelegatedContainer(Container container) {
        Entry root = container.getRoot();
        URI uri = (root == null) ? DEFAULT_ROOT_URI : root.getUri();
        DelegatedContainer delegatedContainer = uriToDelegatedContainer.get(uri);
        if (delegatedContainer == null) {
            uriToDelegatedContainer.put(uri, delegatedContainer =new DelegatedContainer(container));
        }
        return delegatedContainer;
    }

    protected class DelegatedEntry implements Entry, Comparable<DelegatedEntry> {
        protected Entry entry;
        protected Collection<Entry> children;

        public DelegatedEntry(Entry entry) {
            this.entry = entry;
        }

        @Override public Container getContainer() { return getDelegatedContainer(entry.getContainer()); }
        @Override public Entry getParent() { return getDelegatedEntry(entry.getParent()); }
        @Override public URI getUri() { return entry.getUri(); }
        @Override public String getPath() { return entry.getPath(); }
        @Override public boolean isDirectory() { return entry.isDirectory(); }
        @Override public long length() { return entry.length(); }
        @Override public InputStream getInputStream() { return entry.getInputStream(); }

        @Override
        public Collection<Entry> getChildren() {
            if (children == null) {
                children = new ArrayList<>();
                for (Entry child : entry.getChildren()) {
                    if (validEntries.contains(child.getUri())) {
                        children.add(getDelegatedEntry(child));
                    }
                }
            }
            return children;
        }

        @Override
        public int compareTo(DelegatedEntry other) {
            if (entry.isDirectory()) {
                if (!other.isDirectory()) {
                    return -1;
                }
            } else {
                if (other.isDirectory()) {
                    return 1;
                }
            }
            return entry.getPath().compareTo(other.getPath());
        }
    }

    protected class DelegatedContainer implements Container {
        protected Container container;

        public DelegatedContainer(Container container) {
            this.container = container;
        }

        @Override public String getType() { return container.getType(); }
        @Override public Entry getRoot() { return getDelegatedEntry(container.getRoot()); }
    }
}