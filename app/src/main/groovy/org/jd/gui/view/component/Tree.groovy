/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view.component

import javax.swing.*
import java.awt.*
import java.awt.event.KeyEvent

class Tree extends JTree {
    Tree() {
        def ctrlA = KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.defaultToolkit.menuShortcutKeyMask)
        def ctrlC = KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.defaultToolkit.menuShortcutKeyMask)
        def ctrlV = KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.defaultToolkit.menuShortcutKeyMask)

        inputMap.put(ctrlA, 'none')
        inputMap.put(ctrlC, 'none')
        inputMap.put(ctrlV, 'none')
        rootVisible = false
    }
}
