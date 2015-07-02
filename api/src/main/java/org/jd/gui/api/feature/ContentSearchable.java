/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.api.feature;

public interface ContentSearchable {
    public boolean highlightText(String text, boolean caseSensitive);

    public void findNext(String text, boolean caseSensitive);

    public void findPrevious(String text, boolean caseSensitive);
}
