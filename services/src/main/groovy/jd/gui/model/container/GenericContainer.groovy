/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.model.container

import groovy.transform.CompileStatic
import jd.gui.api.API
import jd.gui.api.model.Container

import java.nio.file.*

@CompileStatic
class GenericContainer implements Container {
    protected API api
    protected int rootNameCount
    protected Container.Entry root

    GenericContainer(API api, Container.Entry parentEntry, Path rootPath) {
        this.api = api
        this.rootNameCount = rootPath.nameCount
        this.root = new Entry(parentEntry, rootPath, parentEntry.uri) {
            Entry newChildEntry(Path fsPath) { new Entry(parent, fsPath, null) }
        }
    }

    String getType() { 'generic' }
    Container.Entry getRoot() { root }

    class Entry implements Container.Entry, Comparable<Entry> {
        protected Container.Entry parent
        protected Path fsPath
        protected String strPath
        protected URI uri
        protected Boolean isDirectory
        protected Collection<Container.Entry> children

        Entry(Container.Entry parent, Path fsPath, URI uri) {
            this.parent = parent
            this.fsPath = fsPath
            this.strPath = null
            this.uri = uri
            this.isDirectory = null
            this.children = null
        }

        Entry newChildEntry(Path fsPath) { new Entry(this, fsPath, null) }

        Container getContainer() { GenericContainer.this }
        Container.Entry getParent() { parent }

        URI getUri() {
            if (uri == null) {
                uri = URI.create(root.uri.toString() + '!/' + path)
            }
            return uri
        }

        String getPath() {
            if (strPath == null) {
                if (rootNameCount == fsPath.nameCount) {
                    strPath = ''
                } else {
                    strPath = fsPath.subpath(rootNameCount, fsPath.nameCount).join('/')
                    if (strPath.endsWith(fsPath.fileSystem.separator)) {
                        // Cut last separator
                        strPath = strPath.substring(0, strPath.length()-fsPath.fileSystem.separator.length())
                    }
                }
            }
            return strPath
        }

        boolean isDirectory() {
            if (isDirectory == null) {
                isDirectory = Boolean.valueOf(Files.isDirectory(fsPath))
            }
            return isDirectory
        }

        long length() { Files.size(fsPath) }
        InputStream getInputStream() { Files.newInputStream(fsPath) }

        Collection<Container.Entry> getChildren() {
            if (children == null) {
                if (Files.isDirectory(fsPath)) {
                    children = loadChildrenFromDirectoryEntry()
                } else {
                    children = loadChildrenFromFileEntry()
                }
            }
            return children
        }

        protected Collection<Container.Entry> loadChildrenFromDirectoryEntry() {
            DirectoryStream<Path> stream = null

            try {
                def children = new ArrayList<Container.Entry>()
                int parentNameCount = fsPath.nameCount
                stream = Files.newDirectoryStream(fsPath)

                for (def subPath : stream) {
                    if (subPath.nameCount > parentNameCount) {
                        children.add(newChildEntry(subPath))
                    }
                }

                return children.sort()
            } finally {
                stream?.close()
            }
        }

        protected Collection<Container.Entry> loadChildrenFromFileEntry() {
            def tmpFile = File.createTempFile('jd-gui.', '.tmp.' + fsPath.fileName.toString())
            def tmpPath = Paths.get(tmpFile.toURI())

            tmpFile.withOutputStream { OutputStream os ->
                Files.copy(fsPath, os)
            }

            FileSystem subFileSystem = FileSystems.newFileSystem(tmpPath, null)

            if (subFileSystem) {
                def rootDirectories = subFileSystem.rootDirectories.iterator()

                if (rootDirectories.hasNext()) {
                    tmpFile.deleteOnExit()

                    def rootPath = rootDirectories.next()
                    def container = api.getContainerFactory(rootPath)?.make(api, this, rootPath)
                    if (container) {
                        return container.root.children
                    }
                }
            }

            tmpFile.delete()
            return Collections.emptyList()
        }

        /**
         * Directories before files, sorted by path
         */
        int compareTo(Entry other) {
            if (Files.isDirectory(fsPath)) {
                if (!other.isDirectory()) {
                    return -1
                }
            } else {
                if (other.isDirectory()) {
                    return 1
                }
            }
            return fsPath.compareTo(other.fsPath)
        }
    }
}
