/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view;

import org.jd.gui.api.API;
import org.jd.gui.api.feature.ContainerEntryGettable;
import org.jd.gui.api.feature.TreeNodeExpandable;
import org.jd.gui.api.feature.UriGettable;
import org.jd.gui.api.model.Container;
import org.jd.gui.model.container.DelegatingFilterContainer;
import org.jd.gui.spi.TreeNodeFactory;
import org.jd.gui.util.swing.SwingUtil;
import org.jd.gui.view.component.Tree;
import org.jd.gui.view.renderer.TreeNodeRenderer;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.function.Consumer;

public class SelectLocationView<T extends DefaultMutableTreeNode & ContainerEntryGettable & UriGettable> {
    protected static final DelegatingFilterContainerComparator DELEGATING_FILTER_CONTAINER_COMPARATOR = new DelegatingFilterContainerComparator();

    protected API api;

    protected JDialog selectLocationDialog;
    protected JLabel selectLocationLabel;
    protected Tree selectLocationTree;

    protected Consumer<URI> selectedEntryCallback;
    protected Runnable closeCallback;

    @SuppressWarnings("unchecked")
    public SelectLocationView(API api, JFrame mainFrame) {
        this.api = api;
        // Build GUI
        SwingUtil.invokeLater(() -> {
            selectLocationDialog = new JDialog(mainFrame, "", false);
            selectLocationDialog.setUndecorated(true);
            selectLocationDialog.addWindowListener(new WindowAdapter() {
                @Override public void windowDeactivated(WindowEvent e) { closeCallback.run(); }
            });

            Color bg = UIManager.getColor("ToolTip.background");

            JPanel selectLocationPanel = new JPanel(new BorderLayout());
            selectLocationPanel.setBorder(BorderFactory.createLineBorder(bg.darker()));
            selectLocationPanel.setBackground(bg);
            selectLocationDialog.add(selectLocationPanel);

            selectLocationLabel = new JLabel();
            selectLocationLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
            selectLocationPanel.add(selectLocationLabel, BorderLayout.NORTH);

            selectLocationTree = new Tree();
            selectLocationTree.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            selectLocationTree.setOpaque(false);
            selectLocationTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
            selectLocationTree.setCellRenderer(new TreeNodeRenderer());
            selectLocationTree.addKeyListener(new KeyAdapter() {
                @Override public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        onSelectedEntry();
                    }
                }
            });
            selectLocationTree.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() > 0) {
                        onSelectedEntry();
                    }
                }
            });
            selectLocationTree.addFocusListener(new FocusAdapter() {
                @Override public void focusLost(FocusEvent e) { selectLocationDialog.setVisible(false); }
            });
            selectLocationPanel.add(selectLocationTree, BorderLayout.CENTER);

            // Last setup
            JRootPane rootPane = selectLocationDialog.getRootPane();
            rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "SelectLocationView.cancel");
            rootPane.getActionMap().put("SelectLocationView.cancel", new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) { selectLocationDialog.setVisible(false); }
            });
        });
    }

    @SuppressWarnings("unchecked")
    public void show(Point location, Collection<DelegatingFilterContainer> containers, int locationCount, Consumer<URI> selectedEntryCallback, Runnable closeCallback) {
        this.selectedEntryCallback = selectedEntryCallback;
        this.closeCallback = closeCallback;

        SwingUtil.invokeLater(() -> {
            // Init
            T root = (T)selectLocationTree.getModel().getRoot();

            // Reset tree nodes
            root.removeAllChildren();

            ArrayList<DelegatingFilterContainer> sortedContainers = new ArrayList<>(containers);
            sortedContainers.sort(DELEGATING_FILTER_CONTAINER_COMPARATOR);

            for (DelegatingFilterContainer container : sortedContainers) {
                Container.Entry parentEntry = container.getRoot().getParent();
                TreeNodeFactory factory = api.getTreeNodeFactory(parentEntry);

                if (factory != null) {
                    T node = factory.make(api, parentEntry);

                    if (node != null) {
                        root.add(node);
                        populate(container.getUris(), node);
                    }
                }
            }

            ((DefaultTreeModel)selectLocationTree.getModel()).reload();

            // Expand all nodes
            for (int row = 0; row < selectLocationTree.getRowCount(); row++) {
                selectLocationTree.expandRow(row);
            }

            // Select first leaf
            T node = root;
            while (true) {
                if (node.getChildCount() == 0) {
                    break;
                }
                node = (T)node.getChildAt(0);
            }
            selectLocationTree.setSelectionPath(new TreePath(node.getPath()));

            // Reset preferred size
            selectLocationTree.setPreferredSize(null);

            // Resize
            Dimension ps = selectLocationTree.getPreferredSize();
            if (ps.width < 200)
                ps.width = 200;
            if (ps.height < 50)
                ps.height = 50;
            selectLocationTree.setPreferredSize(ps);

            selectLocationLabel.setText("" + locationCount + " locations:");

            selectLocationDialog.pack();
            selectLocationDialog.setLocation(location);
            // Show
            selectLocationDialog.setVisible(true);
            selectLocationTree.requestFocus();
        });
    }

    @SuppressWarnings("unchecked")
    protected void populate(Set<URI> uris, DefaultMutableTreeNode node) {
        if (node instanceof TreeNodeExpandable) {
            ((TreeNodeExpandable)node).populateTreeNode(api);

            int i = node.getChildCount();

            while (i-- > 0) {
                T child = (T)node.getChildAt(i);

                if (uris.contains(child.getUri())) {
                    populate(uris, child);
                } else {
                    node.remove(i);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void onSelectedEntry() {
        T node = (T)selectLocationTree.getLastSelectedPathComponent();

        if (node != null) {
            selectLocationDialog.setVisible(false);
            selectedEntryCallback.accept(node.getUri());
        }
    }

    protected static class DelegatingFilterContainerComparator implements Comparator<DelegatingFilterContainer> {
        @Override
        public int compare(DelegatingFilterContainer fcw1, DelegatingFilterContainer fcw2) {
            return fcw1.getRoot().getUri().compareTo(fcw2.getRoot().getUri());
        }
    }
}
