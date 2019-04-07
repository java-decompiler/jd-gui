/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.fife.ui.rtextarea;

import org.fife.ui.rsyntaxtextarea.DocumentRange;

import java.util.List;

/*
 * An utility class to call the restricted access methods of 'RTextArea'.
 *
 * JD-GUI uses two workarounds for RSyntaxTextArea:
 * - org.fife.ui.rtextarea.Marker
 * - org.jd.gui.view.component.RoundMarkErrorStrip
 */
public class Marker {
    public static void markAll(RTextArea textArea, List<DocumentRange> ranges) {
        textArea.markAll(ranges);
    }

    public static void clearMarkAllHighlights(RTextArea textArea) {
        textArea.clearMarkAllHighlights();
    }
}
