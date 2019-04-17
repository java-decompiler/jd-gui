/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view.component;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Token;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.TreeMap;

public abstract class HyperlinkPage extends TextPage {
    protected static final Cursor DEFAULT_CURSOR = Cursor.getDefaultCursor();
    protected static final Cursor HAND_CURSOR = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

    protected TreeMap<Integer, HyperlinkData> hyperlinks = new TreeMap<>();

    public HyperlinkPage() {
        MouseAdapter listener = new MouseAdapter() {
            int lastX = -1;
            int lastY = -1;
            int lastModifiers = -1;

            public void mouseClicked(MouseEvent e) {
                if ((e.getClickCount() == 1) && ((e.getModifiers() & (Event.ALT_MASK|Event.META_MASK|Event.SHIFT_MASK)) == 0)) {
                    int offset = textArea.viewToModel(new Point(e.getX(), e.getY()));
                    if (offset != -1) {
                        Map.Entry<Integer, HyperlinkData> entry = hyperlinks.floorEntry(offset);
                        if (entry != null) {
                            HyperlinkData entryData = entry.getValue();
                            if ((entryData != null) && (offset < entryData.endPosition) && (offset >= entryData.startPosition) && isHyperlinkEnabled(entryData)) {
                                openHyperlink(e.getXOnScreen(), e.getYOnScreen(), entryData);
                            }
                        }
                    }
                }
            }

            public void mouseMoved(MouseEvent e) {
                if ((e.getX() != lastX) || (e.getY() != lastY) || (lastModifiers != e.getModifiers())) {
                    lastX = e.getX();
                    lastY = e.getY();
                    lastModifiers = e.getModifiers();

                    if ((e.getModifiers() & (Event.ALT_MASK|Event.META_MASK|Event.SHIFT_MASK)) == 0) {
                        int offset = textArea.viewToModel(new Point(e.getX(), e.getY()));
                        if (offset != -1) {
                            Map.Entry<Integer, HyperlinkData> entry = hyperlinks.floorEntry(offset);
                            if (entry != null) {
                                HyperlinkData entryData = entry.getValue();
                                if ((entryData != null) && (offset < entryData.endPosition) && (offset >= entryData.startPosition) && isHyperlinkEnabled(entryData)) {
                                    if (textArea.getCursor() != HAND_CURSOR) {
                                        textArea.setCursor(HAND_CURSOR);
                                    }
                                    return;
                                }
                            }
                        }
                    }

                    if (textArea.getCursor() != DEFAULT_CURSOR) {
                        textArea.setCursor(DEFAULT_CURSOR);
                    }
                }
            }
        };

        textArea.addMouseListener(listener);
        textArea.addMouseMotionListener(listener);
    }

    protected RSyntaxTextArea newSyntaxTextArea() { return new HyperlinkSyntaxTextArea(); }

    public void addHyperlink(HyperlinkData hyperlinkData) {
        hyperlinks.put(hyperlinkData.startPosition, hyperlinkData);
    }

    public void clearHyperlinks() {
        hyperlinks.clear();
    }

    protected abstract boolean isHyperlinkEnabled(HyperlinkData hyperlinkData);

    protected abstract void openHyperlink(int x, int y, HyperlinkData hyperlinkData);

    public static class HyperlinkData {
        public int startPosition;
        public int endPosition;

        public HyperlinkData(int startPosition, int endPosition) {
            this.startPosition = startPosition;
            this.endPosition = endPosition;
        }
    }

    public class HyperlinkSyntaxTextArea extends RSyntaxTextArea {
        /**
         * @see HyperlinkPage.HyperlinkSyntaxTextArea#getUnderlineForToken(org.fife.ui.rsyntaxtextarea.Token)
         */
        @Override
        public boolean getUnderlineForToken(Token t) {
            Map.Entry<Integer, HyperlinkData> entry = hyperlinks.floorEntry(t.getOffset());
            if (entry != null) {
                HyperlinkData entryData = entry.getValue();
                if ((entryData != null) && (t.getOffset() < entryData.endPosition) && (t.getOffset() >= entryData.startPosition) && isHyperlinkEnabled(entryData)) {
                    return true;
                }
            }
            return super.getUnderlineForToken(t);
        }
    }
}
