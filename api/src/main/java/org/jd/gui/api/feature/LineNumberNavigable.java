/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.api.feature;

public interface LineNumberNavigable {
    public int getMaximumLineNumber();

    public void goToLineNumber(int lineNumber);

    public boolean checkLineNumber(int lineNumber);
}
