/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view.component;

import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rsyntaxtextarea.folding.FoldManager;
import org.fife.ui.rtextarea.*;
import org.jd.gui.api.feature.ContentSearchable;
import org.jd.gui.api.feature.LineNumberNavigable;
import org.jd.gui.api.feature.PreferencesChangeListener;
import org.jd.gui.api.feature.UriOpenable;
import org.jd.gui.util.exception.ExceptionUtil;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class AbstractTextPage extends JPanel implements LineNumberNavigable, ContentSearchable, UriOpenable, PreferencesChangeListener {
    protected static final String FONT_SIZE_KEY = "ViewerPreferences.fontSize";

    protected static final ImageIcon COLLAPSED_ICON = new ImageIcon(AbstractTextPage.class.getClassLoader().getResource("org/jd/gui/images/plus.png"));
    protected static final ImageIcon EXPANDED_ICON = new ImageIcon(AbstractTextPage.class.getClassLoader().getResource("org/jd/gui/images/minus.png"));

    protected static final Color DOUBLE_CLICK_HIGHLIGHT_COLOR = new Color(0x66ff66);
    protected static final Color SEARCH_HIGHLIGHT_COLOR = new Color(0xffff66);
    protected static final Color SELECT_HIGHLIGHT_COLOR = new Color(0xF49810);

    protected static final RSyntaxTextAreaEditorKit.DecreaseFontSizeAction DECREASE_FONT_SIZE_ACTION = new RSyntaxTextAreaEditorKit.DecreaseFontSizeAction();
    protected static final RSyntaxTextAreaEditorKit.IncreaseFontSizeAction INCREASE_FONT_SIZE_ACTION = new RSyntaxTextAreaEditorKit.IncreaseFontSizeAction();

    protected RSyntaxTextArea textArea;
    protected RTextScrollPane scrollPane;

    protected Map<String, String> preferences;

    public AbstractTextPage() {
        super(new BorderLayout());

        textArea = newSyntaxTextArea();
        textArea.setSyntaxEditingStyle(getSyntaxStyle());
        textArea.setCodeFoldingEnabled(true);
        textArea.setAntiAliasingEnabled(true);
        textArea.setCaretPosition(0);
        textArea.setEditable(false);
        textArea.setDropTarget(null);
        textArea.setPopupMenu(null);
        textArea.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    textArea.setMarkAllHighlightColor(DOUBLE_CLICK_HIGHLIGHT_COLOR);
                    SearchEngine.markAll(textArea, newSearchContext(textArea.getSelectedText(), true, true, true, false));
                }
            }
        });

        KeyStroke ctrlA = KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        KeyStroke ctrlC = KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        KeyStroke ctrlV = KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        InputMap inputMap = textArea.getInputMap();
        inputMap.put(ctrlA, "none");
        inputMap.put(ctrlC, "none");
        inputMap.put(ctrlV, "none");

        try {
            Theme theme = Theme.load(getClass().getClassLoader().getResourceAsStream("rsyntaxtextarea/themes/eclipse.xml"));
            theme.apply(textArea);
        } catch (IOException e) {
            assert ExceptionUtil.printStackTrace(e);
        }

        scrollPane = new RTextScrollPane(textArea);
        scrollPane.setFoldIndicatorEnabled(true);
        scrollPane.setFont(textArea.getFont());

        final MouseWheelListener[] mouseWheelListeners = scrollPane.getMouseWheelListeners();

        // Remove default listeners
        for (MouseWheelListener listener : mouseWheelListeners) {
            scrollPane.removeMouseWheelListener(listener);
        }

        scrollPane.addMouseWheelListener(e -> {
            if ((e.getModifiers() & (Event.META_MASK|Event.CTRL_MASK)) != 0) {
                int x = e.getX() + scrollPane.getX() - textArea.getX();
                int y = e.getY() + scrollPane.getY() - textArea.getY();
                int offset = textArea.viewToModel(new Point(x, y));

                // Update font size
                if (e.getWheelRotation() > 0) {
                    DECREASE_FONT_SIZE_ACTION.actionPerformedImpl(null, textArea);
                } else {
                    INCREASE_FONT_SIZE_ACTION.actionPerformedImpl(null, textArea);
                }

                // Save preferences
                if (preferences != null) {
                    preferences.put(FONT_SIZE_KEY, String.valueOf(textArea.getFont().getSize()));
                }

                try {
                    Rectangle newRectangle = textArea.modelToView(offset);
                    int newY = newRectangle.y + (newRectangle.height >> 1);

                    // Scroll
                    Point viewPosition = scrollPane.getViewport().getViewPosition();
                    viewPosition.y = Math.max(viewPosition.y +newY - y, 0);
                    scrollPane.getViewport().setViewPosition(viewPosition);
                } catch (BadLocationException ee) {
                    assert ExceptionUtil.printStackTrace(ee);
                }
            } else {
                // Call default listeners
                for (MouseWheelListener listener : mouseWheelListeners) {
                    listener.mouseWheelMoved(e);
                }
            }
        });

        Gutter gutter = scrollPane.getGutter();
        gutter.setFoldIcons(COLLAPSED_ICON, EXPANDED_ICON);
        gutter.setFoldIndicatorForeground(gutter.getBorderColor());

        add(scrollPane, BorderLayout.CENTER);
        add(new RoundMarkErrorStrip(textArea), BorderLayout.LINE_END);
    }

    protected RSyntaxTextArea newSyntaxTextArea() { return new RSyntaxTextArea(); }

    public String getText() { return textArea.getText(); }

    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    public void setText(String text) {
        textArea.setText(text);
        textArea.setCaretPosition(0);
    }

    public String getSyntaxStyle() { return SyntaxConstants.SYNTAX_STYLE_NONE; }

    /**
     * @see org.fife.ui.rsyntaxtextarea.RSyntaxUtilities#selectAndPossiblyCenter
     * Force center and do not select
     */
    public void setCaretPositionAndCenter(DocumentRange range) {
        final int start = range.getStartOffset();
        final int end = range.getEndOffset();
        boolean foldsExpanded = false;
        FoldManager fm = textArea.getFoldManager();

        if (fm.isCodeFoldingSupportedAndEnabled()) {
            foldsExpanded = fm.ensureOffsetNotInClosedFold(start);
            foldsExpanded |= fm.ensureOffsetNotInClosedFold(end);
        }

        if (!foldsExpanded) {
            try {
                Rectangle rec = textArea.modelToView(start);

                if (rec != null) {
                    // Visible
                    setCaretPositionAndCenter(start, end, rec);
                } else {
                    // Not visible yet
                    SwingUtilities.invokeLater(() -> {
                        try {
                            Rectangle r = textArea.modelToView(start);
                            if (r != null) {
                                setCaretPositionAndCenter(start, end, r);
                            }
                        } catch (BadLocationException e) {
                            assert ExceptionUtil.printStackTrace(e);
                        }
                    });
                }
            } catch (BadLocationException e) {
                assert ExceptionUtil.printStackTrace(e);
            }
        }
    }

    protected void setCaretPositionAndCenter(int start, int end, Rectangle r) {
        if (end != start) {
            try {
                r = r.union(textArea.modelToView(end));
            } catch (BadLocationException e) {
                assert ExceptionUtil.printStackTrace(e);
            }
        }

        Rectangle visible = textArea.getVisibleRect();

        // visible.x = r.x - (visible.width - r.width) / 2;
        visible.y = r.y - (visible.height - r.height) / 2;

        Rectangle bounds = textArea.getBounds();
        Insets i = textArea.getInsets();
        //bounds.x = i.left;
        bounds.y = i.top;
        //bounds.width -= i.left + i.right;
        bounds.height -= i.top + i.bottom;

        //if (visible.x < bounds.x) {
        //    visible.x = bounds.x;
        //}
        //if (visible.x + visible.width > bounds.x + bounds.width) {
        //    visible.x = bounds.x + bounds.width - visible.width;
        //}
        if (visible.y < bounds.y) {
            visible.y = bounds.y;
        }
        if (visible.y + visible.height > bounds.y + bounds.height) {
            visible.y = bounds.y + bounds.height - visible.height;
        }

        textArea.scrollRectToVisible(visible);
        textArea.setCaretPosition(start);
    }

    // --- LineNumberNavigable --- //
    public int getMaximumLineNumber() {
        try {
            return textArea.getLineOfOffset(textArea.getDocument().getLength()) + 1;
        } catch (BadLocationException e) {
            assert ExceptionUtil.printStackTrace(e);
            return 0;
        }
    }

    public void goToLineNumber(int lineNumber) {
        try {
            textArea.setCaretPosition(textArea.getLineStartOffset(lineNumber-1));
        } catch (BadLocationException e) {
            assert ExceptionUtil.printStackTrace(e);
        }
    }

    public boolean checkLineNumber(int lineNumber) { return true; }

    // --- ContentSearchable --- //
    public boolean highlightText(String text, boolean caseSensitive) {
        if (text.length() > 1) {
            textArea.setMarkAllHighlightColor(SEARCH_HIGHLIGHT_COLOR);
            textArea.setCaretPosition(textArea.getSelectionStart());

            SearchContext context = newSearchContext(text, caseSensitive, false, true, false);
            SearchResult result = SearchEngine.find(textArea, context);

            if (!result.wasFound()) {
                textArea.setCaretPosition(0);
                result = SearchEngine.find(textArea, context);
            }

            return result.wasFound();
        } else {
            return true;
        }
    }

    public void findNext(String text, boolean caseSensitive) {
        if (text.length() > 1) {
            textArea.setMarkAllHighlightColor(SEARCH_HIGHLIGHT_COLOR);

            SearchContext context = newSearchContext(text, caseSensitive, false, true, false);
            SearchResult result = SearchEngine.find(textArea, context);

            if (!result.wasFound()) {
                textArea.setCaretPosition(0);
                SearchEngine.find(textArea, context);
            }
        }
    }

    public void findPrevious(String text, boolean caseSensitive) {
        if (text.length() > 1) {
            textArea.setMarkAllHighlightColor(SEARCH_HIGHLIGHT_COLOR);

            SearchContext context = newSearchContext(text, caseSensitive, false, false, false);
            SearchResult result = SearchEngine.find(textArea, context);

            if (!result.wasFound()) {
                textArea.setCaretPosition(textArea.getDocument().getLength());
                SearchEngine.find(textArea, context);
            }
        }
    }

    protected SearchContext newSearchContext(String searchFor, boolean matchCase, boolean wholeWord, boolean searchForward, boolean regexp) {
        SearchContext context = new SearchContext(searchFor, matchCase);
        context.setMarkAll(true);
        context.setWholeWord(wholeWord);
        context.setSearchForward(searchForward);
        context.setRegularExpression(regexp);
        return context;
    }

    // --- UriOpenable --- //
    public boolean openUri(URI uri) {
        String query = uri.getQuery();

        if (query != null) {
            Map<String, String> parameters = parseQuery(query);

            if (parameters.containsKey("lineNumber")) {
                String lineNumber = parameters.get("lineNumber");

                try {
                    goToLineNumber(Integer.parseInt(lineNumber));
                    return true;
                } catch (NumberFormatException e) {
                    assert ExceptionUtil.printStackTrace(e);
                }
            } else if (parameters.containsKey("position")) {
                String position = parameters.get("position");

                try {
                    int pos = Integer.parseInt(position);
                    if (textArea.getDocument().getLength() > pos) {
                        setCaretPositionAndCenter(new DocumentRange(pos, pos));
                        return true;
                    }
                } catch (NumberFormatException e) {
                    assert ExceptionUtil.printStackTrace(e);
                }
            } else if (parameters.containsKey("highlightFlags")) {
                String highlightFlags = parameters.get("highlightFlags");

                if ((highlightFlags.indexOf('s') != -1) && parameters.containsKey("highlightPattern")) {
                    textArea.setMarkAllHighlightColor(SELECT_HIGHLIGHT_COLOR);
                    textArea.setCaretPosition(0);

                    // Highlight all
                    String searchFor = createRegExp(parameters.get("highlightPattern"));
                    SearchContext context =  newSearchContext(searchFor, true, false, true, true);
                    SearchResult result = SearchEngine.find(textArea, context);

                    if (result.getMatchRange() != null) {
                        textArea.setCaretPosition(result.getMatchRange().getStartOffset());
                    }

                    return true;
                }
            }
        }

        return false;
    }

    protected Map<String, String> parseQuery(String query) {
        HashMap<String, String> parameters = new HashMap<>();

        // Parse parameters
        try {
            for (String param : query.split("&")) {
                int index = param.indexOf('=');

                if (index == -1) {
                    parameters.put(URLDecoder.decode(param, "UTF-8"), "");
                } else {
                    String key = param.substring(0, index);
                    String value = param.substring(index + 1);
                    parameters.put(URLDecoder.decode(key, "UTF-8"), URLDecoder.decode(value, "UTF-8"));
                }
            }
        } catch (UnsupportedEncodingException e) {
            assert ExceptionUtil.printStackTrace(e);
        }

        return parameters;
    }

    /**
     * Create a simple regular expression
     *
     * Rules:
     *  '*'        matchTypeEntries 0 ou N characters
     *  '?'        matchTypeEntries 1 character
     */
    public static String createRegExp(String pattern) {
        int patternLength = pattern.length();
        StringBuilder sbPattern = new StringBuilder(patternLength * 2);

        for (int i = 0; i < patternLength; i++) {
            char c = pattern.charAt(i);

            if (c == '*') {
                sbPattern.append(".*");
            } else if (c == '?') {
                sbPattern.append('.');
            } else if (c == '.') {
                sbPattern.append("\\.");
            } else {
                sbPattern.append(c);
            }
        }

        return sbPattern.toString();
    }

    // --- PreferencesChangeListener --- //
    public void preferencesChanged(Map<String, String> preferences) {
        String fontSize = preferences.get(FONT_SIZE_KEY);

        if (fontSize != null) {
            try {
                textArea.setFont(textArea.getFont().deriveFont(Float.parseFloat(fontSize)));
            } catch (Exception e) {
                assert ExceptionUtil.printStackTrace(e);
            }
        }

        this.preferences = preferences;
    }
}
