/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.view

import groovy.swing.SwingBuilder
import jd.gui.Constants
import jd.gui.api.feature.ContentSearchable
import jd.gui.api.feature.ContentSelectable
import jd.gui.api.feature.LineNumberNavigable
import jd.gui.api.feature.PageChangeListener
import jd.gui.api.feature.PageClosable
import jd.gui.api.feature.FocusedTypeGettable
import jd.gui.api.feature.ContentSavable
import jd.gui.api.feature.ContentCopyable
import jd.gui.api.feature.PreferencesChangeListener
import jd.gui.api.feature.SourcesSavable
import jd.gui.api.feature.UriGettable
import jd.gui.api.feature.UriOpenable
import jd.gui.model.configuration.Configuration
import jd.gui.model.history.History
import jd.gui.spi.FileLoader

import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import java.awt.Color
import java.awt.Dimension
import java.awt.Point
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.filechooser.FileNameExtensionFilter

import jd.gui.view.component.IconButton
import jd.gui.view.component.panel.MainTabbedPanel

import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent

class MainView implements UriOpenable, PreferencesChangeListener {
    SwingBuilder swing
    History history
    Closure openFilesClosure
    Color findBackgroundColor
    Color findErrorBackgroundColor

	MainView(
            SwingBuilder swing, Configuration configuration, History history,
            Closure panelClosedClosure,
            Closure currentPageChangedClosure,
            Closure openFilesClosure,
            Closure findCriteriaChangedClosure) {
        this.swing = swing
        this.history = history
        this.openFilesClosure = openFilesClosure

        swing.edt {
            // Setup
            registerBeanFactory('iconButton', IconButton.class)
            registerBeanFactory('mainTabbedPanel', MainTabbedPanel.class)
            // Load GUI description
            build(MainDescription)
            // Add listeners
            mainTabbedPanel.pageChangedListeners.add(new PageChangeListener() {
                JComponent currentPage = null

                public <T extends JComponent & UriGettable> void pageChanged(T page) {
                    if (currentPage != page) {
                        // Update current page
                        currentPage = page
                        currentPageChangedClosure(page)

                        swing.doLater {
                            if (page) {
                                // Update title
                                def path = page.uri.path
                                int index = path.lastIndexOf('/')
                                def name = (index == -1) ? path : path.substring(index + 1)
                                mainFrame.title = name ? name + ' - Java Decompiler' : 'Java Decompiler'
                                // Update history
                                history.add(page.uri)
                                // Update history actions
                                updateHistoryActions()
                                // Update menu
                                saveAction.enabled = (page instanceof ContentSavable)
                                copyAction.enabled = (page instanceof ContentCopyable)
                                selectAllAction.enabled = (page instanceof ContentSelectable)
                                findAction.enabled = (page instanceof ContentSearchable)
                                openTypeHierarchyAction.enabled = (page instanceof FocusedTypeGettable)
                                goToAction.enabled = (page instanceof LineNumberNavigable)
                            } else {
                                // Update title
                                mainFrame.title = 'Java Decompiler'
                                // Update menu
                                saveAction.enabled = false
                                copyAction.enabled = false
                                selectAllAction.enabled = false
                                openTypeHierarchyAction.enabled = false
                                goToAction.enabled = false
                            }
                        }
                    }
                }
            })
            mainTabbedPanel.tabbedPane.addChangeListener(new ChangeListener() {
                int lastTabCount = 0

                void stateChanged(ChangeEvent e) {
                    swing.with {
                        int tabCount = mainTabbedPanel.tabbedPane.tabCount
                        boolean enabled = (tabCount > 0)

                        closeAction.enabled = enabled
                        openTypeAction.enabled = enabled
                        searchAction.enabled = enabled
                        saveAllSourcesAction.enabled = (mainTabbedPanel.tabbedPane.selectedComponent instanceof SourcesSavable)

                        if (tabCount < lastTabCount) {
                            panelClosedClosure()
                        }

                        lastTabCount = tabCount
                    }
                }
            })
            mainTabbedPanel.preferencesChanged(configuration.preferences)
            findComboBox.editor.editorComponent.addKeyListener(new KeyAdapter() {
                String lastStr = ''

                void keyReleased(KeyEvent e) {
                    def findComboBox = swing.findComboBox

                    switch (e.keyCode) {
                    case KeyEvent.VK_ESCAPE:
                        swing.findPanel.visible = false
                        break
                    case KeyEvent.VK_ENTER:
                        def str = getFindText()
                        if (str.length() > 1) {
                            def index = findComboBox.model.getIndexOf(str)
                            if(index != -1 ) {
                                findComboBox.removeItemAt(index)
                            }
                            findComboBox.insertItemAt(str, 0)
                            findComboBox.selectedIndex = 0
                            swing.findNextAction.closure()
                        }
                        break
                    default:
                        def str = getFindText()
                        if (! lastStr.equals(str)) {
                            findCriteriaChangedClosure()
                            lastStr = str
                        }
                    }
                }
            })
            findComboBox.editor.editorComponent.opaque = true

            this.findBackgroundColor = findComboBox.background = findComboBox.editor.editorComponent.background
            this.findErrorBackgroundColor = Color.decode(configuration.preferences.get('JdGuiPreferences.errorBackgroundColor'))
        }
        swing.doLater {
            // Lazy initialization
            new JLabel('<html>init HTML parser</html>')
        }
    }
	
	void show(Point location, Dimension size, boolean maximize) {
        swing.edt {
			// Set position, resize and show
			mainFrame.with {
				setLocation(location)
				setSize(size)
				extendedState = maximize ? JFrame.MAXIMIZED_BOTH : 0
			}
            mainFrame.show()
		}
	}

    void showFindPanel() {
        swing.edt {
            findPanel.visible = true
            findComboBox.requestFocus()
        }
    }

    void setFindBackgroundColor(boolean wasFound) {
        swing.doLater {
            findComboBox.editor.editorComponent.background = wasFound ? findBackgroundColor : findErrorBackgroundColor
        }
    }
	
	JFileChooser createOpenFileChooser() {
		JFileChooser chooser = new JFileChooser() {
            void addFileFilters(Map<String, FileLoader> loaders) {
                removeChoosableFileFilter(getFileFilter())

                def extensions = loaders.collect({ key, value -> key }).sort()
                def description = extensions.collect({ "*.$it" }).join(', ')

                addChoosableFileFilter(new FileNameExtensionFilter("All files ($description)", extensions as String[]))

                for (def extension : extensions) {
                    def loader = loaders[extension]
                    addChoosableFileFilter(new FileNameExtensionFilter(loader.description, loader.extensions))
                }
            }
			void show(Closure okClosure) {
				if (showOpenDialog(swing.mainFrame) == JFileChooser.APPROVE_OPTION) {
					okClosure()
				}
			}
		}
		return chooser
	}

    JFileChooser createSaveFileChooser() {
        JFileChooser chooser = new JFileChooser() {
            void show(Closure okClosure) {
                if (showSaveDialog(swing.mainFrame) == JFileChooser.APPROVE_OPTION) {
                    if (selectedFile.exists()) {
                        def title = 'Are you sure?'
                        def message = "The file '$selectedFile.absolutePath' already isContainsIn.\n Do you want to replace the existing file?"
                        if (swing.optionPane().showConfirmDialog(swing.mainFrame, message, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            okClosure(selectedFile)
                        }
                    } else {
                        okClosure(selectedFile)
                    }
                }
            }
        }
        return chooser
    }

    void showErrorDialog(String message) {
		swing.optionPane().showMessageDialog(swing.mainFrame, message, 'Error', JOptionPane.ERROR_MESSAGE)
	}

    public <T extends JComponent & UriGettable> void addMainPanel(String title, Icon icon, String tip, T component) {
        swing.edt {
            swing.mainTabbedPanel.addPage(title, icon, tip, component)
        }
    }

    public <T extends JComponent & UriGettable> List<T> getMainPanels() {
        return swing.mainTabbedPanel.getPages()
    }

    public <T extends JComponent & UriGettable> T getSelectedMainPanel() {
        return swing.mainTabbedPanel.tabbedPane.getSelectedComponent()
    }

    void closeCurrentTab() {
        swing.doLater {
            def component = mainTabbedPanel.tabbedPane.selectedComponent
            if (component instanceof PageClosable) {
                if (!component.closePage()) {
                    mainTabbedPanel.removeComponent(component)
                }
            } else {
                mainTabbedPanel.removeComponent(component)
            }
        }
    }

    void updateRecentFilesMenu(List<File> files) {
        swing.doLater {
            recentFiles.removeAll()
            files.each { f ->
                recentFiles.add(
                    menuItem() {
                        action(name:reduceRecentFilePath(f.absolutePath), closure:{ openFilesClosure(f) })
                    }
                )
            }
        }
    }

    String getFindText() {
        def doc = swing.findComboBox.editor.editorComponent.document
        return doc.getText(0, doc.length)
    }

    boolean getFindCaseSensitive() { swing.findCaseSensitive.isSelected() }

    void updateHistoryActions() {
        swing.doLater {
            backwardAction.enabled = history.canBackward()
            forwardAction.enabled = history.canForward()
        }
    }

    // --- Utils --- //
    static String reduceRecentFilePath(String path) {
        int lastSeparatorPosition = path.lastIndexOf(File.separatorChar as int)

        if ((lastSeparatorPosition == -1) || (lastSeparatorPosition < Constants.RECENT_FILE_MAX_LENGTH)) {
            return path
        }

        int length = Constants.RECENT_FILE_MAX_LENGTH/2 - 2
        String left = path.substring(0, length)
        String right = path.substring(path.length() - length)

        return left + "..." + right
    }

    // --- URIOpener --- //
    boolean openUri(URI uri) {
        boolean success
        swing.edt {
            success = swing.mainTabbedPanel.openUri(uri)
        }
        return success
    }

    // --- PreferencesChangeListener --- //
    void preferencesChanged(Map<String, String> preferences) {
        swing.mainTabbedPanel.preferencesChanged(preferences)
    }
}
