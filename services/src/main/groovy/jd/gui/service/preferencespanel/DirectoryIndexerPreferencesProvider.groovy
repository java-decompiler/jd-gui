/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.preferencespanel

import jd.gui.spi.PreferencesPanel

import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import java.awt.BorderLayout
import java.awt.Color

class DirectoryIndexerPreferencesProvider extends JPanel implements PreferencesPanel, DocumentListener {
    static final int MAX_VALUE = 30
    static final String MAXIMUM_DEPTH_KEY = 'DirectoryIndexerPreferences.maximumDepth'

    PreferencesPanel.PreferencesPanelChangeListener listener = null
    JTextField maximumDepthTextField
    Color errorBackgroundColor = Color.RED
    Color defaultBackgroundColor

    DirectoryIndexerPreferencesProvider() {
        super(new BorderLayout())

        add(new JLabel('Maximum depth (1..' + MAX_VALUE + '): '), BorderLayout.WEST)

        maximumDepthTextField = new JTextField()
        maximumDepthTextField.document.addDocumentListener(this)
        add(maximumDepthTextField, BorderLayout.CENTER)

        defaultBackgroundColor = maximumDepthTextField.background
    }

    // --- PreferencesPanel --- //
    String getPreferencesGroupTitle() { 'Indexer' }
    String getPreferencesPanelTitle() { 'Directory exploration' }

    public void init(Color errorBackgroundColor) {
        this.errorBackgroundColor = errorBackgroundColor
    }

    void loadPreferences(Map<String, String> preferences) {
        maximumDepthTextField.text = preferences.get(MAXIMUM_DEPTH_KEY) ?: '15'
        maximumDepthTextField.setCaretPosition(maximumDepthTextField.text.size())
    }

    void savePreferences(Map<String, String> preferences) {
        preferences.put(MAXIMUM_DEPTH_KEY, maximumDepthTextField.text)
    }

    boolean arePreferencesValid() {
        try {
            int i = Integer.valueOf(maximumDepthTextField.text)
            return (i > 0) && (i <= MAX_VALUE)
        } catch (NumberFormatException ignore) {
            return false
        }
    }

    void addPreferencesChangeListener(PreferencesPanel.PreferencesPanelChangeListener listener) {
        this.listener = listener
    }

    // --- DocumentListener --- //
    void insertUpdate(DocumentEvent e) { onTextChange() }
    void removeUpdate(DocumentEvent e) { onTextChange() }
    void changedUpdate(DocumentEvent e) { onTextChange() }

    void onTextChange() {
        maximumDepthTextField.background = arePreferencesValid() ? defaultBackgroundColor : errorBackgroundColor
        listener?.preferencesPanelChanged(this)
    }
}
