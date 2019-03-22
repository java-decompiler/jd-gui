/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view.component.panel

import org.jd.gui.api.API
import org.jd.gui.api.feature.PreferencesChangeListener
import org.jd.gui.api.feature.UriGettable
import org.jd.gui.service.platform.PlatformService

import javax.swing.AbstractAction
import javax.swing.JComponent
import javax.swing.JMenu
import javax.swing.JMenuItem
import javax.swing.JPopupMenu
import javax.swing.SwingUtilities
import javax.swing.ToolTipManager
import javax.swing.event.ChangeEvent
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Color
import java.awt.Component
import java.awt.Font
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

import javax.swing.BorderFactory
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTabbedPane
import java.awt.event.MouseEvent
import java.awt.event.MouseListener

class TabbedPanel extends JPanel implements PreferencesChangeListener {
	static final ImageIcon CLOSE_ICON = new ImageIcon(TabbedPanel.class.classLoader.getResource('org/jd/gui/images/close.gif'))
	static final ImageIcon  CLOSE_ACTIVE_ICON = new ImageIcon(TabbedPanel.class.classLoader.getResource('org/jd/gui/images/close_active.gif'))

    static final String TAB_LAYOUT = 'UITabsPreferencesProvider.singleLineTabs'

    API api
	JTabbedPane tabbedPane
    Map<String, String> preferences

    TabbedPanel() {
		create()
	}

	void create() {
		setLayout(new CardLayout())
		add('panel', createPanel(background))
		add('tabs', tabbedPane = createTabPanel())
	}

	JPanel createPanel(Color background) {
		return new JPanel()
	}

	JTabbedPane createTabPanel() {
        JTabbedPane tabPanel = new JTabbedPane() {
            String getToolTipText(MouseEvent e) {
                int index = indexAtLocation(e.x, e.y)
                if (index != -1) {
                    return getTabComponentAt(index).toolTipText
                }
                return super.getToolTipText(e)
            }
        }
        ToolTipManager.sharedInstance().registerComponent(tabPanel)
        tabPanel.addMouseListener(new MouseListener() {
            void mousePressed(MouseEvent e) { showPopupTabMenu(e) }
            void mouseReleased(MouseEvent e) { showPopupTabMenu(e) }
            void showPopupTabMenu(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int index = tabPanel.indexAtLocation(e.x, e.y)
                    if (index != -1) {
                        new PopupTabMenu(tabPanel.getComponentAt(index)).show(e.component, e.x, e.y)
                    }
                }
            }
            void mouseEntered(MouseEvent e) {}
            void mouseExited(MouseEvent e) {}
            void mouseClicked(MouseEvent e) {
                int index = tabPanel.indexAtLocation(e.x, e.y)
                if (index != -1 && SwingUtilities.isMiddleMouseButton(e)) {
                    removeComponent(tabPanel.getComponentAt(index))
                }
            }
        })
        return tabPanel
	}

	static Color darker(Color c) {
		return new Color(
			Math.max((int)(c.red  *0.85), 0),
			Math.max((int)(c.green*0.85), 0),
			Math.max((int)(c.blue *0.85), 0),
			c.alpha)
	}

    public <T extends JComponent & UriGettable> void addPage(String title, Icon icon, String tip, T page) {
        // Update preferences
        if (page instanceof PreferencesChangeListener) {
            page.preferencesChanged(preferences)
        }
        // Add a new tab
        JLabel tabCloseButton = new JLabel(CLOSE_ICON)
        tabCloseButton.toolTipText = 'Close this panel'
        tabCloseButton.addMouseListener(new MouseListener() {
            void mousePressed(MouseEvent e) {}
            void mouseReleased(MouseEvent e) {}
            void mouseEntered(MouseEvent e) { e.source.icon = TabbedPanel.@CLOSE_ACTIVE_ICON }
            void mouseExited(MouseEvent e) { e.source.icon = TabbedPanel.@CLOSE_ICON }
            void mouseClicked(MouseEvent e) { removeComponent(page) }
        })

		JPanel tab = new JPanel(new BorderLayout())
        tab.border = BorderFactory.createEmptyBorder(2, 0, 3, 0)
		tab.opaque = false
        tab.toolTipText = tip
        tab.add(new JLabel(title, icon, JLabel.LEADING), BorderLayout.CENTER)
		tab.add(tabCloseButton, BorderLayout.EAST)
        ToolTipManager.sharedInstance().unregisterComponent(tab)

		int index = tabbedPane.getTabCount()
		tabbedPane.addTab(title, page)
        tabbedPane.setTabComponentAt(index, tab)
        setSelectedIndex(index)

		getLayout().show(this, 'tabs')
	}

    void setSelectedIndex(int index) {
        if (index != -1) {
            if (tabbedPane.tabLayoutPolicy == JTabbedPane.SCROLL_TAB_LAYOUT) {
                // Ensure that the new page is visible (bug with SCROLL_TAB_LAYOUT)
                def event = new ChangeEvent(tabbedPane)
                for (def listener : tabbedPane.getChangeListeners()) {
                    if (listener.class.package.name.startsWith('javax.'))
                        listener.stateChanged(event)
                }
            }

            tabbedPane.selectedIndex = index
        }
    }

    Object showPage(URI uri) {
        def u1 = uri.toString()
        int i = tabbedPane.getTabCount()

        while (i-- > 0) {
            def page = tabbedPane.getComponentAt(i)
            def u2 = page.uri.toString()
            if (u1.startsWith(u2)) {
                tabbedPane.setSelectedIndex(i)
                return page
            }
        }

        return null
    }

    class PopupTabMenu extends JPopupMenu {
        PopupTabMenu(Component component) {
            // Add default popup menu entries
            def menuItem = new JMenuItem('Close', null)
            menuItem.addActionListener(new ActionListener() {
                void actionPerformed(ActionEvent e) { removeComponent(component) }
            })
            add(menuItem)

            menuItem = new JMenuItem('Close Others', null)
            menuItem.addActionListener(new ActionListener() {
                void actionPerformed(ActionEvent e) { removeOtherComponents(component) }
            })
            add(menuItem)

            menuItem = new JMenuItem('Close All', null)
            menuItem.addActionListener(new ActionListener() {
                void actionPerformed(ActionEvent e) { removeAllComponents() }
            })
            add(menuItem)

            // Add 'Select Tab' popup menu entry
            if ((tabbedPane.tabCount > 1) && (PlatformService.instance.isMac || 'true'.equals(preferences.get(TAB_LAYOUT)))) {
                addSeparator()
                def menu = new JMenu('Select Tab')
                int count = tabbedPane.tabCount

                for (int i=0; i<count; i++) {
                    JPanel tab = tabbedPane.getTabComponentAt(i)
                    JLabel label = tab.getComponent(0)
                    JMenuItem subMenuItem = new JMenuItem(label.text, label.icon)
                    subMenuItem.addActionListener(new ActionListener() {
                        final int index = i
                        void actionPerformed(ActionEvent e) { tabbedPane.setSelectedIndex(index) }
                    })
                    if (component == tabbedPane.getComponentAt(i)) {
                        subMenuItem.font = subMenuItem.font.deriveFont(Font.BOLD)
                    }
                    menu.add(subMenuItem)
                }

                add(menu)
            }

            // Add SPI popup menu entries
            def actions = api.getContextualActions(component.entry, null)

            if (actions) {
                addSeparator()

                for (def action : actions) {
                    if (action) {
                        add(action)
                    } else {
                        addSeparator()
                    }
                }
            }
        }
    }

    // --- Popup menu actions --- //
    void removeComponent(Component component) {
        tabbedPane.remove(component)
        if (tabbedPane.tabCount == 0) {
            getLayout().show(this, 'panel')
        }
    }

    void removeOtherComponents(Component component) {
        int i = tabbedPane.tabCount
        while (i-- > 0) {
            def c = tabbedPane.getComponentAt(i)
            if (c != component) {
                tabbedPane.remove(i)
            }
        }
        if (tabbedPane.tabCount == 0) {
            getLayout().show(this, 'panel')
        }
    }

    void removeAllComponents() {
        tabbedPane.removeAll()
        if (tabbedPane.tabCount == 0) {
            getLayout().show(this, 'panel')
        }
    }

    // --- PreferencesChangeListener --- //
    void preferencesChanged(Map<String, String> preferences) {
        // Store preferences
        this.preferences = preferences
        // Update layout
        if ('true'.equals(preferences.get(TAB_LAYOUT))) {
            tabbedPane.tabLayoutPolicy = JTabbedPane.SCROLL_TAB_LAYOUT
        } else {
            tabbedPane.tabLayoutPolicy = JTabbedPane.WRAP_TAB_LAYOUT
        }
        setSelectedIndex(tabbedPane.selectedIndex)
    }
}
