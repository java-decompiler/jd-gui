/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.view

import groovy.swing.SwingBuilder
import org.jd.gui.api.API
import org.jd.gui.api.model.Container
import org.jd.gui.api.model.TreeNodeData
import org.jd.gui.api.model.Type
import org.jd.gui.model.configuration.Configuration
import org.jd.gui.view.component.Tree
import org.jd.gui.view.renderer.TreeNodeRenderer

import javax.swing.*
import javax.swing.event.TreeExpansionEvent
import javax.swing.event.TreeExpansionListener
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath
import java.awt.Point
import java.awt.Rectangle
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

class OpenTypeHierarchyView {
    static final ImageIcon ROOT_CLASS_ICON = new ImageIcon(OpenTypeHierarchyView.class.classLoader.getResource('org/jd/gui/images/generate_class.png'))
    static final ImageIcon ROOT_INTERFACE_ICON = new ImageIcon(OpenTypeHierarchyView.class.classLoader.getResource('org/jd/gui/images/generate_int.png'))

    SwingBuilder swing
    API api
    Closure onTypeSelectedClosure
    Closure getSubTypeNamesClosure
    Closure getEntriesClosure

    OpenTypeHierarchyView(
            SwingBuilder swing, Configuration configuration, API api,
            Closure onTypeSelectedClosure, Closure getSubTypeNamesClosure, Closure getEntriesClosure) {
        this.swing = swing
        this.api = api
        this.onTypeSelectedClosure = onTypeSelectedClosure
        this.getSubTypeNamesClosure = getSubTypeNamesClosure
        this.getEntriesClosure = getEntriesClosure
        // Load GUI description
        swing.edt {
            // Setup
            registerBeanFactory('tree', Tree.class)
            // Load GUI description
            build(OpenTypeHierarchyDescription)
            openTypeHierarchyDialog.with {
                rootPane.with {
                    defaultButton = openTypeHierarchyOpenButton
                    getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "OpenTypeHierarchyView.cancel")
                    actionMap.put("OpenTypeHierarchyView.cancel", openTypeHierarchyCancelAction)
                }
                minimumSize = size
                openTypeHierarchyTree.model = new DefaultTreeModel(new DefaultMutableTreeNode())
                openTypeHierarchyTree.cellRenderer = new TreeNodeRenderer()
                openTypeHierarchyTree.addMouseListener(new MouseAdapter() {
                    void mouseClicked(MouseEvent e) {
                        if (e.clickCount == 2) {
                            onTypeSelected(e.source)
                        }
                    }
                })
                openTypeHierarchyTree.addTreeExpansionListener(new TreeExpansionListener() {
                    void treeExpanded(TreeExpansionEvent e) {
                        def node = e.path.lastPathComponent
                        def tree = e.source
                        // Expand node and find the first leaf
                        while (node.childCount > 0) {
                            if (node.getChildAt(0).userObject == null) {
                                // Remove dummy node and create children
                                populateTreeNode(node, null)
                            }
                            if (node.childCount != 1) {
                                break
                            }
                            node = node.getChildAt(0)
                        }
                        tree.model.reload(e.path.lastPathComponent)
                        tree.selectionPath = new TreePath(node.path)
                    }
                    void treeCollapsed(TreeExpansionEvent e) {}
                })
                openTypeHierarchyTree.addKeyListener(new KeyAdapter() {
                    void keyPressed(KeyEvent e) {
                        if (e.keyCode == KeyEvent.VK_F4) {
                            def node = swing.openTypeHierarchyTree.lastSelectedPathComponent
                            if (node) {
                                updateTree(node.entry, node.typeName)
                            }
                        }
                    }
                })
                openTypeHierarchyTree.addTreeSelectionListener(new TreeSelectionListener() {
                    void valueChanged(TreeSelectionEvent e) {
                        swing.openTypeHierarchyOpenAction.enabled = (swing.openTypeHierarchyTree.lastSelectedPathComponent?.entry != null)
                    }
                })
                openTypeHierarchyOpenAction.closure = { onTypeSelected(openTypeHierarchyTree) }
                pack()
            }
        }
    }

    void show() {
        swing.doLater {
            openTypeHierarchyDialog.locationRelativeTo = openTypeHierarchyDialog.parent
            openTypeHierarchyDialog.visible = true
            openTypeHierarchyTree.requestFocus()
        }
    }

    boolean isVisible() { swing.openTypeHierarchyDialog.visible }

    void updateTree(Container.Entry entry, String typeName) {
        swing.doLater {
            // Clear tree
            JTree tree = swing.openTypeHierarchyTree
            def model = tree.model
            def root = model.root
            root.removeAllChildren()

            def selectedTreeNode = createTreeNode(entry, typeName)
            def parentTreeNode = createParentTreeNode(selectedTreeNode)

            root.add(parentTreeNode)
            model.reload()

            if (selectedTreeNode) {
                def path = new TreePath(selectedTreeNode.path)
                // Expand
                tree.expandPath(path)
                // Scroll to show tree node
                tree.makeVisible(path)
                Rectangle bounds = tree.getPathBounds(path)

                if(bounds) {
                    bounds.x = 0

                    Rectangle lastRowBounds = tree.getRowBounds(tree.getRowCount()-1)

                    if (lastRowBounds) {
                        bounds.y = Math.max(bounds.y-30, 0)
                        bounds.height = Math.min(bounds.height+bounds.y+60, lastRowBounds.height+lastRowBounds.y) - bounds.y
                    }

                    tree.scrollRectToVisible(bounds)
                    if (tree.accessibleContext) {
                        tree.accessibleContext.fireVisibleDataPropertyChange()
                    }
                }
                // Select tree node
                tree.selectionPath = path
            }
        }
    }

    TreeNode createTreeNode(Container.Entry entry, String typeName) {
        def type = api.getTypeFactory(entry).make(api, entry, typeName)

        typeName = type.name

        def entries = getEntriesClosure(typeName)
        def treeNode = new TreeNode(entry, typeName, entries, new TreeNodeBean(type))
        def childTypeNames = getSubTypeNamesClosure(typeName)

        if (childTypeNames) {
            // Add dummy node
            treeNode.add(new DefaultMutableTreeNode())
        }

        return treeNode
    }

    /**
     * Create parent and sibling tree nodes
     */
    TreeNode createParentTreeNode(TreeNode treeNode) {
        def type = api.getTypeFactory(treeNode.entry).make(api, treeNode.entry, treeNode.typeName)
        def superTypeName = type.superName

        if (superTypeName) {
            def superEntries = getEntriesClosure(superTypeName)

            // Search entry in the sane container of 'entry'
            def superEntry

            if (superEntries) {
                superEntry = superEntries.find { it.container == treeNode.entry.container }

                if (superEntry == null) {
                    // Not found -> Choose 1st one
                    superEntry = superEntries.get(0)
                }
            } else {
                superEntry = null
            }

            if (superEntry) {
                // Create parent tree node
                def superTreeNode = createTreeNode(superEntry, superTypeName)
                // Populate parent tree node
                populateTreeNode(superTreeNode, treeNode)
                // Recursive call
                return createParentTreeNode(superTreeNode)
            } else {
                // Entry not found --> Most probable hypothesis : Java type entry
                int lastPackageSeparatorIndex = superTypeName.lastIndexOf('/')
                def package_ = superTypeName.substring(0, lastPackageSeparatorIndex).replace('/', '.')
                def name = superTypeName.substring(lastPackageSeparatorIndex + 1).replace('$', '.')
                def label = package_ ? name + ' - ' + package_ : name
                def icon = ((type.flags & Type.FLAG_INTERFACE) == 0) ? ROOT_CLASS_ICON : ROOT_INTERFACE_ICON
                def rootTreeNode = new TreeNode(null, superTypeName, null, new TreeNodeBean(label, icon))

                if (package_.startsWith('java.')) {
                    // If root type is a JDK type, do not create a tree node for each child types
                    rootTreeNode.add(treeNode)
                } else {
                    populateTreeNode(rootTreeNode, treeNode)
                }

                return rootTreeNode
            }
        } else {
            // super type undefined
            return treeNode
        }
    }

    /**
     * @param superTreeNode  node to populate
     * @param activeTreeNode active child node
     */
    void populateTreeNode(TreeNode superTreeNode, TreeNode activeTreeNode) {
        superTreeNode.removeAllChildren()

        // Search preferred container: if 'superTreeNode' is a root with an unknown super entry, uses the container of active child node
        def notNullEntry = superTreeNode.entry ?: activeTreeNode.entry
        def preferredContainer = notNullEntry.container

        def activeTypName = activeTreeNode?.typeName
        def subTypeNames = getSubTypeNamesClosure(superTreeNode.typeName)

        subTypeNames.collect { tn ->
            if (tn.equals(activeTypName)) {
                return activeTreeNode
            } else {
                // Search entry in the sane container of 'superTreeNode.entry'
                def entries = getEntriesClosure(tn)
                def e = entries.find { it.container == preferredContainer }
                if (e == null) {
                    // Not found -> Choose 1st one
                    e = entries.get(0)
                }
                if (e == null) {
                    return null
                }
                // Create type
                def t = api.getTypeFactory(e).make(api, e, tn)
                if (t == null) {
                    return null
                }
                // Create tree node
                return createTreeNode(e, t.name)
            }
        }.grep { tn ->
            tn != null
        }.sort { tn1, tn2 ->
            tn1.userObject.label.compareTo(tn2.userObject.label)
        }.each { tn ->
            superTreeNode.add(tn)
        }
    }

    void focus() {
        swing.doLater {
            openTypeHierarchyTree.requestFocus()
        }
    }

    void onTypeSelected(JTree openTypeHierarchyTree) {
        def selectedTreeNode = openTypeHierarchyTree.lastSelectedPathComponent
        if (selectedTreeNode) {
            def path = new TreePath(selectedTreeNode.path)
            Rectangle bounds = openTypeHierarchyTree.getPathBounds(path)
            Point listLocation = openTypeHierarchyTree.locationOnScreen
            Point leftBottom = new Point(listLocation.x+bounds.x as int, listLocation.y+bounds.y+bounds.height as int)
            onTypeSelectedClosure(leftBottom, selectedTreeNode.entries, selectedTreeNode.typeName)
        }
    }

    static class TreeNode extends DefaultMutableTreeNode {
        Container.Entry entry
        String typeName
        List<Container.Entry> entries

        TreeNode(Container.Entry entry, String typeName, List<Container.Entry> entries, Object userObject) {
            super(userObject)
            this.entry = entry
            this.typeName = typeName
            this.entries = entries
        }
    }

    // Graphic data for renderer
    static class TreeNodeBean implements TreeNodeData {
        String label
        String tip
        Icon icon
        Icon openIcon

        TreeNodeBean(Type type) {
            this.label = type.displayPackageName ? type.displayTypeName + ' - ' + type.displayPackageName : type.displayTypeName
            this.icon = type.icon
        }

        TreeNodeBean(String label, Icon icon) {
            this.label = label
            this.icon = icon
        }
    }
}
