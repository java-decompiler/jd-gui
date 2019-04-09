/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.model.container;

import org.jd.gui.api.API;
import org.jd.gui.api.model.Container;
import org.jd.gui.spi.ContainerFactory;
import org.jd.gui.util.exception.ExceptionUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class GenericContainer implements Container {
    protected API api;
    protected int rootNameCount;
    protected Container.Entry root;

    public GenericContainer(API api, Container.Entry parentEntry, Path rootPath) {
        this.api = api;
        this.rootNameCount = rootPath.getNameCount();
        this.root = new Entry(parentEntry, rootPath, parentEntry.getUri()) {
            public Entry newChildEntry(Path fsPath) {
                return new Entry(parent, fsPath, null);
            }
        };
    }

    public String getType() { return "generic"; }
    public Container.Entry getRoot() { return root; }

    protected class Entry implements Container.Entry {
        protected Container.Entry parent;
        protected Path fsPath;
        protected String strPath;
        protected URI uri;
        protected Boolean isDirectory;
        protected Collection<Container.Entry> children;

        public Entry(Container.Entry parent, Path fsPath, URI uri) {
            this.parent = parent;
            this.fsPath = fsPath;
            this.strPath = null;
            this.uri = uri;
            this.isDirectory = null;
            this.children = null;
        }

        public Entry newChildEntry(Path fsPath) { return new Entry(this, fsPath, null); }

        public Container getContainer() { return GenericContainer.this; }
        public Container.Entry getParent() { return parent; }

        public URI getUri() {
            if (uri == null) {
                try {
                    uri = new URI(root.getUri().getScheme(), root.getUri().getHost(), root.getUri().getPath() + "!/" + getPath(), null);
                } catch (URISyntaxException e) {
                    assert ExceptionUtil.printStackTrace(e);
                }
            }
            return uri;
        }

        public String getPath() {
            if (strPath == null) {
                if (rootNameCount == fsPath.getNameCount()) {
                    strPath = "";
                } else {
                    strPath = fsPath.subpath(rootNameCount, fsPath.getNameCount()).toString();
                    if (strPath.endsWith(fsPath.getFileSystem().getSeparator())) {
                        // Cut last separator
                        strPath = strPath.substring(0, strPath.length()-fsPath.getFileSystem().getSeparator().length());
                    }
                }
            }
            return strPath;
        }

        public boolean isDirectory() {
            if (isDirectory == null) {
                isDirectory = Boolean.valueOf(Files.isDirectory(fsPath));
            }
            return isDirectory;
        }

        public long length() {
            try {
                return Files.size(fsPath);
            } catch (IOException e) {
                assert ExceptionUtil.printStackTrace(e);
                return -1L;
            }
        }

        public InputStream getInputStream() {
            try {
                return Files.newInputStream(fsPath);
            } catch (IOException e) {
                assert ExceptionUtil.printStackTrace(e);
                return null;
            }
        }

        public Collection<Container.Entry> getChildren() {
            if (children == null) {
                try {
                    if (Files.isDirectory(fsPath)) {
                        children = loadChildrenFromDirectoryEntry();
                    } else {
                        children = loadChildrenFromFileEntry();
                    }
                } catch (IOException e) {
                    assert ExceptionUtil.printStackTrace(e);
                }
            }
            return children;
        }

        protected Collection<Container.Entry> loadChildrenFromDirectoryEntry() throws IOException {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(fsPath)) {
                ArrayList<Container.Entry> children = new ArrayList<>();
                int parentNameCount = fsPath.getNameCount();

                for (Path subPath : stream) {
                    if (subPath.getNameCount() > parentNameCount) {
                        children.add(newChildEntry(subPath));
                    }
                }

                children.sort(ContainerEntryComparator.COMPARATOR);
                return Collections.unmodifiableCollection(children);
            }
        }

        protected Collection<Container.Entry> loadChildrenFromFileEntry() throws IOException {
            File tmpFile = File.createTempFile("jd-gui.", "." + fsPath.getFileName().toString());
            Path tmpPath = Paths.get(tmpFile.toURI());

            tmpFile.delete();
            Files.copy(fsPath, tmpPath);
            tmpFile.deleteOnExit();

            FileSystem subFileSystem = FileSystems.newFileSystem(tmpPath, null);

            if (subFileSystem != null) {
                Iterator<Path> rootDirectories = subFileSystem.getRootDirectories().iterator();

                if (rootDirectories.hasNext()) {
                    Path rootPath = rootDirectories.next();
                    ContainerFactory containerFactory = api.getContainerFactory(rootPath);

                    if (containerFactory != null) {
                        Container container = containerFactory.make(api, this, rootPath);

                        if (container != null) {
                            tmpFile.delete();
                            return container.getRoot().getChildren();
                        }
                    }
                }
            }

            tmpFile.delete();
            return Collections.emptyList();
        }
    }
}
