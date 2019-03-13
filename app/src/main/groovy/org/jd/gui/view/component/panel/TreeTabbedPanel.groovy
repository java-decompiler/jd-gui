/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view.component.panel

import org.jd.gui.api.API
import org.jd.gui.api.feature.PageChangeListener
import org.jd.gui.api.feature.PageChangeable
import org.jd.gui.api.feature.PageClosable
import org.jd.gui.api.feature.PreferencesChangeListener
import org.jd.gui.api.feature.TreeNodeExpandable
import org.jd.gui.api.feature.UriGettable
import org.jd.gui.api.model.TreeNodeData
import org.jd.gui.api.feature.PageCreator
import org.jd.gui.api.feature.UriOpenable
import org.jd.gui.view.component.Tree
import org.jd.gui.view.renderer.TreeNodeRenderer

import javax.swing.*
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.event.TreeExpansionEvent
import javax.swing.event.TreeExpansionListener
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.DefaultMutableTreeNode
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.List

class TreeTabbedPanel extends JPanel implements UriGettable, UriOpenable, PageChangeable, PageClosable, PreferencesChangeListener {
    API api
    URI uri
    Tree tree
    TabbedPanel tabbedPanel
    List<PageChangeListener> pageChangedListeners = []
    // Flags to prevent the event cascades
    boolean updateTreeMenuEnabled = true
    boolean openUriEnabled = true
    boolean treeNodeChangedEnabled = true

    TreeTabbedPanel(API api, URI uri) {
        this.api = api
        this.uri = uri

        tree = new Tree()
        tree.showsRootHandles = true
        tree.setMinimumSize([150, 10] as Dimension)
        tree.expandsSelectedPaths = true
        tree.cellRenderer = new TreeNodeRenderer() {
            Component getTreeCellRendererComponent(
                    JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                // Always render the left tree with focus
                return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, true)
            }
        }
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            void valueChanged(TreeSelectionEvent e) { treeNodeChanged(tree.lastSelectedPathComponent) }
        })
        tree.addTreeExpansionListener(new TreeExpansionListener() {
            void treeExpanded(TreeExpansionEvent e) {
                def node = e.path.lastPathComponent
                if (node instanceof TreeNodeExpandable) {
                    def oldList = node.children().toList()
                    node.populateTreeNode(api);
                    def newList = node.children().toList()
                    if (! oldList.equals(newList)) {
                        tree.model.reload(node)
                    }
                }
            }
            void treeCollapsed(TreeExpansionEvent e) {}
        })
        tree.addMouseListener(new MouseAdapter() {
            void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    def path = tree.getPathForLocation(e.x, e.y)

                    if (path) {
                        tree.selectionPath = path

                        def node = path.lastPathComponent
                        def actions = api.getContextualActions(node.entry, node.uri.fragment)

                        if (actions) {
                            def popup = new JPopupMenu()
                            for (def action : actions) {
                                if (action) {
                                    popup.add(action)
                                } else {
                                    popup.addSeparator()
                                }
                            }
                            popup.show(e.component, e.x, e.y)
                        }
                    }
                }
            }
        })

        tabbedPanel = new TabbedPanel()
        tabbedPanel.api = api
        tabbedPanel.setMinimumSize([150, 10] as Dimension)
        tabbedPanel.tabbedPane.addChangeListener(new ChangeListener() {
            void stateChanged(ChangeEvent e) { pageChanged() }
        })

		layout = new BorderLayout()

		JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tree), tabbedPanel)
        splitter.resizeWeight = 0.2

        add(splitter, BorderLayout.CENTER)
    }

    protected <T extends DefaultMutableTreeNode & UriGettable> void treeNodeChanged(T node) {
        if (treeNodeChangedEnabled && node) {
            try {
                // Disable tabbedPane.changeListener
                updateTreeMenuEnabled = false

                // Search base tree node
                def uri = node.uri

                if ((uri.fragment == null) && (uri.query == null)) {
                    showPage(uri, uri, node)
                } else {
                    def baseUri = new URI(uri.scheme, uri.host, uri.path, null)
                    def baseNode = node

                    while (!baseNode?.uri.equals(baseUri)) {
                        baseNode = baseNode.parent
                    }
                    if (baseNode?.uri.equals(baseUri)) {
                        showPage(uri, baseUri, baseNode)
                    }
                }
            } finally {
                // Enable tabbedPane.changeListener
                updateTreeMenuEnabled = true
            }
        }
    }

    protected boolean showPage(URI uri, URI baseUri, DefaultMutableTreeNode baseNode) {
        def page = tabbedPanel.showPage(baseUri)

        if ((page == null) && (baseNode instanceof PageCreator)) {
            page = baseNode.createPage(api)
            page.putClientProperty('node', baseNode)
            page.putClientProperty('preferences-stamp', Integer.valueOf(api.preferences.hashCode()))
            page.putClientProperty('collectionOfIndexes-stamp', Integer.valueOf(api.collectionOfIndexes.hashCode()))

            def path = baseUri.path
            def label = path.substring(path.lastIndexOf('/')+1)
            def data = baseNode.userObject

            if (data instanceof TreeNodeData) {
                tabbedPanel.addPage(label, data.icon, data.tip, page)
            } else {
                tabbedPanel.addPage(label, null, null, page)
            }
        }

        if (openUriEnabled && page instanceof UriOpenable) {
            page.openUri(uri)
        }

        return (page != null)
    }

    void pageChanged() {
        try {
            // Disable highlight
            openUriEnabled = false

            def page = tabbedPanel.tabbedPane.selectedComponent

            if (updateTreeMenuEnabled) {
                // Synchronize tree
                if (page) {
                    def node = page.getClientProperty('node')
                    // Select tree node
                    tree.selectionPath = node.path
                    tree.scrollPathToVisible(tree.selectionPath)
                } else {
                    tree.clearSelection()
                }
            }
            // Fire page changed event
            for (def listener : pageChangedListeners) {
                listener.pageChanged(page)
            }
        } finally {
            // Enable highlight
            openUriEnabled = true
        }
    }

    // --- URIGetter --- //
    URI getUri() { uri }

    // --- URIOpener --- //
    boolean openUri(URI uri) {
        def baseUri = new URI(uri.scheme, uri.host, uri.path, null)
        def baseNode = searchTreeNode(baseUri, tree.model.root)

        if (baseNode && showPage(uri, baseUri, baseNode)) {
            def node = searchTreeNode(uri, baseNode)
            if (node) {
                try {
                    // Disable tree node changed listener
                    treeNodeChangedEnabled = false
                    // Select tree node
                    tree.selectionPath = node.path
                    tree.scrollPathToVisible(tree.selectionPath)
                } finally {
                    // Enable tree node changed listener
                    treeNodeChangedEnabled = true
                }
            }
            return true
        } else {
            return false
        }
    }

    protected DefaultMutableTreeNode searchTreeNode(URI uri, DefaultMutableTreeNode node) {
        if (node instanceof TreeNodeExpandable) {
            node.populateTreeNode(api)
        }

        def u = uri.toString()
        def child = node.children().find {
            def childU = it.uri.toString()

            if (u.length() > childU.length()) {
                if (u.startsWith(childU)) {
                    char c = u.charAt(childU.length())
                    return (c == '/') || (c == '!')
                } else {
                    return false
                }
            } else {
                return u.equals(childU)
            }
        }

        if (child) {
            if (u.equals(child.uri.toString())) {
                return child
            } else {
                // Parent tree node found -> Recursive call
                return searchTreeNode(uri, child)
            }
        } else {
            // Not found
            return null
        }
    }

    // --- PageChanger --- //
    void addPageChangeListener(PageChangeListener listener) {
        pageChangedListeners.add(listener)
    }

    // --- PageCloser --- //
    boolean closePage() {
        def component = tabbedPanel.tabbedPane.selectedComponent
        if (component) {
            tabbedPanel.removeComponent(component)
            return true
        } else {
            return false
        }
    }

    // --- PreferencesChangeListener --- //
    void preferencesChanged(Map<String, String> preferences) {
        tabbedPanel.preferencesChanged(preferences)
    }
}
