/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view

import javax.swing.ScrollPaneConstants
import java.awt.BorderLayout
import java.awt.Dimension

dialog(
        id:'searchInConstantPoolsDialog',
        owner:mainFrame,
        title:'Search',
        modal:false) {
    panel(border: emptyBorder(15)) {
        borderLayout()
        vbox(constraints: BorderLayout.NORTH) {
            hbox {
                label(text: 'Search string (* = any string, ? = any character):')
                hglue()
            }
            vstrut(10)
            hbox {
                textField(id: 'searchInConstantPoolsEnterTextField', columns: 30)
            }
            vstrut(10)
            hbox {
                panel(border:titledBorder(title:'Search For')) {
                    borderLayout()
                    hbox(constraints: BorderLayout.WEST) {
                        panel {
                            gridLayout(cols: 1, rows: 2)
                            checkBox(id: 'searchInConstantPoolsCheckBoxType', text: 'Type', selected:true)
                            checkBox(id: 'searchInConstantPoolsCheckBoxField', text: 'Field')
                        }
                        panel {
                            gridLayout(cols: 1, rows: 2)
                            checkBox(id: 'searchInConstantPoolsCheckBoxConstructor', text: 'Constructor')
                            checkBox(id: 'searchInConstantPoolsCheckBoxMethod', text: 'Method')
                        }
                        panel {
                            gridLayout(cols: 1, rows: 2)
                            checkBox(id: 'searchInConstantPoolsCheckBoxString', text: 'String Constant')
                        }
                    }
                }
                panel(border:titledBorder(title:'Limit To')) {
                    borderLayout()
                    hbox(constraints: BorderLayout.WEST) {
                        panel {
                            gridLayout(cols: 1, rows: 2)
                            checkBox(id: 'searchInConstantPoolsCheckBoxDeclarations', text: 'Declarations', selected:true)
                            checkBox(id: 'searchInConstantPoolsCheckBoxReferences', text: 'References', selected:true)
                        }
                    }
                }
            }
            vstrut(10)
            hbox {
                label(id:'searchInConstantPoolsLabel', text:'Matching types:')
                hglue()
            }
            vstrut(10)
        }
        scrollPane(
                constraints: BorderLayout.CENTER,
                horizontalScrollBarPolicy: ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER,
                preferredSize: new Dimension(400, 150)) {
            tree(id:'searchInConstantPoolsTree')
        }
        vbox(constraints: BorderLayout.SOUTH) {
            vstrut(25)
            hbox {
                hglue()
                button(id: 'searchInConstantPoolsOkButton') {
                    action(id: 'searchInConstantPoolsOpenAction', name: 'Open', enabled: false)
                }
                hstrut(5)
                button {
                    action(id: 'searchInConstantPoolsCancelAction', name: 'Cancel', closure:{ searchInConstantPoolsDialog.visible = false })
                }
            }
        }
    }
}