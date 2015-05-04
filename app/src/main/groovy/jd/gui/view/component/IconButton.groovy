/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.view.component

import java.awt.Insets

import javax.swing.JButton

class IconButton extends JButton {
	
	IconButton() {
		focusPainted = false
		borderPainted = false
		margin = new Insets(0, 0, 0, 0)
	}
}
