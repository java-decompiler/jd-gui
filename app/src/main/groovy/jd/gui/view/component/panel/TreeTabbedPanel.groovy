/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.view.component.panel

import jd.gui.api.API
import jd.gui.api.feature.PageChangeListener
import jd.gui.api.feature.PageChangeable
import jd.gui.api.feature.PageClosable
import jd.gui.api.feature.TreeNodeExpandable
import jd.gui.api.feature.UriGettable
import jd.gui.api.model.TreeNodeData
import jd.gui.api.feature.PageCreator
import jd.gui.api.feature.UriOpenable
import jd.gui.view.component.Tree
import jd.gui.view.renderer.TreeNodeRenderer

import javax.swing.*
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.event.TreeExpansionEvent
import javax.swing.event.TreeExpansionListener
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.DefaultMutableTreeNode
import java.awt.*
import java.util.List

class TreeTabbedPanel extends JPanel implements UriGettable, UriOpenable, PageChangeable, PageClosable {
    API api
    URI uri
    Tree tree
    TabbedPanel tabbedPanel
    List<PageChangeListener> pageChangedListeners = []
    boolean updateTreeMenu = true

    TreeTabbedPanel(API api, URI uri) {
        this.api = api
        this.uri = uri

        tree = new Tree()
        tree.showsRootHandles = true
        tree.setMinimumSize([150, 10] as Dimension)
        tree.expandsSelectedPaths = true
        tree.cellRenderer = new TreeNodeRenderer()
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            void valueChanged(TreeSelectionEvent e) { showPage(tree.lastSelectedPathComponent) }
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

        tabbedPanel = new TabbedPanel()
        tabbedPanel.setMinimumSize([150, 10] as Dimension)
        tabbedPanel.tabbedPane.addChangeListener(new ChangeListener() {
            void stateChanged(ChangeEvent e) { pageChanged() }
        })

		layout = new BorderLayout()

		JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tree), tabbedPanel)
        splitter.resizeWeight = 0.2

        add(splitter, BorderLayout.CENTER)
    }

    protected <T extends DefaultMutableTreeNode & UriGettable> void showPage(T node) {
        if (node) {
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
        }
    }

    protected boolean showPage(URI uri, URI baseUri, DefaultMutableTreeNode baseNode) {
        try {
            // Disable tabbedPane.changeListener
            updateTreeMenu = false

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

            if (page instanceof UriOpenable) {
                api.addURI(uri)
                page.openUri(uri)
            }

            return (page != null)
        } finally {
            // Enable tabbedPane.changeListener
            updateTreeMenu = true
        }
    }

    void pageChanged() {
        def page = tabbedPanel.tabbedPane.selectedComponent

        if (updateTreeMenu) {
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
                // Select tree node
                tree.selectionPath = node.path
                tree.scrollPathToVisible(tree.selectionPath)
            }
            return true
        }
        return false
    }

    protected DefaultMutableTreeNode searchTreeNode(URI uri, DefaultMutableTreeNode node) {
        if (node instanceof TreeNodeExpandable) {
            node.populateTreeNode(api)
        }

        def u = uri.toString()
        def child = node.children().find { u.startsWith(it.uri.toString()) }

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
}
