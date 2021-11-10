/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view.component;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaEditorKit;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaUI;
import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;
import org.fife.ui.rsyntaxtextarea.folding.Fold;
import org.fife.ui.rsyntaxtextarea.folding.FoldManager;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.LineNumberList;
import org.fife.ui.rtextarea.RTextArea;
import org.fife.ui.rtextarea.RTextAreaUI;

import javax.swing.*;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.View;
import java.awt.*;
import java.util.Arrays;
import java.util.Map;

public abstract class CustomLineNumbersPage extends HyperlinkPage {
    protected Color errorForeground = Color.RED;
    protected boolean showMisalignment = true;

    public void setErrorForeground(Color color) {
        errorForeground = color;
    }

    public void setShowMisalignment(boolean b) {
        showMisalignment = b;
    }

    /**
     * Map[textarea line number] = original line number
     */
    protected int[] lineNumberMap = null;
    protected int maxLineNumber = 0;

    protected void setMaxLineNumber(int maxLineNumber) {
        if (maxLineNumber > 0) {
            if (lineNumberMap == null) {
                lineNumberMap = new int[maxLineNumber+1];
            } else if (lineNumberMap.length <= maxLineNumber) {
                int[] tmp = new int[maxLineNumber+1];
                System.arraycopy(lineNumberMap, 0, tmp, 0, lineNumberMap.length);
                lineNumberMap = tmp;
            }

            this.maxLineNumber = maxLineNumber;
        }
    }

    protected void initLineNumbers() {
        String text = getText();
        int len = text.length();

        if (len == 0) {
            setMaxLineNumber(0);
        } else {
            int mln = len - text.replace("\n", "").length();

            if (text.charAt(len-1) != '\n') {
                mln++;
            }

            setMaxLineNumber(mln);

            for (int i=1; i<=maxLineNumber; i++) {
                lineNumberMap[i] = i;
            }
        }
    }

    protected void setLineNumber(int textAreaLineNumber, int originalLineNumber) {
        if (originalLineNumber > 0) {
            setMaxLineNumber(textAreaLineNumber);
            lineNumberMap[textAreaLineNumber] = originalLineNumber;
        }
    }

    protected void clearLineNumbers() {
        if (lineNumberMap != null) {
            Arrays.fill(lineNumberMap, 0);
        }
    }

    protected int getMaximumSourceLineNumber() { return maxLineNumber; }

    protected int getTextAreaLineNumber(int originalLineNumber) {
        int textAreaLineNumber = 1;
        int greatestLowerSourceLineNumber = 0;
        int i = lineNumberMap.length;

        while (i-- > 0) {
            int sln = lineNumberMap[i];
            if (sln <= originalLineNumber) {
                if (greatestLowerSourceLineNumber < sln) {
                    greatestLowerSourceLineNumber = sln;
                    textAreaLineNumber = i;
                }
            }
        }

        return textAreaLineNumber;
    }

    @Override protected RSyntaxTextArea newSyntaxTextArea() { return new SourceSyntaxTextArea(); }

    public class SourceSyntaxTextArea extends HyperlinkSyntaxTextArea {
        @Override protected RTextAreaUI createRTextAreaUI() { return new SourceSyntaxTextAreaUI(this); }
    }

    /**
     * A lot of code to replace the default LineNumberList...
     */
    public class SourceSyntaxTextAreaUI extends RSyntaxTextAreaUI {
        public SourceSyntaxTextAreaUI(JComponent rSyntaxTextArea) { super(rSyntaxTextArea); }
        @Override public EditorKit getEditorKit(JTextComponent tc) { return new SourceSyntaxTextAreaEditorKit(); }
        @Override public Rectangle getVisibleEditorRect() { return super.getVisibleEditorRect(); }
    }

    public class SourceSyntaxTextAreaEditorKit extends RSyntaxTextAreaEditorKit {
        @Override public LineNumberList createLineNumberList(RTextArea textArea) { return new SourceLineNumberList(textArea); }
    }

    /**
     * Why 'LineNumberList' is so unexpandable ? Too many private fields & methods and too many package scope.
     */
    public class SourceLineNumberList extends LineNumberList {
        protected RTextArea rTextArea;
        protected Map<?,?> aaHints;
        protected Rectangle visibleRect;
        protected Insets textAreaInsets;
        protected Dimension preferredSize;

        public SourceLineNumberList(RTextArea textArea) {
            super(textArea, null);
            this.rTextArea = textArea;
        }

        @Override
        protected void init() {
            super.init();
            visibleRect = new Rectangle();
            aaHints = RSyntaxUtilities.getDesktopAntiAliasHints();
            textAreaInsets = null;
        }

        /**
         * @see org.fife.ui.rtextarea.LineNumberList#paintComponent(java.awt.Graphics)
         */
        @Override
        protected void paintComponent(Graphics g) {
            visibleRect = g.getClipBounds(visibleRect);

            if (visibleRect == null) {
                visibleRect = getVisibleRect();
            }
            if (visibleRect == null) {
                return;
            }

            int cellWidth = getPreferredSize().width;
            int cellHeight = rTextArea.getLineHeight();
            int ascent = rTextArea.getMaxAscent();
            FoldManager fm = ((RSyntaxTextArea)rTextArea).getFoldManager();
            int RHS_BORDER_WIDTH = getRhsBorderWidth();
            FontMetrics metrics = g.getFontMetrics();
            int rhs = getWidth() - RHS_BORDER_WIDTH;

            if (getParent() instanceof Gutter) { // Should always be true
                g.setColor(getParent().getBackground());
            } else {
                g.setColor(getBackground());
            }

            g.fillRect(0, visibleRect.y, cellWidth, visibleRect.height);
            g.setFont(getFont());

            if (aaHints != null) {
                ((Graphics2D)g).addRenderingHints(aaHints);
            }

            if (rTextArea.getLineWrap()) {
                SourceSyntaxTextAreaUI ui = (SourceSyntaxTextAreaUI)rTextArea.getUI();
                View v = ui.getRootView(rTextArea).getView(0);
                Element root = rTextArea.getDocument().getDefaultRootElement();
                int lineCount = root.getElementCount();
                int topPosition = rTextArea.viewToModel(visibleRect.getLocation());
                int topLine = root.getElementIndex(topPosition);
                Rectangle visibleEditorRect = ui.getVisibleEditorRect();
                Rectangle r = LineNumberList.getChildViewBounds(v, topLine, visibleEditorRect);
                int y = r.y;

                int visibleBottom =  visibleRect.y + visibleRect.height;

                // Keep painting lines until our y-coordinate is past the visible
                // end of the text area.

                while (y < visibleBottom) {
                    r = getChildViewBounds(v, topLine, visibleEditorRect);

                    // Paint the line number.
                    paintLineNumber(g, metrics, rhs, y+ascent, topLine + 1);

                    // The next possible y-coordinate is just after the last line
                    // painted.
                    y += r.height;

                    // Update topLine (we're actually using it for our "current line"
                    // variable now).
                    if (fm != null) {
                        Fold fold = fm.getFoldForLine(topLine);
                        if ((fold != null) && fold.isCollapsed()) {
                            topLine += fold.getCollapsedLineCount();
                        }
                    }

                    if (++topLine >= lineCount) {
                        break;
                    }
                }
            } else {
                textAreaInsets = rTextArea.getInsets(textAreaInsets);

                if (visibleRect.y < textAreaInsets.top) {
                    visibleRect.height -= (textAreaInsets.top - visibleRect.y);
                    visibleRect.y = textAreaInsets.top;
                }

                int topLine = (visibleRect.y - textAreaInsets.top) / cellHeight;
                int actualTopY = topLine * cellHeight + textAreaInsets.top;
                int y = actualTopY + ascent;

                // Get the actual first line to paint, taking into account folding.
                topLine += fm.getHiddenLineCountAbove(topLine, true);

                // Paint line numbers
                g.setColor(getForeground());

                int line = topLine + 1;

                while ((y < visibleRect.y + visibleRect.height + ascent) && (line <= rTextArea.getLineCount())) {
                    paintLineNumber(g, metrics, rhs, y, line);

                    y += cellHeight;

                    if (fm != null) {
                        Fold fold = fm.getFoldForLine(line - 1);
                        // Skip to next line to paint, taking extra care for lines with
                        // block ends and begins together, e.g. "} else {"
                        while ((fold != null) && fold.isCollapsed()) {
                            int hiddenLineCount = fold.getLineCount();
                            if (hiddenLineCount == 0) {
                                // Fold parser identified a 0-line fold region... This
                                // is really a bug, but we'll handle it gracefully.
                                break;
                            }
                            line += hiddenLineCount;
                            fold = fm.getFoldForLine(line - 1);
                        }
                    }

                    line++;
                }
            }
        }

        protected void paintLineNumber(Graphics g, FontMetrics metrics, int x, int y, int lineNumber) {
            int originalLineNumber;

            if (lineNumberMap != null) {
                originalLineNumber = (lineNumber < lineNumberMap.length) ? lineNumberMap[lineNumber] : 0;
            } else {
                originalLineNumber = lineNumber;
            }

            if (originalLineNumber != 0) {
                String number = Integer.toString(originalLineNumber);
                int strWidth = metrics.stringWidth(number);
                g.setColor(showMisalignment && (lineNumber != originalLineNumber) ? errorForeground : getForeground());
                g.drawString(number, x-strWidth, y);
            }
        }

        public int getRhsBorderWidth() { return ((RSyntaxTextArea)rTextArea).isCodeFoldingEnabled() ? 0 : 4; }

        @Override
        public Dimension getPreferredSize() {
            if (preferredSize == null) {
                int lineCount = getMaximumSourceLineNumber();

                if (lineCount > 0) {
                    Font font = getFont();
                    FontMetrics fontMetrics = getFontMetrics(font);
                    int count = 1;

                    while (lineCount >= 10) {
                        lineCount = lineCount / 10;
                        count++;
                    }

                    int preferredWidth = fontMetrics.charWidth('9') * count + 10;
                    preferredSize = new Dimension(preferredWidth, 0);
                } else {
                    preferredSize = new Dimension(0, 0);
                }
            }

            return preferredSize;
        }
    }
}
