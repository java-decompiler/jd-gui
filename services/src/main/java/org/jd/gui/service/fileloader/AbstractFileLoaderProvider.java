/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.fileloader;

import org.jd.gui.api.API;
import org.jd.gui.api.feature.UriGettable;
import org.jd.gui.api.model.Container;
import org.jd.gui.api.model.TreeNodeData;
import org.jd.gui.spi.ContainerFactory;
import org.jd.gui.spi.FileLoader;
import org.jd.gui.spi.PanelFactory;
import org.jd.gui.spi.TreeNodeFactory;
import org.jd.gui.util.exception.ExceptionUtil;

import javax.swing.*;
import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

public abstract class AbstractFileLoaderProvider implements FileLoader {
    protected <T extends JComponent & UriGettable> T load(API api, File file, Path rootPath) {
        ContainerEntry parentEntry = new ContainerEntry(file);
        ContainerFactory containerFactory = api.getContainerFactory(rootPath);

        if (containerFactory != null) {
            Container container = containerFactory.make(api, parentEntry, rootPath);

            if (container != null) {
                parentEntry.setChildren(container.getRoot().getChildren());

                PanelFactory panelFactory = api.getMainPanelFactory(container);

                if (panelFactory != null) {
                    T mainPanel = panelFactory.make(api, container);

                    if (mainPanel != null) {
                        TreeNodeFactory treeNodeFactory = api.getTreeNodeFactory(parentEntry);
                        Object data = (treeNodeFactory != null) ? treeNodeFactory.make(api, parentEntry).getUserObject() : null;
                        Icon icon = (data instanceof TreeNodeData) ? ((TreeNodeData)data).getIcon() : null;
                        String location = file.getPath();

                        api.addPanel(file.getName(), icon, "Location: " + location, mainPanel);
                        return mainPanel;
                    }
                }
            }
        }

        return null;
    }

    protected static class ContainerEntry implements Container.Entry {
        protected static final Container PARENT_CONTAINER = new Container() {
            @Override public String getType() { return "generic"; }
            @Override public Container.Entry getRoot() { return null; }
        };

        protected Collection<Container.Entry> children = Collections.emptyList();
        protected File file;
        protected URI uri;
        protected String path;

        public ContainerEntry(File file) {
            this.file = file;
            this.uri = file.toURI();
            this.path = uri.getPath();

            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
        }

        @Override public Container getContainer() { return PARENT_CONTAINER; }
        @Override public Container.Entry getParent() { return null; }
        @Override public URI getUri() { return uri; }
        @Override public String getPath() { return path; }
        @Override public boolean isDirectory() { return file.isDirectory(); }
        @Override public long length() { return file.length(); }
        @Override public Collection<Container.Entry> getChildren() { return children; }

        @Override
        public InputStream getInputStream() {
            try {
                return new BufferedInputStream(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                assert ExceptionUtil.printStackTrace(e);
                return null;
            }
        }

        public void setChildren(Collection<Container.Entry> children) {
            this.children = children;
        }
    }
}
