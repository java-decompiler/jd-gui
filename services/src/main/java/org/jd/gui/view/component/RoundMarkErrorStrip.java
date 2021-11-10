/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view.component;

import org.fife.ui.rsyntaxtextarea.DocumentRange;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.parser.Parser;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.TaskTagParser.TaskNotice;
import org.fife.ui.rtextarea.RTextArea;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;

/*
 * 'private' access prohibit all changes ==> Copy "ErrorStrip" to JD-GUI project just to change the marker.
 *
 * JD-GUI uses two workarounds for RSyntaxTextArea:
 * - org.fife.ui.rtextarea.Marker
 * - org.jd.gui.view.component.RoundMarkErrorStrip
 */

/*
 * 08/10/2009
 *
 * ErrorStrip.java - A component that can visually show Parser messages (syntax
 * errors, etc.) in an RSyntaxTextArea.
 *
 * This library is distributed under a modified BSD license.  See the included
 * RSyntaxTextArea.License.txt file for details.
 */

/**
 * A component to sit alongside an {@link RSyntaxTextArea} that displays
 * colored markers for locations of interest (parser errors, marked
 * occurrences, etc.).<p>
 *
 * <code>ErrorStrip</code>s display <code>ParserNotice</code>s from
 * {@link Parser}s.  Currently, the only way to get lines flagged in this
 * component is to register a <code>Parser</code> on an RSyntaxTextArea and
 * return <code>ParserNotice</code>s for each line to display an icon for.
 * The severity of each notice must be at least the threshold set by
 * {@link #setLevelThreshold(ParserNotice.Level)}
 * to be displayed in this error strip.  The default threshold is
 * {@link ParserNotice.Level#WARNING}.<p>
 *
 * An <code>ErrorStrip</code> can be added to a UI like so:
 * <pre>
 * textArea = createTextArea();
 * textArea.addParser(new MyParser(textArea)); // Identifies lines to display
 * scrollPane = new RTextScrollPane(textArea, true);
 * ErrorStrip es = new ErrorStrip(textArea);
 * JPanel temp = new JPanel(new BorderLayout());
 * temp.add(scrollPane);
 * temp.add(es, BorderLayout.LINE_END);
 * </pre>
 *
 * @author Robert Futrell
 * @version 0.5
 */
/*
 * Possible improvements:
 *    1. Handle marked occurrence changes & "mark all" changes separately from
 *       parser changes. For each property change, call a method that removes
 *       the notices being reloaded from the Markers (removing any Markers that
 *       are now "empty").
 */
public class RoundMarkErrorStrip extends JComponent {

    /**
     * The text area.
     */
    private RSyntaxTextArea textArea;

    /**
     * Listens for events in this component.
     */
    private Listener listener;

    /**
     * Whether "marked occurrences" in the text area should be shown in this
     * error strip.
     */
    private boolean showMarkedOccurrences;

    /**
     * Whether markers for "mark all" highlights should be shown in this
     * error strip.
     */
    private boolean showMarkAll;

    /**
     * Mapping of colors to brighter colors.  This is kept to prevent
     * unnecessary creation of the same Colors over and over.
     */
    private Map<Color, Color> brighterColors;

    /**
     * Added for JD-GUI.
     *
     * Mapping of colors to darker colors.  This is kept to prevent
     * unnecessary creation of the same Colors over and over.
     */
    private Map<Color, Color> darkerColors;

    /**
     * Only notices of this severity (or worse) will be displayed in this
     * error strip.
     */
    private ParserNotice.Level levelThreshold;

    /**
     * Whether the caret marker's location should be rendered.
     */
    private boolean followCaret;

    /**
     * The color to use for the caret marker.
     */
    private Color caretMarkerColor;

    /**
     * Where we paint the caret marker.
     */
    private int caretLineY;

    /**
     * The last location of the caret marker.
     */
    private int lastLineY;

    /**
     * The preferred width of this component.
     */
    private static final int PREFERRED_WIDTH = 14;

    private static final String MSG = "org.fife.ui.rsyntaxtextarea.ErrorStrip";
    private static final ResourceBundle msg = ResourceBundle.getBundle(MSG);

    /**
     * Constructor.
     *
     * @param textArea The text area we are examining.
     */
    public RoundMarkErrorStrip(RSyntaxTextArea textArea) {
        this.textArea = textArea;
        listener = new Listener();
        ToolTipManager.sharedInstance().registerComponent(this);
        setLayout(null); // Manually layout Markers as they can overlap
        addMouseListener(listener);
        setShowMarkedOccurrences(true);
        setShowMarkAll(true);
        setLevelThreshold(ParserNotice.Level.WARNING);
        setFollowCaret(true);
        setCaretMarkerColor(new Color(0x96c5fe));
    }


    /**
     * Overridden so we only start listening for parser notices when this
     * component (and presumably the text area) are visible.
     */
    @Override
    public void addNotify() {
        super.addNotify();
        textArea.addCaretListener(listener);
        textArea.addPropertyChangeListener(
                RSyntaxTextArea.PARSER_NOTICES_PROPERTY, listener);
        textArea.addPropertyChangeListener(
                RSyntaxTextArea.MARK_OCCURRENCES_PROPERTY, listener);
        textArea.addPropertyChangeListener(
                RSyntaxTextArea.MARKED_OCCURRENCES_CHANGED_PROPERTY, listener);
        textArea.addPropertyChangeListener(
                RSyntaxTextArea.MARK_ALL_OCCURRENCES_CHANGED_PROPERTY, listener);
        refreshMarkers();
    }


    /**
     * Manually manages layout since this component uses no layout manager.
     */
    @Override
    public void doLayout() {
        for (int i=0; i<getComponentCount(); i++) {
            Marker m = (Marker)getComponent(i);
            m.updateLocation();
        }
        listener.caretUpdate(null); // Force recalculation of caret line pos
    }


    /**
     * Returns a "brighter" color.
     *
     * @param c The color.
     * @return A brighter color.
     */
    private Color getBrighterColor(Color c) {
        if (brighterColors==null) {
            brighterColors = new HashMap<Color, Color>(5); // Usually small
        }
        Color brighter = brighterColors.get(c);
        if (brighter==null) {
            // Don't use c.brighter() as it doesn't work well for blue, and
            // also doesn't return something brighter "enough."
            int r = possiblyBrighter(c.getRed());
            int g = possiblyBrighter(c.getGreen());
            int b = possiblyBrighter(c.getBlue());
            brighter = new Color(r, g, b);
            brighterColors.put(c, brighter);
        }
        return brighter;
    }


    /**
     * Added for JD-GUI.
     *
     * Returns a "brighter" color.
     *
     * @param c The color.
     * @return A brighter color.
     */
    private Color getDarkerColor(Color c) {
        if (darkerColors==null) {
            darkerColors = new HashMap<Color, Color>(5); // Usually small
        }
        Color darker = darkerColors.get(c);
        if (darker==null) {
            // Don't use c.brighter() as it doesn't work well for blue, and
            // also doesn't return something brighter "enough."
            int r = possiblyDarker(c.getRed());
            int g = possiblyDarker(c.getGreen());
            int b = possiblyDarker(c.getBlue());
            darker = new Color(r, g, b);
            darkerColors.put(c, darker);
        }
        return darker;
    }


    /**
     * returns the color to use when painting the caret marker.
     *
     * @return The caret marker color.
     * @see #setCaretMarkerColor(Color)
     */
    public Color getCaretMarkerColor() {
        return caretMarkerColor;
    }


    /**
     * Returns whether the caret's position should be drawn.
     *
     * @return Whether the caret's position should be drawn.
     * @see #setFollowCaret(boolean)
     */
    public boolean getFollowCaret() {
        return followCaret;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getPreferredSize() {
        int height = textArea.getPreferredScrollableViewportSize().height;
        return new Dimension(PREFERRED_WIDTH, height);
    }


    /**
     * Returns the minimum severity a parser notice must be for it to be
     * displayed in this error strip.  This will be one of the constants
     * defined in the <code>ParserNotice</code> class.
     *
     * @return The minimum severity.
     * @see #setLevelThreshold(ParserNotice.Level)
     */
    public ParserNotice.Level getLevelThreshold() {
        return levelThreshold;
    }


    /**
     * Returns whether "mark all" highlights are shown in this error strip.
     *
     * @return Whether markers are shown for "mark all" highlights.
     * @see #setShowMarkAll(boolean)
     */
    public boolean getShowMarkAll() {
        return showMarkAll;
    }


    /**
     * Returns whether marked occurrences are shown in this error strip.
     *
     * @return Whether marked occurrences are shown.
     * @see #setShowMarkedOccurrences(boolean)
     */
    public boolean getShowMarkedOccurrences() {
        return showMarkedOccurrences;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getToolTipText(MouseEvent e) {
        String text = null;
        int line = yToLine(e.getY());
        if (line>-1) {
            text = msg.getString("Line");
            text = MessageFormat.format(text, Integer.valueOf(line+1));
        }
        return text;
    }


    /**
     * Returns the y-offset in this component corresponding to a line in the
     * text component.
     *
     * @param line The line.
     * @return The y-offset.
     * @see #yToLine(int)
     */
    private int lineToY(int line) {
        int h = textArea.getVisibleRect().height;
        float lineCount = textArea.getLineCount();
        return (int)(((line-1)/(lineCount-1)) * h) - 2;
    }


    /**
     * Overridden to (possibly) draw the caret's position.
     *
     * @param g The graphics context.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (caretLineY>-1) {
            g.setColor(getCaretMarkerColor());
            g.fillRect(0, caretLineY, getWidth(), 2);
        }
    }


    /**
     * Returns a possibly brighter component for a color.
     *
     * @param i An RGB component for a color (0-255).
     * @return A possibly brighter value for the component.
     */
    private static final int possiblyBrighter(int i) {
        if (i<255) {
            i += (int)((255-i)*0.6f);
        }
        return i;
    }


    /**
     * Returns a possibly darker component for a color.
     *
     * @param i An RGB component for a color (0-255).
     * @return A possibly brighter value for the component.
     */
    private static final int possiblyDarker(int i) {
        return i -= (int)(i*0.4f);
    }


    /**
     * Refreshes the markers displayed in this error strip.
     */
    private void refreshMarkers() {

        removeAll(); // listener is removed in Marker.removeNotify()
        Map<Integer, Marker> markerMap = new HashMap<Integer, Marker>();

        List<ParserNotice> notices = textArea.getParserNotices();
        for (ParserNotice notice : notices) {
            if (notice.getLevel().isEqualToOrWorseThan(levelThreshold) ||
                    (notice instanceof TaskNotice)) {
                Integer key = Integer.valueOf(notice.getLine());
                Marker m = markerMap.get(key);
                if (m==null) {
                    m = new Marker(notice);
                    m.addMouseListener(listener);
                    markerMap.put(key, m);
                    add(m);
                }
                else {
                    m.addNotice(notice);
                }
            }
        }

        if (getShowMarkedOccurrences() && textArea.getMarkOccurrences()) {
            List<DocumentRange> occurrences = textArea.getMarkedOccurrences();
            addMarkersForRanges(occurrences, markerMap, textArea.getMarkOccurrencesColor());
        }

        if (getShowMarkAll() /*&& textArea.getMarkAll()*/) {
            Color markAllColor = textArea.getMarkAllHighlightColor();
            List<DocumentRange> ranges = textArea.getMarkAllHighlightRanges();
            addMarkersForRanges(ranges, markerMap, markAllColor);
        }

        revalidate();
        repaint();

    }


    /**
     * Adds markers for a list of ranges in the document.
     *
     * @param ranges The list of ranges in the document.
     * @param markerMap A mapping from line number to <code>Marker</code>.
     * @param color The color to use for the markers.
     */
    private void addMarkersForRanges(List<DocumentRange> ranges,
                                     Map<Integer, Marker> markerMap, Color color) {
        for (DocumentRange range : ranges) {
            int line = 0;
            try {
                line = textArea.getLineOfOffset(range.getStartOffset());
            } catch (BadLocationException ble) { // Never happens
                continue;
            }
            ParserNotice notice = new MarkedOccurrenceNotice(range, color);
            Integer key = Integer.valueOf(line);
            Marker m = markerMap.get(key);
            if (m==null) {
                m = new Marker(notice);
                m.addMouseListener(listener);
                markerMap.put(key, m);
                add(m);
            }
            else {
                if (!m.containsMarkedOccurence()) {
                    m.addNotice(notice);
                }
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void removeNotify() {
        super.removeNotify();
        textArea.removeCaretListener(listener);
        textArea.removePropertyChangeListener(
                RSyntaxTextArea.PARSER_NOTICES_PROPERTY, listener);
        textArea.removePropertyChangeListener(
                RSyntaxTextArea.MARK_OCCURRENCES_PROPERTY, listener);
        textArea.removePropertyChangeListener(
                RSyntaxTextArea.MARKED_OCCURRENCES_CHANGED_PROPERTY, listener);
        textArea.removePropertyChangeListener(
                RSyntaxTextArea.MARK_ALL_OCCURRENCES_CHANGED_PROPERTY, listener);
    }


    /**
     * Sets the color to use when painting the caret marker.
     *
     * @param color The new caret marker color.
     * @see #getCaretMarkerColor()
     */
    public void setCaretMarkerColor(Color color) {
        if (color!=null) {
            caretMarkerColor = color;
            listener.caretUpdate(null); // Force repaint
        }
    }


    /**
     * Toggles whether the caret's current location should be drawn.
     *
     * @param follow Whether the caret's current location should be followed.
     * @see #getFollowCaret()
     */
    public void setFollowCaret(boolean follow) {
        if (followCaret!=follow) {
            if (followCaret) {
                repaint(0,caretLineY, getWidth(),2); // Erase
            }
            caretLineY = -1;
            lastLineY = -1;
            followCaret = follow;
            listener.caretUpdate(null); // Possibly repaint
        }
    }


    /**
     * Sets the minimum severity a parser notice must be for it to be displayed
     * in this error strip.  This should be one of the constants defined in
     * the <code>ParserNotice</code> class.  The default value is
     * {@link ParserNotice.Level#WARNING}.
     *
     * @param level The new severity threshold.
     * @see #getLevelThreshold()
     * @see ParserNotice
     */
    public void setLevelThreshold(ParserNotice.Level level) {
        levelThreshold = level;
        if (isDisplayable()) {
            refreshMarkers();
        }
    }


    /**
     * Sets whether "mark all" highlights are shown in this error strip.
     *
     * @param show Whether to show markers for "mark all" highlights.
     * @see #getShowMarkAll()
     */
    public void setShowMarkAll(boolean show) {
        if (show!=showMarkAll) {
            showMarkAll = show;
            if (isDisplayable()) { // Skip this when we're first created
                refreshMarkers();
            }
        }
    }


    /**
     * Sets whether marked occurrences are shown in this error strip.
     *
     * @param show Whether to show marked occurrences.
     * @see #getShowMarkedOccurrences()
     */
    public void setShowMarkedOccurrences(boolean show) {
        if (show!=showMarkedOccurrences) {
            showMarkedOccurrences = show;
            if (isDisplayable()) { // Skip this when we're first created
                refreshMarkers();
            }
        }
    }


    /**
     * Returns the line in the text area corresponding to a y-offset in this
     * component.
     *
     * @param y The y-offset.
     * @return The line.
     * @see #lineToY(int)
     */
    private final int yToLine(int y) {
        int line = -1;
        int h = textArea.getVisibleRect().height;
        if (y<h) {
            float at = y/(float)h;
            line = Math.round((textArea.getLineCount()-1)*at);
        }
        return line;
    }


    /**
     * Listens for events in the error strip and its markers.
     */
    private class Listener extends MouseAdapter
            implements PropertyChangeListener, CaretListener {

        private Rectangle visibleRect = new Rectangle();

        public void caretUpdate(CaretEvent e) {
            if (getFollowCaret()) {
                int line = textArea.getCaretLineNumber();
                float percent = line / (float)(textArea.getLineCount()-1);
                textArea.computeVisibleRect(visibleRect);
                caretLineY = (int)(visibleRect.height*percent);
                if (caretLineY!=lastLineY) {
                    repaint(0,lastLineY, getWidth(), 2); // Erase old position
                    repaint(0,caretLineY, getWidth(), 2);
                    lastLineY = caretLineY;
                }
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {

            Component source = (Component)e.getSource();
            if (source instanceof Marker) {
                ((Marker)source).mouseClicked(e);
                return;
            }

            int line = yToLine(e.getY());
            if (line>-1) {
                try {
                    int offs = textArea.getLineStartOffset(line);
                    textArea.setCaretPosition(offs);
                } catch (BadLocationException ble) { // Never happens
                    UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                }
            }

        }

        public void propertyChange(PropertyChangeEvent e) {

            String propName = e.getPropertyName();

            // If they change whether marked occurrences are visible in editor
            if (RSyntaxTextArea.MARK_OCCURRENCES_PROPERTY.equals(propName)) {
                if (getShowMarkedOccurrences()) {
                    refreshMarkers();
                }
            }

            // If parser notices changed.
            // TODO: Don't update "mark all/occurrences" markers.
            else if (RSyntaxTextArea.PARSER_NOTICES_PROPERTY.equals(propName)) {
                refreshMarkers();
            }

            // If marked occurrences changed.
            // TODO: Only update "mark occurrences" markers, not all of them.
            else if (RSyntaxTextArea.MARKED_OCCURRENCES_CHANGED_PROPERTY.
                    equals(propName)) {
                if (getShowMarkedOccurrences()) {
                    refreshMarkers();
                }
            }

            // If "mark all" occurrences changed.
            // TODO: Only update "mark all" markers, not all of them.
            else if (RTextArea.MARK_ALL_OCCURRENCES_CHANGED_PROPERTY.
                    equals(propName)) {
                if (getShowMarkAll()) {
                    refreshMarkers();
                }
            }

        }

    }


    /**
     * A notice that wraps a "marked occurrence."
     */
    private class MarkedOccurrenceNotice implements ParserNotice {

        private DocumentRange range;
        private Color color;

        public MarkedOccurrenceNotice(DocumentRange range, Color color) {
            this.range = range;
            this.color = color;
        }

        public int compareTo(ParserNotice other) {
            return 0; // Value doesn't matter
        }

        public boolean containsPosition(int pos) {
            return pos>=range.getStartOffset() && pos<range.getEndOffset();
        }

        @Override
        public boolean equals(Object o) {
            // FindBugs - Define equals() when defining compareTo()
            if (!(o instanceof ParserNotice)) {
                return false;
            }
            return compareTo((ParserNotice)o)==0;
        }

        public Color getColor() {
            return color;
        }

        /**
         * {@inheritDoc}
         */
        public boolean getKnowsOffsetAndLength() {
            return true;
        }

        public int getLength() {
            return range.getEndOffset() - range.getStartOffset();
        }

        public Level getLevel() {
            return Level.INFO; // Won't matter
        }

        public int getLine() {
            try {
                return textArea.getLineOfOffset(range.getStartOffset())+1;
            } catch (BadLocationException ble) {
                return 0;
            }
        }

        public String getMessage() {
            String text = null;
            try {
                String word = textArea.getText(range.getStartOffset(),
                        getLength());
                text = msg.getString("OccurrenceOf");
                text = MessageFormat.format(text, word);
            } catch (BadLocationException ble) {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
            }
            return text;
        }

        public int getOffset() {
            return range.getStartOffset();
        }

        public Parser getParser() {
            return null;
        }

        public boolean getShowInEditor() {
            return false; // Value doesn't matter
        }

        public String getToolTipText() {
            return null;
        }

        @Override
        public int hashCode() { // FindBugs, since we override equals()
            return 0; // Value doesn't matter for us.
        }

    }


    /**
     * A "marker" in this error strip, representing one or more notices.
     */
    private class Marker extends JComponent {

        private List<ParserNotice> notices;

        public Marker(ParserNotice notice) {
            notices = new ArrayList<ParserNotice>(1); // Usually just 1
            addNotice(notice);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setSize(getPreferredSize());
            ToolTipManager.sharedInstance().registerComponent(this);
        }

        public void addNotice(ParserNotice notice) {
            notices.add(notice);
        }

        public boolean containsMarkedOccurence() {
            boolean result = false;
            for (int i=0; i<notices.size(); i++) {
                if (notices.get(i) instanceof MarkedOccurrenceNotice) {
                    result = true;
                    break;
                }
            }
            return result;
        }

        public Color getColor() {
            // Return the color for the highest-level parser.
            Color c = null;
            int lowestLevel = Integer.MAX_VALUE; // ERROR is 0
            for (ParserNotice notice : notices) {
                if (notice.getLevel().getNumericValue()<lowestLevel) {
                    lowestLevel = notice.getLevel().getNumericValue();
                    c = notice.getColor();
                }
            }
            return c;
        }

        @Override
        public Dimension getPreferredSize() {
            int w = PREFERRED_WIDTH - 4; // 2-pixel empty border
            return new Dimension(w, 5);
        }

        @Override
        public String getToolTipText() {

            String text = null;

            if (notices.size()==1) {
                text = notices.get(0).getMessage();
            }
            else { // > 1
                StringBuilder sb = new StringBuilder("<html>");
                sb.append(msg.getString("MultipleMarkers"));
                sb.append("<br>");
                for (int i=0; i<notices.size(); i++) {
                    ParserNotice pn = notices.get(i);
                    sb.append("&nbsp;&nbsp;&nbsp;- ");
                    sb.append(pn.getMessage());
                    sb.append("<br>");
                }
                text = sb.toString();
            }

            return text;

        }

        protected void mouseClicked(MouseEvent e) {
            ParserNotice pn = notices.get(0);
            int offs = pn.getOffset();
            int len = pn.getLength();
            if (offs>-1 && len>-1) { // These values are optional
                textArea.setSelectionStart(offs);
                textArea.setSelectionEnd(offs+len);
            }
            else {
                int line = pn.getLine();
                try {
                    offs = textArea.getLineStartOffset(line);
                    textArea.setCaretPosition(offs);
                } catch (BadLocationException ble) { // Never happens
                    UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {

            // TODO: Give "priorities" and always pick color of a notice with
            // highest priority (e.g. parsing errors will usually be red).

            Color color = getColor();
            if (color==null) {
                color = Color.GRAY;
            }

            Color brighterColor = getBrighterColor(color);
            Color darkerColor = getDarkerColor(color);

            int w = getWidth();
            int h = getHeight();

            // Draw background
            g.setColor(color);
            g.fillRect(0,0, w,h);

            // Draw border
            w--;
            h--;

            g.setColor(darkerColor);
            g.drawLine(w,0, w,h);
            g.drawRect(0,h, w,h);

            g.setColor(brighterColor);
            g.drawLine(0,0, w,0);
            g.drawRect(0,0, 0,h);
        }

        @Override
        public void removeNotify() {
            super.removeNotify();
            ToolTipManager.sharedInstance().unregisterComponent(this);
            removeMouseListener(listener);
        }

        public void updateLocation() {
            int line = notices.get(0).getLine();
            int y = lineToY(line);
            setLocation(2, y);
        }

    }


}