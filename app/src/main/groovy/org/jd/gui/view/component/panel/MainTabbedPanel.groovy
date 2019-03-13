/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view.component.panel

import org.jd.gui.api.feature.PageChangeListener
import org.jd.gui.api.feature.PageChangeable
import org.jd.gui.api.feature.PreferencesChangeListener
import org.jd.gui.api.feature.UriGettable
import org.jd.gui.api.feature.UriOpenable

import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import java.awt.CardLayout
import java.awt.Color
import java.awt.Font
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel

import org.jd.gui.service.platform.PlatformService

class MainTabbedPanel extends TabbedPanel implements UriOpenable, PageChangeListener {
    List<PageChangeListener> pageChangedListeners = []
    // Flag to prevent the event cascades
    boolean pageChangedListenersEnabled = true

	void create() {
		Color bg = darker(background)
		
		if (PlatformService.instance.isWindows) {
			background = bg
		}

        tabbedPane = createTabPanel()
        tabbedPane.addChangeListener(new ChangeListener() {
            void stateChanged(ChangeEvent e) {
                if (pageChangedListenersEnabled) {
                    def subPage = tabbedPane.selectedComponent

                    if (subPage) {
                        def page = subPage.getClientProperty('currentPage')

                        if (page == null) {
                            page = tabbedPane.selectedComponent
                        }
                        // Fire page changed event
                        for (def listener : pageChangedListeners) {
                            listener.pageChanged(page)
                        }
                        // Update current sub-page preferences
                        if (subPage instanceof PreferencesChangeListener) {
                            subPage.preferencesChanged(preferences)
                        }
                    }
                }
            }
        })

		setLayout(new CardLayout())
		add('panel', createPanel(bg))
		add('tabs', tabbedPane)

		border = BorderFactory.createMatteBorder(1, 0, 0, 0, darker(darker(background)))
	}
	
	JPanel createPanel(Color background) {
		JPanel panel = new JPanel()
		
		String fileManager
		
		switch (PlatformService.instance.os) {
			case PlatformService.OS.Linux:
				fileManager = 'your file manager'
				break
			case PlatformService.OS.MacOSX:
				fileManager = 'the Finder'
				break
			case PlatformService.OS.Windows:
				fileManager = 'Explorer'
				break
		}
		
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS))
		panel.background = background
		
		Color fontColor = panel.background.darker()
		
		panel.add(Box.createHorizontalGlue())

		JPanel box = new JPanel()
		box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS))
		box.background = panel.background
		box.add(Box.createVerticalGlue())
		
		JLabel title = new JLabel(text:'No files are open', foreground:fontColor)
		title.font = title.font.deriveFont(Font.BOLD, title.font.size+8)
		
		box.add(title)
		box.add(new JLabel())
		box.add(new JLabel(text:'Open a file with menu "File > Open File..."', foreground:fontColor))
		box.add(new JLabel(text:'Open recent files with menu "File > Recent Files"', foreground:fontColor))
		box.add(new JLabel(text:'Drag and drop files from ' + fileManager, foreground:fontColor))
		box.add(Box.createVerticalGlue())
		
		panel.add(box)
		panel.add(Box.createHorizontalGlue())
		
		return panel
	}

    public <T extends JComponent & UriGettable> void addPage(String title, Icon icon, String tip, T page) {
        super.addPage(title, icon, tip, page)
        if (page instanceof PageChangeable) {
            page.addPageChangeListener(this)
        }
    }

    public <T extends JComponent & UriGettable> List<T> getPages() {
        int i = tabbedPane.getTabCount()
        def pages = new ArrayList<T>(i)
        while (i-- > 0) {
            pages.add(tabbedPane.getComponentAt(i))
        }
        return pages
    }

    // --- URIOpener --- //
    boolean openUri(URI uri) {
        try {
            // Disable page changed event
            pageChangedListenersEnabled = false
            // Search & display main tab
            def page = showPage(uri)
            if (page) {
                if (!uri.equals(page.uri) && (page instanceof UriOpenable)) {
                    // Enable page changed event
                    pageChangedListenersEnabled = true
                    // Search & display sub tab
                    return page.openUri(uri)
                }
                return true
            } else {
                return false
            }
        } finally {
            // Enable page changed event
            pageChangedListenersEnabled = true
        }
    }

    // --- PageChangedListener --- //
    public <T extends JComponent & UriGettable> void pageChanged(T page) {
        // Store active page for current sub tabbed pane
        tabbedPane.selectedComponent?.putClientProperty('currentPage', page)
        // Forward event
        for (def listener : pageChangedListeners) {
            listener.pageChanged(page)
        }
    }

    // --- PreferencesChangeListener --- //
    void preferencesChanged(Map<String, String> preferences) {
        super.preferencesChanged(preferences)

        // Update current sub-page preferences
        def subPage = tabbedPane.selectedComponent
        if (subPage instanceof PreferencesChangeListener) {
            subPage.preferencesChanged(preferences)
        }
    }
}
