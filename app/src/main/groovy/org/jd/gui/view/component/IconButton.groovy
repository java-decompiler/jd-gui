/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view.component

import java.awt.Insets

import javax.swing.JButton

class IconButton extends JButton {
	
	IconButton() {
		focusPainted = false
		borderPainted = false
		margin = new Insets(0, 0, 0, 0)
	}
}
