/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.view

import groovy.swing.SwingBuilder
import jd.gui.api.API
import jd.gui.api.feature.TreeNodeExpandable
import jd.gui.model.configuration.Configuration
import jd.gui.model.container.FilteredContainerWrapper
import jd.gui.view.component.Tree
import jd.gui.view.renderer.TreeNodeRenderer

import javax.swing.JComponent
import javax.swing.JTree
import javax.swing.KeyStroke
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.event.TreeExpansionEvent
import javax.swing.event.TreeExpansionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeModel
import javax.swing.tree.TreePath
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

class SearchInConstantPoolsView {
    static final int SEARCH_TYPE_TYPE = 1
    static final int SEARCH_TYPE_CONSTRUCTOR = 2
    static final int SEARCH_TYPE_METHOD = 4
    static final int SEARCH_TYPE_FIELD = 8
    static final int SEARCH_TYPE_STRING = 16
    static final int SEARCH_TYPE_DECLARATION = 32
    static final int SEARCH_TYPE_REFERENCE = 64

    SwingBuilder swing
    API api
    Closure onTypeSelectedClosure
    Set<URI> accepted
    Set<URI> expanded

    SearchInConstantPoolsView(
            SwingBuilder swing, Configuration configuration, API api,
            Closure onPatternChangedClosure,
            Closure onTypeSelectedClosure) {
        this.swing = swing
        this.api = api
        this.onTypeSelectedClosure = onTypeSelectedClosure
        // Create listeners
        accepted = new HashSet<>()
        expanded = new HashSet<>()
        // Load GUI description
        swing.edt {
            // Setup
            registerBeanFactory('tree', Tree.class)
            // Load GUI description
            build(SearchInConstantPoolsDescription)
            searchInConstantPoolsDialog.with {
                rootPane.with {
                    defaultButton = searchInConstantPoolsOkButton
                    getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "SearchInConstantPoolsView.cancel")
                    actionMap.put("SearchInConstantPoolsView.cancel", searchInConstantPoolsCancelAction)
                }
                minimumSize = size
                searchInConstantPoolsEnterTextField.addKeyListener(new KeyAdapter() {
                    void keyTyped(KeyEvent e)  {
                        switch (e.keyChar) {
                            case '=': case '(': case ')': case '{': case '}': case '[': case ']':
                                e.consume()
                                break
                            default:
                                if (Character.isDigit(e.keyChar) && (swing.searchInConstantPoolsEnterTextField.text.length() == 0)) {
                                    // First character can not be a digit
                                    e.consume()
                                }
                                break
                        }
                    }
                    void keyPressed(KeyEvent e) {
                        if (e.keyCode == KeyEvent.VK_DOWN) {
                            swing.edt {
                                def root = searchInConstantPoolsTree.model.root
                                if (root.childCount > 0) {
                                    searchInConstantPoolsTree.requestFocus()
                                    if (searchInConstantPoolsTree.selectionCount == 0) {
                                        searchInConstantPoolsTree.selectionPath = new TreePath(root.getChildAt(0).path)
                                    }
                                    e.consume()
                                }
                            }
                        }
                    }
                })
                searchInConstantPoolsEnterTextField.addFocusListener(new FocusListener() {
                    void focusGained(FocusEvent e) {
                        swing.edt {
                            searchInConstantPoolsTree.clearSelection()
                            searchInConstantPoolsOpenAction.enabled = false
                        }
                    }
                    void focusLost(FocusEvent e) {}
                })
                searchInConstantPoolsEnterTextField.document.addDocumentListener(new DocumentListener() {
                    void insertUpdate(DocumentEvent e) { call() }
                    void removeUpdate(DocumentEvent e) { call() }
                    void changedUpdate(DocumentEvent e) { call() }
                    void call() { onPatternChangedClosure(swing.searchInConstantPoolsEnterTextField.text, flags) }
                })

                def checkBoxListener = new ItemListener() {
                    void itemStateChanged(ItemEvent e) {
                        onPatternChangedClosure(swing.searchInConstantPoolsEnterTextField.text, flags)
                        swing.searchInConstantPoolsEnterTextField.requestFocus()
                    }
                }

                searchInConstantPoolsCheckBoxType.addItemListener(checkBoxListener)
                searchInConstantPoolsCheckBoxField.addItemListener(checkBoxListener)
                searchInConstantPoolsCheckBoxConstructor.addItemListener(checkBoxListener)
                searchInConstantPoolsCheckBoxMethod.addItemListener(checkBoxListener)
                searchInConstantPoolsCheckBoxString.addItemListener(checkBoxListener)
                searchInConstantPoolsCheckBoxDeclarations.addItemListener(checkBoxListener)
                searchInConstantPoolsCheckBoxReferences.addItemListener(checkBoxListener)

                searchInConstantPoolsTree.showsRootHandles = true
                searchInConstantPoolsTree.addKeyListener(new KeyAdapter() {
                    void keyPressed(KeyEvent e) {
                        if (e.keyCode == KeyEvent.VK_UP) {
                            swing.edt {
                                if (searchInConstantPoolsTree.leadSelectionRow  == 0) {
                                    searchInConstantPoolsEnterTextField.requestFocus()
                                    e.consume()
                                }
                            }
                        }
                    }
                })
                searchInConstantPoolsTree.model = new DefaultTreeModel(new DefaultMutableTreeNode())
                searchInConstantPoolsTree.cellRenderer = new TreeNodeRenderer()
                searchInConstantPoolsTree.addMouseListener(new MouseAdapter() {
                    void mouseClicked(MouseEvent e) {
                        if (e.clickCount == 2) {
                            def node = e.source.lastSelectedPathComponent
                            if (node) {
                                onTypeSelectedClosure(node.uri, swing.searchInConstantPoolsEnterTextField.text, flags)
                            }
                        }
                    }
                })
                searchInConstantPoolsTree.addFocusListener(new FocusListener() {
                    void focusGained(FocusEvent e) {
                        swing.edt {
                            searchInConstantPoolsOpenAction.enabled = searchInConstantPoolsTree.selectionCount > 0
                        }
                    }
                    void focusLost(FocusEvent e) {}
                })
                searchInConstantPoolsTree.addTreeExpansionListener(new TreeExpansionListener() {
                    void treeExpanded(TreeExpansionEvent e) {
                        populate(e.source.model, e.path.lastPathComponent)
                    }
                    void treeCollapsed(TreeExpansionEvent e) {}
                })
                searchInConstantPoolsOpenAction.closure = { onTypeSelected(searchInConstantPoolsTree) }
                pack()
            }
        }
    }

    void populate(TreeModel model, DefaultMutableTreeNode node) {
        // Populate node
        populate(node)
        // Populate children
        int i = node.childCount
        while (i-- > 0) {
            def child = node.getChildAt(i)
            if ((child instanceof TreeNodeExpandable) && !expanded.contains(child.uri)) {
                populate(child)
            }
        }
        // Refresh
        model.reload(node)
    }

    void populate(DefaultMutableTreeNode node) {
        if ((node instanceof TreeNodeExpandable) && !expanded.contains(node.uri)) {
            // Populate
            node.populateTreeNode(api)
            expanded.add(node.uri)
            // Filter
            int i = node.childCount
            while (i-- > 0) {
                if (!accepted.contains(node.getChildAt(i).uri)) {
                    node.remove(i)
                }
            }
        }
    }

    void show() {
        swing.doLater {
            swing.searchInConstantPoolsEnterTextField.selectAll()
            // Show
            searchInConstantPoolsDialog.locationRelativeTo = searchInConstantPoolsDialog.parent
            searchInConstantPoolsDialog.visible = true
            searchInConstantPoolsEnterTextField.requestFocus()
        }
    }

    boolean isVisible() { swing.searchInConstantPoolsDialog.visible }

    String getPattern() { swing.searchInConstantPoolsEnterTextField.text }

    int getFlags() {
        int flags = 0

        if (swing.searchInConstantPoolsCheckBoxType.selected)
            flags += SEARCH_TYPE_TYPE
        if (swing.searchInConstantPoolsCheckBoxConstructor.selected)
            flags += SEARCH_TYPE_CONSTRUCTOR
        if (swing.searchInConstantPoolsCheckBoxMethod.selected)
            flags += SEARCH_TYPE_METHOD
        if (swing.searchInConstantPoolsCheckBoxField.selected)
            flags += SEARCH_TYPE_FIELD
        if (swing.searchInConstantPoolsCheckBoxString.selected)
            flags += SEARCH_TYPE_STRING
        if (swing.searchInConstantPoolsCheckBoxDeclarations.selected)
            flags += SEARCH_TYPE_DECLARATION
        if (swing.searchInConstantPoolsCheckBoxReferences.selected)
            flags += SEARCH_TYPE_REFERENCE

        return flags
    }

    void updateTree(Collection<FilteredContainerWrapper> containers, int matchingTypeCount) {
        swing.doLater {
            def model = searchInConstantPoolsTree.model
            def root = model.root

            // Reset tree nodes
            root.removeAllChildren()

            accepted.clear()
            expanded.clear()

            if (containers) {
                containers.sort { c1, c2 ->
                    c1.root.uri.compareTo(c2.root.uri)
                }.each { container ->
                    // Init uri set
                    accepted.addAll(container.uris)
                    // Populate tree
                    def parentEntry = container.root.parent
                    root.add(api.getTreeNodeFactory(parentEntry)?.make(api, parentEntry))
                }

                // Expand node and find the first leaf
                def node = root
                while (true) {
                    populate(model, node)
                    if (node.childCount == 0) {
                        break
                    }
                    node = node.getChildAt(0)
                }
                searchInConstantPoolsTree.selectionPath = new TreePath(node.path)
            } else {
                model.reload()
            }

            // Update matching item counter
            switch (matchingTypeCount) {
                case 0:
                    searchInConstantPoolsLabel.text = 'Matching types:'
                    break
                case 1:
                    searchInConstantPoolsLabel.text = '1 matching type:'
                    break
                default:
                    searchInConstantPoolsLabel.text = matchingTypeCount + ' matching types:'
            }
        }
    }

    void onTypeSelected(JTree searchInConstantPoolsTree) {
        def selectedTreeNode = searchInConstantPoolsTree.lastSelectedPathComponent
        if (selectedTreeNode) {
            onTypeSelectedClosure(selectedTreeNode.uri, swing.searchInConstantPoolsEnterTextField.text, flags)
        }
    }
}
