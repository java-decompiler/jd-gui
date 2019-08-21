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
import org.jd.gui.util.function.TriConsumer;
import org.jd.gui.util.swing.SwingUtil;
import org.jd.gui.view.component.Tree;
import org.jd.gui.view.renderer.TreeNodeRenderer;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.util.*;
import java.util.function.BiConsumer;

public class SearchInConstantPoolsView<T extends DefaultMutableTreeNode & ContainerEntryGettable & UriGettable> {
    protected static final ContainerComparator CONTAINER_COMPARATOR = new ContainerComparator();

    public static final int SEARCH_TYPE = 1;
    public static final int SEARCH_CONSTRUCTOR = 2;
    public static final int SEARCH_METHOD = 4;
    public static final int SEARCH_FIELD = 8;
    public static final int SEARCH_STRING = 16;
    public static final int SEARCH_MODULE = 32;
    public static final int SEARCH_DECLARATION = 64;
    public static final int SEARCH_REFERENCE = 128;

    protected API api;
    protected Set<URI> accepted = new HashSet<>();
    protected Set<URI> expanded = new HashSet<>();

    protected JDialog searchInConstantPoolsDialog;
    protected JTextField searchInConstantPoolsEnterTextField;
    protected JLabel searchInConstantPoolsLabel;
    protected JCheckBox searchInConstantPoolsCheckBoxType;
    protected JCheckBox searchInConstantPoolsCheckBoxField;
    protected JCheckBox searchInConstantPoolsCheckBoxConstructor;
    protected JCheckBox searchInConstantPoolsCheckBoxMethod;
    protected JCheckBox searchInConstantPoolsCheckBoxString;
    protected JCheckBox searchInConstantPoolsCheckBoxModule;
    protected JCheckBox searchInConstantPoolsCheckBoxDeclarations;
    protected JCheckBox searchInConstantPoolsCheckBoxReferences;
    protected Tree searchInConstantPoolsTree;

    @SuppressWarnings("unchecked")
    public SearchInConstantPoolsView(
            API api, JFrame mainFrame,
            BiConsumer<String, Integer> changedPatternCallback,
            TriConsumer<URI, String, Integer> selectedTypeCallback) {
        this.api = api;
        // Build GUI
        SwingUtil.invokeLater(() -> {
            searchInConstantPoolsDialog = new JDialog(mainFrame, "Search", false);

            JPanel panel = new JPanel();
            panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            panel.setLayout(new BorderLayout());
            searchInConstantPoolsDialog.add(panel);

            // Box for search criteria
            Box vbox = Box.createVerticalBox();

            Box hbox = Box.createHorizontalBox();
            hbox.add(new JLabel("Search string (* = any string, ? = any character):"));
            hbox.add(Box.createHorizontalGlue());
            vbox.add(hbox);

            vbox.add(Box.createVerticalStrut(10));

            // Text field
            vbox.add(searchInConstantPoolsEnterTextField = new JTextField(30));
            searchInConstantPoolsEnterTextField.addKeyListener(new KeyAdapter() {
                @Override public void keyTyped(KeyEvent e)  {
                    switch (e.getKeyChar()) {
                        case '=': case '(': case ')': case '{': case '}': case '[': case ']':
                            e.consume();
                            break;
                        default:
                            if (Character.isDigit(e.getKeyChar()) && (searchInConstantPoolsEnterTextField.getText().length() == 0)) {
                                // First character can not be a digit
                                e.consume();
                            }
                            break;
                    }
                }
                @Override public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        DefaultMutableTreeNode root = (DefaultMutableTreeNode)searchInConstantPoolsTree.getModel().getRoot();
                        if (root.getChildCount() > 0) {
                            searchInConstantPoolsTree.requestFocus();
                            if (searchInConstantPoolsTree.getSelectionCount() == 0) {
                                searchInConstantPoolsTree.setSelectionPath(new TreePath(((DefaultMutableTreeNode)root.getChildAt(0)).getPath()));
                            }
                            e.consume();
                        }
                    }
                }
            });
            searchInConstantPoolsEnterTextField.getDocument().addDocumentListener(new DocumentListener() {
                @Override public void insertUpdate(DocumentEvent e) { call(); }
                @Override public void removeUpdate(DocumentEvent e) { call(); }
                @Override public void changedUpdate(DocumentEvent e) { call(); }
                protected void call() { changedPatternCallback.accept(searchInConstantPoolsEnterTextField.getText(), getFlags()); }
            });

            vbox.add(Box.createVerticalStrut(10));

            hbox = Box.createHorizontalBox();
            vbox.add(hbox);

            JPanel subpanel = new JPanel();
            subpanel.setBorder(BorderFactory.createTitledBorder("Search For"));
            subpanel.setLayout(new BorderLayout());
            hbox.add(subpanel);

            Box subhbox = Box.createHorizontalBox();
            subpanel.add(subhbox, BorderLayout.WEST);

            ItemListener checkBoxListener = (e) -> {
                changedPatternCallback.accept(searchInConstantPoolsEnterTextField.getText(), getFlags());
                searchInConstantPoolsEnterTextField.requestFocus();
            };

            JPanel subsubpanel = new JPanel();
            subsubpanel.setLayout(new GridLayout(2, 1));
            subsubpanel.add(searchInConstantPoolsCheckBoxType = new JCheckBox("Type", true));
            searchInConstantPoolsCheckBoxType.addItemListener(checkBoxListener);
            subsubpanel.add(searchInConstantPoolsCheckBoxField = new JCheckBox("Field"));
            searchInConstantPoolsCheckBoxField.addItemListener(checkBoxListener);
            subhbox.add(subsubpanel);

            subsubpanel = new JPanel();
            subsubpanel.setLayout(new GridLayout(2, 1));
            subsubpanel.add(searchInConstantPoolsCheckBoxConstructor = new JCheckBox("Constructor"));
            searchInConstantPoolsCheckBoxConstructor.addItemListener(checkBoxListener);
            subsubpanel.add(searchInConstantPoolsCheckBoxMethod = new JCheckBox("Method"));
            searchInConstantPoolsCheckBoxMethod.addItemListener(checkBoxListener);
            subhbox.add(subsubpanel);

            subsubpanel = new JPanel();
            subsubpanel.setLayout(new GridLayout(2, 1));
            subsubpanel.add(searchInConstantPoolsCheckBoxString = new JCheckBox("String Constant"));
            searchInConstantPoolsCheckBoxString.addItemListener(checkBoxListener);
            subsubpanel.add(searchInConstantPoolsCheckBoxModule = new JCheckBox("Java Module"));
            searchInConstantPoolsCheckBoxModule.addItemListener(checkBoxListener);
            subhbox.add(subsubpanel);

            subpanel = new JPanel();
            subpanel.setBorder(BorderFactory.createTitledBorder("Limit To"));
            subpanel.setLayout(new BorderLayout());
            hbox.add(subpanel);

            subhbox = Box.createHorizontalBox();
            subpanel.add(subhbox, BorderLayout.WEST);

            subsubpanel = new JPanel();
            subsubpanel.setLayout(new GridLayout(2, 1));
            subsubpanel.add(searchInConstantPoolsCheckBoxDeclarations = new JCheckBox("Declarations", true));
            searchInConstantPoolsCheckBoxDeclarations.addItemListener(checkBoxListener);
            subsubpanel.add(searchInConstantPoolsCheckBoxReferences = new JCheckBox("References", true));
            searchInConstantPoolsCheckBoxReferences.addItemListener(checkBoxListener);
            subhbox.add(subsubpanel);

            vbox.add(Box.createVerticalStrut(10));

            hbox = Box.createHorizontalBox();
            hbox.add(searchInConstantPoolsLabel = new JLabel("Matching types:"));
            hbox.add(Box.createHorizontalGlue());
            vbox.add(hbox);

            vbox.add(Box.createVerticalStrut(10));
            panel.add(vbox, BorderLayout.NORTH);

            JScrollPane scrollPane = new JScrollPane(searchInConstantPoolsTree = new Tree());
            searchInConstantPoolsTree.setShowsRootHandles(true);
            searchInConstantPoolsTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
            searchInConstantPoolsTree.setCellRenderer(new TreeNodeRenderer());
            searchInConstantPoolsTree.addKeyListener(new KeyAdapter() {
                @Override public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_UP) {
                        if (searchInConstantPoolsTree.getLeadSelectionRow() == 0) {
                            searchInConstantPoolsEnterTextField.requestFocus();
                            e.consume();
                        }
                    }
                }
            });
            searchInConstantPoolsTree.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        T node = (T)searchInConstantPoolsTree.getLastSelectedPathComponent();
                        if (node != null) {
                            selectedTypeCallback.accept(node.getUri(), searchInConstantPoolsEnterTextField.getText(), getFlags());
                        }
                    }
                }
            });
            searchInConstantPoolsTree.addTreeExpansionListener(new TreeExpansionListener() {
                @Override public void treeExpanded(TreeExpansionEvent e) {
                    DefaultTreeModel model = (DefaultTreeModel)searchInConstantPoolsTree.getModel();
                    T node = (T)e.getPath().getLastPathComponent();
                    // Expand node and find the first leaf
                    while (true) {
                        populate(model, node);
                        if (node.getChildCount() == 0) {
                            break;
                        }
                        node = (T)node.getChildAt(0);
                    }
                    searchInConstantPoolsTree.setSelectionPath(new TreePath(node.getPath()));
                }
                @Override public void treeCollapsed(TreeExpansionEvent e) {}
            });
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setPreferredSize(new Dimension(400, 150));
            panel.add(scrollPane, BorderLayout.CENTER);

            vbox = Box.createVerticalBox();

            vbox.add(Box.createVerticalStrut(25));

            hbox = Box.createHorizontalBox();
            hbox.add(Box.createHorizontalGlue());
            JButton searchInConstantPoolsOpenButton = new JButton("Open");
            hbox.add(searchInConstantPoolsOpenButton);
            searchInConstantPoolsOpenButton.setEnabled(false);
            Action searchInConstantPoolsOpenActionListener = new AbstractAction() {
                @Override public void actionPerformed(ActionEvent actionEvent) {
                    T selectedTreeNode = (T)searchInConstantPoolsTree.getLastSelectedPathComponent();
                    if (selectedTreeNode != null) {
                        selectedTypeCallback.accept(selectedTreeNode.getUri(), searchInConstantPoolsEnterTextField.getText(), getFlags());
                    }
                }
            };
            searchInConstantPoolsOpenButton.addActionListener(searchInConstantPoolsOpenActionListener);
            hbox.add(Box.createHorizontalStrut(5));
            JButton searchInConstantPoolsCancelButton = new JButton("Cancel");
            hbox.add(searchInConstantPoolsCancelButton);
            Action searchInConstantPoolsCancelActionListener = new AbstractAction() {
                @Override public void actionPerformed(ActionEvent actionEvent) { searchInConstantPoolsDialog.setVisible(false); }
            };
            searchInConstantPoolsCancelButton.addActionListener(searchInConstantPoolsCancelActionListener);

            vbox.add(hbox);

            panel.add(vbox, BorderLayout.SOUTH);

            // Last setup
            JRootPane rootPane = searchInConstantPoolsDialog.getRootPane();
            rootPane.setDefaultButton(searchInConstantPoolsOpenButton);
            rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "SearchInConstantPoolsView.cancel");
            rootPane.getActionMap().put("SearchInConstantPoolsView.cancel", searchInConstantPoolsCancelActionListener);

            searchInConstantPoolsDialog.setMinimumSize(searchInConstantPoolsDialog.getSize());

            searchInConstantPoolsEnterTextField.addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) {
                    searchInConstantPoolsTree.clearSelection();
                    searchInConstantPoolsOpenButton.setEnabled(false);
                }
            });

            searchInConstantPoolsTree.addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) {
                    searchInConstantPoolsOpenButton.setEnabled(searchInConstantPoolsTree.getSelectionCount() > 0);
                }
            });

            // Prepare to display
            searchInConstantPoolsDialog.pack();
            searchInConstantPoolsDialog.setLocationRelativeTo(searchInConstantPoolsDialog.getParent());
        });
    }

    @SuppressWarnings("unchecked")
    protected void populate(DefaultTreeModel model, T node) {
        // Populate node
        populate(node);
        // Populate children
        int i = node.getChildCount();
        while (i-- > 0) {
            T child = (T)node.getChildAt(i);
            if ((child instanceof TreeNodeExpandable) && !expanded.contains(child.getUri())) {
                populate(child);
            }
        }
        // Refresh
        model.reload(node);
    }

    @SuppressWarnings("unchecked")
    protected void populate(T node) {
        if ((node instanceof TreeNodeExpandable) && !expanded.contains(node.getUri())) {
            // Populate
            ((TreeNodeExpandable)node).populateTreeNode(api);
            expanded.add(node.getUri());
            // Filter
            int i = node.getChildCount();
            while (i-- > 0) {
                if (!accepted.contains(((T)node.getChildAt(i)).getUri())) {
                    node.remove(i);
                }
            }
        }
    }

    public void show() {
        SwingUtil.invokeLater(() -> {
            searchInConstantPoolsEnterTextField.selectAll();
            // Show
            searchInConstantPoolsDialog.setVisible(true);
            searchInConstantPoolsEnterTextField.requestFocus();
        });
    }

    public boolean isVisible() { return searchInConstantPoolsDialog.isVisible(); }

    public String getPattern() { return searchInConstantPoolsEnterTextField.getText(); }

    public int getFlags() {
        int flags = 0;

        if (searchInConstantPoolsCheckBoxType.isSelected())
            flags += SEARCH_TYPE;
        if (searchInConstantPoolsCheckBoxConstructor.isSelected())
            flags += SEARCH_CONSTRUCTOR;
        if (searchInConstantPoolsCheckBoxMethod.isSelected())
            flags += SEARCH_METHOD;
        if (searchInConstantPoolsCheckBoxField.isSelected())
            flags += SEARCH_FIELD;
        if (searchInConstantPoolsCheckBoxString.isSelected())
            flags += SEARCH_STRING;
        if (searchInConstantPoolsCheckBoxModule.isSelected())
            flags += SEARCH_MODULE;
        if (searchInConstantPoolsCheckBoxDeclarations.isSelected())
            flags += SEARCH_DECLARATION;
        if (searchInConstantPoolsCheckBoxReferences.isSelected())
            flags += SEARCH_REFERENCE;

        return flags;
    }

    public void showWaitCursor() {
        SwingUtil.invokeLater(() -> searchInConstantPoolsDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)));
    }

    public void hideWaitCursor() {
        SwingUtil.invokeLater(() -> searchInConstantPoolsDialog.setCursor(Cursor.getDefaultCursor()));
    }

    @SuppressWarnings("unchecked")
    public void updateTree(Collection<DelegatingFilterContainer> containers, int matchingTypeCount) {
        SwingUtil.invokeLater(() -> {
            DefaultTreeModel model = (DefaultTreeModel)searchInConstantPoolsTree.getModel();
            T root = (T)model.getRoot();

            // Reset tree nodes
            root.removeAllChildren();

            accepted.clear();
            expanded.clear();

            if (containers != null) {
                ArrayList<DelegatingFilterContainer> list = new ArrayList<>(containers);

                list.sort(CONTAINER_COMPARATOR);

                for (DelegatingFilterContainer container : list) {
                    // Init uri set
                    accepted.addAll(container.getUris());
                    // Populate tree
                    Container.Entry parentEntry = container.getRoot().getParent();
                    TreeNodeFactory treeNodeFactory = api.getTreeNodeFactory(parentEntry);

                    if (treeNodeFactory != null) {
                        root.add(treeNodeFactory.make(api, parentEntry));
                    }
                }

                // Expand node and find the first leaf
                T node = root;
                while (true) {
                    populate(model, node);
                    if (node.getChildCount() == 0) {
                        break;
                    }
                    node = (T)node.getChildAt(0);
                }
                searchInConstantPoolsTree.setSelectionPath(new TreePath(node.getPath()));
            } else {
                model.reload();
            }

            // Update matching item counter
            switch (matchingTypeCount) {
                case 0:
                    searchInConstantPoolsLabel.setText("Matching entries:");
                    break;
                case 1:
                    searchInConstantPoolsLabel.setText("1 matching entry:");
                    break;
                default:
                    searchInConstantPoolsLabel.setText(matchingTypeCount + " matching entries:");
            }
        });
    }

    protected static class ContainerComparator implements Comparator<Container> {
        @Override
        public int compare(Container c1, Container c2) {
            return c1.getRoot().getUri().compareTo(c2.getRoot().getUri());
        }
    }
}
