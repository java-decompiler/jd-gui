/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.view

import groovy.swing.SwingBuilder
import org.jd.gui.api.API
import org.jd.gui.api.model.Container
import org.jd.gui.model.configuration.Configuration
import org.jd.gui.view.bean.OpenTypeListCellBean
import org.jd.gui.view.renderer.OpenTypeListCellRenderer

import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import java.awt.Point
import java.awt.Rectangle
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

class OpenTypeView {
    static final MAX_LINE_COUNT = 80

    SwingBuilder swing
    API api

    OpenTypeView(SwingBuilder swing, Configuration configuration, API api, Closure onPatternChangedClosure, Closure onTypeSelectedClosure) {
        this.swing = swing
        this.api = api
        // Load GUI description
        swing.edt {
            // Load GUI description
            build(OpenTypeDescription)
            openTypeDialog.with {
                rootPane.with {
                    defaultButton = openTypeOpenButton
                    getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "OpenTypeView.cancel")
                    actionMap.put("OpenTypeView.cancel", openTypeCancelAction)
                }
                minimumSize = size
                openTypeEnterTextField.addKeyListener(new KeyAdapter() {
                    void keyTyped(KeyEvent e)  {
                        switch (e.keyChar) {
                            case '=': case '(': case ')': case '{': case '}': case '[': case ']':
                                e.consume()
                                break
                            default:
                                if (Character.isDigit(e.keyChar) && (swing.openTypeEnterTextField.text.length() == 0)) {
                                    // First character can not be a digit
                                    e.consume()
                                }
                                break
                        }
                    }
                    void keyPressed(KeyEvent e) {
                        if (e.keyCode == KeyEvent.VK_DOWN) {
                            swing.edt {
                                if (openTypeList.model.size > 0) {
                                    openTypeList.selectedIndex = 0
                                    openTypeList.requestFocus()
                                    e.consume()
                                }
                            }
                        }
                    }
                })
                openTypeEnterTextField.addFocusListener(new FocusListener() {
                    void focusGained(FocusEvent e) {
                        swing.edt {
                            openTypeList.clearSelection()
                        }
                    }
                    void focusLost(FocusEvent e) {}
                })
                openTypeEnterTextField.document.addDocumentListener(new DocumentListener() {
                    void insertUpdate(DocumentEvent e) { call(e) }
                    void removeUpdate(DocumentEvent e) { call(e) }
                    void changedUpdate(DocumentEvent e) { call(e) }
                    void call(DocumentEvent e) { onPatternChangedClosure(e.document.getText(0, e.document.length)) }
                })
                openTypeList.addKeyListener(new KeyAdapter() {
                    void keyPressed(KeyEvent e) {
                        if (e.keyCode == KeyEvent.VK_UP) {
                            swing.edt {
                                if (openTypeList.selectedIndex  == 0) {
                                    openTypeEnterTextField.requestFocus()
                                    e.consume()
                                }
                            }
                        }
                    }
                })
                openTypeList.model = new DefaultListModel<OpenTypeListCellBean>()
                openTypeList.cellRenderer = new OpenTypeListCellRenderer()
                openTypeList.addMouseListener(new MouseAdapter() {
                    void mouseClicked(MouseEvent e) {
                        if (e.clickCount == 2) {
                            onTypeSelected(onTypeSelectedClosure)
                        }
                    }
                })
                openTypeList.addListSelectionListener(new ListSelectionListener() {
                    void valueChanged(ListSelectionEvent e) {
                        swing.edt {
                            openTypeOpenAction.enabled = (openTypeList.selectedValue != null)
                        }
                    }
                })
                openTypeOpenAction.closure = { onTypeSelected(onTypeSelectedClosure) }
                pack()
            }
        }
    }

    void show() {
        swing.doLater {
            swing.openTypeEnterTextField.selectAll()
            // Show
            openTypeDialog.locationRelativeTo = openTypeDialog.parent
            openTypeDialog.visible = true
            openTypeEnterTextField.requestFocus()
        }
    }

    boolean isVisible() { swing.openTypeDialog.visible }

    String getPattern() { swing.openTypeEnterTextField.text }

    void updateList(Map<String, Collection<Container.Entry>> map) {
        swing.doLater {
            def model = openTypeList.model
            def typeNames = map.keySet().toList().sort()

            model.removeAllElements()

            typeNames.sort { name1, name2 ->
                int lasPackageSeparatorIndex = name1.lastIndexOf('/')
                def shortName1 = name1.substring(lasPackageSeparatorIndex+1)

                lasPackageSeparatorIndex = name2.lastIndexOf('/')
                def shortName2 = name2.substring(lasPackageSeparatorIndex+1)

                return shortName1.compareTo(shortName2)
            }.eachWithIndex { typeName, index ->
                if (index < MAX_LINE_COUNT) {
                    def entries = map.get(typeName)
                    def firstEntry = entries.iterator().next()
                    def type = api.getTypeFactory(firstEntry).make(api, firstEntry, typeName)

                    if (type) {
                        model.addElement(new OpenTypeListCellBean(label:type.displayTypeName, packag:type.displayPackageName, icon:type.icon, entries:entries, typeName:typeName))
                    } else {
                        model.addElement(new OpenTypeListCellBean(label:typeName, entries:entries, typeName:typeName))
                    }
                } else if (index == MAX_LINE_COUNT) {
                    model.addElement(null)
                }
            }

            int count = typeNames.size()
            switch (count) {
            case 0:
                openTypeMatchLabel.text = 'Matching types:'
                break
            case 1:
                openTypeMatchLabel.text = '1 matching type:'
                break
            default:
                openTypeMatchLabel.text = count + ' matching types:'
            }
        }
    }

    void focus() {
        swing.doLater {
            openTypeList.requestFocus()
        }
    }

    void onTypeSelected(Closure typeSelectedClosure) {
        swing.doLater {
            def index = openTypeList.selectedIndex
            if (index != -1) {
                def selectedCellBean = openTypeList.model.getElementAt(index)
                Point listLocation = openTypeList.locationOnScreen
                Rectangle cellBound = openTypeList.getCellBounds(index, index)
                Point leftBottom = new Point(listLocation.x + cellBound.x as int, listLocation.y + cellBound.y + cellBound.height as int)
                typeSelectedClosure(leftBottom, selectedCellBean.entries, selectedCellBean.typeName)
            }
        }
    }
}
