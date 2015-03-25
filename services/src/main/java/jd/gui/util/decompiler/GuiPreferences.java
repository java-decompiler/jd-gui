/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.util.decompiler;

import jd.core.preferences.Preferences;

public class GuiPreferences extends Preferences
{
	protected boolean showPrefixThis;
	protected boolean unicodeEscape;
	protected boolean showLineNumbers;

	public GuiPreferences()
	{ 
		this.showPrefixThis = true; 
		this.unicodeEscape = false;
		this.showLineNumbers = true;
	}

	public GuiPreferences(
            boolean showDefaultConstructor, boolean realignmentLineNumber,
            boolean showPrefixThis, boolean unicodeEscape, boolean showLineNumbers)
	{
		super(showDefaultConstructor, realignmentLineNumber);
		this.showPrefixThis = showPrefixThis;
		this.unicodeEscape = unicodeEscape;
		this.showLineNumbers = showLineNumbers;
	}

    public void setShowDefaultConstructor(boolean b) { showDefaultConstructor = b; }
    public void setRealignmentLineNumber(boolean b) { realignmentLineNumber=b; }
    public void setShowPrefixThis(boolean b) { showPrefixThis = b; }
    public void setUnicodeEscape(boolean b) { unicodeEscape=b; }
    public void setShowLineNumbers(boolean b) { showLineNumbers=b; }

	public boolean isShowPrefixThis() { return showPrefixThis; }
	public boolean isUnicodeEscape() { return unicodeEscape; }
	public boolean isShowLineNumbers() { return showLineNumbers; }
}
