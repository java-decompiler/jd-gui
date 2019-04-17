/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view;

import org.jd.gui.api.API;
import org.jd.gui.api.model.Container;
import org.jd.gui.api.model.Indexes;
import org.jd.gui.api.model.TreeNodeData;
import org.jd.gui.api.model.Type;
import org.jd.gui.util.exception.ExceptionUtil;
import org.jd.gui.util.function.TriConsumer;
import org.jd.gui.util.swing.SwingUtil;
import org.jd.gui.view.component.Tree;
import org.jd.gui.view.renderer.TreeNodeRenderer;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.Future;

public class OpenTypeHierarchyView {
    protected static final ImageIcon ROOT_CLASS_ICON = new ImageIcon(OpenTypeHierarchyView.class.getClassLoader().getResource("org/jd/gui/images/generate_class.png"));
    protected static final ImageIcon ROOT_INTERFACE_ICON = new ImageIcon(OpenTypeHierarchyView.class.getClassLoader().getResource("org/jd/gui/images/generate_int.png"));

    protected static final TreeNodeComparator TREE_NODE_COMPARATOR = new TreeNodeComparator();

    protected API api;
    protected Collection<Future<Indexes>> collectionOfFutureIndexes;

    protected JDialog openTypeHierarchyDialog;
    protected Tree openTypeHierarchyTree;

    protected TriConsumer<Point, Collection<Container.Entry>, String> selectedTypeCallback;

    public OpenTypeHierarchyView(API api, JFrame mainFrame, TriConsumer<Point, Collection<Container.Entry>, String> selectedTypeCallback) {
        this.api = api;
        this.selectedTypeCallback = selectedTypeCallback;
        // Build GUI
        SwingUtil.invokeLater(() -> {
            openTypeHierarchyDialog = new JDialog(mainFrame, "Hierarchy Type", false);

            JPanel panel = new JPanel();
            panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            panel.setLayout(new BorderLayout());
            openTypeHierarchyDialog.add(panel);

            openTypeHierarchyTree = new Tree();
            openTypeHierarchyTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
            openTypeHierarchyTree.setCellRenderer(new TreeNodeRenderer());
            openTypeHierarchyTree.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        onTypeSelected();
                    }
                }
            });
            openTypeHierarchyTree.addTreeExpansionListener(new TreeExpansionListener() {
                @Override public void treeExpanded(TreeExpansionEvent e) {
                    TreeNode node = (TreeNode)e.getPath().getLastPathComponent();
                    // Expand node and find the first leaf
                    while (node.getChildCount() > 0) {
                        if (((DefaultMutableTreeNode)node.getChildAt(0)).getUserObject() == null) {
                            // Remove dummy node and create children
                            populateTreeNode(node, null);
                        }
                        if (node.getChildCount() != 1) {
                            break;
                        }
                        node = ((TreeNode)node.getChildAt(0));
                    }
                    DefaultTreeModel model = (DefaultTreeModel)openTypeHierarchyTree.getModel();
                    model.reload((TreeNode)e.getPath().getLastPathComponent());
                    openTypeHierarchyTree.setSelectionPath(new TreePath(node.getPath()));
                }
                @Override public void treeCollapsed(TreeExpansionEvent e) {}
            });
            openTypeHierarchyTree.addKeyListener(new KeyAdapter() {
                @Override public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_F4) {
                        TreeNode node = (TreeNode)openTypeHierarchyTree.getLastSelectedPathComponent();
                        if (node != null) {
                            updateTree(node.entry, node.typeName);
                        }
                    }
                }
            });

            JScrollPane openTypeHierarchyScrollPane = new JScrollPane(openTypeHierarchyTree);
            openTypeHierarchyScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            openTypeHierarchyScrollPane.setPreferredSize(new Dimension(400, 150));
            panel.add(openTypeHierarchyScrollPane, BorderLayout.CENTER);

            // Buttons "Open" and "Cancel"
            Box vbox = Box.createVerticalBox();
            panel.add(vbox, BorderLayout.SOUTH);
            vbox.add(Box.createVerticalStrut(25));
            Box hbox = Box.createHorizontalBox();
            vbox.add(hbox);
            hbox.add(Box.createHorizontalGlue());
            JButton openTypeHierarchyOpenButton = new JButton("Open");
            hbox.add(openTypeHierarchyOpenButton);
            openTypeHierarchyOpenButton.setEnabled(false);
            openTypeHierarchyOpenButton.addActionListener(e -> onTypeSelected());
            hbox.add(Box.createHorizontalStrut(5));
            JButton openTypeHierarchyCancelButton = new JButton("Cancel");
            hbox.add(openTypeHierarchyCancelButton);
            Action openTypeHierarchyCancelActionListener = new AbstractAction() {
                @Override public void actionPerformed(ActionEvent actionEvent) { openTypeHierarchyDialog.setVisible(false); }
            };
            openTypeHierarchyCancelButton.addActionListener(openTypeHierarchyCancelActionListener);

            openTypeHierarchyTree.addTreeSelectionListener(e -> {
                Object o = openTypeHierarchyTree.getLastSelectedPathComponent();
                if (o != null) {
                    o = ((TreeNode)o).entry;
                }
                openTypeHierarchyOpenButton.setEnabled(o != null);
            });

            // Last setup
            JRootPane rootPane = openTypeHierarchyDialog.getRootPane();
            rootPane.setDefaultButton(openTypeHierarchyOpenButton);
            rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "OpenTypeHierarchyView.cancel");
            rootPane.getActionMap().put("OpenTypeHierarchyView.cancel", openTypeHierarchyCancelActionListener);

            openTypeHierarchyDialog.setMinimumSize(openTypeHierarchyDialog.getSize());

            // Prepare to display
            openTypeHierarchyDialog.pack();
            openTypeHierarchyDialog.setLocationRelativeTo(mainFrame);
        });
    }

    public void show(Collection<Future<Indexes>> collectionOfFutureIndexes, Container.Entry entry, String typeName) {
        this.collectionOfFutureIndexes = collectionOfFutureIndexes;
        SwingUtil.invokeLater(() -> {
            updateTree(entry, typeName);
            openTypeHierarchyDialog.setVisible(true);
            openTypeHierarchyTree.requestFocus();
        });
    }

    public boolean isVisible() { return openTypeHierarchyDialog.isVisible(); }

    public void showWaitCursor() {
        SwingUtil.invokeLater(() -> openTypeHierarchyDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)));
    }

    public void hideWaitCursor() {
        SwingUtil.invokeLater(() -> openTypeHierarchyDialog.setCursor(Cursor.getDefaultCursor()));
    }

    public void updateTree(Collection<Future<Indexes>> collectionOfFutureIndexes) {
        this.collectionOfFutureIndexes = collectionOfFutureIndexes;
        TreeNode selectedTreeNode = (TreeNode)openTypeHierarchyTree.getLastSelectedPathComponent();

        if (selectedTreeNode != null) {
            updateTree(selectedTreeNode.entry, selectedTreeNode.typeName);
        }
    }

    protected void updateTree(Container.Entry entry, String typeName) {
        SwingUtil.invokeLater(() -> {
            // Clear tree
            DefaultTreeModel model = (DefaultTreeModel)openTypeHierarchyTree.getModel();
            DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
            root.removeAllChildren();

            TreeNode selectedTreeNode = createTreeNode(entry, typeName);
            TreeNode parentTreeNode = createParentTreeNode(selectedTreeNode);

            root.add(parentTreeNode);
            model.reload();

            if (selectedTreeNode != null) {
                TreePath path = new TreePath(selectedTreeNode.getPath());
                // Expand
                openTypeHierarchyTree.expandPath(path);
                // Scroll to show tree node
                openTypeHierarchyTree.makeVisible(path);
                Rectangle bounds = openTypeHierarchyTree.getPathBounds(path);

                if(bounds != null) {
                    bounds.x = 0;

                    Rectangle lastRowBounds = openTypeHierarchyTree.getRowBounds(openTypeHierarchyTree.getRowCount()-1);

                    if (lastRowBounds != null) {
                        bounds.y = Math.max(bounds.y-30, 0);
                        bounds.height = Math.min(bounds.height+bounds.y+60, lastRowBounds.height+lastRowBounds.y) - bounds.y;
                    }

                    openTypeHierarchyTree.scrollRectToVisible(bounds);
                    openTypeHierarchyTree.scrollPathToVisible(path);
                    openTypeHierarchyTree.fireVisibleDataPropertyChange();
                }
                // Select tree node
                openTypeHierarchyTree.setSelectionPath(path);
            }
        });
    }

    protected TreeNode createTreeNode(Container.Entry entry, String typeName) {
        Type type = api.getTypeFactory(entry).make(api, entry, typeName);

        typeName = type.getName();

        List<Container.Entry> entries = getEntries(typeName);
        TreeNode treeNode = new TreeNode(entry, typeName, entries, new TreeNodeBean(type));
        List<String> childTypeNames = getSubTypeNames(typeName);

        if (childTypeNames != null) {
            // Add dummy node
            treeNode.add(new DefaultMutableTreeNode());
        }

        return treeNode;
    }

    /**
     * Create parent and sibling tree nodes
     */
    protected TreeNode createParentTreeNode(TreeNode treeNode) {
        Type type = api.getTypeFactory(treeNode.entry).make(api, treeNode.entry, treeNode.typeName);
        String superTypeName = type.getSuperName();

        if (superTypeName != null) {
            List<Container.Entry> superEntries = getEntries(superTypeName);

            // Search entry in the sane container of 'entry'
            Container.Entry superEntry = null;

            if ((superEntries != null) && !superEntries.isEmpty()) {
                for (Container.Entry se : superEntries) {
                    if (se.getContainer() == treeNode.entry.getContainer()) {
                        superEntry = se;
                        break;
                    }
                }

                if (superEntry == null) {
                    // Not found -> Choose 1st one
                    superEntry = superEntries.get(0);
                }
            } else {
                superEntry = null;
            }

            if (superEntry != null) {
                // Create parent tree node
                TreeNode superTreeNode = createTreeNode(superEntry, superTypeName);
                // Populate parent tree node
                populateTreeNode(superTreeNode, treeNode);
                // Recursive call
                return createParentTreeNode(superTreeNode);
            } else {
                // Entry not found --> Most probable hypothesis : Java type entry
                int lastPackageSeparatorIndex = superTypeName.lastIndexOf('/');
                String package_ = superTypeName.substring(0, lastPackageSeparatorIndex).replace('/', '.');
                String name = superTypeName.substring(lastPackageSeparatorIndex + 1).replace('$', '.');
                String label = (package_ != null) ? name + " - " + package_ : name;
                Icon icon = ((type.getFlags() & Type.FLAG_INTERFACE) == 0) ? ROOT_CLASS_ICON : ROOT_INTERFACE_ICON;
                TreeNode rootTreeNode = new TreeNode(null, superTypeName, null, new TreeNodeBean(label, icon));

                if (package_.startsWith("java.")) {
                    // If root type is a JDK type, do not create a tree node for each child types
                    rootTreeNode.add(treeNode);
                } else {
                    populateTreeNode(rootTreeNode, treeNode);
                }

                return rootTreeNode;
            }
        } else {
            // super type undefined
            return treeNode;
        }
    }

    /**
     * @param superTreeNode  node to populate
     * @param activeTreeNode active child node
     */
    protected void populateTreeNode(TreeNode superTreeNode, TreeNode activeTreeNode) {
        superTreeNode.removeAllChildren();

        // Search preferred container: if 'superTreeNode' is a root with an unknown super entry, uses the container of active child node
        Container.Entry notNullEntry = superTreeNode.entry;

        if (notNullEntry == null) {
            notNullEntry = activeTreeNode.entry;
        }

        Container preferredContainer = notNullEntry.getContainer();
        String activeTypName = null;

        if (activeTreeNode != null) {
            activeTypName = activeTreeNode.typeName;
        }

        List<String> subTypeNames = getSubTypeNames(superTreeNode.typeName);
        ArrayList<TreeNode> treeNodes = new ArrayList<>();

        for (String subTypeName : subTypeNames) {
            if (subTypeName.equals(activeTypName)) {
                treeNodes.add(activeTreeNode);
            } else {
                // Search entry in the sane container of 'superTreeNode.entry'
                List<Container.Entry> entries = getEntries(subTypeName);
                Container.Entry entry = null;

                for (Container.Entry e : entries) {
                    if (e.getContainer() == preferredContainer) {
                        entry = e;
                    }
                }

                if (entry == null) {
                    // Not found -> Choose 1st one
                    entry = entries.get(0);
                }
                if (entry != null) {
                    // Create type
                    Type t = api.getTypeFactory(entry).make(api, entry, subTypeName);
                    if (t != null) {
                        // Create tree node
                        treeNodes.add(createTreeNode(entry, t.getName()));
                    }
                }
            }
        }

        treeNodes.sort(TREE_NODE_COMPARATOR);

        for (TreeNode treeNode : treeNodes) {
            superTreeNode.add(treeNode);
        }
    }

    public void focus() {
        SwingUtil.invokeLater(() -> openTypeHierarchyTree.requestFocus());
    }

    protected void onTypeSelected() {
        TreeNode selectedTreeNode = (TreeNode)openTypeHierarchyTree.getLastSelectedPathComponent();

        if (selectedTreeNode != null) {
            TreePath path = new TreePath(selectedTreeNode.getPath());
            Rectangle bounds = openTypeHierarchyTree.getPathBounds(path);
            Point listLocation = openTypeHierarchyTree.getLocationOnScreen();
            Point leftBottom = new Point(listLocation.x+bounds.x, listLocation.y+bounds.y+bounds.height);
            selectedTypeCallback.accept(leftBottom, selectedTreeNode.entries, selectedTreeNode.typeName);
        }
    }

    @SuppressWarnings("unchecked")
    protected List<String> getSubTypeNames(String typeName) {
        ArrayList<String> result = new ArrayList<>();

        try {
            for (Future<Indexes> futureIndexes : collectionOfFutureIndexes) {
                if (futureIndexes.isDone()) {
                    Map<String, Collection> subTypeNames = futureIndexes.get().getIndex("subTypeNames");
                    if (subTypeNames != null) {
                        Collection<String> collection = subTypeNames.get(typeName);
                        if (collection != null) {
                            for (String tn : collection) {
                                if (tn != null) {
                                    result.add(tn);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            assert ExceptionUtil.printStackTrace(e);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    protected List<Container.Entry> getEntries(String typeName) {
        ArrayList<Container.Entry> result = new ArrayList<>();

        try {
            for (Future<Indexes> futureIndexes : collectionOfFutureIndexes) {
                if (futureIndexes.isDone()) {
                    Map<String, Collection> typeDeclarations = futureIndexes.get().getIndex("typeDeclarations");
                    if (typeDeclarations != null) {
                        Collection<Container.Entry> collection = typeDeclarations.get(typeName);
                        if (collection != null) {
                            for (Container.Entry e : collection) {
                                if (e != null) {
                                    result.add(e);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            assert ExceptionUtil.printStackTrace(e);
        }

        return result;
    }

    protected static class TreeNode extends DefaultMutableTreeNode {
        Container.Entry entry;
        String typeName;
        List<Container.Entry> entries;

        TreeNode(Container.Entry entry, String typeName, List<Container.Entry> entries, Object userObject) {
            super(userObject);
            this.entry = entry;
            this.typeName = typeName;
            this.entries = entries;
        }
    }

    // Graphic data for renderer
    protected static class TreeNodeBean implements TreeNodeData {
        String label;
        String tip;
        Icon icon;
        Icon openIcon;

        TreeNodeBean(Type type) {
            this.label = (type.getDisplayPackageName() != null) ? type.getDisplayTypeName() + " - " + type.getDisplayPackageName() : type.getDisplayTypeName();
            this.icon = type.getIcon();
        }

        TreeNodeBean(String label, Icon icon) {
            this.label = label;
            this.icon = icon;
        }

        @Override public String getLabel() { return label; }
        @Override public String getTip() { return tip; }
        @Override public Icon getIcon() { return icon; }
        @Override public Icon getOpenIcon() { return openIcon; }
    }

    protected static class TreeNodeComparator implements Comparator<TreeNode> {
        @Override
        public int compare(TreeNode tn1, TreeNode tn2) {
            return ((TreeNodeBean)tn1.getUserObject()).label.compareTo(((TreeNodeBean)tn2.getUserObject()).label);
        }
    }
}
