/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.model.container

import org.jd.gui.api.model.Container

class FilteredContainerWrapper implements Container {
    Container container
    Map<URI, Container.Entry> uriToEntry
    Map<URI, FilteredContainerWrapper> uriToWrapper
    Container.Entry root

    FilteredContainerWrapper(Container container, Container.Entry parentEntry, Collection<Container.Entry> entries) {
        this.container = container
        this.uriToEntry = new HashMap<>()
        this.uriToWrapper = new HashMap<>()
        this.root = new EntryWrapper(container.root, parentEntry)

        for (def entry : entries) {
            while (entry && !uriToEntry.containsKey(entry.uri)) {
                uriToEntry.put(entry.uri, entry)
                entry = entry.parent
            }
        }
    }

    protected FilteredContainerWrapper(
            Container container, FilteredContainerWrapper.EntryWrapper root,
            Map<URI, Container.Entry> uriToEntry,
            Map<URI, FilteredContainerWrapper> uriToWrapper) {
        this.container = container
        this.uriToEntry = uriToEntry
        this.uriToWrapper = uriToWrapper
        this.root = root
    }

    String getType() { container.type }
    Container.Entry getRoot() { root }
    Container.Entry getEntry(URI uri) { uriToEntry.get(uri) }
    Set<URI> getUris() { uriToEntry.keySet() }

    class EntryWrapper implements Container.Entry, Comparable<EntryWrapper> {
        Container.Entry entry
        Container.Entry parent
        Collection<Container.Entry> children

        EntryWrapper(Container.Entry entry, Container.Entry parent) {
            this.entry = entry
            this.parent = parent
            this.children = null
        }

        Container getContainer() {
            if (entry.container == FilteredContainerWrapper.this.container) {
                return FilteredContainerWrapper.this
            } else {
                def container = entry.container
                def root = container.root
                def wrapper = uriToWrapper.get(root.uri)

                if (wrapper == null) {
                    // Search EntryWrapper root
                    def entryWrapperRoot = this.parent
                    while (entryWrapperRoot.entry.container == container) {
                        entryWrapperRoot = entryWrapperRoot.parent
                    }

                    // Create a sub wrapper container
                    wrapper = new FilteredContainerWrapper(container, entryWrapperRoot, uriToEntry, uriToWrapper)
                    uriToWrapper.put(root.uri, wrapper)
                }

                return wrapper
            }
        }

        Container.Entry getParent() { parent }
        URI getUri() { entry.uri }
        String getPath() { entry.path }
        boolean isDirectory() { entry.isDirectory() }
        long length() { entry.length() }
        InputStream getInputStream() { entry.inputStream }

        Collection<Container.Entry> getChildren() {
            if (children == null) {
                children = entry.children.grep { uriToEntry.containsKey(it.uri) }.collect { new EntryWrapper(it, this) }
            }
            return children
        }

        int compareTo(EntryWrapper other) {
            if (entry.isDirectory()) {
                if (!other.isDirectory()) {
                    return -1
                }
            } else {
                if (other.isDirectory()) {
                    return 1
                }
            }
            return entry.path.compareTo(other.path)
        }
    }
}
