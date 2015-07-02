/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.controller

import groovy.swing.SwingBuilder
import groovy.transform.CompileStatic
import org.jd.gui.api.API
import org.jd.gui.api.feature.ContentCopyable
import org.jd.gui.api.feature.ContentIndexable
import org.jd.gui.api.feature.ContentSavable
import org.jd.gui.api.feature.ContentSearchable
import org.jd.gui.api.feature.FocusedTypeGettable
import org.jd.gui.api.feature.IndexesChangeListener
import org.jd.gui.api.feature.LineNumberNavigable
import org.jd.gui.api.feature.ContentSelectable
import org.jd.gui.api.feature.PreferencesChangeListener
import org.jd.gui.api.feature.SourcesSavable
import org.jd.gui.api.feature.UriGettable
import org.jd.gui.api.model.Container
import org.jd.gui.api.model.Indexes
import org.jd.gui.model.history.History
import org.jd.gui.service.container.ContainerFactoryService
import org.jd.gui.service.fileloader.FileLoaderService
import org.jd.gui.service.indexer.IndexerService
import org.jd.gui.service.mainpanel.PanelFactoryService
import org.jd.gui.service.pastehandler.PasteHandlerService
import org.jd.gui.service.actions.ContextualActionsFactoryService
import org.jd.gui.service.preferencespanel.PreferencesPanelService
import org.jd.gui.service.sourcesaver.SourceSaverService
import org.jd.gui.service.treenode.TreeNodeFactoryService
import org.jd.gui.service.type.TypeFactoryService
import org.jd.gui.service.uriloader.UriLoaderService
import org.jd.gui.spi.ContainerFactory
import org.jd.gui.spi.FileLoader
import org.jd.gui.spi.Indexer
import org.jd.gui.spi.PanelFactory
import org.jd.gui.spi.SourceSaver
import org.jd.gui.spi.TreeNodeFactory
import org.jd.gui.spi.TypeFactory
import org.jd.gui.spi.UriLoader
import org.jd.gui.util.net.UriUtil

import javax.swing.Action
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JFileChooser
import javax.swing.JLayer
import javax.swing.TransferHandler

import org.jd.gui.model.configuration.Configuration
import org.jd.gui.view.MainView

import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.filechooser.FileSystemView
import java.awt.Desktop
import java.awt.EventDispatchThread
import java.awt.Frame
import java.awt.Point
import java.awt.Toolkit
import java.awt.WaitDispatchSupport
import java.awt.datatransfer.DataFlavor
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.nio.file.Path
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainController implements API {
    SwingBuilder swing
    Configuration configuration
    MainView mainView

    GoToController goToController
    OpenTypeController openTypeController
    OpenTypeHierarchyController openTypeHierarchyController
    PreferencesController preferencesController
    SearchInConstantPoolsController searchInConstantPoolsController
    SaveAllSourcesController saveAllSourcesController
    SelectLocationController selectLocationController

    History history = new History()
    JComponent currentPage = null
    ExecutorService executor
    Collection<IndexesChangeListener> containerChangeListeners = []

	MainController(SwingBuilder swing, Configuration configuration) {
        this.swing = swing
        this.configuration = configuration
        // Create main frame
        mainView = new MainView(
            swing,
            configuration,
            this,
            history,
            { panelClosed() },                      // panelClosedClosure
            { page -> onCurrentPageChanged(page) }, // currentPageChangedClosure
            { file -> openFiles([file]) },          // openFilesClosure
            { onFindCriteriaChanged() }             // findCriteriaChangedClosure
        )
	}
	
	// --- Show GUI --- //
	void show() {
        // Show main frame
		mainView.show(
			configuration.mainWindowLocation, 
			configuration.mainWindowSize, 
			configuration.mainWindowMaximize)

        mainView.updateRecentFilesMenu(configuration.recentFiles)

        swing.doLater {
            // Setup closures
            openAction.closure = { onOpen() }

            saveAllSourcesController = new SaveAllSourcesController(swing, configuration, this)
            saveAllSourcesAction.closure = { onSaveAllSources() }

            containerChangeListeners.add(openTypeController = new OpenTypeController(swing, configuration, this))
            openTypeAction.closure = { onOpenType() }

            containerChangeListeners.add(openTypeHierarchyController = new OpenTypeHierarchyController(swing, configuration, this))
            openTypeHierarchyAction.closure = { onOpenTypeHierarchy() }

            closeAction.closure = { onClose() }
            saveAction.closure = { onSaveSource() }
            copyAction.closure = { onCopy() }
            pasteAction.closure = { onPaste() }
            selectAllAction.closure = { onSelectAll() }

            findAction.closure = { onFind() }
            findNextAction.closure = { onFindNext() }
            findPreviousAction.closure = { onFindPrevious() }
            findCaseSensitiveAction.closure = { onFindCriteriaChanged() }

            goToController = new GoToController(swing, configuration)
            goToAction.closure = { onGoTo() }

            backwardAction.closure = { openURI(history.backward()) }
            forwardAction.closure = { openURI(history.forward()) }

            containerChangeListeners.add(searchInConstantPoolsController = new SearchInConstantPoolsController(swing, configuration, this))
            searchAction.closure = { onSearch() }

            wikipediaAction.closure = { onWikipedia() }

            preferencesController = new PreferencesController(swing, configuration, this, PreferencesPanelService.instance.providers)
            preferencesAction.closure = { onPreferences() }

            // Add listeners
            mainFrame.addComponentListener(new MainFrameListener(configuration))
            // Set drop files transfer handler
            mainFrame.setTransferHandler(new FilesTransferHandler())

            // Create executor
            executor = Executors.newSingleThreadExecutor()
            // Background initializations
            executor.execute(new Runnable() {
                void run() {
                    // Background controller creation
                    selectLocationController = new SelectLocationController(swing, configuration, MainController.this)
                    // Background service initialization
                    ContextualActionsFactoryService.instance
                    ContainerFactoryService.instance
                    FileLoaderService.instance
                    IndexerService.instance
                    PasteHandlerService.instance
                    PreferencesPanelService.instance
                    TreeNodeFactoryService.instance
                    TypeFactoryService.instance
                    UriLoaderService.instance
                    // Background class loading
                    new JFileChooser().addChoosableFileFilter(new FileNameExtensionFilter('', 'dummy'))
                    new WaitDispatchSupport(new EventDispatchThread(null, 'dummy', null), null)
                    FileSystemView.getFileSystemView().isFileSystemRoot(new File('dummy'))
                    new JLayer()
                    new JComponent.KeyboardState()
                    new JComponent.IntVector()
                }
            })
        }
    }

	// --- Actions --- //
	void onOpen() {
		mainView.createOpenFileChooser().with {
            addFileFilters(FileLoaderService.instance.mapProviders)
			currentDirectory = configuration.configRecentLoadDirectory
			show({ openFiles([ selectedFile ]) })
			configuration.configRecentLoadDirectory = currentDirectory
		}
	}

    void onClose() {
        mainView.closeCurrentTab()
    }

    void onSaveSource() {
        if (currentPage instanceof ContentSavable) {
            mainView.createSaveFileChooser().with {
                selectedFile = new File(configuration.configRecentSaveDirectory, currentPage.fileName)
                show({ file -> file.withOutputStream { os -> currentPage.save(this, os) } })
                configuration.configRecentSaveDirectory = currentDirectory
            }
        }
    }

    void onSaveAllSources() {
        if (! saveAllSourcesController.isActivated()) {
            def currentPanel = mainView.selectedMainPanel

            if (currentPanel instanceof SourcesSavable) {
                mainView.createSaveFileChooser().with {
                    selectedFile = new File(configuration.configRecentSaveDirectory, currentPanel.sourceFileName)
                    show({ file -> saveAllSourcesController.show(executor, currentPanel, file) })
                    configuration.configRecentSaveDirectory = currentDirectory
                }
            }
        }
    }

    void onCopy() {
        if (currentPage instanceof ContentCopyable) {
            currentPage.copy()
        }
    }

    void onPaste() {
        try {
            def transferable = Toolkit.defaultToolkit.systemClipboard.getContents(null)

            if (transferable?.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                def obj = transferable.getTransferData(DataFlavor.stringFlavor)
                PasteHandlerService.instance.get(obj)?.paste(this, obj)
            }
        } catch (Exception ignore) {
        }
    }

    void onSelectAll() {
        if (currentPage instanceof ContentSelectable) {
            currentPage.selectAll()
        }
    }

    void onFind() {
        if (currentPage instanceof ContentSearchable) {
            mainView.showFindPanel()
        }
    }

    void onFindCriteriaChanged() {
        if (currentPage instanceof ContentSearchable) {
            mainView.setFindBackgroundColor(currentPage.highlightText(mainView.findText, mainView.findCaseSensitive))
        }
    }

    void onFindNext() {
        if (currentPage instanceof ContentSearchable) {
            currentPage.findNext(mainView.findText, mainView.findCaseSensitive)
        }
    }

    void onOpenType() {
        openTypeController.show(collectionOfIndexes, { uri -> openURI(uri) })
    }

    void onOpenTypeHierarchy() {
        if (currentPage instanceof FocusedTypeGettable) {
            openTypeHierarchyController.show(collectionOfIndexes, currentPage.entry, currentPage.focusedTypeName, { uri -> openURI(uri) })
        }
    }

    void onGoTo() {
        if (currentPage instanceof LineNumberNavigable) {
            goToController.show(currentPage, { int lineNumber -> currentPage.goToLineNumber(lineNumber) })
        }
    }

    void onSearch() {
        searchInConstantPoolsController.show(collectionOfIndexes, { uri -> openURI(uri) })
    }

    void onFindPrevious() {
        if (currentPage instanceof ContentSearchable) {
            currentPage.findPrevious(mainView.findText, mainView.findCaseSensitive)
        }
    }

    void onWikipedia() {
        if (Desktop.isDesktopSupported()) {
            def desktop = Desktop.desktop
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(URI.create('http://en.wikipedia.org/wiki/Java_Decompiler'))
            }
        }
    }

    void onPreferences() {
        preferencesController.show({
            checkPreferencesChange(currentPage)
            mainView.preferencesChanged(preferences)
        })
    }
	
    void onCurrentPageChanged(JComponent page) {
        currentPage = page
        checkIndexesChange(page)
        checkPreferencesChange(page)
    }

    void checkIndexesChange(JComponent page) {
        if (page instanceof IndexesChangeListener) {
            def collectionOfIndexes = getCollectionOfIndexes()
            def currentHashcode = Integer.valueOf(collectionOfIndexes.hashCode())
            def lastHashcode = page.getClientProperty('collectionOfIndexes-stamp')

            if (!currentHashcode.equals(lastHashcode)) {
                page.indexesChanged(collectionOfIndexes)
                page.putClientProperty('collectionOfIndexes-stamp', currentHashcode)
            }
        }
    }

    void checkPreferencesChange(JComponent page) {
        if (page instanceof PreferencesChangeListener) {
            def preferences = configuration.preferences
            def currentHashcode = Integer.valueOf(preferences.hashCode())
            def lastHashcode = page.getClientProperty('preferences-stamp')

            if (!currentHashcode.equals(lastHashcode)) {
                page.preferencesChanged(preferences)
                page.putClientProperty('preferences-stamp', currentHashcode)
            }
        }
    }

    // --- Operations --- //
    void openFiles(List<File> files) {
        def errors = []

        for (def file : files) {
            // Check input file
            if (file.exists()) {
                FileLoader loader = getFileLoader(file)
                if (! loader?.accept(this, file)) {
                    errors << "Invalid input fileloader: '$file.absolutePath'"
                }
            } else {
                errors << "File not found: '$file.absolutePath'"
            }
        }

        if (errors) {
            String messages = ''

            errors.eachWithIndex { message, index ->
                if (index == 0) {
                    messages += message
                } else if (index < 20) {
                    messages += "\n$message"
                } else if (index == 20) {
                    messages += '\n...'
                }
            }

            mainView.showErrorDialog(messages)
        } else {
            for (def file : files) {
                if (openURI(file.toURI())) {
                    configuration.addRecentFile(file)
                    mainView.updateRecentFilesMenu(configuration.recentFiles)
                }
            }
        }
    }

    // --- Drop files transfer handler --- //
    class FilesTransferHandler extends TransferHandler {
        boolean canImport(TransferHandler.TransferSupport info) {
            return info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
        }

        boolean importData(TransferHandler.TransferSupport info) {
            if (info.isDrop() && info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                try {
                    openFiles(info.transferable.getTransferData(DataFlavor.javaFileListFlavor))
                    return true
                } catch (Exception ignore) {
                }
            }
            return false
        }
    }

    // --- ComponentListener --- //
    class MainFrameListener extends ComponentAdapter {
        Configuration configuration

        MainFrameListener(Configuration configuration) {
            this.configuration = configuration
        }

        void componentMoved(ComponentEvent e) {
            def f = e.source

            if ((f.extendedState & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
                configuration.mainWindowMaximize = true
            } else {
                configuration.mainWindowLocation = f.location
                configuration.mainWindowMaximize = false
            }
        }

        void componentResized(ComponentEvent e) {
            def f = e.source

            if ((f.extendedState & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
                configuration.mainWindowMaximize = true
            } else {
                configuration.mainWindowSize = f.size
                configuration.mainWindowMaximize = false
            }
        }
    }

    protected void panelClosed() {
        swing.doLater {
            // Fire 'indexesChanged' event
            def collectionOfIndexes = getCollectionOfIndexes()
            for (def listener : containerChangeListeners) {
                listener.indexesChanged(collectionOfIndexes)
            }
            if (currentPage instanceof IndexesChangeListener) {
                currentPage.indexesChanged(collectionOfIndexes)
            }
        }
    }

    // --- API --- //
    boolean openURI(URI uri) {
        if (uri) {
            boolean success = mainView.openUri(uri) || getUriLoader(uri)?.load(this, uri)
            if (success) {
                swing.doLater {
                    addURI(uri)
                    closeAction.enabled = true
                    openTypeAction.enabled = true
                }
            }
            return success
        }
        return false
    }

    boolean openURI(int x, int y, Collection<Container.Entry> entries, String query, String fragment) {
        if (entries) {
            if (entries.size() == 1) {
                // Open the single entry uri
                def entry = entries.iterator().next()
                return openURI(UriUtil.createURI(this, collectionOfIndexes, entry, query, fragment))
            } else {
                // Multiple entries -> Open a "Select location" popup
                def collectionOfIndexes = getCollectionOfIndexes()
                selectLocationController.show(
                    new Point(x+(16+2) as int, y+2 as int),
                    entries,
                    { entry -> openURI(UriUtil.createURI(this, collectionOfIndexes, entry, query, fragment)) },   // entry selected closure
                    { })                                                                                          // popup close closure
                return true
            }
        }

        return false
    }

    void addURI(URI uri) {
        history.add(uri)
        mainView.updateHistoryActions()
    }

    public <T extends JComponent & UriGettable> void addPanel(String title, Icon icon, String tip, T component) {
        mainView.addMainPanel(title, icon, tip, component)

        if (component instanceof ContentIndexable) {
            if (executor == null) {
                executor = Executors.newSingleThreadExecutor()
            }

            def futureIndexes = executor.submit(new Callable<Indexes>() {
                Indexes call() throws Exception {
                    return component.index(MainController.this)
                }
            })
            def indexes = new Indexes() {
                void waitIndexers() { futureIndexes.get() }

                Map<String, Collection> getIndex(String name) { futureIndexes.get().getIndex(name) }
            }

            component.putClientProperty('indexes', indexes)

            swing.doLater {
                // Fire 'indexesChanged' event
                def collectionOfIndexes = getCollectionOfIndexes()
                for (def listener : containerChangeListeners) {
                    listener.indexesChanged(collectionOfIndexes)
                }
                if (currentPage instanceof IndexesChangeListener) {
                    currentPage.indexesChanged(collectionOfIndexes)
                }
            }
        }

        checkIndexesChange(currentPage)
    }

    @CompileStatic
    Collection<Action> getContextualActions(Container.Entry entry, String fragment) { ContextualActionsFactoryService.instance.get(this, entry, fragment) }

    @CompileStatic
    FileLoader getFileLoader(File file) { FileLoaderService.instance.get(this, file) }

    @CompileStatic
    UriLoader getUriLoader(URI uri) { UriLoaderService.instance.get(this, uri) }

    @CompileStatic
    PanelFactory getMainPanelFactory(Container container) { PanelFactoryService.instance.get(container) }

    @CompileStatic
    ContainerFactory getContainerFactory(Path rootPath) { ContainerFactoryService.instance.get(this, rootPath) }

    @CompileStatic
    TreeNodeFactory getTreeNodeFactory(Container.Entry entry) { TreeNodeFactoryService.instance.get(entry) }

    @CompileStatic
    TypeFactory getTypeFactory(Container.Entry entry) { TypeFactoryService.instance.get(entry) }

    @CompileStatic
    Indexer getIndexer(Container.Entry entry) { IndexerService.instance.get(entry) }

    @CompileStatic
    SourceSaver getSourceSaver(Container.Entry entry) { SourceSaverService.instance.get(entry) }

    @CompileStatic
    Map<String, String> getPreferences() { configuration.preferences }

    Collection<Indexes> getCollectionOfIndexes() {
        mainView.mainPanels.collect { it.getClientProperty('indexes') }.grep { it != null }
    }
}
