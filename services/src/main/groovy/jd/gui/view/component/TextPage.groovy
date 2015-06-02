/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.view.component

import groovy.transform.CompileStatic
import jd.gui.api.API
import jd.gui.api.feature.ContentCopyable
import jd.gui.api.feature.ContentSavable
import jd.gui.api.feature.ContentSearchable
import jd.gui.api.feature.ContentSelectable
import jd.gui.api.feature.LineNumberNavigable
import jd.gui.api.feature.UriOpenable
import jd.gui.util.io.NewlineOutputStream
import org.fife.ui.rsyntaxtextarea.DocumentRange
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaEditorKit
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rsyntaxtextarea.Theme
import org.fife.ui.rsyntaxtextarea.folding.FoldManager
import org.fife.ui.rtextarea.RTextScrollPane
import org.fife.ui.rtextarea.SearchContext
import org.fife.ui.rtextarea.SearchEngine

import javax.swing.*
import javax.swing.text.BadLocationException
import java.awt.*
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import java.awt.event.MouseWheelListener

@CompileStatic
class TextPage extends JPanel implements ContentCopyable, ContentSelectable, LineNumberNavigable, ContentSearchable, UriOpenable, ContentSavable {
    protected static final ImageIcon COLLAPSED_ICON = new ImageIcon(TextPage.class.classLoader.getResource('images/plus.png'))
    protected static final ImageIcon EXPANDED_ICON = new ImageIcon(TextPage.class.classLoader.getResource('images/minus.png'))

    protected static final Color DOUBLE_CLICK_HIGHLIGHT_COLOR = new Color(0x66ff66)
    protected static final Color SEARCH_HIGHLIGHT_COLOR = new Color(0xffff66)
    protected static final Color SELECT_HIGHLIGHT_COLOR = new Color(0xF49810)

    protected static final RSyntaxTextAreaEditorKit.DecreaseFontSizeAction DECREASE_FONT_SIZE_ACTION = new RSyntaxTextAreaEditorKit.DecreaseFontSizeAction()
    protected static final RSyntaxTextAreaEditorKit.IncreaseFontSizeAction INCREASE_FONT_SIZE_ACTION = new RSyntaxTextAreaEditorKit.IncreaseFontSizeAction()

    protected RSyntaxTextArea textArea
    protected RTextScrollPane scrollPane

    TextPage() {
        super(new BorderLayout())

        textArea = newRSyntaxTextArea()
        textArea.setSyntaxEditingStyle(getSyntaxStyle())
        textArea.setCodeFoldingEnabled(true);
        textArea.setAntiAliasingEnabled(true);
        textArea.caretPosition = 0
        textArea.editable = false
        textArea.dropTarget = null
        textArea.popupMenu = null
        textArea.addMouseListener(new MouseAdapter() {
            void mouseClicked(MouseEvent e) {
                if (e.clickCount == 2) {
                    textArea.markAllHighlightColor = DOUBLE_CLICK_HIGHLIGHT_COLOR
                    SearchEngine.markAll(textArea, newSearchContext(textArea.selectedText, true, true, true, false))
                }
            }
        })
        textArea.addMouseWheelListener(new MouseWheelListener() {
            void mouseWheelMoved(MouseWheelEvent e) {
                if ((e.modifiers & (Event.META_MASK|Event.CTRL_MASK)) != 0) {
                    int offset = textArea.viewToModel(new Point(e.x, e.y))

                    // Update font size
                    if (e.wheelRotation > 0) {
                        INCREASE_FONT_SIZE_ACTION.actionPerformedImpl(null, textArea)
                    } else {
                        DECREASE_FONT_SIZE_ACTION.actionPerformedImpl(null, textArea)
                    }

                    Rectangle newRectangle = textArea.modelToView(offset)
                    int newY = newRectangle.@y + (newRectangle.@height >> 1)

                    // Scroll
                    Point viewPosition = scrollPane.viewport.viewPosition
                    viewPosition.translate(0, newY-e.y)
                    scrollPane.viewport.viewPosition = viewPosition
                }
            }
        })

        def ctrlA = KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.defaultToolkit.menuShortcutKeyMask)
        def ctrlC = KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.defaultToolkit.menuShortcutKeyMask)
        def ctrlV = KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.defaultToolkit.menuShortcutKeyMask)
        def inputMap = textArea.inputMap
        inputMap.put(ctrlA, 'none')
        inputMap.put(ctrlC, 'none')
        inputMap.put(ctrlV, 'none')

        def theme = Theme.load(getClass().classLoader.getResourceAsStream('rsyntaxtextarea/themes/eclipse.xml'))
        theme.apply(textArea)

        scrollPane = new RTextScrollPane(textArea)
        scrollPane.foldIndicatorEnabled = true
        scrollPane.font = textArea.font

        def gutter = scrollPane.gutter
        gutter.setFoldIcons(COLLAPSED_ICON, EXPANDED_ICON)
        gutter.foldIndicatorForeground = gutter.borderColor

        add(scrollPane, BorderLayout.CENTER)
        add(new ErrorStrip(textArea), BorderLayout.LINE_END)
    }

    protected RSyntaxTextArea newRSyntaxTextArea() { new RSyntaxTextArea() }

    String getText() { textArea.text }

    void setText(String text) {
        textArea.text = text
        textArea.caretPosition = 0
    }

    String getSyntaxStyle() { SyntaxConstants.SYNTAX_STYLE_NONE }

    /**
     * @see org.fife.ui.rsyntaxtextarea.RSyntaxUtilities#selectAndPossiblyCenter
     * Force center and do not select
     */
    void setCaretPositionAndCenter(DocumentRange range) {
        int start = range.startOffset
        int end = range.endOffset
        boolean foldsExpanded = false
        FoldManager fm = textArea.foldManager

        if (fm.isCodeFoldingSupportedAndEnabled()) {
            foldsExpanded = fm.ensureOffsetNotInClosedFold(start)
            foldsExpanded |= fm.ensureOffsetNotInClosedFold(end)
        }

        if (foldsExpanded == false) {
            try {
                Rectangle r = textArea.modelToView(start)

                if (r) {
                    // Visible
                    setCaretPositionAndCenter(start, end, r)
                } else {
                    // Not visible yet
                    SwingUtilities.invokeLater(new Runnable() {
                        void run() {
                            r = textArea.modelToView(start)
                            if (r) {
                                setCaretPositionAndCenter(start, end, r)
                            }
                        }
                    })
                }
            } catch (BadLocationException ignore) {
            }
        }
    }

    protected void setCaretPositionAndCenter(int start, int end, Rectangle r) {
        if (end != start) {
            r = r.union(textArea.modelToView(end))
        }

        Rectangle visible = textArea.visibleRect

        // visible.@x = r.@x - (visible.@width - r.@width) / 2 as int
        visible.@y = r.@y - (visible.@height - r.@height) / 2 as int

        Rectangle bounds = textArea.bounds
        Insets i = textArea.insets
        //bounds.@x = i.left
        bounds.@y = i.top
        //bounds.@width -= i.left + i.right
        bounds.@height -= i.top + i.bottom

        //if (visible.@x < bounds.@x) {
        //    visible.@x = bounds.@x
        //}
        //if (visible.@x + visible.@width > bounds.@x + bounds.@width) {
        //    visible.@x = bounds.@x + bounds.@width - visible.@width
        //}
        if (visible.@y < bounds.@y) {
            visible.@y = bounds.@y
        }
        if (visible.@y + visible.@height > bounds.@y + bounds.@height) {
            visible.@y = bounds.@y + bounds.@height - visible.@height
        }

        textArea.scrollRectToVisible(visible)
        textArea.caretPosition = start
    }

    // --- ContentCopyable --- //
    void copy() {
        if (textArea.selectionStart == textArea.selectionEnd) {
            toolkit.systemClipboard.setContents(new StringSelection(''), null)
        } else {
            textArea.copyAsRtf()
        }
    }

    // --- ContentSelectable --- //
    void selectAll() {
        textArea.selectAll()
    }

    // --- LineNumberNavigable --- //
    int getMaximumLineNumber() {
        return textArea.getLineOfOffset(textArea.document.length)
    }

    void goToLineNumber(int lineNumber) {
        textArea.caretPosition = textArea.getLineStartOffset(lineNumber-1)
    }

    boolean checkLineNumber(int lineNumber) { true }

    // --- ContentSearchable --- //
    boolean highlightText(String text, boolean caseSensitive) {
        if (text.length() > 1) {
            textArea.markAllHighlightColor = SEARCH_HIGHLIGHT_COLOR
            textArea.caretPosition = textArea.selectionStart

            def context = newSearchContext(text, caseSensitive, false, true, false)
            def result = SearchEngine.find(textArea, context)

            if (!result.wasFound()) {
                textArea.caretPosition = 0
                result = SearchEngine.find(textArea, context)
            }

            return result.wasFound()
        } else {
            return true
        }
    }

    void findNext(String text, boolean caseSensitive) {
        if (text.length() > 1) {
            textArea.markAllHighlightColor = SEARCH_HIGHLIGHT_COLOR

            def context = newSearchContext(text, caseSensitive, false, true, false)
            def result = SearchEngine.find(textArea, context)

            if (!result.wasFound()) {
                textArea.caretPosition = 0
                SearchEngine.find(textArea, context)
            }
        }
    }

    void findPrevious(String text, boolean caseSensitive) {
        if (text.length() > 1) {
            textArea.markAllHighlightColor = SEARCH_HIGHLIGHT_COLOR

            def context = newSearchContext(text, caseSensitive, false, false, false)
            def result = SearchEngine.find(textArea, context)

            if (!result.wasFound()) {
                textArea.caretPosition = textArea.document.length
                SearchEngine.find(textArea, context)
            }
        }
    }

    protected SearchContext newSearchContext(
            String searchFor, boolean matchCase, boolean wholeWord, boolean searchForward, boolean regexp) {
        def context = new SearchContext(searchFor, matchCase)
        context.markAll = true
        context.wholeWord = wholeWord
        context.searchForward = searchForward
        context.regularExpression = regexp
        return context
    }

    // --- UriOpenable --- //
    boolean openUri(URI uri) {
        def query = uri.query

        if (query) {
            Map<String, String> parameters = parseQuery(query)

            if (parameters.containsKey('lineNumber')) {
                def lineNumber = parameters.get('lineNumber')
                if (lineNumber.isNumber()) {
                    goToLineNumber(lineNumber.toInteger())
                    return true
                }
            } else if (parameters.containsKey('position')) {
                def position = parameters.get('position')
                if (position.isNumber()) {
                    int pos = position.toInteger()
                    if (textArea.document.length > pos) {
                        setCaretPositionAndCenter(new DocumentRange(pos, pos))
                        return true
                    }
                }
            } else if (parameters.containsKey('highlightFlags')) {
                def highlightFlags = parameters.get('highlightFlags')

                if ((highlightFlags.indexOf('s') != -1) && parameters.containsKey('highlightPattern')) {
                    textArea.markAllHighlightColor = SELECT_HIGHLIGHT_COLOR
                    textArea.caretPosition = 0

                    // Highlight all
                    def searchFor = createRegExp(parameters.get('highlightPattern'))
                    def context =  newSearchContext(searchFor, true, false, true, true)
                    def result = SearchEngine.find(textArea, context)

                    if (result.matchRange) {
                        textArea.caretPosition = result.matchRange.startOffset
                    }

                    return true
                }
            }
        }

        return false
    }

    protected Map<String, String> parseQuery(String query) {
        Map<String, String> parameters = [:]

        // Parse parameters
        for (def param : query.split('&')) {
            int index = param.indexOf('=')

            if (index == -1) {
                parameters.put(URLDecoder.decode(param, 'UTF-8'), '')
            } else {
                def key = param.substring(0, index)
                def value = param.substring(index+1)
                parameters.put(URLDecoder.decode(key, 'UTF-8'), URLDecoder.decode(value, 'UTF-8'))
            }
        }

        return parameters
    }

    /**
     * Create a simple regular expression
     *
     * Rules:
     *  '*'        matchTypeEntries 0 ou N characters
     *  '?'        matchTypeEntries 1 character
     */
    static String createRegExp(String pattern) {
        int patternLength = pattern.length()
        def sbPattern = new StringBuffer(patternLength * 2)

        for (int i = 0; i < patternLength; i++) {
            char c = pattern.charAt(i)

            if (c == '*') {
                sbPattern.append('.*')
            } else if (c == '?') {
                sbPattern.append('.')
            } else if (c == '.') {
                sbPattern.append('\\.')
            } else {
                sbPattern.append(c)
            }
        }

        return sbPattern.toString()
    }

    // --- ContentSavable --- //
    String getFileName() { 'file.txt' }

    void save(API api, OutputStream os) {
        new NewlineOutputStream(os).withWriter('UTF-8') { Writer w ->
            w.write(textArea.text)
        }
    }
}
