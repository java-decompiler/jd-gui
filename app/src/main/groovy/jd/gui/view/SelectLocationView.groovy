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

import javax.swing.AbstractAction
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.KeyStroke
import javax.swing.UIManager
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Point
import java.awt.event.ActionEvent
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent

class SelectLocationView {
    SwingBuilder swing
    API api

    TreeListener treeListener
    DialogClosedListener dialogClosedListener

    JDialog selectLocationDialog
    JLabel selectLocationLabel
    Tree selectLocationTree

    SelectLocationView(SwingBuilder swing, Configuration configuration, API api) {
        this.swing = swing
        this.api = api
        // Load GUI description
        this.treeListener = new TreeListener()
        this.dialogClosedListener = new DialogClosedListener()

        Color bg = UIManager.getColor('ToolTip.background')

        selectLocationLabel = new JLabel()
        selectLocationLabel.border = new EmptyBorder(5, 5, 0, 5)

        selectLocationTree = new Tree()
        selectLocationTree.with {
            border = new EmptyBorder(5, 5, 5, 5)
            opaque = false
            model = new DefaultTreeModel(new DefaultMutableTreeNode())
            cellRenderer = new TreeNodeRenderer()
            addKeyListener(treeListener)
            addMouseListener(treeListener)
        }

        def selectLocationPanel = new JPanel(new BorderLayout())
        selectLocationPanel.with {
            border = new LineBorder(bg.darker())
            background = bg
            add(selectLocationLabel, BorderLayout.NORTH)
            add(selectLocationTree, BorderLayout.CENTER)
        }

        selectLocationDialog = new JDialog(swing.mainFrame, '', false)
        selectLocationDialog.with {
            undecorated = true
            add(selectLocationPanel)
            rootPane.with {
                getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), 'SelectLocationView.closeClosure')
                actionMap.put('SelectLocationView.closeClosure', new AbstractAction() {
                    void actionPerformed(ActionEvent e) { selectLocationDialog.visible = false }
                })
            }
            addWindowListener(dialogClosedListener)
        }

        selectLocationTree.addFocusListener(new FocusAdapter() {
            void focusLost(FocusEvent e) { selectLocationDialog.visible = false }
        })
    }

    void show(Point location, Collection<FilteredContainerWrapper> containers, int locationCount, Closure entrySelectedClosure, Closure closeClosure) {
        swing.doLater {
            treeListener.entrySelectedClosure = entrySelectedClosure
            dialogClosedListener.closeClosure = closeClosure

            selectLocationTree.with {
                def root = model.root

                // Reset tree nodes
                root.removeAllChildren()

                containers.sort { c1, c2 ->
                    c1.root.uri.compareTo(c2.root.uri)
                }.each { container ->
                    def parentEntry = container.root.parent
                    def node = api.getTreeNodeFactory(parentEntry)?.make(api, parentEntry)

                    if (node) {
                        root.add(node)
                        populate(container.uris, node)
                    }
                }

                model.reload()

                // Expand all nodes
                for (int row = 0; row < getRowCount(); row++) {
                    expandRow(row)
                }

                // Select first leaf
                def node = root
                while (true) {
                    if (node.childCount == 0) {
                        break
                    }
                    node = node.getChildAt(0)
                }
                selectionPath = new TreePath(node.path)

                // Reset preferred size
                preferredSize = null

                // Resize
                Dimension ps = preferredSize
                if (ps.width < 200)
                    ps.width = 200
                if (ps.height < 50)
                    ps.height = 50
                preferredSize = ps
            }

            // Update label
            selectLocationLabel.text = '' + locationCount + ' locations:'

            selectLocationDialog.with {
                pack()
                setLocation(location)
                visible = true
            }

            selectLocationTree.requestFocus()
        }
    }

    void populate(Set<URI> uris, DefaultMutableTreeNode node) {
        if (node instanceof TreeNodeExpandable) {
            node.populateTreeNode(api)

            int i = node.childCount

            while (i-- > 0) {
                def child = node.getChildAt(i)

                if (uris.contains(child.uri)) {
                    populate(uris, child)
                } else {
                    node.remove(i)
                }
            }
        }
    }

    class TreeListener implements KeyListener, MouseListener {
        Closure entrySelectedClosure

        void callClosure(JTree tree) {
            def node = tree.lastSelectedPathComponent
            if (node) {
                selectLocationDialog.visible = false
                entrySelectedClosure(node.uri)
            }
        }

        // --- KeyListener --- //
        void keyPressed(KeyEvent e) {
            if (e.keyCode == KeyEvent.VK_ENTER) {
                callClosure(e.source)
            }
        }

        void keyTyped(KeyEvent e) {}
        void keyReleased(KeyEvent e) {}

        // --- MouseListener --- //
        void mouseClicked(MouseEvent e) {
            if (e.clickCount == 2) {
                callClosure(e.source)
            }
        }

        void mousePressed(MouseEvent e) {}
        void mouseReleased(MouseEvent e) {}
        void mouseEntered(MouseEvent e) {}
        void mouseExited(MouseEvent e) {}
    }

    class DialogClosedListener extends WindowAdapter {
        Closure closeClosure

        void windowDeactivated(WindowEvent e) { closeClosure() }
    }
}
