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

public class FilteredContainerWrapper implements Container {
    protected Container container;
    protected EntryWrapper root;

    protected Set<URI> validEntries = new HashSet<>();
    protected Map<URI, EntryWrapper> uriToEntryWrapper = new HashMap<>();
    protected Map<URI, ContainerWrapper> uriToContainerWrapper = new HashMap<>();

    public FilteredContainerWrapper(Container container, Collection<Entry> entries) {
        this.container = container;
        this.root = getEntryWrapper(container.getRoot());

        for (Entry entry : entries) {
            while ((entry != null) && !validEntries.contains(entry.getUri())) {
                validEntries.add(entry.getUri());
                entry = entry.getParent();
            }
        }
    }

    @Override public String getType() { return container.getType(); }
    @Override public Container.Entry getRoot() { return root; }

    public Container.Entry getEntry(URI uri) { return uriToEntryWrapper.get(uri); }
    public Set<URI> getUris() { return validEntries; }

    protected EntryWrapper getEntryWrapper(Container.Entry entry) {
        URI uri = entry.getUri();
        EntryWrapper entryWrapper = uriToEntryWrapper.get(uri);
        if (entryWrapper == null) {
            uriToEntryWrapper.put(uri, entryWrapper=new EntryWrapper(entry));
        }
        return entryWrapper;
    }

    protected ContainerWrapper getContainerWrapper(Container.Entry entry) {
        URI uri = entry.getContainer().getRoot().getUri();
        ContainerWrapper containerWrapper = uriToContainerWrapper.get(uri);
        if (containerWrapper == null) {
            uriToContainerWrapper.put(uri, containerWrapper=new ContainerWrapper(entry.getContainer()));
        }
        return containerWrapper;
    }

    protected class EntryWrapper implements Entry, Comparable<EntryWrapper> {
        protected Entry entry;
        protected Collection<Entry> children;

        public EntryWrapper(Entry entry) {
            this.entry = entry;
        }

        @Override public Container getContainer() { return getContainerWrapper(entry.getContainer().getRoot()); }
        @Override public Entry getParent() { return getEntryWrapper(entry.getParent()); }
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
                        children.add(getEntryWrapper(child));
                    }
                }
            }
            return children;
        }

        @Override
        public int compareTo(EntryWrapper other) {
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

    protected class ContainerWrapper implements Container {
        protected Container container;

        public ContainerWrapper(Container container) {
            this.container = container;
        }

        @Override public String getType() { return container.getType(); }
        @Override public Entry getRoot() { return getEntryWrapper(container.getRoot()); }
    }
}