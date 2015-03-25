/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.view

import javax.swing.UIManager
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Desktop
import java.awt.Font
import java.awt.GridLayout
import java.awt.Toolkit

import javax.swing.WindowConstants

import jd.gui.Constants
import jd.gui.service.platform.PlatformService

actions {
	action(
		id:'openAction',
		name:'Open File...',
		mnemonic:'O',
		accelerator:shortcut('O'),
		shortDescription:'Open a file'
	)
    action(
        id:'closeAction',
        name:'Close',
        accelerator:shortcut('W'),
        enabled:false
    )
    action(
        id:'saveAction',
        name:'Save',
        accelerator:shortcut('S'),
        enabled:false
    )
    action(
        id:'saveAllSourcesAction',
        name:'Save All Sources',
        accelerator:shortcut('alt S'),
        enabled:false
    )
	action(
		id:'exitAction',
		name:'Exit',
		mnemonic:'x',
		accelerator:'alt X',
		shortDescription:'Quit this program',
		closure:{ mainFrame.dispose() }
	)
    action(
        id:'copyAction',
        name:'Copy',
        accelerator:shortcut('C'),
        enabled:false
    )
    action(
        id:'pasteAction',
        name:'Paste Log',
        accelerator:shortcut('V')
    )
    action(
        id:'selectAllAction',
        name:'Select all',
        accelerator:shortcut('A'),
        enabled:false
    )
    action(
        id:'findAction',
        name:'Find...',
        accelerator:shortcut('F'),
        enabled:false
    )
    action(
        id:'openTypeAction',
        name:'Open Type...',
        accelerator:shortcut('shift T'),
        enabled:false
    )
    action(
        id:'openTypeHierarchyAction',
        name:'Open Type Hierarchy...',
        accelerator:'F4',
        enabled:false
    )
    action(
        id:'goToAction',
        name:'Go to Line...',
        accelerator:shortcut('L'),
        enabled:false
    )
    action(
        id:'backwardAction',
        name:'Back',
        accelerator:'alt LEFT',
        enabled:false
    )
    action(
        id:'forwardAction',
        name:'Forward',
        accelerator:'alt RIGHT',
        enabled:false
    )
    action(
        id:'searchAction',
        name:'Search...',
        accelerator:shortcut('shift S'),
        enabled:false
    )
    action(
		id:'wikipediaAction',
		name:'Wikipedia',
		shortDescription:'Wikipedia',
        enabled:Desktop.isDesktopSupported() ? Desktop.desktop.isSupported(Desktop.Action.BROWSE) : false
	)
	action(
		id:'preferencesAction',
		name:'Preferences...',
		accelerator:shortcut('shift P'),
		shortDescription:'Preferences'
	)
	action(
		id:'aboutAction',
		name:'About...',
		accelerator:'F1',
		shortDescription:'About JD-GUI',
        closure:{
            aboutDialog.pack()
            aboutDialog.setLocationRelativeTo(mainFrame)
            aboutDialog.visible = true
        }
	)
    action(
        id:'findNextAction',
        name:'Next'
    )
    action(
        id:'findPreviousAction',
        name:'Previous'
    )
    action(
        id:'findCaseSensitiveAction'
    )
    action(
        id:'findCloseAction',
        closure: { findPanel.visible = false }
    )
}

frame(
        id:'mainFrame',
        title:'Java Decompiler',
        iconImage:Toolkit.defaultToolkit.getImage(getClass().classLoader.getResource('images/Icon_java_128.png')),
        minimumSize:[Constants.MINIMAL_WIDTH, Constants.MINIMAL_HEIGHT],
        defaultCloseOperation:WindowConstants.EXIT_ON_CLOSE) {
    menuBar {
        menu('File') {
            menuItem(openAction)
            separator()
            menuItem(closeAction)
            separator()
            menuItem(saveAction)
            menuItem(saveAllSourcesAction)
            separator()
            menu(id:'recentFiles', text:'Recent Files')
            if (!PlatformService.instance.isMac) {
                separator()
                menuItem(exitAction)
            }
        }
        menu('Edit') {
            menuItem(copyAction)
            menuItem(pasteAction)
            separator()
            menuItem(selectAllAction)
            separator()
            menuItem(findAction) {
            }
        }
        menu('Navigation') {
            menuItem(openTypeAction)
            menuItem(openTypeHierarchyAction)
            separator()
            menuItem() {
                action(
                    name:'Quick Outline...',
                    accelerator:shortcut('shift O'),
                    enabled:false,
                    closure:{ println("clicked on the Save menu item!") }
                )
            }
            separator()
            menuItem(goToAction)
            separator()
            menuItem(backwardAction)
            menuItem(forwardAction)
        }
        menu('Search') {
            menuItem(searchAction)
        }
        menu('Help') {
            menuItem(wikipediaAction)
            separator()
            menuItem(preferencesAction)
            if (!PlatformService.instance.isMac) {
                separator()
                menuItem(aboutAction)
            }
        }
    }
    panel {
        borderLayout()
        toolBar(constraints:PAGE_START, floatable:false, rollover:true) {
            iconButton(action:openAction, text:null, icon:imageIcon(resource:'/images/open.png'))
            separator()
            iconButton(action:openTypeAction, text:null, icon:imageIcon(resource:'/images/open_type.png'))
            iconButton(action:searchAction, text:null, icon:imageIcon(resource:'/images/search_src.png'))
            separator()
            iconButton(action:backwardAction, text:null, icon:imageIcon(resource:'/images/backward_nav.png'))
            iconButton(action:forwardAction, text:null, icon:imageIcon(resource:'/images/forward_nav.png'))
        }
        mainTabbedPanel(id:'mainTabbedPanel', constraints:CENTER)
        hbox(id:'findPanel', constraints:PAGE_END, border:emptyBorder(2), visible:false) {
            label(text:'Find: ')
            comboBox(id:'findComboBox', editable: true)
            hstrut(5)
            toolBar(floatable:false, rollover:true) {
                iconButton(action:findNextAction, icon:imageIcon(resource: '/images/next_nav.png'))
                hstrut(5)
                iconButton(action:findPreviousAction, icon:imageIcon(resource: '/images/prev_nav.png'))
            }
            checkBox(id:'findCaseSensitive', action:findCaseSensitiveAction, text:'Case sensitive')
            hglue()

            def closeIcon = imageIcon(resource:'/images/close.gif')
            def closeActivateIcon = imageIcon(resource:'/images/close_active.gif')

            iconButton(action:findCloseAction, contentAreaFilled:false, icon:closeIcon, rolloverIcon:closeActivateIcon)
        }
    }
}

dialog(
        id:'aboutDialog',
        owner:mainFrame,
        title:'About Java Decompiler',
        modal:false,
        resizable:false) {
    panel(border:emptyBorder(15)) {
        borderLayout()
        vbox(constraints:BorderLayout.NORTH) {
            panel(
                    border:lineBorder(color:Color.BLACK),
                    background:Color.WHITE) {
                borderLayout()
                label(icon:imageIcon(resource:'/images/Icon_java_64.png'), border:emptyBorder(15), constraints:BorderLayout.WEST)
                vbox(border:emptyBorder([15,0,15,15]), constraints:BorderLayout.EAST) {
                    hbox {
                        label(text: 'Java Decompiler', font:UIManager.getFont('Label.font').deriveFont(Font.BOLD, 14))
                        hglue()
                    }
                    hbox {
                        panel(layout:new GridLayout(2,2), opaque:false, border:emptyBorder([5,10,5,50])) {
                            label(text: 'JD-GUI')
                            label(text: 'version 1.0.0')
                            label(text: 'JD-Core')
                            label(text: 'version 0.7.1')
                        }
                        hglue()
                    }
                    hbox {
                        label(text: 'Copyright © 2008-2015 Emmanuel Dupuy')
                        hglue()
                    }
                }
            }
            vstrut(10)
        }
        hbox(constraints:BorderLayout.SOUTH) {
            hglue()
            button {
                action(id:'aboutOkAction', name:'    Ok    ', closure:{ aboutDialog.visible = false })
            }
            hglue()
        }
    }
}
