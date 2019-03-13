/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.api;

import org.jd.gui.api.feature.UriGettable;
import org.jd.gui.api.model.Container;
import org.jd.gui.api.model.Indexes;
import org.jd.gui.spi.*;

import javax.swing.*;
import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

public interface API {
    public boolean openURI(URI uri);

    public boolean openURI(int x, int y, Collection<Container.Entry> entries, String query, String fragment);

    public void addURI(URI uri);

    public <T extends JComponent & UriGettable> void addPanel(String title, Icon icon, String tip, T component);

    public Collection<Action> getContextualActions(Container.Entry entry, String fragment);

    public UriLoader getUriLoader(URI uri);

    public FileLoader getFileLoader(File file);

    public ContainerFactory getContainerFactory(Path rootPath);

    public PanelFactory getMainPanelFactory(Container container);

    public TreeNodeFactory getTreeNodeFactory(Container.Entry entry);

    public TypeFactory getTypeFactory(Container.Entry entry);

    public Indexer getIndexer(Container.Entry entry);

    public SourceSaver getSourceSaver(Container.Entry entry);

    public Map<String, String> getPreferences();

    public Collection<Indexes> getCollectionOfIndexes();
}
