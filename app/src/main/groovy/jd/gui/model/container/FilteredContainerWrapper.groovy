/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.model.container

import jd.gui.api.model.Container

class FilteredContainerWrapper implements Container {
    Container container
    Map<URI, Container.Entry> map
    Container.Entry root

    FilteredContainerWrapper(Container container, Container.Entry parentEntry, Collection<Container.Entry> entries) {
        this.container = container
        this.map = new HashMap<>()
        this.root = new EntryWrapper(container.root, parentEntry)

        for (def entry : entries) {
            while (entry && !map.containsKey(entry.uri)) {
                map.put(entry.uri, entry)
                entry = entry.parent
            }
        }
    }

    String getType() { container.type }
    Container.Entry getRoot() { root }
    Container.Entry getEntry(URI uri) { map.get(uri) }
    Set<URI> getUris() { map.keySet() }

    class EntryWrapper implements Container.Entry, Comparable<EntryWrapper> {
        Container.Entry entry
        Container.Entry parent
        Collection<Container.Entry> children

        EntryWrapper(Container.Entry entry, Container.Entry parent) {
            this.entry = entry
            this.parent = parent
            this.children = null
        }

        Container getContainer() { FilteredContainerWrapper.this }
        Container.Entry getParent() { parent }
        URI getUri() { entry.uri }
        String getPath() { entry.path }
        boolean isDirectory() { entry.isDirectory() }
        long length() { entry.length() }
        InputStream getInputStream() { entry.inputStream }

        Collection<Container.Entry> getChildren() {
            if (children == null) {
                children = entry.children.grep { map.containsKey(it.uri) }.collect { new EntryWrapper(it, this) }
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
