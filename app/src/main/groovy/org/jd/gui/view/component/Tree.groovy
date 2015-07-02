/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
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
