/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view

import groovy.swing.SwingBuilder
import org.jd.gui.model.configuration.Configuration
import org.jd.gui.spi.PreferencesPanel

import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.KeyStroke
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Toolkit
import java.awt.event.KeyEvent

class PreferencesView implements PreferencesPanel.PreferencesPanelChangeListener {
    SwingBuilder swing
    Map<String, String> preferences
    Collection<PreferencesPanel> panels
    Map<PreferencesPanel, Boolean> valids

    PreferencesView(SwingBuilder swing, Configuration configuration, Collection<PreferencesPanel> panels) {
        this.swing = swing
        this.preferences = configuration.preferences
        this.panels = panels
        this.valids = [:]
        // Load GUI description
        swing.edt {
            build(PreferencesDescription)
            preferencesDialog.with {
                rootPane.with {
                    defaultButton = preferencesOkButton
                    getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), 'PreferencesDescription.cancel')
                    actionMap.put("PreferencesDescription.cancel", preferencesCancelAction)
                }

                preferencesPanels.opaque = true
                preferencesPanels.background = preferencesDialog.background

                def groups = [:].withDefault { [] }
                def errorBackgroundColor = Color.decode(configuration.preferences.get('JdGuiPreferences.errorBackgroundColor'))

                for (def panel : panels) {
                    panel.init(errorBackgroundColor)
                    panel.addPreferencesChangeListener(this)
                    groups.get(panel.preferencesGroupTitle).add(panel)
                }

                for (def groupEntry : groups.entrySet().sort { e1, e2 -> e1.key.compareTo(e2.key) }) {
                    def vbox = Box.createVerticalBox()
                    vbox.border = BorderFactory.createTitledBorder(groupEntry.key)

                    for (def panel : groupEntry.value.sort { p1, p2 -> p1.preferencesPanelTitle.compareTo(p2.preferencesPanelTitle) }) {
                        // Add title
                        def hbox = Box.createHorizontalBox()
                        def title = new JLabel(panel.preferencesPanelTitle)
                        title.font = title.font.deriveFont(Font.BOLD)
                        hbox.add(title)
                        hbox.add(Box.createHorizontalGlue())
                        hbox.border = BorderFactory.createEmptyBorder(0, 0, 5, 0)
                        vbox.add(hbox)
                        // Add panel
                        panel.maximumSize = new Dimension(panel.maximumSize.width as int, panel.preferredSize.height as int)
                        vbox.add(panel)
                    }

                    preferencesPanels.add(vbox)
                }

                preferencesPanels.add(Box.createVerticalGlue())

                // Size of the screen
                def screenSize = Toolkit.defaultToolkit.screenSize
                // Height of the task bar
                def scnMax = Toolkit.defaultToolkit.getScreenInsets(getGraphicsConfiguration())
                // screen height in pixels without taskbar
                int taskBarHeight = scnMax.bottom + scnMax.top
                int maxHeight = screenSize.height - taskBarHeight

                int preferredHeight = preferencesPanels.preferredSize.height + 2
                if (preferredHeight > maxHeight)
                    preferredHeight = maxHeight
                preferencesScrollPane.preferredSize = new Dimension(400, preferredHeight)

                minimumSize = new Dimension(300, 200)

                pack()
                locationRelativeTo = parent
            }
        }
    }

    void show(Closure okClosure) {
        swing.doLater {
            preferencesOkAction.closure = { onOk(okClosure) }
            for (def panel : panels) {
                panel.loadPreferences(preferences)
            }
            preferencesDialog.visible = true
        }
    }

    void onOk(Closure okClosure) {
        swing.doLater {
            for (def panel : panels) {
                panel.savePreferences(preferences)
            }
            preferencesDialog.visible = false
            okClosure()
        }
    }

    // --- PreferencesPanel.PreferencesChangeListener --- //
    void preferencesPanelChanged(PreferencesPanel source) {
        swing.doLater {
            boolean valid = source.arePreferencesValid()

            valids.put(source, Boolean.valueOf(valid))

            if (valid) {
                for (def panel : panels) {
                    if (valids.get(panel) == Boolean.FALSE) {
                        preferencesOkAction.enabled = false
                        return
                    }
                }
                preferencesOkAction.enabled = true
            } else {
                preferencesOkAction.enabled = false
            }
        }
    }
}
