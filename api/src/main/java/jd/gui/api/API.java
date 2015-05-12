/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.api;

import jd.gui.api.feature.UriGettable;
import jd.gui.api.model.Container;
import jd.gui.api.model.Indexes;
import jd.gui.spi.*;

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
